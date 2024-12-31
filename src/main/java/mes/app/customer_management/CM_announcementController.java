package mes.app.customer_management;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mes.app.customer_management.service.CM_FAQService;
import mes.app.customer_management.service.CM_announcementService;
import mes.config.Settings;
import mes.domain.entity.User;
import mes.domain.entity.factcheckEntity.TB_BBSINFO;
import mes.domain.entity.factcheckEntity.TB_FAQINFO;
import mes.domain.entity.factcheckEntity.TB_FILEINFO;
import mes.domain.model.AjaxResult;
import mes.domain.repository.factcheckRepository.BBSINFORepository;
import mes.domain.repository.factcheckRepository.FILEINFORepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("api/announcement")
public class CM_announcementController {
    @Autowired
    CM_announcementService cmAnnouncementService;

    @Autowired
    BBSINFORepository bbsinfoRepository;

    @Autowired
    FILEINFORepository fileinfoRepository;

    @Autowired
    private Settings settings;

    @PostMapping("/uploadEditor")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String uploadDir = "c:\\temp\\editorFile\\";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File destinationFile = new File(uploadDir + fileName);
            file.transferTo(destinationFile);

            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String fileUrl = baseUrl + "/editorFile/" + fileName; // 클라이언트 접근 URL

            return ResponseEntity.ok(Collections.singletonMap("location", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "파일 업로드 실패: " + e.getMessage()));
        }
    }
    // 공지사항 등록
    @PostMapping("/save")
    public AjaxResult saveBBS(@ModelAttribute TB_BBSINFO BBSINFO,
                              @RequestParam(value = "filelist", required = false) MultipartFile[] files,
                              @RequestPart(value = "deletedFiles2", required = false) MultipartFile[] deletedFiles,
                              Authentication auth
    ) {
        AjaxResult result = new AjaxResult();
        ObjectMapper objectMapper = new ObjectMapper();
        User user = (User) auth.getPrincipal();
        try {
            // Repository를 통해 데이터 저장
            bbsinfoRepository.save(BBSINFO);
            // 파일 저장 처리
            if(files != null){
                for (MultipartFile multipartFile : files) {
                    String path = settings.getProperty("file_upload_path") + "공지사항";
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
                    fileinfo.setFILEORNM(fileName);
                    fileinfo.setFILESIZE(BigDecimal.valueOf(fileSize));
                    //fileinfo.setINDATEM(); // ("reqdate".replaceAll("-","")
                    fileinfo.setINUSERID(String.valueOf(user.getId()));
                    fileinfo.setFILEEXTNS(ext);
                    fileinfo.setFILEURL(saveFilePath);

                    try {
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

            result.data = "데이터가 성공적으로 저장되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            result.data = "데이터 저장 중 오류가 발생했습니다.";
        }

        return result;
    }
    // 답변 삭제
    @PostMapping("/delete")
    public AjaxResult deleteBBS(@RequestParam int BBSSEQ) {
        AjaxResult result = new AjaxResult();

        try {
            // Repository를 통해 데이터 저장
            cmAnnouncementService.deleteBBS(BBSSEQ);
            cmAnnouncementService.deleteFile(BBSSEQ);
            result.data = "데이터가 성공적으로 저장되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            result.data = "데이터 저장 중 오류가 발생했습니다.";
        }

        return result;
    }

}
