package mes.app.IssueInquiry;

import lombok.extern.slf4j.Slf4j;
import mes.app.IssueInquiry.service.IssueInquiryService;
import mes.domain.entity.User;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/IssueInquiry")
public class IssueInquiryController {

  @Autowired
  IssueInquiryService issueInquiryService;

  @GetMapping("/List")
  public AjaxResult getList(@RequestParam(value = "startDate") String startDate,
                            @RequestParam(value = "endDate") String endDate,
                            @RequestParam(value = "SearchKeywords", required = false) String SearchKeywords,
                            Authentication auth) {

    AjaxResult result = new AjaxResult();



    log.info("등기부 발급 조회 등어옴 startDate={}, endDate={}, SearchKeywords={} ", startDate, endDate, SearchKeywords);
    try {
      User user = (User)auth.getPrincipal();
      List<Map<String, Object>> getList = issueInquiryService.getList(startDate, endDate, SearchKeywords, user.getUsername());

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

  @PostMapping("/SaveViewHistory")
  public AjaxResult SaveViewHistory(@RequestParam(value = "realId") int REALID ,
                                    Authentication auth) {
    AjaxResult result = new AjaxResult();

    log.info("상세 조회 요청 - realId={}", REALID);

    try {
      User user = (User)auth.getPrincipal();

      List<Map<String, Object>> SaveViewHistory = issueInquiryService.SaveViewHistory(REALID, user);

      result.success = true;
      result.message = "상세 데이터 저장 성공";
      result.data = SaveViewHistory;
    } catch (Exception e) {
      log.error("데이터 저장 중 오류 발생: {}", e.getMessage());
      result.success = false;
      result.message = "데이터 저장 중 오류 발생";
    }

    return result;

  }

  @GetMapping("/pdf")
  public ResponseEntity<byte[]> getPdf(@RequestParam(value = "realId") int realId ){
    try {
      // DB에서 realId에 해당하는 PDFFILENAME을 조회
      String pdfFileName = issueInquiryService.findPdfFilenameByRealId(realId);

      // PDFFILENAME이 null이면 404 응답 반환
      if (pdfFileName == null || pdfFileName.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      // PDF 파일 경로 (DB에서 찾아온 파일 이름 사용)
      String pdfFilePath = "/path/to/pdf/" + pdfFileName;  // 실제 경로를 사용해야 함

      // PDF 파일을 바이트 배열로 읽기
      Path path = Paths.get(pdfFilePath);
      byte[] pdfBytes = Files.readAllBytes(path);

      // HTTP 응답 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDisposition(ContentDisposition.builder("inline").filename(pdfFileName).build());

      return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    } catch (IOException e) {
      // 파일이 없거나 IO 오류 발생 시 404 반환
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      // 기타 예외 처리 (예: DB에서 파일 이름을 못 찾은 경우)
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


}
