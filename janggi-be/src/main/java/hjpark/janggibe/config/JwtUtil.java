package hjpark.janggibe.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRATION_TIME = Duration.ofDays(1).toMillis(); // 1일

    // 서명 키 가져오기
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 여러 정보를 담은 JWT 토큰 생성
     * @param claims 사용자 정보 (email, nickname, picture 등)
     * @return JWT 토큰
     */
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims) // 사용자 ID 설정
                .setIssuedAt(new Date()) // 토큰 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 토큰 만료 시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact(); // JWT 토큰 생성
    }

    /**
     * JWT 토큰에서 정보 추출
     * @param token JWT 토큰
     * @return Claims (토큰 내 정보)
     */
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }

    // JWT 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
