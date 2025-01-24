package mes.app.user_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mes.app.user_management.service.UM_userchartService;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/report")
public class UM_userchartContoller {

  @Autowired
  UM_userchartService userchartService;

  @Autowired
  private ObjectMapper objectMapper;

  @GetMapping("/read")
  public AjaxResult getReportList(@RequestParam(value = "startDate", required = false) String startDate,
                                  @RequestParam(value = "endDate", required = false) String endDate,
                                  @RequestParam(value = "selectedRows", required = false)  List<String> columns) {
    AjaxResult result = new AjaxResult();
    log.info("들어온 데이터: startDate={}, endDate={},columns={}", startDate, endDate, columns);

    try {
      if (columns == null || columns.isEmpty()) {
        columns = List.of("region", "district", "inDatem"); // 기본값 설정
        log.info("columns가 null이므로 기본값으로 설정: {}", columns);
      }

      // 원본 데이터 가져오기
      List<Map<String, Object>> rawData = userchartService.getGridList(startDate, endDate,columns);
      log.info("받아온 데이터 :{}", rawData);

      // 날짜 포맷터 생성 (입력 데이터 형식에 맞게 설정)
      DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"); // 소수점 이하 초 처리
      DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy"); // 연도만 추출

      /*for (Map<String, Object> user : rawData) {
        // **동적으로 컬럼 데이터 처리**
        if (columns != null && !columns.isEmpty()) {
          // 성별 처리
          if (columns.contains("sexYn") && user.containsKey("sexYn")) {
            Object sexYnValue = user.get("sexYn");
            if (sexYnValue != null) {
              String sexYnStr = sexYnValue.toString();
              if ("1".equals(sexYnStr)) {
                user.put("sexYn", "남자");
              } else if ("2".equals(sexYnStr)) {
                user.put("sexYn", "여자");
              } else {
                user.put("sexYn", "알 수 없음");
              }
            }
          }

          // 날짜 처리
          if (columns.contains("inDatem") && user.containsKey("inDatem")) {
            Object indatemValue = user.get("inDatem");
            if (indatemValue != null) {
              try {
                // 입력 데이터를 포맷터로 변환 후 연도만 추출
                LocalDateTime dateTime = LocalDateTime.parse(indatemValue.toString().trim(), inputFormatter);
                String year = dateTime.format(yearFormatter); // 연도만 추출
                user.put("inDatem", year);
              } catch (Exception ex) {
                // 포맷 오류 처리
                log.error("날짜 포맷 중 오류 발생: {}", ex.getMessage());
                user.put("inDatem", "잘못된 날짜 형식");
              }
            }
          }
        }
      }*/
      result.success = true;
      result.message = "데이터 조회 성공";
      result.data = rawData;
    } catch (Exception e) {
      log.error("에러 발생: {}", e.getMessage(), e); // 에러 로그 추가
      result.success = false;
      result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
    }

    return result;
  }

  // REALGUBUN 값을 매핑하는 메서드
  private String mapRealGubun(String realGubun) {
    switch (realGubun) {
      case "아파트":
        return "apartment";
      case "빌라":
        return "villa";
      case "단독":
        return "house";
      case "오피스":
        return "office";
      default:
        return "other";
    }
  }

  @GetMapping("/getUserInfo")
  public ResponseEntity<List<Map<String, Object>>> getUserInfo(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam String region,
      @RequestParam String district,
      @RequestParam String ageGroup) {

    List<Map<String, Object>> userInfo = userchartService.getUserInfo(startDate, endDate, region, district, ageGroup);

    return ResponseEntity.ok(userInfo);
  }
}

