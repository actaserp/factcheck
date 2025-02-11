package mes.app.actas_inspec.LTSA;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mes.app.UtilClass;
import mes.app.actas_inspec.service.PDFTableProcessor;
import mes.app.tilko.TilkoParsing;
import mes.domain.DTO.TB_RP620Dto;
import mes.domain.DTO.TB_RP621Dto;
import mes.domain.DTO.TB_RP622Dto;
import mes.domain.model.AjaxResult;
import mes.domain.repository.TB_RP620Repository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class LTSAController {

    TilkoParsing tilkoParsing;

    public Map<String, Object> uploadPDF(File file) throws IOException {

        Map<String, Object> resultMap = new HashMap<>();

        File tempFile = UtilClass.saveFileToTempAsFile(file);

        Map<String, String> dtoValue = new HashMap<>();

        String pdfPageContent = "";
        List<String> pdfListContent;

        // 각 테이블 별 넣어야 하는 데이터들 파싱 리스트
        // 부동산 등기부 등본 기본 데이터
        Map<String, Object> RegisterMap = new HashMap<>();
        Map<String, Object> REALAOWNMap = new HashMap<>();
        List<Map<String, Object>> GabguData = new ArrayList<>();
        Map<String, Object> REALBOWNMap = new HashMap<>();
        List<Map<String, Object>> eulguData = new ArrayList<>();
        Map<String, Object> RegisterDataGMap = new HashMap<>();
        List<Map<String, Object>> RegisterDataGItemsList = new ArrayList<>();// 담보
        Map<String, Object> RegisterDataHMap = new HashMap<>();
        List<Map<String, Object>> RegisterDataHItemsList = new ArrayList<>(); // 전세
        List<Map<String, Object>> RegisterDataJMap = new ArrayList<>();
        List<Map<String, Object>> TradeAmount = new ArrayList<>();
        Map<String, Object> gubunData = new HashMap<>();

        // 주요 등기사항 요약
        Map<String, Object> Summary = new HashMap<>(); // 부동산 등기부정보
        List<Map<String, Object>> SummaryDataEMap = new ArrayList<>(); // 저당권 및 전세권 등 (을구)
        List<Map<String, Object>> SummaryDataKMap = new ArrayList<>(); // 소유지분을 제외한 소유권에 관한 사항(갑구)
        List<Map<String, Object>> SummaryDataAMap = new ArrayList<>(); // 소유지분현황(갑구)

        //TODO: 운영 환경에서만
        //PDF를 이미지로 변환
        try(PDDocument document = PDDocument.load(tempFile)){
            PDFRenderer pdfRenderer  = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();
            System.out.println("totalpage: " + totalPages);
            // `갑 구`, '을 구'데이터 이어받기 위한 플래그
            boolean isParsingGabgu = false;
            boolean isParsingEulgu = false;
            List<String> gabguDataSubset = new ArrayList<>();
            List<String> eulguDataSubset = new ArrayList<>();

            for(int pageIndex = 0; pageIndex < totalPages; pageIndex++){
                // 페이지를 이미지로 변환 (150 DPI 설정) 중요함... DPI는 해상도인데 해상도 달라지면 인식을 다르게 해서 배열이 꼬인다.
                BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 96);

                File imageFile = new File(tempFile.getParent(), "page-" + (pageIndex + 1) + ".jpg");
                ImageIO.write(image, "jpg", imageFile);
                // OCR데이터 추출
                Map<String, Object> item = OCRDataProcessingToString(imageFile);
                pdfPageContent = item.get("일반데이터").toString();
                pdfListContent = (List<String>) item.get("표데이터");
                System.out.println("일반데이터 : " + pdfPageContent);
                System.out.println("표데이터 : " + pdfListContent);

                // 일반데이터에서 텍스트를 찾아서 각 데이터에 맞게 분배
                // 첫번째 페이지
                if (pageIndex == 0) {
                    // 날짜 및 시간 추출
                    String WksbiBalDate = tilkoParsing.extractDate(pdfPageContent);
                    String WksbiBalNoTime = tilkoParsing.extractTime(pdfPageContent);
                    gubunData = tilkoParsing.extractGubun(pdfListContent);
                    RegisterMap.put("WksbiBalDate",WksbiBalDate);
                    RegisterMap.put("WksbiBalNoTime",WksbiBalNoTime);
                    RegisterMap.put("IssOffice","법원행정처 등기정보중앙관리소");
                    RegisterMap.put("IssNo",""); // 발급번호 (열람용, 발급용 구분 발급용 300원추가결제 해당 발급번호로 3개월이내 5회 무료추가발급 가능)
                    RegisterMap.put("SumYn","Y");
                }
                // 표데이터에서 갑구 확인 인덱스
                int gabguStartIndex = tilkoParsing.findStartIndex(pdfListContent, "갑 구");
                int eulguStartIndex = tilkoParsing.findStartIndex(pdfListContent, "을\\s구|을\\|구");
                int eulguEndIndex = tilkoParsing.findStartIndex(pdfListContent, "등기명의인\\|\\(주민\\)등록번호");
                // `을구` 인덱스가 없으면 리스트 끝까지
                int endIdx = (eulguStartIndex != -1) ? eulguStartIndex : pdfListContent.size();



                if (gabguStartIndex != -1 || isParsingGabgu) {
                    // 갑구 데이터 수집 시작 또는 이어받기
                    if (gabguStartIndex != -1) {
                        int startIdx = gabguStartIndex + 1;
                        // 안전한 인덱스 범위 확인
                        if (startIdx < endIdx) {
                            gabguDataSubset.addAll(pdfListContent.subList(startIdx, endIdx));
                        } else {
                            System.out.println("🚨 비정상적인 인덱스 범위: startIdx=" + startIdx + ", endIdx=" + endIdx);
                        }
                        isParsingGabgu = true;  // 갑구 데이터 수집 활성화
                    } else if (isParsingGabgu) {
                        // 이전 페이지에서 이어진 갑구 데이터 수집
                        gabguDataSubset.addAll(pdfListContent.subList(0, endIdx));
                    }

                    // 을구 데이터 발견 시 갑구 데이터 파싱
                    if (eulguStartIndex != -1) {
                        System.out.println("🚩 을구 데이터 감지: 갑구 데이터 파싱 시작");
                        Map<String, Object> result = tilkoParsing.parseGabguTable(gabguDataSubset);
                        List<Map<String, Object>> parsedData = (List<Map<String, Object>>) result.get("parsedData");
                        List<Map<String, Object>> TradeDATA = (List<Map<String, Object>>) result.get("TradeAmount");

                        if (!TradeDATA.isEmpty()) {
                            System.out.println("마지막 매매 거래가액: " + TradeDATA.get(TradeDATA.size() - 1).get("Amount"));
                            TradeAmount.add(TradeDATA.get(TradeDATA.size() - 1));
                        }
                        GabguData.addAll(parsedData);
                        gabguDataSubset.clear();  // 데이터 초기화
                        isParsingGabgu = false;  // 갑구 데이터 수집 비활성화
                    }
                }
                // --- 을구 데이터 수집 ---
                if (eulguStartIndex != -1 || isParsingEulgu) {
                    if (eulguStartIndex != -1) {
                        int startIdx = eulguStartIndex + 1;
                        int endEulguIdx = (eulguEndIndex != -1) ? eulguEndIndex : pdfListContent.size();
                        if (startIdx < endEulguIdx) {
                            eulguDataSubset.addAll(pdfListContent.subList(startIdx, endEulguIdx));
                        } else {
                            System.out.println("🚨 비정상적인 을구 인덱스 범위: startIdx=" + startIdx + ", endEulguIdx=" + endEulguIdx);
                        }
                        isParsingEulgu = true;
                    } else if (isParsingEulgu) {
                        eulguDataSubset.addAll(pdfListContent);
                    }

                    if (eulguEndIndex != -1) {
                        System.out.println("🚩 을구 데이터 파싱 시작");
                        Map<String, Object> result = tilkoParsing.parseeulguTable(eulguDataSubset);
                        List<Map<String, Object>> parsedData = (List<Map<String, Object>>) result.get("parsedData");
                        List<Map<String, Object>> collateralData = (List<Map<String, Object>>) result.get("collateralData");
                        List<Map<String, Object>> leaseData = (List<Map<String, Object>>) result.get("leaseData");

                        // 을구 데이터 추가
                        eulguData.addAll(parsedData);

                        // 담보 데이터 전체 추가
                        if (!collateralData.isEmpty()) {
                            RegisterDataGItemsList.addAll(collateralData);
                            System.out.println("✅ 담보 데이터 저장 완료: " + collateralData.size() + "건");
                        } else {
                            System.out.println("담보 데이터가 없습니다.");
                        }

                        // 전세 데이터 전체 추가
                        if (!leaseData.isEmpty()) {
                            RegisterDataHItemsList.addAll(leaseData);
                            System.out.println("✅ 전세 데이터 저장 완료: " + leaseData.size() + "건");
                        } else {
                            System.out.println("전세 데이터가 없습니다.");
                        }

                        eulguDataSubset.clear();  // 데이터 초기화
                        isParsingEulgu = false;  // 을구 데이터 수집 비활성화
                    }
                }

                // 마지막에서 두 번째 페이지에서 관할등기소 정보 추출
                if (pageIndex == totalPages - 2) {
                    String WksbiJrisdictionOffice = tilkoParsing.extractJurisdictionOffice(pdfPageContent);
                    RegisterMap.put("WksbiJrisdictionOffice", WksbiJrisdictionOffice);
                }
                // 마지막 페이지에서 Summary데이터 추출
                if (pageIndex == totalPages - 1) {
                    Map<String, Object> summaryResult = tilkoParsing.parseSummaryTable(pdfListContent, pdfPageContent);

                    List<Map<String, Object>> summaryDataA = (List<Map<String, Object>>) summaryResult.get("summaryDataA");
                    List<Map<String, Object>> summaryDataK = (List<Map<String, Object>>) summaryResult.get("summaryDataK");
                    List<Map<String, Object>> summaryDataE = (List<Map<String, Object>>) summaryResult.get("summaryDataE");

                    if (summaryDataA != null) {
                        SummaryDataAMap.addAll(summaryDataA);
                    } else {
                        System.out.println("SummaryDataAMap이 비어 있습니다.");
                    }

                    if (summaryDataK != null) {
                        SummaryDataKMap.addAll(summaryDataK);
                    } else {
                        System.out.println("SummaryDataKMap이 비어 있습니다.");
                    }

                    if (summaryDataE != null) {
                        SummaryDataEMap.addAll(summaryDataE);
                    } else {
                        System.out.println("SummaryDataEMap이 비어 있습니다.");
                    }

                }

            }
            REALAOWNMap.put("REALAOWNDATA", GabguData);// 갑구에 대한 데이터 정리
            REALBOWNMap.put("REALBOWNDATA", eulguData);// 을구에 대한 데이터 정리
            resultMap.put("RegisterMap", RegisterMap);
            resultMap.put("REALAOWNMap", REALAOWNMap);
            resultMap.put("REALBOWNMap", REALBOWNMap);
            resultMap.put("TradeAmount", TradeAmount);
            resultMap.put("RegisterDataGItemsList", RegisterDataGItemsList);
            resultMap.put("RegisterDataHItemsList", RegisterDataHItemsList);
            resultMap.put("SummaryDataEMap", SummaryDataEMap);
            resultMap.put("SummaryDataKMap", SummaryDataKMap);
            resultMap.put("SummaryDataAMap", SummaryDataAMap);
            resultMap.put("gubunData", gubunData);
        } catch (IOException e) {
            System.out.println("🚨 PDF 처리 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if(tempFile.exists()){
                tempFile.delete();
            }
        }
        //TODO: 운영 환경에서만 끝.
        return resultMap;
    }
    // 파싱데이터 저장 로직
    private void SaveProcessor(List<String> table, Map<String, String> dtoValue) {

        //requestScopeData.setYear("2023");
        //requestScopeData.setQuarter("3");
        // 621 save로직
        List<String> keyword = List.of("보증조건", "보증조건",
                "개런티 보증조건", "개런티 보증조건",
                "워런티 보증조건", "워런티 보증조건"
        );
        List<String> keyword2 = List.of("연간 누적 이용율", "연간 누적 효율",
                "Capacity Factor", "Cumulative Efficiency",
                "Capacity Factor","Cumulative Efficiency");

        Map<String, List<String>> result = getKeyword2ValuesOnly(table,keyword, keyword2); // 보증조건, 워런티조건, 비고


//        TB_RP620Dto RP620Dto = ltsaService.TB_RP620DtoSet(result);
//        ltsaService.saveRP620(RP620Dto, dtoValue, year, month);


        //622 저장

        List<String> keywords = List.of("기간");
        List<String> keywords2 = List.of("누계");
        List<Integer> index = List.of(5);

        Map<String, List<String>> result2 = getValuesAfterKeywords2(table, keywords, keywords2, index);



        //623 저장
        List<String> Totalkeywords = List.of("계약자 성능 보증 기준", "계약자 워런티 보증 기준", "연료소모량", "계약자 운전 실적", "워런티 연료소모");
        List<String> Totalkeywords2 = List.of("이전누계", "이전누계", "이전누계", "이전누계", "이전누계");
        List<Integer> Totalindex = List.of(5, 5, 5 ,5, 5);

        Map<String, List<String>> Totalresult = getValuesAfterKeywords2(table,
                Totalkeywords,Totalkeywords2,Totalindex); // 보증조건, 워런티조건, 비고


        //List<TB_RP627Dto> RP627DtoList = TB_RP627DtoSet(result, dtoValue);
        //ltsaService.saveRP627(RP627DtoList);
    }

    private Map<String, Object> OCRDataProcessingToString(File imageFile) throws IOException {

        Map<String, Object> item = new HashMap<>();

        // 이미지 파일 파싱 메서드
        StringBuffer ocr =  new PDFTableProcessor().NaverClovaORCAPI(imageFile);
        //System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@일반데이터 어케 찍히냐");
        StringBuilder jsonParserobj = JsonObjectParser(ocr);
        //System.out.println(jsonParserobj.toString());

        //System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@일반데이터 어케 찍히냐");

       String  tableData = ocr.toString();

        item.put("일반데이터", jsonParserobj.toString());
        item.put("표데이터", parseTableFromOcrResponse(tableData));


        return item;
    }

    private List<String> OCRDataProcessingToList(File imageFile) throws IOException {

        //두개로 나누어서 파싱한이유는 일반 데이터와 표 데이터 부분 가져오는 접근 방법이 다름
        //일반 데이터 --> 키워드 옆에 글자를 파싱해서 가져옴,     표 데이터 --> Map과 List로 row단위로 관리해서 가져오는데 여러줄로 나오는 일반 데이터를 가져오기 까다로움

        StringBuffer ocr =  new PDFTableProcessor().NaverClovaORCAPI(imageFile);;
        System.out.println("OCR 결과: " + ocr.toString());


        //표 데이터가 이용
        String jsonResponseParsed = ocr.toString(); //TODO: 우녕용


       // System.out.println("표데이터 : @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        //System.out.println(jsonResponseParsed);

        List<String> table = parseTableFromOcrResponse(jsonResponseParsed); //TODO: 운영용


     //   System.out.println("talbe : @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

        for(String row : table){
            System.out.println(row);
        }

      //  System.out.println("표데이터끝끝끝끝 : @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

        return table;
    }




    //테스트용 메서드
    public static List<String> parseTextToList(String inputText) {
        List<String> lines = new ArrayList<>();

        // 줄바꿈(\n)을 기준으로 문자열을 분리하고 각 줄을 리스트에 추가
        String[] splitLines = inputText.split("\n");
        for (String line : splitLines) {
            lines.add(line.trim()); // 각 줄의 앞뒤 공백 제거 후 추가
        }

        return lines;
    }


    public static Map<String, List<String>> getKeyword2ValuesOnly(List<String> list, List<String> keywords1, List<String> keywords2) {
        Map<String, List<String>> result = new HashMap<>();
        int tableCounter = 1;  // "row" 뒤에 붙을 숫자를 위한 카운터
        int subCounter = 1;    // "sub" 뒤에 붙을 숫자를 위한 카운터

        // 각 키워드 쌍에 대해 키워드2가 포함된 인덱스만 찾음
        for (int k = 0; k < keywords1.size(); k++) {
            String keyword1 = keywords1.get(k);
            String keyword2 = keywords2.get(k);

            int startIndex = -1;
            int targetIndex = -1;

            // keyword1의 인덱스를 찾음
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).contains(keyword1)) {
                    startIndex = i;
                    break;
                }
            }

            // keyword1 이후에서 keyword2가 포함된 인덱스만 찾음
            if (startIndex != -1) {
                for (int j = startIndex + 1; j < list.size(); j++) {
                    if (list.get(j).contains(keyword2)) {
                        targetIndex = j;  // keyword2가 포함된 인덱스를 설정
                        break;
                    }
                }
            }

            // targetIndex가 유효한 경우에만 결과에 추가
            if (targetIndex != -1) {
                List<String> rowData = new ArrayList<>();
                rowData.add(list.get(targetIndex));  // keyword2가 포함된 인덱스의 값만 추가
                result.put("row" + tableCounter, rowData);
                tableCounter++;

                // Capacity Factor를 찾았고 %가 포함되어 있지 않은 경우, 직전 인덱스를 "sub" + 숫자로 추가
                if (keyword2.contains("Capacity Factor") && !list.get(targetIndex).contains("%")) {
                    List<String> subRowData = new ArrayList<>();

                    subRowData.add(list.get(targetIndex - 1));  // targetIndex의 직전 값
                    result.put("sub" + subCounter, subRowData);
                    subCounter++;  // 다음 "sub" 키 번호 증가
                }
            } else {
                // 구간을 찾지 못한 경우 빈 리스트 추가
                result.put("row" + tableCounter, new ArrayList<>());
                tableCounter++;
            }
        }

        return result;
    }


    public static Map<String, List<String>> getValuesAfterKeywords2(List<String> list, List<String> keywords, List<String> keywords2, List<Integer> additionalIndexes) {
        Map<String, List<String>> result = new HashMap<>();
        int tableCounter = 1;  // "row" 뒤에 붙을 숫자를 위한 카운터

        // 리스트를 순회하면서 keywords에서 첫 번째 키워드를 찾음
        for (int k = 0; k < keywords.size(); k++) {
            String keyword = keywords.get(k);
            String keyword2 = keywords2.get(k);
            int additionalCount = additionalIndexes.get(k);  // 추가적으로 가져올 인덱스 개수

            // 첫 번째 키워드(keyword)의 인덱스를 찾음
            int keywordIndex = -1;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).contains(keyword)) {
                    keywordIndex = i;
                    break;
                }
            }

            boolean found = false;

            // 첫 번째 키워드가 발견되었을 경우, 그 이후에 두 번째 키워드(keyword2)를 찾음
            if (keywordIndex != -1) {
                for (int j = keywordIndex + 1; j < list.size(); j++) {
                    if (list.get(j).contains(keyword2)) {
                        // "row" + 숫자 형태의 키값으로 저장 (첫 번째 매칭된 키워드)
                        List<String> rowData = new ArrayList<>(Arrays.asList(list.get(j).split("\\|")));
                        String tableKey = "row" + tableCounter;
                        result.put(tableKey, rowData);
                        tableCounter++;  // 다음 테이블 번호를 위해 카운터 증가

                        // 추가적으로 가져올 데이터 인덱스에 대한 처리 (각 인덱스마다 새로운 row로 저장)
                        for (int addIdx = 1; addIdx <= additionalCount; addIdx++) {
                            if (j + addIdx < list.size()) {
                                List<String> additionalRowData = new ArrayList<>(Arrays.asList(list.get(j + addIdx).split("\\|")));
                                String additionalTableKey = "row" + tableCounter;
                                result.put(additionalTableKey, additionalRowData);
                                tableCounter++;  // 다음 테이블 번호를 위해 카운터 증가
                            }
                        }
                        found = true;  // 해당 키워드를 찾았음을 표시
                        break;  // 첫 번째 매칭된 키워드만 처리
                    }
                }
            }

            // 키워드를 찾지 못한 경우 빈 값을 넣기
            if (!found) {
                String tableKey = "row" + tableCounter;
                result.put(tableKey, new ArrayList<>());  // 빈 리스트로 추가
                tableCounter++;
            }
        }

        return result;
    }



    // 리스트에서 첫 번째로 키워드가 포함된 인덱스를 찾는 메서드
    private static int getIndexOfKeyword(List<String> list, String keyword, int startIndex) {
        for (int i = startIndex; i < list.size(); i++) {
            if (list.get(i).contains(keyword)) {
                return i;
            }
        }
        return -1;  // 키워드가 없으면 -1 반환
    }


    public static Map<String, List<String>> groupDataByMonth(List<String> data, String monthKeyword, String endKeyword, int startIndex) {
        Map<String, List<String>> result = new HashMap<>();
        int tableCounter = 1;

        // 주어진 startIndex 이후에 monthKeyword의 인덱스를 찾기
        int monthStartIndex = getIndexOfKeyword(data, monthKeyword, startIndex);

        // monthKeyword의 인덱스를 찾지 못하면 빈 결과 반환
        if (monthStartIndex == -1) {
           // System.out.println(monthKeyword + "를 찾을 수 없습니다.");
            return result;
        }

        // monthStartIndex 이후에 endKeyword의 인덱스를 찾음
        int endIndex = getIndexOfKeyword(data, endKeyword, monthStartIndex);

        // endIndex가 발견되지 않았을 경우 data.size()로 설정하여 마지막까지 그룹화
        if (endIndex == -1) {
            endIndex = data.size();
        }

        // 찾은 monthStartIndex부터 endIndex까지 12개씩 그룹화하여 result에 추가
        for (int i = monthStartIndex; i < endIndex; i += 12) {
            List<String> yearGroup = new ArrayList<>();
            for (int j = i; j < i + 12 && j < endIndex; j++) {
                yearGroup.add(data.get(j));
            }
            result.put("table" + tableCounter, yearGroup);
            tableCounter++;
        }

        return result;
    }


    public static Map<String, Object> getValuesBetweenKeywords(List<String> list, String keyword, String keyword2) {
        Map<String, Object> result = new HashMap<>();

        // 첫 번째 키워드(keyword)의 인덱스를 찾음
        int startIndex = getIndexOfKeyword(list, keyword);

        // 두 번째 키워드(keyword2)의 인덱스를 찾음
        int endIndex = getIndexOfKeyword(list.subList(startIndex + 1, list.size()), keyword2) + startIndex + 1;

        // 시작 인덱스와 끝 인덱스를 확인하고 데이터 추출
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            List<String> rowData = new ArrayList<>(list.subList(startIndex + 1, endIndex + 1));
            // "table0" 키로 rowData 저장
            result.put("table0", rowData);
            result.put("endIndex", endIndex);  // endIndex를 추가하여 반환
        } else {
            // 키워드를 찾지 못한 경우 빈 리스트와 -1 인덱스 추가
            result.put("table0", new ArrayList<>());
            result.put("endIndex", -1);
        }

        return result;
    }

    private static int getIndexOfKeyword(List<String> list, String keyword) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains(keyword)) {
                return i;
            }
        }
        return -1;  // 키워드가 없으면 -1 반환
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    // "목적" 이후의 텍스트를 추출하는 메서드
    public static List<String> extractTextAfterKeywords(String text, List<String> keywords) {
        List<String> extractedTexts = new ArrayList<>();

        // 각 키워드에 대해 텍스트를 추출
        for (String keyword : keywords) {
            int keywordIndex = text.indexOf(keyword);
            if (keywordIndex == -1) {
                extractedTexts.add("키워드를 찾을 수 없습니다."); // 키워드가 없을 때
                continue;
            }

            // 키워드 이후의 텍스트 부분 추출
            String afterKeyword = text.substring(keywordIndex + keyword.length());

            // 구분자 "|"로 분리하여 텍스트 추출
            String[] parts = afterKeyword.split("\\|");

            // 첫 번째 유효한 텍스트 블록 추출
            StringBuilder extractedText = new StringBuilder();
            boolean isFirstValidBlockFound = false;

            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    // 유효한 텍스트만 추가
                    extractedText.append(trimmed).append(" ");
                    isFirstValidBlockFound = true;
                }

                // 숫자. (예: 4.) 패턴이 나타나면 종료
                if (isFirstValidBlockFound && (trimmed.matches("\\d+\\.\\s*.*"))) {
                    break; // 숫자 패턴이 감지되면 종료
                }
            }

            // 결과의 마지막 2글자를 제거
            if (extractedText.length() > 2) {
                extractedText.setLength(extractedText.length() - 3);
            }

            // 최종 텍스트 리스트에 추가
            extractedTexts.add(extractedText.toString().trim());
        }

        return extractedTexts;
    }

    // 키워드 이후로 줄바꿈 전까지만 텍스트를 추출하는 메서드
    // 여러 키워드 이후로 줄바꿈 전까지 텍스트를 추출하는 메서드
    public static List<String> extractTextUntilLineBreakForKeywords(String text, List<String> keywords) {
        List<String> extractedTexts = new ArrayList<>();

        // 각 키워드에 대해 텍스트를 추출
        for (String keyword : keywords) {
            int keywordIndex = text.indexOf(keyword);
            if (keywordIndex == -1) {
                extractedTexts.add("키워드를 찾을 수 없습니다.");
                continue;
            }

            // 키워드 이후의 텍스트 부분 추출
            String afterKeyword = text.substring(keywordIndex + keyword.length());

            // 줄바꿈 전까지만 텍스트 추출
            int lineBreakIndex = afterKeyword.indexOf("\n");
            if (lineBreakIndex != -1) {
                // 줄바꿈이 있으면 줄바꿈 전까지만 가져옴
                extractedTexts.add(afterKeyword.substring(0, lineBreakIndex).trim());
            } else {
                // 줄바꿈이 없으면 끝까지 가져옴
                extractedTexts.add(afterKeyword.trim());
            }
        }

        return extractedTexts;
    }

    // 주어진 키워드 앞의 숫자를 추출하는 메서드
    public static List<String> extractNumbersBeforeKeywords(String text, List<String> keywords) {
        List<String> numbers = new ArrayList<>();
        int startIndex = 0;

        for (String keyword : keywords) {
            int keywordIndex = text.indexOf(keyword, startIndex); // startIndex부터 검색

            if (keywordIndex != -1) {
                StringBuilder number = new StringBuilder();

                // 키워드 앞의 숫자를 역으로 추출
                for (int i = keywordIndex - 1; i >= 0; i--) {
                    char c = text.charAt(i);
                    if (Character.isDigit(c)) {
                        number.insert(0, c); // 숫자를 앞에 추가
                    } else {
                        break; // 숫자가 아닌 문자가 나오면 중단
                    }
                }

                // 숫자가 발견되면 리스트에 추가
                if (number.length() > 0) {
                    numbers.add(number.toString());
                } else {
                    numbers.add("F");
                }

                startIndex = keywordIndex + keyword.length(); // 다음 키워드 검색 시작 위치 갱신
            } else {
                numbers.add("Z");
            }
        }

        return numbers; // 결과 리스트 반환
    }

    public static List<String> parseTableFromOcrResponse(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        List<String> tableData = new ArrayList<>();

        // images 배열을 순회
        JsonNode imagesNode = rootNode.path("images");
        for (JsonNode imageNode : imagesNode) {
            // tables 배열을 찾음
            JsonNode tablesNode = imageNode.path("tables");
            if (!tablesNode.isMissingNode()) {
                // 각 테이블을 순회
                for (JsonNode tableNode : tablesNode) {
                    JsonNode cellsNode = tableNode.path("cells");

                    // 행렬 형태로 데이터를 저장할 Map
                    Map<Integer, List<String>> rowDataMap = new HashMap<>();

                    // 셀 데이터를 순회하여 행렬로 정렬
                    for (JsonNode cellNode : cellsNode) {
                        int rowIndex = cellNode.path("rowIndex").asInt();
                        int colIndex = cellNode.path("columnIndex").asInt();

                        // inferText 가져오기
                        String inferText = "";
                        JsonNode cellTextLines = cellNode.path("cellTextLines");
                        for (JsonNode textLine : cellTextLines) {
                            JsonNode cellWords = textLine.path("cellWords");
                            for (JsonNode word : cellWords) {
                                inferText += word.path("inferText").asText() + " ";
                            }
                        }

                        // inferText가 비어 있으면 "null"로 처리
                        if (inferText.trim().isEmpty()) {
                            inferText = "null";
                        }

                        // 현재 행을 리스트로 가져오거나 새로 생성
                        rowDataMap.putIfAbsent(rowIndex, new ArrayList<>());
                        List<String> row = rowDataMap.get(rowIndex);

                        // 현재 열에 텍스트 추가
                        while (row.size() <= colIndex) {
                            row.add("null"); // 빈 값으로 열 크기 맞추기
                        }
                        row.set(colIndex, inferText.trim()); // 추출한 텍스트 저장
                    }

                    // 행 데이터를 구분자로 이어서 리스트로 저장
                    for (List<String> row : rowDataMap.values()) {
                        tableData.add(String.join("|", row)); // 각 행을 |로 구분된 문자열로 변환
                    }
                }
            }
        }

        return tableData;
    }

    public StringBuilder JsonObjectParser(StringBuffer jsonResponseBuffer) throws JsonProcessingException {

        String jsonResponse = jsonResponseBuffer.toString();

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        // 최종 텍스트를 저장할 StringBuilder
        StringBuilder parsedText = new StringBuilder();

        // images 배열을 순회
        JsonNode imagesNode = rootNode.path("images");
        for (JsonNode imageNode : imagesNode) {
            // fields 배열을 순회
            JsonNode fieldsNode = imageNode.path("fields");
            for (int i = 0; i < fieldsNode.size(); i++) {
                JsonNode fieldNode = fieldsNode.get(i);
                String inferText = fieldNode.path("inferText").asText();
                boolean lineBreak = fieldNode.path("lineBreak").asBoolean();

                // inferText 추가
                parsedText.append(inferText);

                // lineBreak가 true일 경우 줄바꿈 추가, false일 경우 공백 추가
                if (lineBreak) {
                    parsedText.append("\n");
                } else {
                    parsedText.append(" ");
                }

                // 마지막 요소가 아니면 구분자 ',' 추가
                if (i < fieldsNode.size() - 1) {
                    parsedText.append("|");
                }
            }
        }

        return parsedText;

    }





    //TODO: 운영 환경
    /*// 파일을 임시 디렉터리에 저장하는 메서드
    private File saveFileToTemp(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("uploaded-", ".pdf"); // 임시 파일 생성
        file.transferTo(tempFile);  // 파일을 임시 파일로 변환
        return tempFile;
    }*/

    //TODO: 개발 환경


}

