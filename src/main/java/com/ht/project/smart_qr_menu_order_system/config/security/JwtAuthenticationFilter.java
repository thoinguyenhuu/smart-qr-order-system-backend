package com.ht.project.smart_qr_menu_order_system.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ht.project.smart_qr_menu_order_system.entity.User;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import com.ht.project.smart_qr_menu_order_system.repository.UserRepository;
import com.ht.project.smart_qr_menu_order_system.utils.ApiResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Parse bearer token and create authenticated principal for protected APIs.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorizationHeader.substring(7);
            AuthenticatedUser authenticatedUser = jwtTokenService.parseAccessToken(token);
            User user = userRepository.findById(authenticatedUser.userId())
                    .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_FOUND));

            if (!user.isActive()) {
                throw new AppException(AppErrorCode.USER_NOT_ACTIVE);
            }

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + authenticatedUser.role().name()))
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (AppException exception) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, exception.getErrorCode(), exception.getHandleMessage());
        }
    }

    private void writeErrorResponse(HttpServletResponse response, AppErrorCode errorCode, String message) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(new ObjectMapper().writeValueAsString(
                ApiResult.builder()
                        .success(0)
                        .message(message)
                        .build()
        ));
    }
}
