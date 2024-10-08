package comatching.comatching3.users.auth.jwt;

import comatching.comatching3.admin.enums.AdminRole;
import comatching.comatching3.users.enums.Role;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@Component
public class JwtUtil {

    private SecretKey secretKey;

    public static final long REFRESH_TOKEN_EXPIRATION = TimeUnit.DAYS.toMillis(1);
//    public static final long ACCESS_TOKEN_EXPIRATION = TimeUnit.HOURS.toMillis(1);
    public static final long USER_ACCESS_TOKEN_EXPIRATION = TimeUnit.MINUTES.toMillis(60);
    public static final long ADMIN_ACCESS_TOKEN_EXPIRATION = TimeUnit.MINUTES.toMillis(240);


    public JwtUtil(@Value("${jwt.secret}") String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));

            secretKey = new SecretKeySpec(hash, "HmacSHA256");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found.", e);
        }
    }

    public String getUUID(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("uuid", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }
    public String generateAccessToken(String uuid, String role) {
        if (role.equals(AdminRole.ROLE_ADMIN.getRoleName())) {
            return generateToken(uuid, role, ADMIN_ACCESS_TOKEN_EXPIRATION);
        }
        return generateToken(uuid, role, USER_ACCESS_TOKEN_EXPIRATION);
    }
    public String generateRefreshToken(String uuid, String role) {
        return generateToken(uuid, role, REFRESH_TOKEN_EXPIRATION);
    }

    public String generateToken(String uuid, String role, long expiredMs) {
        return Jwts.builder()
                .claim("uuid", uuid)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

}
