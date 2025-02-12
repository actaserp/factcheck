package mes.config;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class OneSignalService {

    private static final String ONESIGNAL_APP_ID = "683381a8-bf12-4cd7-b27f-cfa00c5bf860";
    private static final String ONESIGNAL_API_KEY = "os_v2_app_nazydkf7cjgnpmt7z6qayw7ymavvs5ysfgous5mie26vrdnqk63ke5ryqd25b7lvpbdwf6egxhnxxeevnuvb3b45fmer7rzdc7cakci";
    private static final String ONESIGNAL_URL = "https://api.onesignal.com/notifications?c=push";

    public void sendPushNotification(String pushId, String message) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + ONESIGNAL_API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("app_id", ONESIGNAL_APP_ID);
        body.put("include_external_user_ids", Collections.singletonList(pushId)); // 회원의 pushid 사용
        body.put("contents", Map.of("en", message));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(ONESIGNAL_URL, HttpMethod.POST, request, String.class);
        System.out.println("OneSignal Response: " + response.getBody());
    }
}
