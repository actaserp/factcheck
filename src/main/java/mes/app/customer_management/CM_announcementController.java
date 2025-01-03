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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

    // 문의 리스트
    @GetMapping("/read")
    public AjaxResult getBBSList(){
        List<Map<String, Object>> items = cmAnnouncementService.getBBSList();

        for(Map<String, Object> item : items){
            item.put("no", items.indexOf(item)+1);

            // 날짜 형식 변환 (BBSDATE)
            if (item.containsKey("BBSDATE")) {
                String setupdt = (String) item.get("BBSDATE");
                if (setupdt != null && setupdt.length() == 8) {
                    String formattedDate = setupdt.substring(0, 4) + "-" + setupdt.substring(4, 6) + "-" + setupdt.substring(6, 8);
                    item.put("BBSDATE", formattedDate);
                }
            }
            // 날짜 형식 변환 (BBSFRDATE)
            if (item.containsKey("BBSFRDATE")) {
                String setupdt = (String) item.get("BBSFRDATE");
                if (setupdt != null && setupdt.length() == 8) {
                    String formattedDate = setupdt.substring(0, 4) + "-" + setupdt.substring(4, 6) + "-" + setupdt.substring(6, 8);
                    item.put("BBSFRDATE", formattedDate);
                }
            }
            // 날짜 형식 변환 (BBSTODATE)
            if (item.containsKey("BBSTODATE")) {
                String setupdt = (String) item.get("BBSTODATE");
                if (setupdt != null && setupdt.length() == 8) {
                    String formattedDate = setupdt.substring(0, 4) + "-" + setupdt.substring(4, 6) + "-" + setupdt.substring(6, 8);
                    item.put("BBSTODATE", formattedDate);
                }
            }
            ObjectMapper objectMapper = new ObjectMapper();
            if (item.get("hd_files") != null) {
                try {
                    // JSON 문자열을 List<Map<String, Object>>로 변환
                    List<Map<String, Object>> fileitems = objectMapper.readValue((String) item.get("hd_files"), new TypeReference<List<Map<String, Object>>>() {});

                    for (Map<String, Object> fileitem : fileitems) {
                        if (fileitem.get("filepath") != null && fileitem.get("fileornm") != null) {
                            String filenames = (String) fileitem.get("fileornm");
                            String filepaths = (String) fileitem.get("filepath");
                            String filesvnms = (String) fileitem.get("filesvnm");

                            List<String> fileornmList = filenames != null ? Arrays.asList(filenames.split(",")) : Collections.emptyList();
                            List<String> filepathList = filepaths != null ? Arrays.asList(filepaths.split(",")) : Collections.emptyList();
                            List<String> filesvnmList = filesvnms != null ? Arrays.asList(filesvnms.split(",")) : Collections.emptyList();

                            item.put("isdownload", !fileornmList.isEmpty() && !filepathList.isEmpty());
                        } else {
                            item.put("isdownload", false);
                        }
                    }

                    // fileitems를 다시 item에 넣어 업데이트
                    item.remove("hd_files");
                    item.put("hd_files", fileitems);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        AjaxResult result = new AjaxResult();
        result.data = items;
        return result;
    }
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
        // 유저정보 TB_BBSINFO 객체에 바인드
        BBSINFO.setBBSUSER(user.getUsername());
        BBSINFO.setINUSERID(user.getUsername());

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
                    fileinfo.setFiledate(BBSINFO.getBBSDATE());
                    fileinfo.setFILEORNM(fileName);
                    fileinfo.setFILESIZE(BigDecimal.valueOf(fileSize));
                    //fileinfo.setINDATEM(); // ("reqdate".replaceAll("-","")
                    fileinfo.setINUSERID(String.valueOf(user.getId()));
                    fileinfo.setFILEEXTNS(ext);
                    fileinfo.setFILEURL(saveFilePath);
                    fileinfo.setCHECKSEQ("01");

                    try {
                        fileinfo.setBbsseq(BBSINFO.getBBSSEQ());
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
            // 웹 에디터 이미지 수정시 기존 이미지 삭제 작업
            if (BBSINFO.getBBSSEQ() != null) {
                bbsinfoRepository.findById(BBSINFO.getBBSSEQ()).ifPresent(bbsinfo -> {
                    // 기존 및 새로운 이미지 URL 목록 추출
                    List<String> originImages = extractImageUrlsFromHtml(bbsinfo.getBBSTEXT());
                    List<String> newImages = extractImageUrlsFromHtml(BBSINFO.getBBSTEXT());

                    // 삭제할 이미지 목록 추출 (기존 이미지 중 새로운 이미지에 없는 것)
                    List<String> imagesToDelete = new ArrayList<>(originImages);
                    imagesToDelete.removeAll(newImages);

                    // 서버 디렉토리에서 삭제
                    for (String imageUrl : imagesToDelete) {
                        deleteImageFromServer(imageUrl);
                    }
                });
            }
            result.message = "공지사항이 성공적으로 저장되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            result.message = "공지사항 저장 중 오류가 발생했습니다.";
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
            bbsinfoRepository.findById(BBSSEQ).ifPresent(bbsinfo -> {
                List<String> deleteImages = extractImageUrlsFromHtml(bbsinfo.getBBSTEXT());
                // 서버 디렉토리에서 삭제
                for (String imageUrl : deleteImages) {
                    deleteImageFromServer(imageUrl);
                }
            });
            result.data = "데이터가 성공적으로 저장되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            result.data = "데이터 저장 중 오류가 발생했습니다.";
        }

        return result;
    }

    // 에디터 파일 파싱 메서드
    private List<String> extractImageUrlsFromHtml(String htmlContent) {
        List<String> imageUrls = new ArrayList<>();

        try {
            // JSoup 라이브러리를 사용해 HTML 파싱
            Document doc = Jsoup.parse(htmlContent);
            Elements images = doc.select("img"); // <img> 태그 선택

            for (Element img : images) {
                String src = img.attr("src"); // src 속성 값 추출
                if (src != null && !src.isEmpty()) {
                    imageUrls.add(src);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageUrls;
    }
    // 에디터 파일 삭제 메서드
    private boolean deleteImageFromServer(String imageUrl) {
        try {
            // 이미지 URL에서 파일 경로 추출
            String uploadDir = "c:/temp/editorFile/"; // 업로드된 파일이 저장된 디렉토리
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1); // 파일 이름 추출
            File file = new File(uploadDir + fileName);

            // 파일 존재 여부 확인 후 삭제
            if (file.exists()) {
                return file.delete();
            } else {
                System.err.println("삭제할 파일이 존재하지 않습니다: " + file.getAbsolutePath());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
