package mes.app.IssueInquiry;

import lombok.extern.slf4j.Slf4j;
import mes.app.IssueInquiry.service.IssueInquiryService;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/api/IssueInquiryAPI")
public class IssueInquiryAPIController {

  @Autowired
  IssueInquiryService issueInquiryService;

  @GetMapping("/List")
  public AjaxResult getList(@RequestParam(value = "startDate") String startDate,
                            @RequestParam(value = "endDate") String endDate,
                            @RequestParam(value = "SearchKeywords", required = false) String SearchKeywords) {

    AjaxResult result = new AjaxResult();

    log.info("등기부 api 조회 들어옴 startDate={}, endDate={}, SearchKeywords={} ", startDate, endDate, SearchKeywords);
    try {
      List<Map<String, Object>> getList = issueInquiryService.getAPIList(startDate, endDate, SearchKeywords);

      result.success = true;
      result.message = "데이터 조회 성공";
      result.data = getList;

    } catch (Exception e) {
      // 예외 처리
      result.success = false;
      result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
    }

    return result;

  }

  @GetMapping("")
  public AjaxResult DetailsList(@RequestParam(value = "realId") String REALID) {
    AjaxResult result = new AjaxResult();

    log.info("상세 조회 요청 - realId={}", REALID);

    try {
      List<Map<String, Object>> details = issueInquiryService.getDetails(REALID);
      result.success = true;
      result.message = "상세 데이터 조회 성공";
      result.data = details;
    } catch (Exception e) {
      log.error("데이터 조회 중 오류 발생: {}", e.getMessage());
      result.success = false;
      result.message = "데이터 조회 중 오류 발생";
    }

    return result;
  }

}
