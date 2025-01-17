package mes.app.user.service;


import lombok.extern.slf4j.Slf4j;
import mes.app.user.controller.SocialLoginProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class SocialLoginService {

    @Autowired
    private AuthUserService authUserService;

    /**
     * 소셜 제공자로부터 Access Token을 요청합니다.
     *
     * @param provider 소셜 로그인 제공자
     * @param code     소셜 로그인 인증 코드
     * @return Access Token 및 기타 응답 데이터
     */
    public Map<String, Object> getAccessToken(SocialLoginProvider provider, String code) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> params = provider.getAccessTokenRequestParams(
                null, // clientId를 null로 전달하면 provider 내부 값 사용
                null, // clientSecret을 null로 전달하면 provider 내부 값 사용
                code,
                null  // redirectUri를 null로 전달하면 provider 내부 값 사용
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(provider.getAccessTokenUrl(), params, Map.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to retrieve access token: " + response.getStatusCode());
        }

        return response.getBody();
    }

    /**
     * 소셜 제공자로부터 사용자 정보를 요청합니다.
     *
     * @param provider    소셜 로그인 제공자
     * @param accessToken Access Token
     * @return 사용자 정보
     */
    public Map<String, Object> getUserInfo(SocialLoginProvider provider, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                provider.getUserInfoUrl(accessToken),
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to retrieve user info: " + response.getStatusCode());
        }

        return response.getBody();
    }

    /**
     * 소셜 로그인 처리 로직
     *
     * @param provider 소셜 로그인 제공자
     * @param code     인증 코드
     */
    public void handleSocialLogin(SocialLoginProvider provider, String code) {
        // 1. Access Token 요청
        Map<String, Object> tokenResponse = getAccessToken(provider, code);
        String accessToken = (String) tokenResponse.get("access_token");

        // 2. 사용자 정보 요청
        Map<String, Object> userInfo = getUserInfo(provider, accessToken);

        // 3. 사용자 생성/업데이트 위임
        authUserService.createOrUpdateUser(userInfo, provider.getClass().getSimpleName());
    }

}
