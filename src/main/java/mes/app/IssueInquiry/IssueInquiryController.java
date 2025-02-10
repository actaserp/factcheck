package mes.app.IssueInquiry;

import lombok.extern.slf4j.Slf4j;
import mes.app.IssueInquiry.service.IssueInquiryService;
import mes.domain.entity.User;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                            @RequestParam(value = "realId", required = false) Integer realId,
                            Authentication auth) {

    AjaxResult result = new AjaxResult();

    // realId가 null이면 기본값 0으로 설정
    if (realId == null) {
      realId = 0;
    }

    log.info("등기부 발급 조회 요청: startDate={}, endDate={}, SearchKeywords={}, realId={} ", startDate, endDate, SearchKeywords, realId);

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
  @RequestMapping(value = "/pdf", method = RequestMethod.GET)
  public ResponseEntity<Resource> getPdf(@RequestParam("realId") int realId) {
    try {
      log.info("📄 PDF 조회 요청: realId={}", realId);

      // 1️⃣ DB에서 PDF 파일명 조회
      Optional<String> optionalPdfFileName = issueInquiryService.findPdfFilenameByRealId(realId);
      if (optionalPdfFileName.isEmpty()) {
        log.warn("📌 PDF 파일명을 찾을 수 없음: realId={}", realId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      // 2️⃣ 파일명 그대로 사용
      String pdfFileName = optionalPdfFileName.get();
      log.info("📌 사용 파일명: {}", pdfFileName);

      // 3️⃣ 운영체제별 저장 경로 설정
      String osName = System.getProperty("os.name").toLowerCase();
      String uploadDir = osName.contains("win") ? "C:\\Temp\\registerFiles\\"
          : System.getProperty("user.home") + "/registerFiles/";

      // 4️⃣ PDF 파일 경로 설정 및 존재 여부 확인
      Path pdfPath = Paths.get(uploadDir, pdfFileName);
      log.info("📌 PDF 파일 경로: {}", pdfPath.toString());

      if (!Files.exists(pdfPath)) {
        log.warn("📌 파일이 존재하지 않음: {}", pdfPath.toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      // 5️⃣ PDF 파일을 Resource로 변환 후 응답
      File file = pdfPath.toFile();
      Resource resource = new FileSystemResource(file);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDisposition(ContentDisposition.inline().filename(pdfFileName, StandardCharsets.UTF_8).build());

      // ✅ `X-Frame-Options` 제거 (필요한 경우 추가 가능)
      headers.add("X-Frame-Options", "ALLOW-FROM http://localhost:8040");
      headers.add("Access-Control-Allow-Origin", "*");  // 모든 도메인 허용
      headers.add("Access-Control-Allow-Methods", "GET, OPTIONS");
      headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");

      return ResponseEntity.ok()
          .headers(headers)
          .contentLength(file.length())
          .body(resource);

    } catch (Exception e) {
      log.error("📌 서버 내부 오류 발생", e);
      return ResponseEntity.internalServerError().build();
    }
  }


  /*public ResponseEntity<Resource> getPdf(@RequestParam("realId") int realId) {
    log.info("📄 PDF 조회 요청: realId={}", realId);

    try {
      // 1️⃣ DB에서 PDF 파일명 조회
      Optional<String> optionalPdfFileName = issueInquiryService.findPdfFilenameByRealId(realId);

      if (optionalPdfFileName.isEmpty()) {
        log.warn("📌 PDF 파일명을 찾을 수 없음: realId={}", realId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      // 2️⃣ 파일명 인코딩 및 디코딩
      String pdfFileName = optionalPdfFileName.get();
      String encodedFileName = URLEncoder.encode(pdfFileName, StandardCharsets.UTF_8).replace("+", "%20");
      String decodedFileName = URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);

      log.info("📌 원본 파일명: {}", pdfFileName);
      log.info("📌 인코딩된 파일명: {}", encodedFileName);
      log.info("📌 디코딩된 파일명: {}", decodedFileName);

      // 3️⃣ 운영체제별 저장 경로 설정
      String osName = System.getProperty("os.name").toLowerCase();
      String uploadDir;

      if (osName.contains("win")) {
        uploadDir = "C:\\Temp\\registerFiles\\";  // Windows 환경
      } else {
        String userHome = System.getProperty("user.home");
        uploadDir = userHome + "/registerFiles/"; // Mac, Linux, Android 환경
      }

      // 4️⃣ PDF 파일 경로 설정 및 존재 여부 확인
      Path pdfPath = Paths.get(uploadDir, decodedFileName);
      log.info("📌 PDF 파일 경로: {}", pdfPath.toString());

      if (!Files.exists(pdfPath)) {
        log.warn("📌 파일이 존재하지 않음: {}", pdfPath.toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      // 5️⃣ PDF 파일을 Resource로 변환 후 응답
      File file = pdfPath.toFile();
      Resource resource = new InputStreamResource(new FileInputStream(file));

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDisposition(ContentDisposition.inline().filename(encodedFileName, StandardCharsets.UTF_8).build());

      return ResponseEntity.ok()
          .headers(headers)
          .contentLength(file.length())
          .body(resource);
    } catch (IOException e) {
      log.error("📌 파일을 읽는 중 오류 발생", e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (Exception e) {
      log.error("📌 서버 내부 오류 발생", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }*/

  @RequestMapping(value = "/pdfDownload", method = RequestMethod.GET)
  public ResponseEntity<Resource> downloadPdf(@RequestParam("realId") int realId) {
    try {
      log.info("📄 PDF 다운로드 요청: realId={}", realId);

      // 1️⃣ DB에서 PDF 파일명 조회
      Optional<String> optionalPdfFileName = issueInquiryService.findPdfFilenameByRealId(realId);
      if (optionalPdfFileName.isEmpty()) {
        log.warn("📌 PDF 파일명을 찾을 수 없음: realId={}", realId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      // 2️⃣ 파일명 그대로 사용
      String pdfFileName = optionalPdfFileName.get();
      log.info("📌 다운로드 파일명: {}", pdfFileName);

      // 3️⃣ 운영체제별 저장 경로 설정
      String osName = System.getProperty("os.name").toLowerCase();
      String uploadDir = osName.contains("win") ? "C:\\Temp\\registerFiles\\"
          : System.getProperty("user.home") + "/registerFiles/";

      // 4️⃣ PDF 파일 경로 설정 및 존재 여부 확인
      Path pdfPath = Paths.get(uploadDir, pdfFileName);
      if (!Files.exists(pdfPath)) {
        log.warn("📌 파일이 존재하지 않음: {}", pdfPath.toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      // 5️⃣ PDF 파일을 Resource로 변환 후 응답
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
      log.error("📌 서버 내부 오류 발생", e);
      return ResponseEntity.internalServerError().build();
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
