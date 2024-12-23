package mes.app.actas_inspec;

import com.fasterxml.jackson.databind.ObjectMapper;
import mes.app.actas_inspec.service.ElecSafeService;
import mes.app.common.CommonController;
import mes.app.common.service.CommonService;
import mes.config.Settings;
import mes.domain.entity.actasEntity.TB_RP750;
import mes.domain.entity.actasEntity.TB_RP750_PK;
import mes.domain.entity.User;
import mes.domain.entity.actasEntity.TB_RP760;
import mes.domain.entity.actasEntity.TB_RP760_PK;
import mes.domain.model.AjaxResult;
import mes.domain.repository.actasRepository.TB_RP750Repository;
import mes.domain.repository.actasRepository.TB_RP760Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/inspec/elec_safe")
public class ElecSafeController {

    @Autowired
    ElecSafeService elecSafeService;

    @Autowired
    TB_RP750Repository TBRP750Repository;

    @Autowired
    TB_RP760Repository TBRP760Repository;

    @Autowired
    CommonController commonController;

    @Autowired
    CommonService commonService;

    @Autowired
    Settings settings;

    @GetMapping("/read")
    public AjaxResult getList(@RequestParam(value = "startDate", required = false) String startDate,
                              @RequestParam(value = "endDate", required = false) String endDate,
                              @RequestParam(value = "searchtitle", required = false) String searchTitle,
                              @RequestParam(value = "spworkcd") String spworkcd,
                              @RequestParam(value = "spcompcd") String spcompcd,
                              @RequestParam(value = "spplancd") String spplancd) {

        if (searchTitle == null) {
            searchTitle = "";
        }

        if (startDate == null) {
            startDate = "";
        }

        if (endDate == null) {
            endDate = "";
        }

        String c_startDate = startDate.replaceAll("-", "");
        String c_endDate = endDate.replaceAll("-", "");

        List<Map<String, Object>> items = this.elecSafeService.getList(searchTitle, c_startDate, c_endDate, spworkcd, spcompcd, spplancd);

        // 각 항목의 endresult 값을 변환
        for (Map<String, Object> item : items) {
            if (item.containsKey("endresult")) {
                Object endresult = item.get("endresult");
                if (endresult instanceof Integer) {
                    // endresult 값이 Integer인 경우, findById 메서드를 통해 문자열로 변환
                    String endresultValue = this.commonService.findById((Integer) endresult);
                    item.put("endresult", endresultValue);
                }
            }

            // 날짜 형식 변환 (registdt, checkdt)
            if (item.containsKey("registdt")) {
                String registdt = (String) item.get("registdt");
                if (registdt != null && registdt.length() == 8) {
                    String formattedDate = registdt.substring(0, 4) + "-" + registdt.substring(4, 6) + "-" + registdt.substring(6, 8);
                    item.put("registdt", formattedDate);
                }
            }

            if (item.containsKey("checkdt")) {
                String checkdt = (String) item.get("checkdt");
                if (checkdt != null && checkdt.length() == 8) {
                    String formattedDate = checkdt.substring(0, 4) + "-" + checkdt.substring(4, 6) + "-" + checkdt.substring(6, 8);
                    item.put("checkdt", formattedDate);
                }
            }

            // 파일 이름과 경로를 리스트로 변환
            if (item.containsKey("filenames") && item.containsKey("filepaths")) {
                String filenames = (String) item.get("filenames");
                String filepaths = (String) item.get("filepaths");

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

    @PostMapping("/save")
    public AjaxResult saveElecSafe(@RequestParam Map<String, String> params,
                                   @RequestParam(value = "filelist", required = false) MultipartFile[] files,
                                   @RequestPart(value = "deletedFiles", required = false) MultipartFile[] deletedFiles,
                                   Authentication auth) throws IOException {

        User user = (User) auth.getPrincipal();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String nspworkcd = params.get("spworkcd");
        String nspcompcd = params.get("spcompcd");
        String nspplancd = params.get("spplancd");
        String ncheckdt = params.get("checkdt").replaceAll("-", "");
        String ncheckseq = params.get("checkseq");

        TB_RP750_PK pk = new TB_RP750_PK();
        pk.setSpworkcd(nspworkcd);
        pk.setSpcompcd(nspcompcd);
        pk.setSpplancd(nspplancd);
        pk.setCheckdt(ncheckdt);

        String finalCheckseq;
        if (ncheckseq != null && !ncheckseq.isEmpty()) {
            pk.setCheckseq(ncheckseq);
            finalCheckseq = ncheckseq;
        } else {
            // 점검 순번 유지 또는 생성 로직
            Optional<String> checkseqvalue = TBRP750Repository.findMaxCheckseq(nspworkcd, nspcompcd, nspplancd, ncheckdt);
            String newSeq = checkseqvalue.map(s -> String.valueOf(Integer.parseInt(s) + 1)).orElse("1");
            pk.setCheckseq(newSeq);
            finalCheckseq = newSeq;
        }

        String nregistdt = params.get("registdt").replaceAll("-", "");

        TB_RP750 TBRP750 = new TB_RP750();
        TBRP750.setId(pk);
        TBRP750.setSpworknm(params.get("spworknm"));
        TBRP750.setSpcompnm(params.get("spcompnm"));
        TBRP750.setSpplannm(params.get("spplannm"));
        TBRP750.setChecktitle(params.get("checktitle"));
        TBRP750.setCheckusr(params.get("checkusr"));
        TBRP750.setDocdv(params.get("docdv"));
        TBRP750.setCheckarea(params.get("checkarea"));
        TBRP750.setBfconsres(params.get("bfconsres"));
        TBRP750.setCheckusr(params.get("checkusr"));
        TBRP750.setEndresult(Integer.parseInt(params.get("endresult")));
        TBRP750.setIndatem(now);
        TBRP750.setInusernm(user.getFirst_name());
        TBRP750.setInuserid(user.getUsername());
        TBRP750.setRegistdt(nregistdt);

        AjaxResult result = new AjaxResult();

        boolean success = elecSafeService.save(TBRP750);
        boolean success2 = true;

        // 삭제된 파일 처리
        if (deletedFiles != null && deletedFiles.length > 0) {
            List<TB_RP760> tbRp760List = new ArrayList<>();
            for (MultipartFile deletedFile : deletedFiles) {
                String content = new String(deletedFile.getBytes(), StandardCharsets.UTF_8);
                Map<String, String> deletedFileMap = new ObjectMapper().readValue(content, Map.class);

                String spworkcd = deletedFileMap.get("spworkcd");
                String spcompcd = deletedFileMap.get("spcompcd");
                String spplancd = deletedFileMap.get("spplancd");
                String checkdt = deletedFileMap.get("checkdt");
                String checkseq = deletedFileMap.get("checkseq");
                String fileseq = deletedFileMap.get("fileseq");

                TB_RP760_PK pk2 = new TB_RP760_PK(spworkcd, spcompcd, spplancd, checkdt, checkseq, fileseq);
                TB_RP760 tbRp760 = TBRP760Repository.findById(pk2).orElse(null);
                if (tbRp760 != null) {
                    // 파일 삭제
                    String filePath = tbRp760.getFilepath();
                    String fileName = tbRp760.getFilesvnm();
                    File file = new File(filePath, fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    tbRp760List.add(tbRp760);
                }
            }
            TBRP760Repository.deleteAll(tbRp760List);
        }


        if (files != null) {
            for (MultipartFile multipartFile : files) {

                // 파일 시퀀스 생성 로직
                Optional<String> maxFileseqValue = TBRP760Repository.findMaxFileseq(nspworkcd, nspcompcd, nspplancd, ncheckdt, finalCheckseq);
                String newFileSeq = maxFileseqValue.map(s -> String.valueOf(Integer.parseInt(s) + 1)).orElse("1");

                TB_RP760_PK pk2 = new TB_RP760_PK(nspworkcd, nspcompcd, nspplancd, ncheckdt, finalCheckseq, newFileSeq);

                TB_RP760 TBRP760 = new TB_RP760();
                TBRP760.setId(pk2);
                TBRP760.setSpworknm(params.get("spworknm"));
                TBRP760.setSpcompnm(params.get("spcompnm"));
                TBRP760.setSpplannm(params.get("spplannm"));

                String path = settings.getProperty("file_upload_path") + "전기안전점검";
                MultipartFile file = multipartFile;
                String fileName = file.getOriginalFilename();
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                String file_uuid_name = UUID.randomUUID().toString() + "." + ext;
                String saveFilePath = path;
                File saveDir = new File(saveFilePath);
                MultipartFile mFile = null;

                mFile = file;
                float fileSize = (float) file.getSize();

                //디렉토리 없으면 생성
                if (!saveDir.isDirectory()) {
                    saveDir.mkdirs();
                }

                File saveFile = new File(path + File.separator + file_uuid_name);
                mFile.transferTo(saveFile);

                TBRP760.setFilepath(saveFilePath);
                TBRP760.setFilesvnm(file_uuid_name);
                TBRP760.setFileextns(ext);
                TBRP760.setFileurl(saveFilePath);
                TBRP760.setFileornm(fileName);
                TBRP760.setFilesize(fileSize);
                TBRP760.setIndatem(now);
                TBRP760.setInusernm(user.getFirst_name());
                TBRP760.setInuserid(user.getUsername());

                if (!elecSafeService.saveFile(TBRP760)) {
                    success2 = false;
                    break;
                }
            }
        }

        if (success && success2) {

            result.success = true;
            result.message = "저장하였습니다.";
        } else {
            result.success = false;
            result.message = "저장에 실패하였습니다.";
        }
        return result;

    }

    @DeleteMapping("/delete")
    public AjaxResult deleteElecSafe(@RequestBody List<TB_RP750_PK> pkList) {
        AjaxResult result = new AjaxResult();

        for (TB_RP750_PK pk : pkList) {

            if (pk.getCheckdt() != null) {
                pk.setCheckdt(pk.getCheckdt().replaceAll("-", ""));
            }

            boolean success = elecSafeService.delete(pk);

            if (success) {
                result.success = true;
                result.message = "삭제하였습니다.";
            } else {
                result.success = false;
                result.message = "삭제에 실패하였습니다.";
            }
        }
        return result;
    }

    @PostMapping("/modfind")
    public AjaxResult getById(@RequestBody TB_RP750_PK pk) throws IOException {
        AjaxResult result = new AjaxResult();

        Map<String, Object> item = elecSafeService.findById(pk);
        result.data = item;
        return result;
    }

    @PostMapping("/downloader")
    public ResponseEntity<?> downloadFile(@RequestBody List<TB_RP750_PK> pkList) throws IOException {

        // 파일 목록과 파일 이름을 담을 리스트 초기화
        List<File> filesToDownload = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();

        // ZIP 파일 이름을 설정할 변수 초기화
        String checkdt = null;
        String checktitle = null;

        // 파일을 메모리에 쓰기
        for (TB_RP750_PK pk : pkList) {
            List<Map<String, Object>> fileList = elecSafeService.download(pk);

            for (Map<String, Object> fileInfo : fileList) {
                String filePath = (String) fileInfo.get("filepath");
                String fileName = (String) fileInfo.get("filesvnm");
                String originFileName = (String) fileInfo.get("fileornm");

                if (checkdt == null) {
                    checkdt = (String) fileInfo.get("checkdt");
                }
                if (checktitle == null) {
                    checktitle = (String) fileInfo.get("checktitle");
                }

                File file = new File(filePath + File.separator + fileName);

                // 파일이 실제로 존재하는지 확인
                if (file.exists()) {
                    filesToDownload.add(file);
                    fileNames.add(originFileName); // 다운로드 받을 파일 이름을 originFileName으로 설정
                }
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
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originFileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(file.length());

            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        }

        String zipFileName = (checkdt != null && checktitle != null) ? checkdt + "_" + checktitle + ".zip" : "download.zip";

        // 파일이 두 개 이상인 경우 ZIP 파일로 묶어서 다운로드
        ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(zipBaos)) {

            for (int i = 0; i < filesToDownload.size(); i++) {
                File file = filesToDownload.get(i);
                String originFileName = fileNames.get(i); // originFileName 가져오기

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
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"; filename*=UTF-8''" + encodedZipFileName);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(zipResource.contentLength());

        return ResponseEntity.ok()
                .headers(headers)
                .body(zipResource);
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(@RequestParam String query, @RequestParam String field) {
        List<String> suggestions = elecSafeService.getSuggestions(query, field);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/newform")
    public AjaxResult getByIdforNew(@RequestParam(value = "spworkcd") String spworkcd,
                                    @RequestParam(value = "spcompcd") String spcompcd,
                                    @RequestParam(value = "spplancd") String spplancd) throws IOException {
        AjaxResult result = new AjaxResult();

        Map<String, Object> item = this.elecSafeService.getFirst(spworkcd, spcompcd, spplancd);
        result.data = item;
        return result;
    }

}
