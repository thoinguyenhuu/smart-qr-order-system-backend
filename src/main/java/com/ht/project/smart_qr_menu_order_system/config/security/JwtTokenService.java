package com.ht.project.smart_qr_menu_order_system.config.security;

import com.ht.project.smart_qr_menu_order_system.entity.User;
import com.ht.project.smart_qr_menu_order_system.enums.Role;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * Utility for generating and validating JWT tokens.
 */
@Component
public class JwtTokenService {
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String ACCESS_TOKEN = "ACCESS";
    private static final String REFRESH_TOKEN = "REFRESH";
    private static final String REGISTER_TOKEN = "REGISTER";

    @Value("${app.jwt.access_key}")
    private String accessKey;

    @Value("${app.jwt.refresh_key}")
    private String refreshKey;

    @Value("${app.register.key}")
    private String registerKey;

    public String generateAccessToken(User user) {
        return generateToken(user, accessKey, 1, ACCESS_TOKEN);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshKey, 14, REFRESH_TOKEN);
    }

    public String generateRegisterToken(User user) {
        return generateToken(user, registerKey, 1, REGISTER_TOKEN);
    }

    public AuthenticatedUser parseAccessToken(String token) {
        SignedJWT signedJWT = verifyToken(token, accessKey, ACCESS_TOKEN);
        try {
            return new AuthenticatedUser(
                    signedJWT.getJWTClaimsSet().getStringClaim(CLAIM_USER_ID),
                    signedJWT.getJWTClaimsSet().getSubject(),
                    Role.valueOf(signedJWT.getJWTClaimsSet().getStringClaim(CLAIM_ROLE))
            );
        } catch (ParseException exception) {
            throw new AppException(AppErrorCode.TOKEN_NOT_VERIFY);
        }
    }

    public String extractEmailFromRegisterToken(String token) {
        SignedJWT signedJWT = verifyToken(token, registerKey, REGISTER_TOKEN);
        try {
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException exception) {
            throw new AppException(AppErrorCode.TOKEN_NOT_VERIFY);
        }
    }

    public boolean isValidAccessToken(String token) {
        verifyToken(token, accessKey, ACCESS_TOKEN);
        return true;
    }

    public UserTokenClaims parseRefreshToken(String token) {
        SignedJWT signedJWT = verifyToken(token, refreshKey, REFRESH_TOKEN);
        try {
            return new UserTokenClaims(
                    signedJWT.getJWTClaimsSet().getStringClaim(CLAIM_USER_ID),
                    signedJWT.getJWTClaimsSet().getSubject()
            );
        } catch (ParseException exception) {
            throw new AppException(AppErrorCode.TOKEN_NOT_VERIFY);
        }
    }

    private String generateToken(User user, String key, int amount, String tokenType) {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(amount, ChronoUnit.DAYS)))
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS512), new Payload(claimsSet.toJSONObject()));
        try {
            jwsObject.sign(new MACSigner(key.getBytes()));
        } catch (JOSEException exception) {
            throw new AppException(AppErrorCode.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
        return jwsObject.serialize();
    }

    private SignedJWT verifyToken(String token, String key, String expectedType) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean verified = signedJWT.verify(new MACVerifier(key.getBytes()));
            String tokenType = signedJWT.getJWTClaimsSet().getStringClaim(CLAIM_TOKEN_TYPE);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            if (!verified || expirationTime == null || expirationTime.before(new Date()) || !expectedType.equals(tokenType)) {
                throw new AppException(AppErrorCode.TOKEN_NOT_VERIFY);
            }
            return signedJWT;
        } catch (ParseException | JOSEException exception) {
            throw new AppException(AppErrorCode.TOKEN_NOT_VERIFY);
        }
    }

    public record UserTokenClaims(
            String userId,
            String email
    ) {
    }
}
