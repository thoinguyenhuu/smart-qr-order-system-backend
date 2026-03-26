package com.ht.project.smart_qr_menu_order_system.service.impl;

import com.ht.project.smart_qr_menu_order_system.config.security.JwtTokenService;
import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateUserRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.LoginRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.RefreshTokenRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.LoginResponse;
import com.ht.project.smart_qr_menu_order_system.dtos.response.UserResponse;
import com.ht.project.smart_qr_menu_order_system.entity.User;
import com.ht.project.smart_qr_menu_order_system.enums.Role;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import com.ht.project.smart_qr_menu_order_system.mapper.UserMapper;
import com.ht.project.smart_qr_menu_order_system.repository.UserRepository;
import com.ht.project.smart_qr_menu_order_system.service.AuthService;
import com.ht.project.smart_qr_menu_order_system.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final JwtTokenService jwtTokenService;

    @Value("${app.register.ui_host}")
    private String uiHost;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info(String.valueOf(request));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(AppErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException(AppErrorCode.INVALID_CREDENTIALS);
        }
        if (!user.isActive()) {
            throw new AppException(AppErrorCode.USER_NOT_ACTIVE);
        }

        return LoginResponse.builder()
                .accessToken(jwtTokenService.generateAccessToken(user))
                .refreshToken(jwtTokenService.generateRefreshToken(user))
                .build();
    }

    @Override
    public void logout() {
        // Stateless JWT flow does not require server-side logout in current scope.
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        JwtTokenService.UserTokenClaims claims = jwtTokenService.parseRefreshToken(request.refreshToken());
        User user = userRepository.findById(claims.userId())
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new AppException(AppErrorCode.USER_NOT_ACTIVE);
        }
        return LoginResponse.builder()
                .accessToken(jwtTokenService.generateAccessToken(user))
                .refreshToken(jwtTokenService.generateRefreshToken(user))
                .build();
    }

    @Override
    public boolean authenticate(String token) {
        return jwtTokenService.isValidAccessToken(token);
    }

    @Override
    public UserResponse register(CreateUserRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new AppException(AppErrorCode.BAD_REQUEST, "Email already exists");
        });

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(false);
        user.setRole(Role.OWNER);

        User savedUser = userRepository.save(user);
        String token = jwtTokenService.generateRegisterToken(savedUser);
        String verificationUrl = UriComponentsBuilder.fromUriString(uiHost)
                .queryParam("token", token)
                .build()
                .toUriString();
        emailService.sendAccountVerificationEmail(savedUser.getEmail(), verificationUrl);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse verify(String token) {
        String email = jwtTokenService.extractEmailFromRegisterToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_FOUND));
        user.setActive(true);
        return userMapper.toUserResponse(userRepository.save(user));
    }
}
