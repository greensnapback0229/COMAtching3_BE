package comatching.comatching3.users.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import comatching.comatching3.admin.dto.response.TokenRes;
import comatching.comatching3.admin.entity.University;
import comatching.comatching3.admin.repository.UniversityRepository;
import comatching.comatching3.charge.repository.ChargeRequestRepository;
import comatching.comatching3.exception.BusinessException;
import comatching.comatching3.history.entity.PointHistory;
import comatching.comatching3.history.enums.PointHistoryType;
import comatching.comatching3.history.repository.PointHistoryRepository;
import comatching.comatching3.users.auth.jwt.JwtUtil;
import comatching.comatching3.users.auth.refresh_token.service.RefreshTokenService;
import comatching.comatching3.users.dto.BuyPickMeReq;
import comatching.comatching3.users.dto.CurrentPointRes;
import comatching.comatching3.users.dto.UserFeatureReq;
import comatching.comatching3.users.dto.UserInfoRes;
import comatching.comatching3.users.entity.UserAiFeature;
import comatching.comatching3.users.entity.Users;
import comatching.comatching3.users.enums.Hobby;
import comatching.comatching3.users.enums.Role;
import comatching.comatching3.users.enums.UserCrudType;
import comatching.comatching3.users.repository.UserAiFeatureRepository;
import comatching.comatching3.users.repository.UsersRepository;
import comatching.comatching3.util.RabbitMQ.UserCrudRabbitMQUtil;
import comatching.comatching3.util.ResponseCode;
import comatching.comatching3.util.UUIDUtil;
import comatching.comatching3.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final UserAiFeatureRepository userAiFeatureRepository;
    private final ChargeRequestRepository chargeRequestRepository;
    private final UniversityRepository universityRepository;
    private final SecurityUtil securityUtil;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserCrudRabbitMQUtil userCrudRabbitMQUtil;
    private final PointHistoryRepository pointHistoryRepository;

    public Long getParticipations() {
        return usersRepository.count();
    }


    /**
     * 소셜 유저 피처 정보 입력 후 유저로 역할 변경까지
     * @param form social 유저의 Feature
     */
    @Transactional
    public TokenRes inputUserInfo(UserFeatureReq form) {

        Users user = securityUtil.getCurrentUsersEntity();
        List<Hobby> hobbyList = form.getHobby().stream()
                .map(Hobby::valueOf)
                .toList();

        University university = universityRepository.findByUniversityName(form.getUniversity())
                .orElseThrow(() -> new BusinessException(ResponseCode.SCHOOL_NOT_EXIST));

        UserAiFeature userAiFeature = user.getUserAiFeature();

        userAiFeature.updateMajor(form.getMajor());
        userAiFeature.updateGender(form.getGender());
        userAiFeature.updateAge(form.getAge());
        userAiFeature.updateMbti(form.getMbti());
        userAiFeature.updateHobby(hobbyList);
        userAiFeature.updateContactFrequency(form.getContactFrequency());
        userAiFeature.updateAdmissionYear(form.getAdmissionYear());

        userAiFeatureRepository.save(userAiFeature);

        user.updateSong(form.getSong());
        user.updateComment(form.getComment());
        user.updateRole(Role.USER.getRoleName());
        user.updateUniversity(university);
        user.updateContactId(form.getContactId());

        usersRepository.save(user);

        //역할 업데이트된 jwt 토큰 발행 및 토큰 교체
        String uuid = UUIDUtil.bytesToHex(user.getUserAiFeature().getUuid());
        String accessToken = jwtUtil.generateAccessToken(uuid, Role.USER.getRoleName());
        String refreshToken = refreshTokenService.getRefreshToken(uuid);

        securityUtil.setAuthentication(accessToken);

        Boolean isSuccess = userCrudRabbitMQUtil.sendUserChange(user.getUserAiFeature(), UserCrudType.CREATE);
        if(!isSuccess){
            throw new BusinessException(ResponseCode.INPUT_FEATURE_FAIL);
        }

        return TokenRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }



    /**
     * 메인 페이지 유저 정보 조회
     * @return 유저 정보 조회
     */
    @Transactional
    public UserInfoRes getUserInfo() {

        Users user = securityUtil.getCurrentUsersEntity();
//        log.info(user.getUsername());

        Boolean canRequest = !chargeRequestRepository.existsByUsers(user);

        if(user.getPickMe() <= 0){
            if(user.getPickMe() < 0) {
                user.updatePickMe(0);
                userCrudRabbitMQUtil.sendUserChange(user.getUserAiFeature(), UserCrudType.DELETE);
            }
        }

        return UserInfoRes.builder()
                .username(user.getUsername())
                .major(user.getUserAiFeature().getMajor())
                .age(user.getUserAiFeature().getAge())
                .song(user.getSong())
                .mbti(user.getUserAiFeature().getMbti())
                .contactId(user.getContactId())
                .point(user.getPoint())
                .pickMe(user.getPickMe())
                .canRequestCharge(canRequest)
                .participations(getParticipations())
                .admissionYear(user.getUserAiFeature().getAdmissionYear())
                .comment(user.getComment())
                .contactFrequency(user.getUserAiFeature().getContactFrequency())
                .hobbies(user.getUserAiFeature().getHobby())
                .gender(user.getUserAiFeature().getGender())
                .event1(user.getEvent1())
                .build();
    }

    /**
     * 유저 포인트 조회
     * @return 유저 포인트
     */
    public Integer getPoints() {
        Users user = securityUtil.getCurrentUsersEntity();
        return user.getPoint();
    }

    @Transactional
    public void buyPickMe(BuyPickMeReq req) {

        if (req == null || req.getAmount() == null) {
            throw new BusinessException(ResponseCode.BAD_REQUEST_PiCKME);
        }

        Users user = securityUtil.getCurrentUsersEntity();
        int price = 500;
        int userPoint = user.getPoint();
        int reqPoint = (req.getAmount() / 3) * (price * 2) + (req.getAmount() % 3) * price;

        if (reqPoint > userPoint) {
            throw new BusinessException(ResponseCode.NOT_ENOUGH_POINT);
        }

        if (user.getPickMe() == 0){
            Boolean isSuccess = userCrudRabbitMQUtil.sendUserChange(user.getUserAiFeature(), UserCrudType.CREATE);

            if(isSuccess){
                throw new BusinessException(ResponseCode.ADD_PICKME_FAIL);
            }
        }

        PointHistory pointHistory = PointHistory.builder()
                .users(user)
                .pointHistoryType(PointHistoryType.BUY_PICK_ME)
                .changeAmount(reqPoint)
                .build();

        user.subtractPoint(reqPoint);
        user.addPickMe(req.getAmount());

        pointHistory.setTotalPoint(user.getPoint());
        pointHistory.setPickMe(user.getPickMe());

        user.getPointHistoryList().add(pointHistory);

        pointHistoryRepository.save(pointHistory);
    }

    public CurrentPointRes inquiryCurrentPoint(){
        Users users = securityUtil.getCurrentUsersEntity();
        return new CurrentPointRes(users.getPoint());
    }


    @Transactional
    public void requestEventPickMe(){
        Users users = securityUtil.getCurrentUsersEntity();
        if (users.getEvent1()) {
            throw new BusinessException(ResponseCode.ALREADY_PARTICIPATED);
        }

        if(users.getPickMe() <= 0){
            userCrudRabbitMQUtil.sendUserChange(users.getUserAiFeature(), UserCrudType.CREATE);
            users.updatePickMe(0);
        }
        else{
            users.updatePickMe(users.getPickMe() + 3);
        }
        users.updateEvent1(true);

    }

    @Transactional
    public void notRequestEventPickMe(){
        Users users = securityUtil.getCurrentUsersEntity();
        users.updateEvent1(true);
    }
}
