package mes.app.actas_inspec;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import mes.app.UtilClass;
import mes.app.actas_inspec.service.FileUploaderService;
import mes.app.actas_inspec.service.InspecService;
import mes.app.common.service.FileService;
import mes.config.Settings;
import mes.domain.DTO.Actas_Fileset;
import mes.domain.entity.AttachFile;
import mes.domain.entity.User;
import mes.domain.entity.actasEntity.TB_INSPEC;
import mes.domain.entity.actasEntity.TB_RP710;
import mes.domain.entity.actasEntity.TB_RP715;
import mes.domain.model.AjaxResult;
import mes.domain.repository.AttachFileRepository;
import mes.domain.repository.actasRepository.TB_INSPECRepository;
import mes.domain.repository.actasRepository.TB_RP710Repository;
import mes.domain.repository.actasRepository.TB_RP715Repository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xpath.objects.XObject;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/inspec_report")
public class InspecController {

    @Autowired
    TB_RP715Repository tb_rp715Repository;

    @Autowired
    FileService attachFileService;

    @Autowired
    AttachFileRepository attachFileRepository;

    @Autowired
    FileUploaderService FileService;


    private final InspecService inspecService;
    private final Settings settings;
    private final TB_RP710Repository tb_rp710Repository;
    private final TB_INSPECRepository tB_INSPECRepository;
    @Autowired
    private FileUploaderService fileUploaderService;


    public InspecController(InspecService inspecService, TB_RP710Repository tb_rp710Repository, Settings settings,
                            TB_INSPECRepository tB_INSPECRepository){
        this.inspecService = inspecService;
        this.tb_rp710Repository = tb_rp710Repository;
        this.settings = settings;
        this.tB_INSPECRepository = tB_INSPECRepository;
    }


    @GetMapping("/read")
    public AjaxResult getList(@RequestParam(value = "searchusr", required = false) String searchusr,
                              @RequestParam(value = "searchfrdate", required = false) String searchfrdate,
                              @RequestParam(value = "searchtodate", required = false) String searchtodate,
                              @RequestParam(value = "searchflag", required = false) String flag,
                              @RequestParam(value = "spworkcd") String spworkcd,
                              @RequestParam(value = "spcompcd") String spcompcd,
                              @RequestParam(value = "spplancd") String spplancd
                              ){
        List<Map<String, Object>> items = new ArrayList<>();

        searchusr = Optional.ofNullable(searchusr).orElse("");
        searchfrdate = Optional.ofNullable(searchfrdate).orElse("20000101");
        searchtodate = Optional.ofNullable(searchtodate).orElse("29991231");

        if(searchfrdate.isEmpty()){
            searchfrdate = "20000101";
        }
        if(searchtodate.isEmpty()){
            searchtodate = "29991231";
        }


        items = this.inspecService.getInspecList(searchusr, searchfrdate, searchtodate, "", flag, spworkcd, spcompcd, spplancd);




        AjaxResult result = new AjaxResult();
        result.data = items;

        return result;
    }

