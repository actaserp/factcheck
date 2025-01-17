package mes.app.user.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        // 입력된 값이 없으면 기본 redirectUri와 clientId 사용
        String finalRedirectUri = redirectUri != null ? redirectUri : this.redirectUri;
        String finalClientId = clientId != null ? clientId : this.clientId;

        return String.format(
                "https://nid.naver.com/oauth2.0/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                finalClientId, finalRedirectUri
        );
    }

    @Override
    public String getAccessTokenUrl() {
        return "https://nid.naver.com/oauth2.0/token";
    }

    @Override
    public Map<String, String> getAccessTokenRequestParams(String clientId, String clientSecret, String code, String redirectUri) {
        // 입력된 값이 없으면 기본 redirectUri, clientId, clientSecret 사용
        return Map.of(
                "client_id", clientId != null ? clientId : this.clientId,
                "client_secret", clientSecret != null ? clientSecret : this.clientSecret,
                "code", code,
                "redirect_uri", redirectUri != null ? redirectUri : this.redirectUri,
                "grant_type", "authorization_code"
        );
    }


    //액세스 토큰으로 사용자 정보를 가져오는 URL을 반환
    @Override
    public String getUserInfoUrl(String accessToken) {
        return "https://openapi.naver.com/v1/nid/me";
    }
}
