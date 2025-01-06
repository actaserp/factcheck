package mes.app.customer_management;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mes.app.customer_management.service.CM_FAQService;
import mes.app.customer_management.service.CM_QnaService;
import mes.config.Settings;
import mes.domain.entity.User;
import mes.domain.entity.factcheckEntity.TB_FAQINFO;
import mes.domain.entity.factcheckEntity.TB_FILEINFO;
import mes.domain.entity.factcheckEntity.TB_USERQST;
import mes.domain.model.AjaxResult;
import mes.domain.repository.factcheckRepository.FAQRepository;
import mes.domain.repository.factcheckRepository.FILEINFORepository;
import mes.domain.repository.factcheckRepository.QnARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("api/QnA")
public class CM_QnACotroller {
    @Autowired
    CM_QnaService qnaService;

    @Autowired
    QnARepository qnaRepository;

    @Autowired
    FILEINFORepository fileinfoRepository;

    @Autowired
    private Settings settings;

    // 문의 리스트
    @GetMapping("/getQnAList")
    public AjaxResult getQnAList(@RequestParam(value = "search_text", required = false) String searchText){
        List<Map<String, Object>> items = qnaService.getQnAList(searchText);

        for(Map<String, Object> item : items){
            item.put("no", items.indexOf(item)+1);
            item.put("datetum", null);
            // 차일 계산
            if (item.containsKey("INDATEM") && item.containsKey("answerdate")) {
                Timestamp indatem = (Timestamp) item.get("INDATEM");
                Timestamp answerdate = (Timestamp) item.get("answerdate");

                if (indatem != null && answerdate != null) {
                    // Timestamp를 LocalDate로 변환 (날짜만 추출)
                    LocalDate indatemDate = indatem.toLocalDateTime().toLocalDate();
                    LocalDate answerdateDate = answerdate.toLocalDateTime().toLocalDate();

                    // 두 날짜의 차이 계산 (일 단위)
                    long daysDifference = ChronoUnit.DAYS.between(indatemDate, answerdateDate);
                    // 결과를 "1일", "2일" 형식으로 저장
                    String daysDifferenceWithUnit = daysDifference + "일";

                    // 결과 저장
                    item.put("datetum", daysDifferenceWithUnit);
                }
            }
            // 날짜 형식 변환 (INDATEM)
            if (item.containsKey("INDATEM")) {
                Timestamp workdt = (Timestamp) item.get("INDATEM");
                if (workdt != null) {
                    // Timestamp를 LocalDateTime으로 변환
                    LocalDateTime localDateTime = workdt.toLocalDateTime();

                    // 원하는 형식으로 변환 (yyyy-MM-ddTHH:mm)
                    String formattedDate = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    item.put("INDATEM", formattedDate);
                }
            }
            // 날짜 형식 변환 (answerdate)
            if (item.containsKey("answerdate")) {
                Timestamp workdt = (Timestamp) item.get("answerdate");
                if (workdt != null) {
                    // Timestamp를 LocalDateTime으로 변환
                    LocalDateTime localDateTime = workdt.toLocalDateTime();

                    // 원하는 형식으로 변환 (yyyy-MM-ddTHH:mm)
                    String formattedDate = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    item.put("answerdate", formattedDate);
                }
            }

            // 파일 이름과 경로를 리스트로 변환
            if (item.containsKey("fileornm") && item.containsKey("filepath")) {
                String filenames = (String) item.get("fileornm");
                String filepaths = (String) item.get("filepath");

                List<String> filenameList = filenames != null ? Arrays.asList(filenames.split(",")) : Collections.emptyList();
                List<String> filepathList = filepaths != null ? Arrays.asList(filepaths.split(",")) : Collections.emptyList();

                item.put("filenameList", filenameList);
                item.put("filepathList", filepathList);
                item.put("isdownload", !filenameList.isEmpty() && !filepathList.isEmpty());
            } else {
                item.put("filenameList", Collections.emptyList());
                item.put("filepathList", Collections.emptyList());
                item.put("isdownload", false);
            }
        }
        AjaxResult result = new AjaxResult();
        result.data = items;
        return result;
    }
    // 문의/답변 등록
    @PostMapping("/save")
    public AjaxResult saveFAQ( @ModelAttribute TB_USERQST faqAnswer,
                               @RequestParam(value = "filelist", required = false) MultipartFile[] files,
                               @RequestPart(value = "deletedFiles2", required = false) MultipartFile[] deletedFiles,
                               Authentication auth) {
        AjaxResult result = new AjaxResult();
        ObjectMapper objectMapper = new ObjectMapper();
        User user = (User) auth.getPrincipal();
        // 유저정보 TB_USERQST 객체에 바인드
        faqAnswer.setINUSERID(user.getUsername());
        faqAnswer.setUSERID(user.getUsername());
        log.info("INDATEM : {}", faqAnswer.getINDATEM());
        try {
            // Repository를 통해 데이터 저장
            qnaRepository.save(faqAnswer);
            // 파일 저장 처리
            if(files != null){
                for (MultipartFile multipartFile : files) {
                    String path = settings.getProperty("file_upload_path") + "1:1문의";
                    MultipartFile file = multipartFile;
                    int fileSize = (int) file.getSize();

                    if(fileSize > 52428800){
                        result.message = "파일의 크기가 초과하였습니다.";
                        return result;
                    }
                    String fileName = file.getOriginalFilename();
                    String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                    String file_uuid_name = UUID.randomUUID().toString() + "." + ext;
                    String saveFilePath = path;
                    File saveDir = new File(saveFilePath);



                    //디렉토리 없으면 생성
                    if(!saveDir.isDirectory()){
                        saveDir.mkdirs();
                    }
                    File saveFile = new File(path + File.separator + file_uuid_name);
                    file.transferTo(saveFile);
                    TB_FILEINFO fileinfo = new TB_FILEINFO();

                    fileinfo.setFILEPATH(saveFilePath);
                    fileinfo.setFiledate(String.valueOf(faqAnswer.getINDATEM()));
                    fileinfo.setFILEORNM(fileName);
                    fileinfo.setFILESIZE(BigDecimal.valueOf(fileSize));
                    //fileinfo.setINDATEM(); // ("reqdate".replaceAll("-","")
                    fileinfo.setINUSERID(String.valueOf(user.getId()));
                    fileinfo.setFILEEXTNS(ext);
                    fileinfo.setFILEURL(saveFilePath);
                    fileinfo.setCHECKSEQ("02");

                    try {
                        fileinfo.setBbsseq(faqAnswer.getQSTSEQ());
                        fileinfoRepository.save(fileinfo);
                    }catch (Exception e) {
                        result.success = false;
                        result.message = "저장에 실패하였습니다.";
                    }
                }
            }
            // 삭제된 파일 처리
            if (deletedFiles != null && deletedFiles.length > 0) {
                List<TB_FILEINFO> FileList = new ArrayList<>();

                for (MultipartFile deletedFile : deletedFiles) {
                    String content = new String(deletedFile.getBytes(), StandardCharsets.UTF_8);
                    Map<String, Object> deletedFileMap = new ObjectMapper().readValue(content, new TypeReference<Map<String, Object>>() {});

                    Integer fileid = (Integer) deletedFileMap.get("fileid");

                    TB_FILEINFO File = fileinfoRepository.findById(fileid).orElse(null);
                    // id : fileid
                    if (File != null) {
                        // 파일 삭제
                        String filePath = File.getFILEPATH();
                        String fileName = File.getFILESVNM();
                        File file = new File(filePath, fileName);
                        if (file.exists()) {
                            file.delete();
                        }
                        FileList.add(File);
                    }
                }
                fileinfoRepository.deleteAll(FileList);
            }

            result.message = "답변이 성공적으로 저장되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            result.message = "답변 저장 중 오류가 발생했습니다.";
        }

        return result;
    }
    // 답변 삭제
    @PostMapping("/delete")
    public AjaxResult deleteQnA(@RequestBody Map<String, Integer> requestData) {
        Integer qnaseq = requestData.get("qnaseq");
        AjaxResult result = new AjaxResult();
        if (qnaseq == null) {
            result.message = "인식번호가 전달되지 않았습니다.";
            return result;
        }

        try {
            qnaService.deleteQnA(qnaseq);
            qnaService.deleteFile(qnaseq);

            result.message = "답변내용이 성공적으로 삭제되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            result.message = "답변내용 삭제 중 오류가 발생했습니다.";
        }

        return result;
    }

}
