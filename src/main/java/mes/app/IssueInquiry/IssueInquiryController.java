package mes.app.IssueInquiry;

import lombok.extern.slf4j.Slf4j;
import mes.app.IssueInquiry.service.IssueInquiryService;
import mes.domain.entity.User;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
                            @RequestParam(value = "realId", required = false) Integer realId,
                            Authentication auth) {

    AjaxResult result = new AjaxResult();

    // realId가 null이면 기본값 0으로 설정
    if (realId == null) {
      realId = 0;
    }
   // log.info("등기부 발급 조회 요청: startDate={}, endDate={}, SearchKeywords={}, realId={} ", startDate, endDate, SearchKeywords, realId);
    try {
      User user = (User) auth.getPrincipal();
      List<Map<String, Object>> getList = issueInquiryService.getList(startDate, endDate, SearchKeywords, user.getUsername(), realId);

      result.success = true;
      result.message = "데이터 조회 성공";
      result.data = getList;
    } catch (Exception e) {
      // 예외 처리
      log.error("데이터 조회 중 오류 발생", e);
      result.success = false;
      result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
    }

    return result;
  }

  @GetMapping("/DetailsList")
  public AjaxResult DetailsList(@RequestParam(value = "realId") String REALID) {
    AjaxResult result = new AjaxResult();
   // log.info("상세 조회 요청 - realId={}", REALID);
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
   // log.info("상세 조회 요청 - realId={}", REALID);
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
  @RequestMapping(value = "/pdf", method = RequestMethod.GET)
  public ResponseEntity<Resource> getPdf(@RequestParam("realId") int realId) {
    try {
     // log.info("PDF 조회 요청: realId={}", realId);

      // DB에서 PDF 파일명 조회
      Optional<String> optionalPdfFileName = issueInquiryService.findPdfFilenameByRealId(realId);
      if (optionalPdfFileName.isEmpty()) {
        log.warn("PDF 파일명을 찾을 수 없음: realId={}", realId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      // 파일명 그대로 사용
      String pdfFileName = optionalPdfFileName.get();
     // log.info("사용 파일명: {}", pdfFileName);

      //운영체제별 저장 경로 설정
      String osName = System.getProperty("os.name").toLowerCase();
      String uploadDir = osName.contains("win") ? "C:\\Temp\\registerFiles\\"
          : System.getProperty("user.home") + "/registerFiles/";

      // PDF 파일 경로 설정 및 존재 여부 확인
      Path pdfPath = Paths.get(uploadDir, pdfFileName);
      //log.info("PDF 파일 경로: {}", pdfPath.toString());

      if (!Files.exists(pdfPath)) {
        log.warn("파일이 존재하지 않음: {}", pdfPath.toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      // PDF 파일을 Resource로 변환 후 응답
      File file = pdfPath.toFile();
      Resource resource = new FileSystemResource(file);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDisposition(ContentDisposition.inline().filename(pdfFileName, StandardCharsets.UTF_8).build());

      // `X-Frame-Options` 제거 (필요한 경우 추가 가능)
      headers.add("X-Frame-Options", "ALLOW-FROM http://localhost:8040");
      headers.add("Access-Control-Allow-Origin", "*");  // 모든 도메인 허용
      headers.add("Access-Control-Allow-Methods", "GET, OPTIONS");
      headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");

      return ResponseEntity.ok()
          .headers(headers)
          .contentLength(file.length())
          .body(resource);

    } catch (Exception e) {
      log.error(" 서버 내부 오류 발생", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  //pdf 다운로드 
  @RequestMapping(value = "/pdfDownload", method = RequestMethod.GET)
  public ResponseEntity<Resource> downloadPdf(@RequestParam("realId") int realId) {
    try {
      //log.info("📄 PDF 다운로드 요청: realId={}", realId);

      // DB에서 PDF 파일명 조회
      Optional<String> optionalPdfFileName = issueInquiryService.findPdfFilenameByRealId(realId);
      if (optionalPdfFileName.isEmpty()) {
        //log.warn("PDF 파일명을 찾을 수 없음: realId={}", realId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      // 파일명 그대로 사용
      String pdfFileName = optionalPdfFileName.get();
     // log.info("다운로드 파일명: {}", pdfFileName);

      // 운영체제별 저장 경로 설정
      String osName = System.getProperty("os.name").toLowerCase();
      String uploadDir = osName.contains("win") ? "C:\\Temp\\registerFiles\\"
          : System.getProperty("user.home") + "/registerFiles/";

      // PDF 파일 경로 설정 및 존재 여부 확인
      Path pdfPath = Paths.get(uploadDir, pdfFileName);
      if (!Files.exists(pdfPath)) {
       // log.warn("파일이 존재하지 않음: {}", pdfPath.toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      // PDF 파일을 Resource로 변환 후 응답
      File file = pdfPath.toFile();
      Resource resource = new FileSystemResource(file);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDisposition(ContentDisposition.attachment().filename(pdfFileName, StandardCharsets.UTF_8).build());

      return ResponseEntity.ok()
          .headers(headers)
          .contentLength(file.length())
          .body(resource);

    } catch (Exception e) {
      log.error("서버 내부 오류 발생", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  //로그인 체크
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


  @GetMapping("/popupDetails")
  public ResponseEntity<Map<String, Object>> popupDetails(@RequestParam(value = "realid") int realid) {
    //log.info("이력 내역 조회 시작 - realid: {}", realid);
    Map<String, Object> response = new HashMap<>();
    try {
      // Step 1: realid를 이용해 PinNo 찾기
      List<Map<String, Object>> findPinNo = issueInquiryService.getFindPinNo(realid);
     // log.info("[Step 1] 조회된 PinNo 리스트: {}", findPinNo);

      // Step 2: PinNo에서 REALID 추출
      Set<Integer> realIds = new TreeSet<>(Collections.reverseOrder()); // 큰 값이 먼저 오도록 정렬
      for (Map<String, Object> pinData : findPinNo) {
        Object realIdObj = pinData.get("REALID");
        if (realIdObj != null) {
          int extractedRealId = Integer.parseInt(realIdObj.toString());
          realIds.add(extractedRealId);
         // log.info(" [Step 2] 추출된 REALID: {}", extractedRealId);
        }
      }
     // log.info("[Step 2] 총 {}개의 REALID 추출됨: {}", realIds.size(), realIds);

      // Step 3: 추출한 REALID 리스트를 이용해 데이터 조회
      List<Map<String, Object>> findnK = new ArrayList<>();
      List<Map<String, Object>> findE = new ArrayList<>();

      for (int rid : realIds) { // 큰 REALID부터 조회
        //log.info(" [Step 3] REALID={}에 대한 findnK, findE 조회 시작", rid);
        findnK.addAll(issueInquiryService.getFindnK(rid));
        findE.addAll(issueInquiryService.getFindE(rid));
      }

      // Step 4: 결과 응답
      if (!findnK.isEmpty() || !findE.isEmpty() || !findPinNo.isEmpty()) {
        response.put("success", true);
        response.put("findPinNO", findPinNo);
        response.put("findnK", findnK);
        response.put("findnE", findE);
       // log.info(" [Step 4] 최종 응답 데이터 구성 완료 - 데이터 있음");
      } else {
        response.put("success", false);
        response.put("message", "데이터를 찾을 수 없습니다.");
        log.warn("[Step 4] 조회 결과 없음 - 데이터 없음");
      }
    } catch (Exception e) {
      log.error("[popupDetails] 요청 중 오류 발생", e);
      response.put("success", false);
      response.put("message", "요청 중 오류 발생: " + e.getMessage());
    }
    return ResponseEntity.ok(response);
  }


}
