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
                                  @RequestParam(value = "inDatem" ,required = false) String inDatem,
                                  @RequestParam(value = "district" , required = false) String district,
                                  @RequestParam(value = "region", required = false) String region) {
    AjaxResult result = new AjaxResult();
    //log.info("들어온 데이터[나이/구축물]: startDate={}, endDate={}, sexYn={},inDatem={}, district={}, region={} ", startDate, endDate, sexYn, inDatem, district, region);

    try {

      if ("inDatem".equals(inDatem)) {
        //log.info(" inDatem이 'inDatem'이므로 inDatem을 endDate({})로 설정", endDate);
        inDatem = endDate; // inDatem을 endDate 값으로 설정
      }
      if ("district".equals(district)) {
        //  log.info(" district가 'district'이므로 전체 지역(district) 데이터 조회");
        district = "%"; // 모든 값을 포함하는 조건
      }
      if("region".equals(region)) {
        region = "%";
      }
      if ("sexYn".equals(sexYn)) {
        // log.info(" sexYn 'sexYn'이므로 전체 성별(sexYn) 데이터 조회");
        sexYn = "%"; // 모든 값을 포함하는 조건
      }

      // 원본 데이터 가져오기
      List<Map<String, Object>> rawData = userchartService.getGridListDynamic(startDate, endDate, sexYn, inDatem, district, region);
      //log.info("받아온 데이터 :{}", rawData);

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


  @GetMapping("/read2")
  public AjaxResult getReportList2(@RequestParam(value = "startDate", required = false) String startDate,
                                   @RequestParam(value = "endDate", required = false) String endDate,
                                   @RequestParam(value = "inDatem" ,required = false) String inDatem,
                                   @RequestParam(value = "sexYn" , required = false) String sexYn,
                                   @RequestParam(value = "district" , required = false) String district) {
    AjaxResult result = new AjaxResult();
    //log.info("지역/구축물_들어온 데이터: startDate={}, endDate={},inDatem={},sexYn={}, district={} ", startDate, endDate, inDatem, sexYn, district);

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
      @RequestParam(value = "yearMonth",required = false) String yearMonth, // 발급일자 (년, 월, 일 포함)
      @RequestParam(value = "dateType",required = false) String dateType, // Year, Month, Day 중 선택
      @RequestParam(value = "region",required = false) String region,
      @RequestParam(value = "district",required = false) String district,
      @RequestParam(value = "sexYn",required = false) String sexYn,
      @RequestParam(value = "selectedColumn",required = false) String selectedColumn // 선택한 열(예: "아파트", "오피스")
  ) {
    log.info("엑셀 다운_들어온 데이터: dateType={}, yearMonth={}, region={}, district={}, sexYn={}, selectedColumn={}",
        dateType, yearMonth, region, district, sexYn, selectedColumn);

    // 📌 성별(sex) 값 변환 ("남자" → 1, "여자" → 2)
    Integer sexCode = null;
    if ("남자".equals(sexYn)) {
      sexCode = 1;
    } else if ("여자".equals(sexYn)) {
      sexCode = 2;
    }

    // 📌 `yearMonth` 가공 (dateType에 맞게 변환)
    if (yearMonth != null) {
      yearMonth = yearMonth.replace("-", "");  // "-" 제거하여 YYYY, YYYYMM, YYYYMMDD 형식으로 변경
    }

    // 📌 userchartService에서 해당 조건으로 데이터 가져오기
    List<Map<String, Object>> userInfo = userchartService.getUserInfo(yearMonth, dateType, region, district, sexCode, selectedColumn);

    return ResponseEntity.ok(userInfo);
  }


}

