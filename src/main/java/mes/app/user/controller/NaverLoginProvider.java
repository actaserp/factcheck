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
public class NaverLoginProvider implements SocialLoginProvider {

    @Value("${naver.clientKey}")
    private String clientId;

    @Value("${naver.clientSecret}")
    private String clientSecret;

    @Value("${naver.redirectUri}")
    private String redirectUri;

    @Override
    public String getAuthorizationUrl(String redirectUri, String clientId, Map<String, String> additionalParams) {
        // 네이버 인증 URL 생성
        String finalRedirectUri = redirectUri != null ? redirectUri : this.redirectUri;
        String finalClientId = clientId != null ? clientId : this.clientId;

        String authorizationUrl = String.format(
            "https://nid.naver.com/oauth2.0/authorize?client_id=%s&redirect_uri=%s&response_type=code",
            finalClientId, finalRedirectUri
        );

        //log.info("네이버 인증 URL 생성: {}", authorizationUrl);
        return authorizationUrl;
    }

    @Override
    public String getAccessTokenUrl() {
        return "https://nid.naver.com/oauth2.0/token";
    }

    @Override
    public Map<String, String> getAccessTokenRequestParams(String clientId, String clientSecret, String code, String redirectUri) {
        // 입력된 값이 없으면 기본 필드 값을 사용
        String finalClientId = clientId != null ? clientId : this.clientId;
        String finalClientSecret = clientSecret != null ? clientSecret : this.clientSecret;
        String finalRedirectUri = redirectUri != null ? redirectUri : this.redirectUri;

        // 요청 파라미터 구성
        Map<String, String> params = Map.of(
            "client_id", finalClientId,
            "client_secret", finalClientSecret,
            "code", code,
            "redirect_uri", finalRedirectUri,
            "grant_type", "authorization_code"
        );

        //log.info("네이버 Access Token 요청 파라미터: {}", params);
        return params;
    }

    @Override
    public String getUserInfoUrl(String accessToken) {
        return "https://openapi.naver.com/v1/nid/me";
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
            //log.info("발급된 Access Token: {}", accessToken);
            return accessToken;

        } catch (HttpClientErrorException e) {
            log.error("HTTP 요청 실패: {}", e.getResponseBodyAsString());
            throw new IllegalStateException("Failed to retrieve access token", e);
        }
    }
}