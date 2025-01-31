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
                                  @RequestParam(value = "endDate", required = false) String endDate) {
    AjaxResult result = new AjaxResult();
    log.info("들어온 데이터: startDate={}, endDate={}", startDate, endDate);

    try {

      // 원본 데이터 가져오기
      List<Map<String, Object>> rawData = userchartService.getDynamicData(startDate, endDate);
//      log.info("받아온 데이터 :{}", rawData);

      // 날짜 포맷터 목록 (다양한 소수점 자리수를 처리하기 위함)
      List<DateTimeFormatter> formatters = Arrays.asList(
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"), // 소수점 3자리
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS"),  // 소수점 2자리
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"),   // 소수점 1자리
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")      // 소수점 없음
      );

      DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 날짜만 있는 경우
      DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy"); // 연도만 추출

      // 날짜 변환 처리
      for (Map<String, Object> row : rawData) {
        if (row.containsKey("inDatem") && row.get("inDatem") != null) {
          String originalDate = row.get("inDatem").toString().trim();

          boolean parsed = false;

          // 1. 먼저 날짜만 있는 경우 (yyyy-MM-dd) 변환 시도
          try {
            LocalDate date = LocalDate.parse(originalDate, dateOnlyFormatter);
            row.put("inDatem", String.valueOf(date.getYear())); // 연도만 저장
            parsed = true;
          } catch (Exception ignored) {
            // 변환 실패하면 다음 포맷 시도
          }

          // 2. 날짜 + 시간 포맷 시도
          if (!parsed) {
            for (DateTimeFormatter formatter : formatters) {
              try {
                LocalDateTime dateTime = LocalDateTime.parse(originalDate, formatter);
                row.put("inDatem", dateTime.format(yearFormatter)); // 연도만 저장
                parsed = true;
                break; // 변환 성공하면 루프 종료
              } catch (Exception ignored) {
                // 변환 실패한 경우 무시하고 다음 포맷 시도
              }
            }
          }

          // 3. 모든 포맷에서 변환 실패 시 기본값 설정
          if (!parsed) {
            log.error("날짜 변환 실패: {} (원본 데이터: {})", originalDate, row);
            row.put("inDatem", "알 수 없음");
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

