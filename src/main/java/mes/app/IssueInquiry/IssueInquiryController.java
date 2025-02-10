package mes.app.IssueInquiry;

import lombok.extern.slf4j.Slf4j;
import mes.app.IssueInquiry.service.IssueInquiryService;
import mes.domain.entity.User;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

  // PDF 파일 조회 API
  @GetMapping("/pdf")
  public ResponseEntity<byte[]> getPdf(@RequestParam(value = "realId") int realId) {
    log.info("📄 PDF 조회 요청: realId={}", realId);

    try {
      // 1. DB에서 PDF 파일명 조회
      Optional<String> optionalPdfFileName = issueInquiryService.findPdfFilenameByRealId(realId);

      if (optionalPdfFileName.isEmpty()) {
//        log.warn(" PDF 파일명을 찾을 수 없음: realId={}", realId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      String pdfFileName = optionalPdfFileName.get();

      // 2. 운영체제별 저장 경로 설정
      String osName = System.getProperty("os.name").toLowerCase();
      String uploadDir;

      if (osName.contains("win")) {
        uploadDir = "C:\\temp\\registerFiles\\"; // Windows 환경
      } else {
        String userHome = System.getProperty("user.home");
        uploadDir = userHome + "/registerFiles/"; // Mac, Linux, Android 환경
      }

      // 3. PDF 파일 경로 설정 및 존재 여부 확인
      String pdfFilePath = uploadDir + pdfFileName;
      log.info("PDF 파일 경로: {}", pdfFilePath);

      Path path = Paths.get(pdfFilePath);
      if (!Files.exists(path)) {
        log.warn("파일이 존재하지 않음: {}", pdfFilePath);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      // 4. PDF 파일 읽기 및 반환
      byte[] pdfBytes = Files.readAllBytes(path);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDisposition(ContentDisposition.inline().filename(pdfFileName).build());

      return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    } catch (IOException e) {
      log.error("파일을 읽는 중 오류 발생", e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (Exception e) {
      log.error("서버 내부 오류 발생", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }


  @GetMapping("/checkLogin")
  public ResponseEntity<Map<String, Object>> checkLogin(HttpSession session, HttpServletResponse response) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());

    if (!isLoggedIn) {
      // 비회원이면 로그인 페이지로 리디렉트
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header("Location", "/mobile/mlogin")
          .body(null);
    }

    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("isLoggedIn", true);
    return ResponseEntity.ok(responseBody);
  }



}
