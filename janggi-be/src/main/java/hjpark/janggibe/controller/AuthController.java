package hjpark.janggibe.controller;

import hjpark.janggibe.config.JwtUtil;
import hjpark.janggibe.model.User;
import hjpark.janggibe.repository.UserRepository;
import hjpark.janggibe.service.CustomOAuth2UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomOAuth2UserService oAuth2UserService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 일반 로그인 (JWT 발급)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // 사용자 이름이나 비밀번호가 없는 경우
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("아이디와 비밀번호를 입력하세요.");
        }

        // 사용자 이름으로 사용자 찾기
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty() || !passwordEncoder.matches(password, userOptional.get().getPassword())) {
            return ResponseEntity.status(401).body("잘못된 자격증명입니다.");
        }

        // JWT 토큰 발급
        String token = jwtUtil.generateToken(username);
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * 회원가입 (ID/PW 기반)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("아이디와 비밀번호를 입력하세요.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }

        // 비밀번호 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(password);
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(encodedPassword);
        userRepository.save(newUser);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    /**
     * OAuth2 (구글 로그인 후 JWT 발급)
     */
    @GetMapping("/google")
    public ResponseEntity<?> googleLoginSuccess(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            return ResponseEntity.status(400).body("Google 계정에서 이메일 정보를 가져올 수 없습니다.");
        }

        // 데이터베이스에서 사용자 확인, 없으면 자동 등록
        Optional<User> existingUser = userRepository.findByUsername(email);
        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setPassword(""); // OAuth 로그인 사용자는 비밀번호 필요 없음
            userRepository.save(newUser);
        }

        // JWT 발급
        String token = jwtUtil.generateToken(email);
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * 현재 로그인된 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }

        return ResponseEntity.ok(Map.of("username", userDetails.getUsername()));
    }

    @PostMapping("/google/callback")
    public ResponseEntity<?> googleCallback(@RequestBody Map<String, String> body) {
        String credential = body.get("credential");
        if (credential == null) {
            return ResponseEntity.badRequest().body("Credential is required");
        }

        try {
            // JWT 토큰(credential) 디코딩
            String[] chunks = credential.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            
            // payload 부분만 디코딩
            String payload = new String(decoder.decode(chunks[1]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode payloadJson = mapper.readTree(payload);
            
            // 이메일 추출
            String email = payloadJson.get("email").asText();
            
            // 사용자 확인 또는 생성
            Optional<User> existingUser = userRepository.findByUsername(email);
            if (existingUser.isEmpty()) {
                User newUser = new User();
                newUser.setUsername(email);
                newUser.setPassword(""); // OAuth 사용자는 비밀번호 불필요
                userRepository.save(newUser);
            }

            // JWT 토큰 생성
            String token = jwtUtil.generateToken(email);
            return ResponseEntity.ok(Map.of("token", token));
            
        } catch (Exception e) {
            logger.error("Google callback processing failed", e);
            return ResponseEntity.status(500).body("Failed to process Google callback");
        }
    }
}
