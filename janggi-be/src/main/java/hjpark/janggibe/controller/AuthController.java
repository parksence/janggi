package hjpark.janggibe.controller;

import hjpark.janggibe.config.JwtUtil;
import hjpark.janggibe.model.User;
import hjpark.janggibe.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // 사용자 이름이나 비밀번호가 없는 경우
        if (username == null || password == null) {
            return ResponseEntity.badRequest().build();
        }

        // 사용자 이름으로 사용자 찾기
        User user = userRepository.findByUsername(username).orElse(null);
        if(user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("잘못된 자격증명입니다.");
        }

        // JWT 토큰 발급
        String token = jwtUtil.generateToken(username);
        Map<String, String> response = Map.of("token", token);

        return ResponseEntity.ok(response);
    }
}
