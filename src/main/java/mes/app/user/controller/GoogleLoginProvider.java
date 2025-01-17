package mes.app.user.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleLoginProvider implements SocialLoginProvider {

    @Value("${google.clientKey}")
    private String clientId;

    @Value("${google.pw}")
    private String clientSecret;

    @Value("${google.redirectUri}")
    private String redirectUri;

    @Override
    public Map<String, String> getAccessTokenRequestParams(String clientId, String clientSecret, String code, String redirectUri) {
        return Map.of(
                "client_id", this.clientId,
                "client_secret", this.clientSecret,
                "code", code,
                "redirect_uri", this.redirectUri,
                "grant_type", "authorization_code"
        );
    }

    @Override
    public String getAuthorizationUrl(String redirectUri, String clientId, Map<String, String> additionalParams) {
        return String.format(
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=email profile",
                clientId, redirectUri
        );
    }

    @Override
    public String getAccessTokenUrl() {
        return "https://oauth2.googleapis.com/token";
    }

    @Override
    public String getUserInfoUrl(String accessToken) {
        return "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken;
    }
}