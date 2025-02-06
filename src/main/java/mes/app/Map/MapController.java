package mes.app.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mes.app.Map.service.MapService;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/Map")
public class MapController {

  @Autowired
  MapService mapService;

  @Value("${kakao.map.api.key}")
  private String kakaoMapApiKey;  //JavaScript key

  @Value("${kakao.map.clientKey}")
  private String kakaoClientKey;  //REST Key

  @Value("${sgis.consumer_key}")
  private String consumer_key;

  @Value("${sgis.consumer_secret}")
  private String consumer_secrets;

  @GetMapping("/script")
  public ResponseEntity<String> getKakaoMapScriptUrl() {
    // 클러스터링 및 장소 검색(library=clusterer,services) 추가
    String scriptUrl = "https://dapi.kakao.com/v2/maps/sdk.js?appkey=" + kakaoMapApiKey + "&libraries=clusterer,services";
    return ResponseEntity.ok(scriptUrl);
  }

  //지역정보
  @GetMapping
  public AjaxResult fetchSgisData(@RequestParam(required = false) String cd,
                                  @RequestParam(defaultValue = "0") String pg_yn) {

    AjaxResult ajaxResult = new AjaxResult();

    try {
      // Access Token 발급
      String accessToken = getAccessToken();

      // URL 생성
      String apiUrl = buildUrl(accessToken, cd, pg_yn);

      // HttpURLConnection 설정 및 요청
      HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);

      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();

        // JSON 응답 파싱 및 처리
        Map<String, Object> responseData = new ObjectMapper().readValue(response.toString(), Map.class);
        ajaxResult.success = true;
        ajaxResult.data = responseData;
      } else {
        ajaxResult.success = false;
        ajaxResult.message = "API 요청 실패: HTTP " + connection.getResponseCode();
      }
    } catch (Exception e) {
      ajaxResult.success = false;
      ajaxResult.message = "API 요청 중 에러 발생: " + e.getMessage();
    }

    return ajaxResult;
  }

  private String buildUrl(String accessToken, String cd, String pg_yn) {
    String baseUrl = "https://sgisapi.kostat.go.kr/OpenAPI3/addr/stage.json";
    StringBuilder url = new StringBuilder(baseUrl);
    url.append("?accessToken=").append(accessToken)
        .append("&pg_yn=").append(pg_yn);

    if (cd != null) {
      url.append("&cd=").append(URLEncoder.encode(cd, StandardCharsets.UTF_8));
    }

    return url.toString();
  }

  @GetMapping("/coordinates")
  public Map<String, Object> getCoordinates(@RequestParam String name) {
    Map<String, Object> response = new HashMap<>();
    String apiUrl = "https://dapi.kakao.com/v2/local/search/address.json?query=" + name;

    try {
      RestTemplate restTemplate = new RestTemplate();
      org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
      headers.set("Authorization", "KakaoAK " + kakaoClientKey);
      headers.set("User-Agent", "Mozilla/5.0");
      headers.set("KA", "sdk/1.0 os/java origin/my-app-name"); // 정확한 KA 필드 값 추가

      org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

      org.springframework.http.ResponseEntity<Map> apiResponse = restTemplate.exchange(apiUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
      Map<String, Object> body = apiResponse.getBody();

      if (body != null && body.containsKey("documents")) {
        List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");

        if (documents.isEmpty()) { //리스트가 비어 있는지 확인
          response.put("success", false);
          response.put("message", "좌표를 찾을 수 없습니다.");
          //log.warn(" 카카오 API 응답에서 좌표를 찾을 수 없음: {}", body);
        } else {
          Map<String, Object> firstResult = documents.get(0);
          response.put("success", true);
          response.put("lat", firstResult.get("y")); // 위도
          response.put("lng", firstResult.get("x")); // 경도

          //log.info("좌표 검색 성공: lat = {}, lng = {}", firstResult.get("y"), firstResult.get("x"));
        }
      } else {
        response.put("success", false);
        response.put("message", "카카오 API 응답이 올바르지 않습니다.");
        log.warn(" 카카오 API 응답이 올바르지 않음: {}", body);
      }
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "API 요청 중 오류가 발생했습니다: " + e.getMessage());

      log.error("카카오 좌표 API 요청 중 오류 발생: {}", e.getMessage(), e);
    }

    return response;
  }

  @GetMapping("/getList")
  public ResponseEntity<Map<String, Object>> getList(@RequestParam String name) {
    Map<String, Object> response = new HashMap<>();
    try {
      List<Map<String, Object>> results = mapService.getList(name);
      if (!results.isEmpty()) {
        response.put("success", true);
        response.put("data", results);
      } else {
        response.put("success", false);
        response.put("message", "데이터를 찾을 수 없습니다.");
      }
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "요청 중 오류 발생: " + e.getMessage());
    }
    return ResponseEntity.ok(response);
  }

  private String currentAccessToken; // 현재 유효한 토큰 저장
  private long tokenExpiryTime = 0;  // 토큰 만료 시간 (Unix Time 기준)

  //지역 정보 토큰
  private String getAccessToken() throws Exception {
    if (System.currentTimeMillis() < tokenExpiryTime && currentAccessToken != null) {
      return currentAccessToken; // 유효한 토큰이 있다면 반환
    }

    String tokenUrl = "https://sgisapi.kostat.go.kr/OpenAPI3/auth/authentication.json";
    StringBuilder url = new StringBuilder(tokenUrl)
        .append("?consumer_key=").append(URLEncoder.encode(consumer_key, StandardCharsets.UTF_8))
        .append("&consumer_secret=").append(URLEncoder.encode(consumer_secrets, StandardCharsets.UTF_8));

    HttpURLConnection connection = (HttpURLConnection) new URL(url.toString()).openConnection();
    connection.setRequestMethod("GET");
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(5000);

    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      // JSON 응답 파싱
      String responseBody = response.toString();
      Map<String, Object> responseMap = new ObjectMapper().readValue(responseBody, Map.class);

      if (responseMap.containsKey("result")) {
        Map<String, Object> result = (Map<String, Object>) responseMap.get("result");

        if (result.containsKey("accessToken") && result.containsKey("accessTimeout")) {
          currentAccessToken = (String) result.get("accessToken");
          tokenExpiryTime = Long.parseLong((String) result.get("accessTimeout")); // 만료 시간 설정
          //log.info("새 Access Token 발급: {}, 만료 시간: {}", currentAccessToken, tokenExpiryTime);
          return currentAccessToken;
        } else {
          log.error("SGIS API 응답 데이터에 필요한 필드가 없습니다: {}", result);
          throw new RuntimeException("SGIS API 응답 데이터에 필요한 필드가 없습니다.");
        }
      } else {
        log.error("SGIS API 오류: {}", responseMap);
        throw new RuntimeException("SGIS API 오류: " + responseMap.getOrDefault("errMsg", "알 수 없는 오류"));
      }
    } else {
      BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
      StringBuilder errorResponse = new StringBuilder();
      String errorLine;
      while ((errorLine = errorReader.readLine()) != null) {
        errorResponse.append(errorLine);
      }
      errorReader.close();

      log.error("Access Token 발급 실패: HTTP {}, 응답: {}", connection.getResponseCode(), errorResponse.toString());
      throw new RuntimeException("Access Token 발급 실패: HTTP " + connection.getResponseCode());
    }
  }

  //마커
  @GetMapping("/markersByRegion")
  public ResponseEntity<Map<String, Object>> getMarkersForRegion(
      @RequestParam String region) {
    // log.info("마커 들어옴 region={}", region);

    String[] parts = region.split(" ");
    String sido = parts.length > 0 ? parts[0] : "";  // 시/도
    String gugun = parts.length > 1 ? parts[1] : ""; // 구/군

    List<Map<String, Object>> getMarker = mapService.getMarkersForRegion(sido, gugun);
    //log.info("마커 데이터(받아온거) ={} ", getMarker);

    // 각 마커 데이터에 좌표 추가
    for (Map<String, Object> marker : getMarker) {
      String address = (marker.get("address") != null && !marker.get("address").toString().trim().isEmpty())
          ? marker.get("address").toString()
          : (marker.get("RESIDO") + " " + marker.get("REGUGUN"));

      Map<String, Object> coordinates = getCoordinates(address);

      if (coordinates.get("success").equals(true)) {
        marker.put("lat", coordinates.get("lat"));
        marker.put("lng", coordinates.get("lng"));
        //log.info("좌표 변환 성공: {} -> lat: {}, lng: {}", address, coordinates.get("lat"), coordinates.get("lng"));

        // avg_score를 기반으로 등급 매기기
        if (marker.get("avg_score") != null) {
          int score = Integer.parseInt(marker.get("avg_score").toString());
          marker.put("grade", getGrade(score));
        } else {
          marker.put("grade", "Unknown");  // 점수 없을 경우
        }

      } else {
        log.warn("좌표 변환 실패: {}", address);
      }
    }

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("markers", getMarker);
    return ResponseEntity.ok(response);
  }

  private String getGrade(int score) {
    if (score >= 90) return "S";
    else if (score >= 80) return "A";
    else if (score >= 70) return "B";
    else if (score >= 60) return "C";
    else if (score >= 50) return "D";
    else if (score >= 40) return "E";
    else return "F";
  }
}