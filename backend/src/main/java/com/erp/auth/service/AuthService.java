package com.erp.auth.service;

import com.erp.auth.dto.LoginRequest;
import com.erp.auth.dto.LoginResponse;
import com.erp.auth.dto.TokenRefreshRequest;
import com.erp.auth.security.JwtTokenProvider;
import com.erp.common.exception.BusinessException;
import com.erp.master.entity.Company;
import com.erp.master.entity.User;
import com.erp.master.repository.CompanyRepository;
import com.erp.master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new BusinessException("아이디 또는 비밀번호가 올바르지 않습니다.", "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("아이디 또는 비밀번호가 올바르지 않습니다.", "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
        }
        if (!user.isActive()) {
            throw new BusinessException("비활성화된 계정입니다.", "ACCOUNT_INACTIVE", HttpStatus.UNAUTHORIZED);
        }

        Company company = companyRepository.findById(user.getCompanyId())
                .orElseThrow(() -> BusinessException.notFound("회사"));

        String accessToken = tokenProvider.createAccessToken(user.getId(), user.getCompanyId(), user.getEmail());
        String refreshToken = tokenProvider.createRefreshToken(user.getId(), user.getCompanyId(), user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .companyId(user.getCompanyId())
                .companyName(company.getName())
                .build();
    }

    public LoginResponse refresh(TokenRefreshRequest request) {
        String token = request.getRefreshToken();
        if (!tokenProvider.validate(token)) {
            throw new BusinessException("유효하지 않은 리프레시 토큰입니다.", "INVALID_TOKEN", HttpStatus.UNAUTHORIZED);
        }

        Long userId = tokenProvider.getUserId(token);
        Long companyId = tokenProvider.getCompanyId(token);
        String email = tokenProvider.getEmail(token);

        String newAccessToken = tokenProvider.createAccessToken(userId, companyId, email);
        String newRefreshToken = tokenProvider.createRefreshToken(userId, companyId, email);

        User user = userRepository.findById(userId).orElseThrow(() -> BusinessException.notFound("사용자"));
        Company company = companyRepository.findById(companyId).orElseThrow(() -> BusinessException.notFound("회사"));

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(userId)
                .email(email)
                .name(user.getName())
                .companyId(companyId)
                .companyName(company.getName())
                .build();
    }
}
