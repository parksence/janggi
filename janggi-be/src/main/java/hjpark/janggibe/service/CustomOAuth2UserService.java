package hjpark.janggibe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest);
    }

    public String getGoogleUserEmail(String code) {
        try {
            // 1. 액세스 토큰 얻기
            String tokenUrl = "https://oauth2.googleapis.com/token";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
            tokenRequest.add("client_id", clientId);
            tokenRequest.add("client_secret", clientSecret);
            tokenRequest.add("code", code);
            tokenRequest.add("redirect_uri", "http://localhost:5173/auth/google");
            tokenRequest.add("grant_type", "authorization_code");

            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(
                tokenUrl,
                new HttpEntity<>(tokenRequest, headers),
                String.class
            );

            // 2. 액세스 토큰으로 사용자 정보 얻기
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(tokenResponse.getBody());
            String accessToken = root.path("access_token").asText();

            String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
            headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );

            root = mapper.readTree(userInfoResponse.getBody());
            return root.path("email").asText();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get user info from Google", e);
        }
    }
} 