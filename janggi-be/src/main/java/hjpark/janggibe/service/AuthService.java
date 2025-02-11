package hjpark.janggibe.service;

import hjpark.janggibe.config.JwtUtil;
import hjpark.janggibe.model.Provider;
import hjpark.janggibe.model.User;
import hjpark.janggibe.repository.UserRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * OAuth2 로그인 처리
     * @param oAuth2User OAuth2 로그인 사용자 정보
     * @param provider 로그인 제공자 (GOOGLE, KAKAO 등)
     * @param nickname 사용자 입력 닉네임 (최초 로그인 시 입력받음)
     * @return JWT 토큰
     */
    @Transactional
    public String loginOAuthUser(OAuth2User oAuth2User, Provider provider, String nickname) {
        String email = oAuth2User.getAttribute("email");
        String picture = oAuth2User.getAttribute("picture");
        String sub = oAuth2User.getAttribute("sub"); // 구글 고유 ID

        // 사용자 정보 확인
        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            // 기존 사용자 로그인
            user = existingUser.get(); // 기존 사용자 로그인
        } else {
            // 신규 사용자 등록
            user = new User();
            user.setEmail(email);
            user.setNickname(nickname);
            user.setProfileImage(picture);
            user.setProvider(provider);
            user.setProviderId(sub);
            userRepository.save(user);
        }

        // 3. JWT 생성 시 여러 정보 포함
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("nickname", user.getNickname());
        claims.put("profileImage", user.getProfileImage());
        claims.put("provider", user.getProvider().name());
        claims.put("providerId", user.getProviderId());

        // JWT 토큰 생성
        return jwtUtil.generateToken(claims);
    }

}
