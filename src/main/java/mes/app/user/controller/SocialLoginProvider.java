package mes.app.user.controller;

import java.util.Map;

public interface SocialLoginProvider {
    String getAuthorizationUrl(String redirectUri, String clientId, Map<String, String> additionalParams);

    String getAccessTokenUrl();

    Map<String, String> getAccessTokenRequestParams(String clientId, String clientSecret, String code, String redirectUri);

    String getUserInfoUrl(String accessToken);

  String getAccessToken(String code);
}