    //순회점검 일지 저장
    @PostMapping("/save")
    @Transactional
    public AjaxResult saveFilter(
            //@ModelAttribute TB_RP710 tbRp710
            @RequestParam(value = "supplier", required = false) String supplier,
            @RequestParam(value = "checkstdt", required = false) String checkstdt,
            @RequestParam(value = "checkdt", required = false) String checkdt,
            @RequestParam(value = "checkendt", required = false) String checkendt,
            @RequestParam(value = "checkusr", required = false) String checkusr,
            @RequestParam(value = "checkarea", required = false) String checkarea,
            @RequestParam(value = "randomuuid", required = false) String randomuuid,
            @RequestParam(value = "spworkcd", required = false) String spworkcd,
            @RequestParam(value = "spworknm", required = false) String spworknm,
            @RequestParam(value = "spcompcd", required = false) String spcompcd,
            @RequestParam(value = "spcompnm", required = false) String spcompnm,
            @RequestParam(value = "spplannm", required = false) String spplannm,
            @RequestParam(value = "spplancd", required = false) String spplancd,
            @RequestParam(value = "doc-list", required = false) List<String> doc_list,
            @RequestParam(value = "filelist", required = false) MultipartFile[] files,
            @RequestPart(value = "deletedFiles", required = false) MultipartFile[] deletedFiles
            //@RequestParam Map<String, String> params
    ) throws IOException {


        AjaxResult result = new AjaxResult();


        TB_RP710 tbRp710dto = new TB_RP710();

        String checkdtconvertvalue = checkdt.replaceAll("-","");

        List<Map<String, Object>> rp710items = this.inspecService.getInspecList("", "", "", randomuuid, "", null, null, null);

        String formattedValue;


        //수정
        if(!rp710items.isEmpty()){

            tbRp710dto.setSpworkcd(rp710items.get(0).get("spworkcd").toString());
            tbRp710dto.setSpworknm(rp710items.get(0).get("spworknm").toString());
            tbRp710dto.setSpcompcd(rp710items.get(0).get("spcompcd").toString());
            tbRp710dto.setSpcompnm(rp710items.get(0).get("spcompnm").toString());
            tbRp710dto.setSpplancd(rp710items.get(0).get("spplancd").toString());
            tbRp710dto.setSpplannm(rp710items.get(0).get("spplannm").toString());
            tbRp710dto.setSpuncode(rp710items.get(0).get("spuncode").toString());
            tbRp710dto.setCheckdt(rp710items.get(0).get("checkdt").toString());
            tbRp710dto.setCheckno(rp710items.get(0).get("checkno").toString());


        } //저장
        else
        {
            Optional<String> checknovalue = tb_rp710Repository.findMaxChecknoByCheckdt(checkdtconvertvalue);

            if(checknovalue.isPresent()){

                Integer checknointvalue = Integer.parseInt(checknovalue.get()) + 1;

                formattedValue = String.format("%02d", checknointvalue);

            }else{
                formattedValue = "01";
            }

            tbRp710dto.setSpworkcd(spworkcd);
            tbRp710dto.setSpworknm(spworknm);
            tbRp710dto.setSpcompcd(spcompcd);
            tbRp710dto.setSpcompnm(spcompnm);
            tbRp710dto.setSpplancd(spplancd);
            tbRp710dto.setSpplannm(spplannm);
            tbRp710dto.setCheckno(formattedValue);
            tbRp710dto.setSpuncode(randomuuid);
            tbRp710dto.setCheckdt(checkdtconvertvalue);

        }


        tbRp710dto.setFlag("N");


        tbRp710dto.setCheckstdt(checkstdt);
        tbRp710dto.setCheckendt(checkendt);
        tbRp710dto.setCheckusr(checkusr);  //TODO: 수정로직이랑 좀 고려를 해보자.
        tbRp710dto.setCheckarea(checkarea);

        tbRp710dto.setSupplier(supplier);

        /*String path = settings.getProperty("file_upload_path") + "순회점검일지첨부파일";

        List<TB_RP715> fileEntities = new ArrayList<>();*/

        if (deletedFiles != null && deletedFiles.length > 0){

            for(MultipartFile deletedFile : deletedFiles){
                String content = new String(deletedFile.getBytes(), StandardCharsets.UTF_8);
                Map<String, String> deleteFileMap = new ObjectMapper().readValue(content, Map.class);

                String checkseq = deleteFileMap.get("checkseq");
                String spuncode_id = deleteFileMap.get("spuncode_id");
                String filepath = deleteFileMap.get("filepath");
                String filesvnm = deleteFileMap.get("filesvnm");

                new FileUploaderService().deleteFileFromDisk(filepath, filesvnm);

                tb_rp715Repository.deleteBySpuncodeIdAAndCheckseq(spuncode_id, checkseq);

            }
        }


        boolean successcode = inspecService.save(tbRp710dto, files, doc_list);
        if (successcode) {
            result.success = true;
            result.message = "저장하였습니다.";
        } else {
            result.success = false;
            result.message = "저장에 실패하였습니다.";
        }


        return result;
    }




