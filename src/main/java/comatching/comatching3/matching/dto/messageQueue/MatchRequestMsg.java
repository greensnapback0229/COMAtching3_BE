package comatching.comatching3.matching.dto.messageQueue;

import comatching.comatching3.history.entity.MatchingHistory;
import comatching.comatching3.matching.dto.request.AdminMatchReq;
import comatching.comatching3.matching.dto.request.MatchReq;
import comatching.comatching3.matching.enums.AgeOption;
import comatching.comatching3.matching.enums.ContactFrequencyOption;
import comatching.comatching3.users.entity.UserAiFeature;
import comatching.comatching3.users.enums.HobbyEnum;
import comatching.comatching3.util.UUIDUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MatchRequestMsg {

    private String matcherUuid;
    private ContactFrequencyOption contactFrequencyOption;
    private String myGender;
    private String hobbyOption;
    private Boolean sameMajorOption;
    private AgeOption ageOption;
    private String mbtiOption;
    private String myMajor;
    private Integer myAge;
    private String university;
    private List<String> duplicationList = new ArrayList<String>();

    public void fromMatchReqAndUserAiFeature(MatchReq matchReq, UserAiFeature applierFeature, String university) {
        this.matcherUuid = UUIDUtil.bytesToHex(applierFeature.getUuid());
        this.contactFrequencyOption = matchReq.getContactFrequencyOption();
        this.myGender = applierFeature.getGender().getAiValue();
        this.hobbyOption = HobbyEnum.convertHobbiesString(matchReq.getHobbyEnumOption());
        this.sameMajorOption = matchReq.getSameMajorOption();
        this.ageOption = matchReq.getAgeOption();
        this.mbtiOption = matchReq.getMbtiOption();
        this.myMajor = applierFeature.getMajor();
        this.myAge = applierFeature.getAge();
        this.university = university;
    }

    public void fromAdminMatchReqAndUserAiFeature(AdminMatchReq adminMatchReq, UserAiFeature applierFeature) {
        this.matcherUuid = UUIDUtil.bytesToHex(applierFeature.getUuid());
        this.contactFrequencyOption = adminMatchReq.getContactFrequencyOption();
        this.myGender = applierFeature.getGender().getAiValue();
        this.hobbyOption = HobbyEnum.convertHobbiesString(adminMatchReq.getHobbyEnumOption());
        this.sameMajorOption = adminMatchReq.getSameMajorOption();
        this.ageOption = adminMatchReq.getAgeOption();
        this.mbtiOption = adminMatchReq.getMbtiOption();
        this.myMajor = applierFeature.getMajor();
        this.myAge = applierFeature.getAge();
    }

    public void updateDuplicationListFromHistory(List<MatchingHistory> matchingHistories) {
        for (MatchingHistory history : matchingHistories) {
            String uuid = UUIDUtil.bytesToHex(history.getEnemy().getUserAiFeature().getUuid());
            this.duplicationList.add(uuid);
        }
    }
}
