package mes.app.actas_inspec.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.RectangularTextContainer;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.ObjectExtractor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

public class PDFTableProcessor {

    private PDDocument pdDocument;
    private ObjectExtractor extractor;
    private List<String> pageTexts;
    private List<List<Table>> allTables;

//    // PDF 파일을 로드하고 텍스트 및 테이블을 추출하는 메서드
//    public void loadPDF(File pdfFile) throws IOException {
//        pdDocument = PDDocument.load(pdfFile);
//        extractor = new ObjectExtractor(pdDocument);
//
//        int numberOfPages = pdDocument.getNumberOfPages();
//        PDFTextStripper pdfStripper = new PDFTextStripper();
//
//        pageTexts = new ArrayList<>();
//        allTables = new ArrayList<>();
//
//        CustomTextStripper customStripper  = new CustomTextStripper("7-1) 계약 보증 조건 및");
//
//        for (int page = 1; page <= numberOfPages; page++) {
//            pdfStripper.setStartPage(page);
//            pdfStripper.setEndPage(page);
//
//            // 페이지의 텍스트와 테이블을 추출하여 저장
//            String pageText = pdfStripper.getText(pdDocument);
//            pageTexts.add(pageText);
//
//            // 해당 페이지에서 "성능보증 결과" 텍스트의 Y 좌표를 찾음
//            customStripper.setStartPage(page);
//            customStripper.setEndPage(page);
//            customStripper.getText(pdDocument);
//
//            float yPosition = customStripper.getYPosition();
//            if (yPosition != -1) {
//                System.out.println("Page: " + page + ", Y Position: " + yPosition); // Y 좌표 출력
//            }
//
//            Page pdfPage = extractor.extract(page);
//            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
//            List<Table> tables = sea.extract(pdfPage);
//            allTables.add(tables);
//        }
//    }

    // 이미지 파싱
    public StringBuffer NaverClovaORCAPI(File file){

        String apiURL = "https://0benujza5m.apigw.ntruss.com/custom/v1/38294/1466ca738734a946c8939e8fb4ae3516b4e23b52170b9f6cc3a6d060fa7c56bc/general";
        String secretKey = "dXprc0xCemRHQ2dKY3N3d2NOVnZQTUxSa2RKSHJ4RHY=";


        try{
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setReadTimeout(30000);
            con.setRequestMethod("POST");
            String boundary = "----" + UUID.randomUUID().toString().replaceAll("-","");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("X-OCR-SECRET", secretKey);

            JSONObject json = new JSONObject();
            json.put("version", "V2");
            json.put("requestId", UUID.randomUUID().toString());
            json.put("timestamp", System.currentTimeMillis());
            json.put("enableTableDetection", true);
            JSONObject image = new JSONObject();
            image.put("format", "jpg");
            image.put("name", "demo");


            JSONArray images = new JSONArray();
            images.put(image);
            json.put("images", images);
            String postParams = json.toString();

            con.connect();
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());

            writeMultiPart(wr, postParams, file, boundary);
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode == 200){
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }else{
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            /*System.out.println(response);*/
            System.out.println("OCR 호출됨!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            return response;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeMultiPart(OutputStream out, String jsonMessage, File file, String boundary) throws
            IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n");
        sb.append(jsonMessage);
        sb.append("\r\n");

        out.write(sb.toString().getBytes("UTF-8"));
        out.flush();

        if (file != null && file.isFile()) {
            out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
            StringBuilder fileString = new StringBuilder();
            fileString
                    .append("Content-Disposition:form-data; name=\"file\"; filename=");
            fileString.append("\"" + file.getName() + "\"\r\n");
            fileString.append("Content-Type: application/octet-stream\r\n\r\n");
            out.write(fileString.toString().getBytes("UTF-8"));
            out.flush();

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.write("\r\n".getBytes());
            }

            out.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
        }
        out.flush();
    }
    // 여러 값을 동시에 추출하는 메서드 (모든 테이블 순회)
    public Map<String, String> extractMultipleDataAfterText(String targetText, String[] rowTexts, String[] colTexts) throws IOException {
        Map<String, String> extractedData = new HashMap<>();

        // 페이지별로 순회하면서 텍스트를 찾음
        for (int page = 0; page < pageTexts.size(); page++) {
            String pageText = pageTexts.get(page);

            // 특정 텍스트가 포함된 페이지 찾기
            if (pageText.contains(targetText)) {
                System.out.println("Target text found on page: " + (page + 1));

                // 해당 페이지의 모든 테이블 순회
                List<Table> tables = allTables.get(page);
                for (Table table : tables) {
                    // 각 rowText와 colText 쌍에 대해 데이터 추출
                    for (String rowText : rowTexts) {
                        int targetRowIndex = findRowIndex(table, rowText);
                        if (targetRowIndex != -1) {
                            for (String colText : colTexts) {
                                int targetColIndex = findColumnIndex(table, colText);
                                if (targetColIndex != -1) {
                                    List<RectangularTextContainer> targetRow = table.getRows().get(targetRowIndex);
                                    RectangularTextContainer targetCell = targetRow.get(targetColIndex);
                                    extractedData.put(rowText + " - " + colText, targetCell.getText().trim());
                                }
                            }
                        }
                    }
                }
                break; // 필요한 데이터를 모두 추출했으므로 루프 종료
            }
        }
        return extractedData;
    }


