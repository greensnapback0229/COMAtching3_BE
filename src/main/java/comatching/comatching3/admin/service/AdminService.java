package comatching.comatching3.admin.service;

import comatching.comatching3.admin.dto.AdminLoginForm;
import comatching.comatching3.admin.dto.AdminRegisterForm;
import comatching.comatching3.admin.dto.TokenDto;
import comatching.comatching3.admin.entity.Admin;
import comatching.comatching3.admin.entity.University;
import comatching.comatching3.admin.enums.AdminRole;
import comatching.comatching3.admin.exception.AccountIdAlreadyExistsException;
import comatching.comatching3.admin.exception.InvalidLoginException;
import comatching.comatching3.admin.repository.AdminRepository;
import comatching.comatching3.admin.repository.UniversityRepository;
import comatching.comatching3.users.auth.jwt.JwtUtil;
import comatching.comatching3.users.auth.refresh_token.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public void adminRegister(AdminRegisterForm form) {

        Optional<Admin> existAdmin = adminRepository.findByAccountId(form.getAccountId());

        if (existAdmin.isPresent()) {
            throw new AccountIdAlreadyExistsException("ACCOUNT_ID_DUPLICATED");
        }

        String encryptedPassword = passwordEncoder.encode(form.getPassword());
        // 제휴된 학교가 미리 등록되어 있으면 문제없지만 그렇지 않은 경우 DB에 null로 저장될 수 있음.
        University university = universityRepository.findByUniversityName(form.getUniversityName()).orElse(null);
        AdminRole role = AdminRole.valueOf(form.getRole());

        Admin admin = Admin.builder()
                .accountId(form.getAccountId())
                .password(encryptedPassword)
                .university(university)
                .universityAuth(form.getIsUniversityVerified())
                .adminRole(role)
                .build();

        adminRepository.save(admin);
    }

    public TokenDto adminLogin(AdminLoginForm form) {

        Optional<Admin> existAdmin = adminRepository.findByAccountId(form.getAccountId());

        if (existAdmin.isEmpty()) {
            throw new InvalidLoginException("INVALID_ADMIN_LOGIN");
        }

        Admin admin = existAdmin.get();

        if (!passwordEncoder.matches(form.getPassword(), admin.getPassword())) {
            throw new InvalidLoginException("INVALID_ADMIN_LOGIN");
        }

        String accessToken = jwtUtil.generateAccessToken(admin.getAccountId(), admin.getAdminRole().getRoleName());
        String refreshToken = refreshTokenService.getRefreshToken(admin.getAccountId());

        if (refreshToken == null) {
            refreshToken = jwtUtil.generateRefreshToken(admin.getAccountId(), admin.getAdminRole().getRoleName());
            refreshTokenService.saveRefreshToken(admin.getAccountId(), refreshToken);
        }

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
