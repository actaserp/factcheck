package mes.app.user.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                this.clientId, this.redirectUri
        );
    }

    @Override
    public String getAccessTokenUrl() {
        return "https://kauth.kakao.com/oauth/token";
    }

    @Override
    public Map<String, String> getAccessTokenRequestParams(String clientId, String clientSecret, String code, String redirectUri) {
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
        return "https://kapi.kakao.com/v2/user/me";
    }
}