    public Map<String, List<List<String>>> extractRowsAfterText() throws IOException {
        Map<String, List<List<String>>> extractedData = new HashMap<>();
        int tableCounter = 1; // 테이블 번호를 카운팅하는 변수

        // 각 페이지의 테이블들을 순회
        for (List<Table> tables : allTables) {
            // 각 페이지 내의 모든 테이블을 순회
            for (Table table : tables) {
                // 테이블이 비어 있지 않은지 확인
                if (!table.getRows().isEmpty()) {
                    List<List<String>> tableRows = new ArrayList<>();

                    // 테이블의 모든 행을 추출
                    for (List<RectangularTextContainer> row : table.getRows()) {
                        List<String> rowValues = new ArrayList<>();
                        for (RectangularTextContainer cell : row) {
                            rowValues.add(cell.getText());
                        }
                        tableRows.add(rowValues);  // 테이블의 각 행을 추가
                    }

                    // 테이블 구분자 (예: "table1", "table2" 등)와 함께 데이터를 맵에 추가
                    String tableKey = "table" + tableCounter;
                    extractedData.put(tableKey, tableRows);
                    tableCounter++; // 테이블 번호 증가
                }
            }
        }

        return extractedData;  // 모든 테이블의 행 데이터를 테이블 구분자와 함께 반환
    }

    // 특정 텍스트를 포함한 행의 인덱스를 찾는 메서드
    private int findRowIndex(Table table, String rowText) {
        for (int rowIndex = 0; rowIndex < table.getRows().size(); rowIndex++) {
            List<RectangularTextContainer> row = table.getRows().get(rowIndex);
            for (RectangularTextContainer cell : row) {
                if (cell.getText().contains(rowText)) {
                    return rowIndex;
                }
            }
        }
        return -1;
    }


    // 특정 텍스트를 포함한 열의 인덱스를 찾는 메서드
    private int findColumnIndex(Table table, String colText) {
        List<RectangularTextContainer> headerRow = table.getRows().get(0); // 첫 번째 행을 헤더로 가정
        for (int colIndex = 0; colIndex < headerRow.size(); colIndex++) {
            if (headerRow.get(colIndex).getText().contains(colText)) {
                return colIndex;
            }
        }
        return -1;
    }

    // 리소스 정리 메서드
    public void close() throws IOException {
        if (extractor != null) extractor.close();
        if (pdDocument != null) pdDocument.close();
    }
}
