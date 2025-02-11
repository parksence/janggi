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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;


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
     * 현재 로그인된 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }

        return ResponseEntity.ok(Map.of("username", userDetails.getUsername()));
    }

    @GetMapping("/dark")
    public ResponseEntity<?> getDarkMode(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok().body(Map.of("isDarkMode", user.isDarkMode()));
    }

    @PutMapping("/dark")
    public ResponseEntity<?> updateDarkMode(@RequestParam boolean darkMode, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDarkMode(darkMode);
        userRepository.save(user);
        return ResponseEntity.ok().body(Map.of("isDarkMode", user.isDarkMode()));
    }

}