    @PostMapping("/filesave")
    public AjaxResult fileupload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("spuncode") String spuncode) throws IOException {

        AjaxResult result = new AjaxResult();

        String path = settings.getProperty("file_upload_path") + "순회점검일지첨부파일";

        Pageable pageable = (Pageable) PageRequest.of(0, 1);

        List<String> tb_rp715 =  tb_rp715Repository.findByFilesvnm(spuncode, pageable);

        tb_rp715Repository.deleteBySpuncodeIdAAndRepyn(spuncode);

        if(!tb_rp715.isEmpty()){
            new FileUploaderService().deleteFileFromDisk(path, tb_rp715.get(0));
        }


        Map<String, Object> fileinform = FileService.saveFiles(file, path);

        inspecService.TB_RP715_Save(spuncode, fileinform, "Y");

        result.success = true;
        result.message = "저장하였습니다.";

        return result;
    }

    @PostMapping("/FileDownload")
    public ResponseEntity<Resource> downloadFile(@RequestBody Map<String, String> request){

        String spuncode = request.get("spuncode");

        Pageable pageable = (Pageable) PageRequest.of(0,1);
        List<String> tb_rp715 = tb_rp715Repository.findByFilesvnm(spuncode, pageable);

        if(tb_rp715.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        String path = settings.getProperty("file_upload_path") + "순회점검일지첨부파일/";
        String fileName = tb_rp715.get(0);

        return fileUploaderService.downloadFile(fileName, path);
    }

    @PostMapping("/modfind")
    public AjaxResult getById(@RequestBody String spuncode){
        AjaxResult result = new AjaxResult();

        String SpunCodeValue = spuncode.replaceAll("[\"']", "");

        Map<String, Object> item = inspecService.findById(SpunCodeValue);
        List<Map<String, Object>> inspecitem = inspecService.getInspecDocList(SpunCodeValue);

        item.put("inspecitem", inspecitem);


        result.data = item;
        return result;
    }

    @PostMapping("/delete")
    @Transactional
    public AjaxResult delete(
            @RequestParam(value = "spuncode") String spuncode
    ){

        AjaxResult result = new AjaxResult();

        List<String> paramList = new UtilClass().parseUserIds(spuncode);


        for(String param : paramList){

            List<Map<String, Object>> FileItems = inspecService.getFileList(param);

            if(!FileItems.isEmpty()){
                for (Map<String, Object> fileItem : FileItems) {
                    new FileUploaderService().deleteFileFromDisk(fileItem.get("filepath").toString(), fileItem.get("filesvnm").toString());
                }
            }

            System.out.println(param);
            //TODO: 이거 자식테이블먼저 삭제해야한다.
            tb_rp715Repository.deleteBySpuncodeId(param);
            tB_INSPECRepository.deleteBySpuncodeId(param);
            tb_rp710Repository.deleteBySpuncode(param);
        }




        result.success = true;
        result.message = "성공";
        return result;
    }

    @PostMapping("/approve")
    @Transactional
    public AjaxResult approve(
            @RequestParam(value = "spuncode") String spuncode
    ){

        AjaxResult result = new AjaxResult();

        List<String> paramList = new UtilClass().parseUserIds(spuncode);

        for(String param : paramList){
            System.out.println(param);
            //TODO: 이거 자식테이블먼저 삭제해야한다.
            tb_rp710Repository.updateFlagToYBySpuncode(param);
        }


        result.success = true;
        result.message = "성공";
        return result;
    }

    @PostMapping("/download-docs")
    public void downloadDocs(HttpServletResponse response, @RequestBody List<String> selectedList) throws IOException, Docx4JException {
        String path = settings.getProperty("file_upload_path") + "순회점검일지양식.docx";

        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(zipOutputStream);

        for (int i = 0; i < selectedList.size(); i++) {
            FileInputStream fis = new FileInputStream(path);
            XWPFDocument document = new XWPFDocument(fis);
            List<XWPFTable> tables = document.getTables();

            List<Map<String, Object>> rp710items = this.inspecService.getInspecList("", "", "", selectedList.get(i), "", null, null, null);
            List<Map<String, Object>> items = this.inspecService.getInspecDocList(selectedList.get(i));
            List<Map<String, Object>> FileItems = this.inspecService.getFileList(selectedList.get(i));

            if (tables.size() > 0) {
                for (int j = 1; j < tables.get(1).getRows().size(); j++) {
                    XWPFTableRow row = tables.get(1).getRow(j);
                    XWPFTableCell cell = row.getCell(1);
                    switch (j) {
                        case 1:
                            clearAndSetCellText(cell, rp710items.get(0).get("supplier").toString());
                            break;
                        case 2:
                            clearAndSetCellText(cell, rp710items.get(0).get("checkdt").toString());
                            break;
                        case 3:
                            clearAndSetCellText(cell, rp710items.get(0).get("checkusr").toString());
                            break;
                        case 4:
                            clearAndSetCellText(cell, rp710items.get(0).get("checkarea").toString());
                            break;
                        default:
                            clearAndSetCellText(cell, "");
                            break;
                    }
                } //2번째 테이블 작성완료 (1번째테이블은 결재테이블이라서 작성할 필요없음)


                //3번째 테이블 작성
                int itemValueIndex = 0;
                for(int k = 2; k < tables.get(2).getRows().size(); k++){
                    if(itemValueIndex >= items.size()) break;
                    XWPFTableRow row = tables.get(2).getRow(k);
                    XWPFTableCell cell = row.getCell(1);
                    System.out.println(k + "," + 1);
                    System.out.println(itemValueIndex);
                    Object insepcContValue = items.get(itemValueIndex).get("inspeccont");
                    String inspecContText = (insepcContValue != null) ? insepcContValue.toString() : "";

                    clearCellText(cell);
                    clearAndSetCellText(cell, inspecContText);

                    String cellText = cell.getText();
                    System.out.println("cellText :: " + cellText);
                    itemValueIndex++;
                }

                //개선사항 작성
                int itemValueIndex2 = 0;
                for(int k = 2; k < tables.get(2).getRows().size(); k++){
                    if(itemValueIndex2 >= items.size()) break;
                    XWPFTableRow row = tables.get(2).getRow(k);
                    XWPFTableCell cell = row.getCell(4);
                    System.out.println(k + "," + 2);
                    System.out.println(itemValueIndex2);
                    Object insepcContValue = items.get(itemValueIndex2).get("inspecreform");
                    String inspecContText = (insepcContValue != null) ? insepcContValue.toString() : "";

                    clearCellText(cell);
                    clearAndSetCellText(cell, inspecContText);

                    String cellText = cell.getText();
                    System.out.println("cellText :: " + cellText);
                    itemValueIndex2++;
                }

                //점검결과 작성 (O)
                int itemValueIndex3 = 0;
                for(int k = 2; k < tables.get(2).getRows().size(); k++){
                    if(itemValueIndex3 >= items.size()) break;
                    XWPFTableRow row = tables.get(2).getRow(k);
                    XWPFTableCell cell = row.getCell(2);
                    System.out.println(k + "," + 2);
                    System.out.println(itemValueIndex3);
                    Object insepcContValue = items.get(itemValueIndex3).get("inspecresult");
                    String inspecContText = (insepcContValue != null) ? insepcContValue.toString() : "";
                    String InsPecResultText = "";
                    if(inspecContText != null){
                        switch (inspecContText){
                            case "O": InsPecResultText = "O";
                                break;
                            case "X": InsPecResultText = "";
                        }
                    }

                    clearCellText(cell);
                    clearAndSetCellTextOX(cell, InsPecResultText);

                    String cellText = cell.getText();
                    System.out.println("cellText :: " + cellText);
                    itemValueIndex3++;
                }

                //점검결과 작성 (X)
                int itemValueIndex4 = 0;
                for(int k = 2; k < tables.get(2).getRows().size(); k++){
                    if(itemValueIndex4 >= items.size()) break;
                    XWPFTableRow row = tables.get(2).getRow(k);
                    XWPFTableCell cell = row.getCell(3);
                    System.out.println(k + "," + 3);
                    System.out.println(itemValueIndex4);
                    Object insepcContValue = items.get(itemValueIndex4).get("inspecresult");
                    String inspecContText = (insepcContValue != null) ? insepcContValue.toString() : "";
                    String InsPecResultText = "";
                    if(inspecContText != null){
                        switch (inspecContText){
                            case "O": InsPecResultText = "";
                                break;
                            case "X": InsPecResultText = "X";
                        }
                    }

                    clearCellText(cell);
                    clearAndSetCellTextOX(cell, InsPecResultText);

                    String cellText = cell.getText();
                    System.out.println("cellText :: " + cellText);
                    itemValueIndex4++;
                }




            }
            //사진칸 작성
            if(!FileItems.isEmpty()){
                for(int j=0; j < 2; j++){
                    int index = 0;
                    if(j==1) index=3;
                    if(j+1 > FileItems.size()) break;
                    String imagePath = settings.getProperty("file_upload_path") + "순회점검일지첨부파일/" + FileItems.get(j).get("filesvnm");


                    XWPFTableRow firstRow = tables.get(3).getRow(index);
                    XWPFTableCell firstCell = firstRow.getCell(0);

                    XWPFTableRow SecondRow = tables.get(3).getRow(index+2);
                    XWPFTableCell SecondCell = SecondRow.getCell(1);

                    XWPFTableRow ThirdRow = tables.get(3).getRow(index+2);
                    XWPFTableCell ThirdCell = ThirdRow.getCell(3);


                    clearAndSetCellText(SecondCell, rp710items.get(0).get("checkarea").toString());
                    clearAndSetCellText(ThirdCell, rp710items.get(0).get("checkdt").toString());

                    clearCellText(firstCell);

                    try(FileInputStream is = new FileInputStream(imagePath)){
                        XWPFParagraph paragraph = firstCell.addParagraph();
                        XWPFRun run = paragraph.createRun();
                        run.addPicture(is, Document.PICTURE_TYPE_PNG, imagePath, Units.toEMU(500), Units.toEMU(300)); // 이미지 크기 설정 (100x100 EMU)
                    } catch (InvalidFormatException e) {
                        throw new RuntimeException(e);
                    }


                }
            }

            ByteArrayOutputStream documentOutputStream = new ByteArrayOutputStream();
            //DOCX 파일의 내용을 메모리에 저장하기 위해 사용되는 출력 스트림

            document.write(documentOutputStream); //객체(docx)를 documentOutputStream에 씁니다., XWPFDocument 객체의 내용을 ByteArrayOutputStream에 저장합니다.
            document.close(); //XWPFDocument 객체를 닫습니다.
            fis.close();  //FileInputStream 객체를 닫습니다. 이는 파일 입력 스트림을 닫아 리소스를 해제합니다.

            byte[] documentBytes = documentOutputStream.toByteArray(); //documentOutputStream에 저장된 데이터를 바이트 배열로 변환합니다. 이제 DOCX 파일의 내용이 바이트 배열 documentBytes에 저장됩니다.

            //documentBytes를 입력 스트림으로 변환하여 docInputStream을 생성합니다. 이를 통해 메모리에 저장된 DOCX 파일을 다시 읽을 수 있습니다.
            ByteArrayInputStream docInputStream = new ByteArrayInputStream(documentBytes);


            //docInputStream을 사용하여 WordprocessingMLPackage 객체를 로드합니다. WordprocessingMLPackage는 docx4j 라이브러리의 주요 객체로, DOCX 파일을 나타냅니다.
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docInputStream);

            //FOSettings 객체를 생성합니다. 이는 PDF 변환 설정을 구성하는 데 사용됩니다.
            FOSettings foSettings = Docx4J.createFOSettings();

            //FOSettings 객체에 WordprocessingMLPackage 객체를 설정합니다. 이를 통해 변환할 DOCX 파일을 지정합니다.
            foSettings.setWmlPackage(wordMLPackage);

            //PDF 데이터를 저장하기 위해 pdfOutputStream을 생성합니다.
            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

            //Docx4J를 사용하여 DOCX 파일을 PDF로 변환, 변환된 PDF 데이터는 pdfOutputStream에 저장
            //FOSettings 객체와 출력 스트림을 인자로 받아 변환을 수행
            //Docx4J.FLAG_EXPORT_PREFER_XSL 플래그는 XSLT 기반의 변환 방식을 사용하도록 지정
            Docx4J.toFO(foSettings, pdfOutputStream, Docx4J.FLAG_EXPORT_PREFER_XSL);


            //pdfOutputStream에 저장된 데이터를 바이트 배열로 변환합니다. 이제 PDF 파일의 내용이 바이트 배열 pdfBytes에 저장
            byte[] pdfBytes = pdfOutputStream.toByteArray();
            String pdfName = "modified_document_" + (i + 1) + ".pdf";

            zos.putNextEntry(new ZipEntry(pdfName));
            zos.write(pdfBytes);
            zos.closeEntry();


            /*이거는 pdf로 변환안하고 docx로 내보냄
            ByteArrayOutputStream documentOutputStream = new ByteArrayOutputStream();
            document.write(documentOutputStream);
            document.close();
            fis.close();

            byte[] documentBytes = documentOutputStream.toByteArray();
            String documentName = "modified_document_" + (i + 1) + ".docx";

            zos.putNextEntry(new ZipEntry(documentName));
            zos.write(documentBytes);
            zos.closeEntry();*/
        }

        zos.close();

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=modified_documents.zip");

        try (ServletOutputStream outputStream = response.getOutputStream()) {
            zipOutputStream.writeTo(outputStream);
        }
    }



    //셀 지우고 줄바꿈
    private void clearAndSetCellText(XWPFTableCell cell, String text) {
        // 셀의 모든 문단을 제거합니다.
        int numParagraphs = cell.getParagraphs().size();
        for (int i = 0; i < numParagraphs; i++) {
            cell.removeParagraph(0);
        }

        // 텍스트를 '-' 기호로 나눕니다.
        String[] lines = text.split(" - ");

        // 각 부분을 새로운 문단으로 추가합니다.
        for (int i = 0; i < lines.length; i++) {
            XWPFParagraph paragraph = cell.addParagraph();
            XWPFRun run = paragraph.createRun();
            // 첫 줄은 그대로, 이후 줄은 '-' 기호를 앞에 붙입니다.
            if (i == 0) {
                run.setText(lines[i]);
            } else {
                run.setText("- " + lines[i]);
            }
        }
    }

    // 닥스에 OX적는게 있어서 분기별로 처리하려 생성하고 가운데 정렬
    private void clearAndSetCellTextOX(XWPFTableCell cell, String text) {
        // 셀의 모든 문단을 제거합니다.
        int numParagraphs = cell.getParagraphs().size();
        for (int i = 0; i < numParagraphs; i++) {
            cell.removeParagraph(0);
        }

        // 텍스트를 '-' 기호로 나눕니다.
        String[] lines = text.split(" - ");

        // 각 부분을 새로운 문단으로 추가합니다.
        for (int i = 0; i < lines.length; i++) {
            XWPFParagraph paragraph = cell.addParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = paragraph.createRun();
            // 첫 줄은 그대로, 이후 줄은 '-' 기호를 앞에 붙입니다.
            if (i == 0) {
                run.setText(lines[i]);
            } else {
                run.setText("- " + lines[i]);
            }
        }
    }

    //셀에 있는 글자를 지움
    public void clearCellText(XWPFTableCell cell) {
        // 셀의 모든 문단을 제거합니다.
        int numParagraphs = cell.getParagraphs().size();
        for (int i = 0; i < numParagraphs; i++) {
            cell.removeParagraph(0);
        }
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(@RequestParam String query, @RequestParam String field) {
        // field 값에서 숫자(- 이후의 숫자)를 제거
        String cleanField = field.replaceAll("-\\d+$", "");  // -뒤의 숫자 제거

        List<String> suggestions = inspecService.getSuggestions(query, cleanField);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/recentData")
    public AjaxResult recentData(){

        AjaxResult result = new AjaxResult();

        result.data = tb_rp710Repository.findTopByOrderByINDATEMDesc();

        return result;
    }






}
