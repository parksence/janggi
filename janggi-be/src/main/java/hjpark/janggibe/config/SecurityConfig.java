package hjpark.janggibe.config;

import hjpark.janggibe.model.Provider;
import hjpark.janggibe.model.User;
import hjpark.janggibe.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;
    private final DefaultOAuth2UserService oAuth2UserService;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService, JwtFilter jwtFilter, DefaultOAuth2UserService oAuth2UserService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
        this.oAuth2UserService = oAuth2UserService;
        this.userRepository = userRepository;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
                        .requestMatchers("/auth/dark").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*"))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                            
                            // OAuth2에서 받아온 사용자 정보 로깅
                            log.info("OAuth2 User Attributes: {}", oAuth2User.getAttributes());
                            
                            String email = oAuth2User.getAttribute("email");
                            String picture = oAuth2User.getAttribute("picture");
                            String name = oAuth2User.getAttribute("name");
                            String sub = oAuth2User.getAttribute("sub");
                            
                            log.info("Email: {}", email);
                            log.info("Picture: {}", picture);
                            log.info("Name: {}", name);
                            log.info("Sub: {}", sub);
                            
                            // 이메일 주소에서 특수문자가 변경되지 않도록 처리
                            email = email.trim();  // 앞뒤 공백 제거
                            
                            // 사용자 정보 저장 또는 업데이트
                            User user = userRepository.findByEmail(email)
                                .orElse(User.builder()
                                    .email(email)
                                    .nickname(name)
                                    .profileImage(picture)
                                    .provider(Provider.GOOGLE)
                                    .providerId(sub)
                                    .build());
                                
                            userRepository.save(user);
                            
                            // JWT에 저장할 사용자 정보 생성
                            Map<String, Object> claims = new HashMap<>();
                            claims.put("email", email);
                            claims.put("nickname", name);
                            claims.put("picture", picture);
                            
                            String token = jwtUtil.generateToken(claims);
                            response.setCharacterEncoding("UTF-8");
                            response.sendRedirect("http://localhost:5173?token=" + token);
                        })
                )
                .exceptionHandling(e -> e
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Unauthorized");
                    })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
