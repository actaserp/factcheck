package mes.app.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class KakaoLoginProvider implements SocialLoginProvider {

    @Value("${kakao.clientKey}")
    private String clientId;

    @Value("${kakao.admin.key}")
    private String clientSecret; // 실제로 admin key 사용 여부 확인 필요
    // Kakao의 경우 clientSecret이 없는 설정도 가능하므로 상황에 따라 변경 가능

    @Value("${kakao.redirectUri}")
    private String redirectUri;

    @Override
    public String getAuthorizationUrl(String redirectUri, String clientId, Map<String, String> additionalParams) {
//        log.info("KakaoLoginProvider - 인증 URL 요청 수신");
//        log.debug("clientId: {}, redirectUri: {}", this.clientId, this.redirectUri);
        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                this.clientId, this.redirectUri
        );
    }

    @Override
    public String getAccessTokenUrl() {
//        log.info("KakaoLoginProvider - 액세스 토큰 URL 요청");
        return "https://kauth.kakao.com/oauth/token";
    }

    @Override
    public Map<String, String> getAccessTokenRequestParams(String clientId, String clientSecret, String code, String redirectUri) {
//        log.info("KakaoLoginProvider - 액세스 토큰 요청 파라미터 생성");
//        log.debug("code: {}, redirectUri: {}", code, this.redirectUri);
        return Map.of(
                "client_id", this.clientId,
                "client_secret", this.clientSecret, // 필요한 경우에만 사용
                "code", code,
                "redirect_uri", this.redirectUri,
                "grant_type", "authorization_code"
        );
    }

    @Override
    public String getUserInfoUrl(String accessToken) {
//        log.info("KakaoLoginProvider - 사용자 정보 URL 요청");
//        log.debug("accessToken: {}", accessToken);
        return "https://kapi.kakao.com/v2/user/me";
    }


    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> params = getAccessTokenRequestParams(null, null, code, null);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.setAll(params);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                getAccessTokenUrl(),
                HttpMethod.POST,
                requestEntity,
                Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Failed to retrieve access token");
            }

            Map<String, Object> responseBody = response.getBody();
            String accessToken = (String) responseBody.get("access_token");
//            log.info("발급된 Kakao Access Token: {}", accessToken);
            return accessToken;

        } catch (HttpClientErrorException e) {
            log.error("카카오 액세스 토큰 요청 실패: {}", e.getResponseBodyAsString());
            throw new IllegalStateException("Failed to retrieve Kakao access token", e);
        }
    }
}