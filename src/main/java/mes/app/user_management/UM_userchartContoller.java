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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                                  @RequestParam(value = "sexYn", required = false)String sexYn,
                                  @RequestParam(value = "columns", required = false)  List<String> columns) {
    AjaxResult result = new AjaxResult();
    log.info("들어온 데이터: startDate={}, endDate={},columns={}, sexYn={}", startDate, endDate, columns, sexYn);

    try {
      if (columns == null || columns.isEmpty()) {
        columns = List.of("region", "district", "inDatem"); // 기본값 설정
       // log.info("columns가 null이므로 기본값으로 설정: {}", columns);
      }

      // 원본 데이터 가져오기
      List<Map<String, Object>> rawData = userchartService.getGridList(startDate, endDate,columns, sexYn);
      //log.info("받아온 데이터 :{}", rawData);

      // 날짜 포맷터 생성 (입력 데이터 형식에 맞게 설정)
      DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"); // 소수점 이하 초 처리
      DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy"); // 연도만 추출

      for (Map<String, Object> user : rawData) {
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
      }
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


  @GetMapping("/read2")
  public AjaxResult getReportList2(@RequestParam(value = "startDate", required = false) String startDate,
                                   @RequestParam(value = "endDate", required = false) String endDate,
                                   @RequestParam(value = "inDatem" ,required = false) String inDatem,
                                   @RequestParam(value = "sexYn" , required = false) String sexYn,
                                   @RequestParam(value = "district" , required = false) String district) {
    AjaxResult result = new AjaxResult();
    log.info("지역/구축물_들어온 데이터: startDate={}, endDate={},inDatem={},sexYn={}, district={} ", startDate, endDate, inDatem, sexYn, district);

    try {

      // inDatem이 "inDatem"라면 inDatem 을 endDate로 설정
      if ("inDatem".equals(inDatem)) {
        //log.info(" inDatem이 'inDatem'이므로 inDatem을 endDate({})로 설정", endDate);
        inDatem = endDate; // inDatem을 endDate 값으로 설정
      }
      if ("district".equals(district)) {
      //  log.info(" district가 'district'이므로 전체 지역(district) 데이터 조회");
        district = "%"; // 모든 값을 포함하는 조건
      }
      if ("sexYn".equals(sexYn)) {
       // log.info(" sexYn 'sexYn'이므로 전체 성별(sexYn) 데이터 조회");
        sexYn = "%"; // 모든 값을 포함하는 조건
      }

      // 원본 데이터 가져오기
      List<Map<String, Object>> rawData = userchartService.getDynamicData(startDate, endDate, inDatem, sexYn, district);
      //log.info("받은 데이터 _지역/구축물 : {}", rawData);

      for (Map<String, Object> row : rawData) {
        if (row.containsKey("sexYn") && row.get("sexYn") != null) {
          String originalSex = row.get("sexYn").toString().trim();
          switch (originalSex) {
            case "1":
              row.put("sexYn", "남자");
              break;
            case "2":
              row.put("sexYn", "여자");
              break;
            default:
              row.put("sexYn", "알 수 없음");
              break;
          }
        }
      }

      // 날짜 변환 처리
      for (Map<String, Object> row : rawData) {
        if (row.containsKey("inDatem") && row.get("inDatem") != null) {
          String originalDate = row.get("inDatem").toString().trim();
          String selectedFormat = inDatem; // "Year", "Month", "Day" 값 중 하나

          // 쿼리에서 변환되어 왔으므로 그대로 사용
          switch (selectedFormat) {
            case "Year":
            case "Month":
            case "Day":
              row.put("inDatem", originalDate); // 변환 없이 그대로 사용
              break;
            default:
              row.put("inDatem", originalDate); // 기본적으로 원본 유지
          }
        }
      }

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

  @GetMapping("/getUserInfo")
  public ResponseEntity<List<Map<String, Object>>> getUserInfo(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam String region,
      @RequestParam String district,
      @RequestParam String ageGroup) {

    log.info("엑셀 다운_들어온 데이터: startDate={}, endDate={},region={},district={}, ageGroup={}", startDate, endDate, region, district, ageGroup);
    List<Map<String, Object>> userInfo = userchartService.getUserInfo(startDate, endDate, region, district, ageGroup);

    return ResponseEntity.ok(userInfo);
  }
}

