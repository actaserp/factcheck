package mes.app.user.service;


import lombok.extern.slf4j.Slf4j;
import mes.app.user.controller.SocialLoginProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
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

        // 파라미터 디버깅 로그
        log.info("네이버 Access Token 요청 파라미터: {}", params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(params, headers);

        // Access Token 요청
        ResponseEntity<Map> response = restTemplate.postForEntity(provider.getAccessTokenUrl(), requestEntity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Access Token 요청 실패: 상태 코드 - {}", response.getStatusCode());
            throw new IllegalStateException("Failed to retrieve access token: " + response.getStatusCode());
        }

        // 응답 디버깅
        log.info("Access Token 응답 데이터: {}", response.getBody());
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

       // log.info("사용자 정보 요청 Authorization 헤더: {}", headers);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String userInfoUrl = provider.getUserInfoUrl(accessToken);
        //log.info("사용자 정보 요청 URL: {}", userInfoUrl);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("HTTP 응답 상태 코드: {}", response.getStatusCode());
                throw new IllegalStateException("Failed to retrieve user info");
            }

            //log.info("사용자 정보 응답 데이터: {}", response.getBody());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("HTTP 에러 상태 코드: {}", e.getStatusCode());
            log.error("HTTP 에러 메시지: {}", e.getResponseBodyAsString());
            throw new IllegalStateException("Failed to retrieve user info due to HTTP error", e);
        } catch (Exception e) {
            log.error("사용자 정보 요청 중 예외 발생", e);
            throw new IllegalStateException("An unexpected error occurred while retrieving user info", e);
        }
    }
}
