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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
@RequestMapping("/api/QnA")
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

            List<Map<String, Object>> filelist = new ArrayList<>();
            // 파일 이름과 경로를 리스트로 변환
            if (item.containsKey("fileornm") && item.containsKey("filepath")) {
                String filenames = (String) item.get("fileornm");
                String filepaths = (String) item.get("filepath");
                String filesvnms = (String) item.get("filesvnm");
                String filesizes = (String) item.get("filesize");

                List<String> filenameList = filenames != null ? Arrays.asList(filenames.split(",")) : Collections.emptyList();
                List<String> filepathList = filepaths != null ? Arrays.asList(filepaths.split(",")) : Collections.emptyList();
                List<String> filesvnmList = filesvnms != null ? Arrays.asList(filesvnms.split(",")) : Collections.emptyList();
                List<String> filesizeList = filesizes != null ? Arrays.asList(filesizes.split(",")) : Collections.emptyList();

                for (int i = 0; i < filenameList.size(); i++) {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("fileornm", filenameList.get(i));
                    if (i < filepathList.size()) {
                        fileInfo.put("filepath", filepathList.get(i));
                    }
                    if (i < filesvnmList.size()) {
                        fileInfo.put("filesvnm", filesvnmList.get(i));
                    }
                    if (i < filesizeList.size()) {
                        fileInfo.put("filesize", filesizeList.get(i));
                    }
                    filelist.add(fileInfo);
                }
                item.put("filelist", filelist);
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
        try {
            // Repository를 통해 데이터 저장
            qnaRepository.save(faqAnswer);
            // 파일 저장 처리
            if(files != null){
                for (MultipartFile multipartFile : files) {
                    String path = settings.getProperty("file_upload_path") + "QnA문의";
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
                    fileinfo.setFiledate(String.valueOf(faqAnswer.getINDATEM()).replace("-",""));
                    fileinfo.setFILEORNM(fileName);
                    fileinfo.setFILESIZE(BigDecimal.valueOf(fileSize));
                    fileinfo.setINDATEM(faqAnswer.getINDATEM().atStartOfDay()); // ("reqdate".replaceAll("-","")
                    fileinfo.setINUSERID(String.valueOf(user.getId()));
                    fileinfo.setFILEEXTNS(ext);
                    fileinfo.setFILEURL(saveFilePath);
                    fileinfo.setFILESVNM(file_uuid_name);
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

                    String fileid = (String) deletedFileMap.get("name");

                    TB_FILEINFO File = fileinfoRepository.findBySvnmAndSeq(fileid, faqAnswer.getQSTSEQ());
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
    public AjaxResult deleteQnA(@RequestBody Map<String, Object> requestData) {
        AjaxResult result = new AjaxResult();
        int qnaseq = Integer.parseInt((String) requestData.get("QSTSEQ"));

        try {
            qnaService.deleteQnA(qnaseq);
            // 파일 서버에서 삭제
            List<TB_FILEINFO> filelist = fileinfoRepository.findAllByCheckseqAndBbsseq("02", qnaseq);
            for (TB_FILEINFO fileinfo : filelist) {
                String filePath = fileinfo.getFILEPATH();
                String fileName = fileinfo.getFILESVNM();
                File file = new File(filePath, fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
            qnaService.deleteFile(qnaseq);
            result.message = "답변내용이 성공적으로 삭제되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            result.message = "답변내용 삭제 중 오류가 발생했습니다.";
        }

        return result;
    }
    @PostMapping("/downloader")
    public ResponseEntity<?> downloadFile(@RequestBody List<Map<String, Object>> downloadList) throws IOException {

        // 파일 목록과 파일 이름을 담을 리스트 초기화
        List<File> filesToDownload = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();

        // ZIP 파일 이름을 설정할 변수 초기화
        String tketcrdtm = null;
        String tketnm = null;

        // 파일을 메모리에 쓰기
        for (Map<String, Object> fileInfo : downloadList) {
            String filePath = (String) fileInfo.get("filepath");    // 파일 경로
            String fileName = (String) fileInfo.get("filesvnm");    // 파일 이름(uuid)
            String originFileName = (String) fileInfo.get("fileornm");  //파일 원본이름(origin Name)

            File file = new File(filePath + File.separator + fileName);

            // 파일이 실제로 존재하는지 확인
            if (file.exists()) {
                filesToDownload.add(file);
                fileNames.add(originFileName); // 다운로드 받을 파일 이름을 originFileName으로 설정
            }
        }

        // 파일이 없는 경우
        if (filesToDownload.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 파일이 하나인 경우 그 파일을 바로 다운로드
        if (filesToDownload.size() == 1) {
            File file = filesToDownload.get(0);
            String originFileName = fileNames.get(0); // originFileName 가져오기

            HttpHeaders headers = new HttpHeaders();
            String encodedFileName = URLEncoder.encode(originFileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=*''" + encodedFileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(file.length());

            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        }

        String zipFileName = (tketcrdtm != null && tketnm != null) ? tketcrdtm + "_" + tketnm + ".zip" : "download.zip";

        // 파일이 두 개 이상인 경우 ZIP 파일로 묶어서 다운로드
        ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(zipBaos)) {

            Set<String> addedFileNames = new HashSet<>(); // 이미 추가된 파일 이름을 저장할 Set
            int fileCount = 1;

            for (int i = 0; i < filesToDownload.size(); i++) {
                File file = filesToDownload.get(i);
                String originFileName = fileNames.get(i); // originFileName 가져오기

                // 파일 이름이 중복될 경우 숫자를 붙여 고유한 이름으로 만듦
                String uniqueFileName = originFileName;
                while (addedFileNames.contains(uniqueFileName)) {
                    uniqueFileName = originFileName.replace(".", "_" + fileCount++ + ".");
                }

                // 고유한 파일 이름을 Set에 추가
                addedFileNames.add(uniqueFileName);

                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(originFileName);
                    zipOut.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, len);
                    }

                    zipOut.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            zipOut.finish();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        ByteArrayResource zipResource = new ByteArrayResource(zipBaos.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        String encodedZipFileName = URLEncoder.encode(zipFileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=*''" + encodedZipFileName);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(zipResource.contentLength());

        return ResponseEntity.ok()
                .headers(headers)
                .body(zipResource);
    }
}
