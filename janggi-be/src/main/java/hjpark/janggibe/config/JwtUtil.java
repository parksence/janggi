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

    // JWT 토큰 생성
    public String generateToken(String email, String picture, String sub) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", sub);         // 구글에서 제공하는 고유 사용자 ID
        claims.put("email", email);     // 사용자 이메일
        claims.put("picture", picture); // 사용자 프로필 사진 URL

        return Jwts.builder()
                .setClaims(claims) // 사용자 ID 설정
                .setIssuedAt(new Date()) // 토큰 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 토큰 만료 시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact(); // JWT 토큰 생성
    }

    // JWT에서 사용자 이름 추출
    public String getUsername(String token) {
        return Jwts.parser() // 최신 방식 적용
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
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
}
