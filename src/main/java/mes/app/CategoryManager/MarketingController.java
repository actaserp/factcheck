package mes.app.CategoryManager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mes.app.CategoryManager.Service.MarketingService;
import mes.domain.entity.User;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/Marketing")
public class MarketingController {

  @Autowired
  private MarketingService marketingService;

  //저장
  @PostMapping("/save")
  public ResponseEntity<Map<String, Object>> saveMarketing(
      @RequestPart("formData") String formDataJson,
      @RequestPart(value = "files", required = false) List<MultipartFile> files,
      Authentication auth
  ) {
    Map<String, Object> response = new HashMap<>();
    User user = (User) auth.getPrincipal();
    String userid = user.getUsername();
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> formData = objectMapper.readValue(formDataJson, new TypeReference<>() {});

      //log.info("✅ 저장에 들어온 데이터: {}", formData);

     /* // 기존 파일 확인
      List<Map<String, Object>> fileList = (List<Map<String, Object>>) formData.get("filelist");
      if (fileList != null) {
        log.info("📂 기존 파일 개수: {}", fileList.size());
      } else {
        log.info("📁 기존 파일 없음.");
      }

      // 📂 업로드된 파일 개수 확인
      if (files != null) {
        log.info("📂 서버에서 받은 파일 개수: {}", files.size());
        for (MultipartFile file : files) {
          log.info("📁 서버에서 받은 파일: {}, 크기: {} 바이트", file.getOriginalFilename(), file.getSize());
        }
      } else {
        log.info("📁 새롭게 업로드된 파일 없음.");
      }*/

      // **마케팅 데이터 저장 (서비스 호출)**
      Integer makSave = marketingService.saveOrUpdateMarketingData(formData, userid, files);

      response.put("success", true);
      response.put("message", "마케팅 정보가 저장되었습니다.");
      response.put("makseq", makSave);
      return ResponseEntity.ok(response);

    } catch (IOException e) {
      log.error("JSON 파싱 오류", e);
      response.put("success", false);
      response.put("message", "JSON 파싱 중 오류 발생");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

    } catch (Exception e) {
      log.error("저장 중 오류 발생", e);
      response.put("success", false);
      response.put("message", "데이터 저장 중 오류 발생");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/read")
  public AjaxResult MarketingList(@RequestParam(value = "startDate", required = false, defaultValue = "") String startDate,
                                  @RequestParam(value = "endDate", required = false, defaultValue = "") String endDate,
                                  @RequestParam(value = "searchUserNm", required = false, defaultValue = "") String searchUserNm) {
    AjaxResult result = new AjaxResult();
    //log.info("들어온 데이터: startDate={}, endDate={}, searchUserNm={}", startDate, endDate, searchUserNm);

    try {
      // 데이터 조회
      List<Map<String, Object>> MarketingList = marketingService.getList(startDate, endDate, searchUserNm);

      SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
      ObjectMapper objectMapper = new ObjectMapper();

      for (Map<String, Object> user : MarketingList) {
        //날짜 포맷
        Object indatemValue = user.get("indatem");
        if (indatemValue != null) {
          try {
            // 날짜를 포맷하여 다시 user 맵에 저장
            String formattedDate = dateFormatter.format(indatemValue);
            user.put("indatem", formattedDate);
          } catch (Exception ex) {
            // 포맷 오류 처리
            log.error("날짜 포맷 중 오류 발생: {}", ex.getMessage());
            user.put("indatem", "잘못된 날짜 형식");
          }
        }
        if(indatemValue != null) {
          try{
            String formattedDate = dateFormatter.format(indatemValue);
            user.put("makdate", formattedDate);
          }catch(Exception ex){
            log.error("날짜 포맷 중 오류 발생: {}", ex.getMessage());
            user.put("makdate", "잘못된 날짜 형식");
          }
        }

        if (user.get("fileinfos") != null) {
          try {
            List<Map<String, Object>> fileitems = objectMapper.readValue(
                (String) user.get("fileinfos"),
                new TypeReference<List<Map<String, Object>>>() {}
            );

            boolean hasFiles = fileitems != null && !fileitems.isEmpty();
            user.put("isdownload", hasFiles);
            user.put("filelist", fileitems);

          } catch (Exception e) {
            log.error("파일 정보 파싱 중 오류 발생: {}", e.getMessage());
            user.put("filelist", Collections.emptyList());
            user.put("isdownload", false);
          }
        } else {
          user.put("filelist", Collections.emptyList());
          user.put("isdownload", false);
        }

        user.remove("fileinfos");

      }
//      log.info("최종 데이터  :{}", MarketingList);
      // 데이터가 있을 경우 성공 메시지
      result.success = true;
      result.message = "데이터 조회 성공";
      result.data = MarketingList;

    } catch (Exception e) {
      // 예외 처리
      result.success = false;
      result.message = "데이터 조회 중 오류 발생: " + e.getMessage();
    }

    return result;
  }

  @PostMapping("/delete")
  public AjaxResult deleteCategory(@RequestBody Map<String, Object> requestData) {
    AjaxResult result = new AjaxResult();
    log.info("마케팅 관리 삭제 들어옴: {}", requestData);

    try {
      if (!requestData.containsKey("makseq")) {
        throw new IllegalArgumentException("삭제할 데이터의 makseq가 누락되었습니다.");
      }

      // 🔹 Integer로 직접 변환
      Integer makseq;
      if (requestData.get("makseq") instanceof Integer) {
        makseq = (Integer) requestData.get("makseq"); // 이미 Integer일 경우
      } else {
        makseq = Integer.parseInt(requestData.get("makseq").toString()); // String인 경우 변환
      }

      log.info("삭제 요청 makseq: {}", makseq);

      // 서비스 단 호출
      marketingService.deleteRegisterById(makseq);

      result.success = true;
      result.message = "삭제가 완료되었습니다.";
    } catch (Exception e) {
      log.error("❌ 삭제 중 오류 발생", e);
      result.success = false;
      result.message = "삭제 중 오류 발생: " + e.getMessage();
    }
    return result;
  }


}
