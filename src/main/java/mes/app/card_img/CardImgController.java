package mes.app.card_img;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mes.app.CategoryManager.Service.MarketingService;
import mes.app.card_img.Service.CardImgService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/CardImg")
public class CardImgController {

    @Autowired
    private CardImgService cardImgService;

    //저장
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveMarketing(
            @RequestPart("formData") String formDataJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart("imgfilenm") String imgFilenm,
            @RequestPart(value = "deletedFiles", required = false) String deletedFilesJson,
            Authentication auth
    ) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) auth.getPrincipal();
        String userid = user.getUsername();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> formData = objectMapper.readValue(formDataJson, new TypeReference<>() {});

            // 🗑 삭제된 파일 목록 처리
            List<String> deletedFiles = new ArrayList<>();
            if (deletedFilesJson != null && !deletedFilesJson.isEmpty()) {
                deletedFiles = objectMapper.readValue(deletedFilesJson, new TypeReference<List<String>>() {});
                log.info("🗑 삭제할 파일 개수: {}", deletedFiles.size());
                log.info("🗑 삭제할 파일 목록: {}", deletedFiles);
            } else {
                log.info("🗑 삭제할 파일 없음.");
            }

            Integer makSave = cardImgService.saveOrUpdateimgData(formData, userid, files, imgFilenm, deletedFiles);

            response.put("success", true);
            response.put("message", "이미지 정보가 저장되었습니다.");
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
        if (!endDate.isEmpty()) {
            LocalDate parsedEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            endDate = parsedEndDate.plusDays(1).toString(); // 하루 추가
        }
        try {
            // 데이터 조회
            List<Map<String, Object>> CardImgList = cardImgService.getList(startDate, endDate, searchUserNm);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH시 mm분 ss초");
            ObjectMapper objectMapper = new ObjectMapper();

            int rownum = 1;
            for (Map<String, Object> card : CardImgList) {
                //날짜 포맷
                Object indatemValue = card.get("indatem");
                if (indatemValue != null) {
                    try {
                        // 날짜를 포맷하여 다시 user 맵에 저장
                        String formattedDate = dateFormatter.format(indatemValue);
                        card.put("indatem", formattedDate);
                    } catch (Exception ex) {
                        // 포맷 오류 처리
                        log.error("날짜 포맷 중 오류 발생: {}", ex.getMessage());
                        card.put("indatem", "잘못된 날짜 형식");
                    }
                }

                if (card.get("fileinfos") != null) {
                    try {
                        List<Map<String, Object>> fileitems = objectMapper.readValue(
                                (String) card.get("fileinfos"),
                                new TypeReference<List<Map<String, Object>>>() {}
                        );

                        boolean hasFiles = fileitems != null && !fileitems.isEmpty();
                        card.put("isdownload", hasFiles);
                        card.put("filelist", fileitems);

                    } catch (Exception e) {
                        log.error("파일 정보 파싱 중 오류 발생: {}", e.getMessage());
                        card.put("filelist", Collections.emptyList());
                        card.put("isdownload", false);
                    }
                } else {
                    card.put("filelist", Collections.emptyList());
                    card.put("isdownload", false);
                }
                // imgflag 포맷팅
                Object imgflag = card.get("imgflag");
                if (imgflag != null) {
                    if (imgflag.equals("00")) {
                        imgflag = "카드 이미지";
                    } else if (imgflag.equals("01")) {
                        imgflag = "기타 이미지";
                    }
                    card.put("imgflag", imgflag);
                }
                card.put("rownum", rownum);
                card.remove("fileinfos");
                rownum += 1;
            }
//      log.info("최종 데이터  :{}", MarketingList);
            // 데이터가 있을 경우 성공 메시지
            result.success = true;
            result.message = "데이터 조회 성공";
            result.data = CardImgList;

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
        try {
            if (!requestData.containsKey("imgseq")) {
                throw new IllegalArgumentException("삭제할 데이터의 imgseq가 누락되었습니다.");
            }

            // 🔹 Integer로 직접 변환
            Integer imgseq;
            if (requestData.get("imgseq") instanceof Integer) {
                imgseq = (Integer) requestData.get("imgseq"); // 이미 Integer일 경우
            } else {
                imgseq = Integer.parseInt(requestData.get("imgseq").toString()); // String인 경우 변환
            }

            // 서비스 단 호출
            cardImgService.deleteRegisterById(imgseq);

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
