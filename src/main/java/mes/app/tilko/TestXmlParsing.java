package mes.app.tilko;

import mes.app.tilko.service.TilkoService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/tilkotest")
public class TestXmlParsing {

    @Autowired
    TilkoService testTilkoService;

    @GetMapping("/searchGoyuList")
    public void test() {
        String filePath = "C:\\Users\\Act-PC-12\\Downloads\\hou.json";
        int realMaxNum = 303;


        try {
            // 파일에서 JSON 문자열 읽기
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            String jsonContent = new String(fileBytes, "UTF-8").trim();

            // BOM 제거
            if (jsonContent.startsWith("\uFEFF")) {
                jsonContent = jsonContent.substring(1);
            }

            System.out.println("Raw JSON Content: [" + jsonContent + "]");

            // JSON 문자열을 JSONObject로 변환
            JSONObject responseJson = new JSONObject(jsonContent);

            // "Result" 키 접근
            org.json.JSONObject resultObject = responseJson.optJSONObject("Result");

            if (resultObject != null) {
                // "Register" 키 접근
                org.json.JSONObject registerObject = resultObject.optJSONObject("Register");

                // "summary" 키 접근
                org.json.JSONObject summaryObject = resultObject.optJSONObject("Summary");
                if (registerObject != null) {
                    Map<String, Object> registerData = new HashMap<>();
                    registerData.put("PinNo", registerObject.optString("PinNo", ""));
                    registerData.put("WksbiAddress", registerObject.optString("WksbiAddress", ""));
                    registerData.put("Address", registerObject.optString("Address", ""));
                    registerData.put("WksbiBalDate", registerObject.optString("WksbiBalDate", ""));
                    registerData.put("WksbiBalNoTime", registerObject.optString("WksbiBalNoTime", ""));
                    registerData.put("IssOffice", registerObject.optString("IssOffice", ""));
                    registerData.put("IssNo", registerObject.optString("IssNo", ""));
                    registerData.put("SumYn", registerObject.optString("SumYn", ""));
                    registerData.put("WksbiJrisdictionOffice", registerObject.optString("WksbiJrisdictionOffice", ""));
                    registerData.put("REALID", realMaxNum);

                    // realinfoxml 테이블 데이터 저장

                    // 데이터 삽입 수행
                    testTilkoService.saveTilkoXML(registerData);
                    // Data 키 접근()
                    // 표제부 주소 출력
                    org.json.JSONArray  dataW = registerObject.optJSONArray("DataH");
                    if(dataW != null) {
                        for (int i = 0; i < dataW.length(); i++) {
                            org.json.JSONObject dataWItem = dataW.getJSONObject(i);
                            // 건물 구분자 출력
                            String buldCont = dataWItem.optString("BuldCont", null);
                            System.out.println("buldCont(건축물 구분): " + buldCont);
                        }
                    }
                    // 공동전세목록 (TB_REGISTERDATAH / TB_REGISTERDATAHITEMS)
                    org.json.JSONArray  dataH = registerObject.optJSONArray("DataH");
                    // 매매목록 (TB_REGISTER.DATAJ / TB_TradeAmount / TB_items)
                    org.json.JSONArray  dataJ = registerObject.optJSONArray("DataJ");
                    // 을구 소유권 사항 (TB_REALBOWN)
                    org.json.JSONArray  dataE = registerObject.optJSONArray("DataE");
                    // 갑구 소유권 사항 (TB_REALAOWN)
                    org.json.JSONArray  dataK = registerObject.optJSONArray("DataK");
                    // 담보목록 (TB_REGISTERDATAH / TB_REGISTERDATAHITEMS)
                    org.json.JSONArray  dataG = registerObject.optJSONArray("DataG");
                    if (dataK != null) {
                        for (int i = 0; i < dataK.length(); i++) {
                            org.json.JSONObject dataKItem = dataK.getJSONObject(i);

                            // "IndiNo"와 같은 개별 항목 접근
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("RgsAimCont", dataKItem.optString("RgsAimCont", null));
                            dataMap.put("Receve", dataKItem.optString("Receve", null));
                            dataMap.put("RgsCaus", dataKItem.optString("RgsCaus", null));
                            dataMap.put("RankNo", dataKItem.optString("RankNo", null));
                            dataMap.put("NomprsAndEtc", dataKItem.optString("NomprsAndEtc", null));
                            dataMap.put("REALID", realMaxNum);

                            try {
                                testTilkoService.saveRegisterDataK(dataMap);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("DataH Array is null or empty");
                    }
                    if (dataH != null) {
                        for (int i = 0; i < dataH.length(); i++) {
                            org.json.JSONObject dataHItem = dataH.getJSONObject(i);

                            // "IndiNo"와 같은 개별 항목 접근
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("SeqNo", dataHItem.optString("SeqNo", null));
                            dataMap.put("EstateRightDisplay", dataHItem.optString("EstateRightDisplay", null));
                            dataMap.put("OwnJuris", dataHItem.optString("OwnJuris", null));
                            dataMap.put("RankNo", dataHItem.optString("RankNo", null));
                            dataMap.put("CrtResn", dataHItem.optString("CrtResn", null));
                            dataMap.put("DstInfo", dataHItem.optString("DstInfo", null));
                            dataMap.put("REALID", realMaxNum);

                            try {
                                testTilkoService.savedataHItems(dataMap);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("DataH Array is null or empty");
                    }
                    if(dataJ != null){
//                        Map<String, Object> dataJMap = new HashMap<>();
//                        dataJMap.put("Number", dataJ.optString("Number", ""));
//
//                        // realinfoxml 테이블 데이터 저장
//                        try{
//                            tilkoService.savedataJ(dataJMap);
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                        // TradeAmount 접근
//                        org.json.JSONArray  TradeAmount = registerObject.optJSONArray("DataE");
//                        // Items 접근
//                        org.json.JSONArray  Items = registerObject.optJSONArray("DataE");
//                        if(TradeAmount != null){
//                            for (int i = 0; i < TradeAmount.length(); i++) {
//                                org.json.JSONObject TradeAmountItem = TradeAmount.getJSONObject(i);
//
//                                // "IndiNo"와 같은 개별 항목 접근
//                                Map<String, Object> dataMap = new HashMap<>();
//                                dataMap.put("Amount", TradeAmountItem.optString("Amount", null));
//                                dataMap.put("UpdateResn", TradeAmountItem.optString("UpdateResn", null));
//                                dataMap.put("REALID", realMaxNum);
//
//                                try {
//                                    tilkoService.saveTradeAmount(dataMap);
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                            }
//                        }else if(Items != null){
//                            for (int i = 0; i < Items.length(); i++) {
//                                org.json.JSONObject Itemsdata = Items.getJSONObject(i);
//
//                                // "IndiNo"와 같은 개별 항목 접근
//                                Map<String, Object> dataMap = new HashMap<>();
//                                dataMap.put("SeqNo", Itemsdata.optString("SeqNo", null));
//                                dataMap.put("EstateDisplay", Itemsdata.optString("EstateDisplay", null));
//                                dataMap.put("RankNo", Itemsdata.optString("RankNo", null));
//                                dataMap.put("CrtResn", Itemsdata.optString("CrtResn", null));
//                                dataMap.put("UpdateResn", Itemsdata.optString("UpdateResn", null));
//                                dataMap.put("REALID", realMaxNum);
//
//                                try {
//                                    tilkoService.saveItemsdata(dataMap);
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                            }
//                        }else{
//                            System.out.println("Regester DataJ Array is null or empty");
//                        }
//                        for (int i = 0; i < dataJ.length(); i++) {
//                            org.json.JSONObject dataJItem = dataJ.getJSONObject(i);
//
//                            // "IndiNo"와 같은 개별 항목 접근
//                            Map<String, Object> dataMap = new HashMap<>();
//                            dataMap.put("IndiNo", dataJItem.optString("SeqNo", null));
//                            dataMap.put("EstateRightDisplay", dataJItem.optString("EstateRightDisplay", null));
//                            dataMap.put("OwnJuris", dataJItem.optString("OwnJuris", null));
//                            dataMap.put("RankNo", dataJItem.optString("RankNo", null));
//                            dataMap.put("CrtResn", dataJItem.optString("CrtResn", null));
//                            dataMap.put("DstInfo", dataJItem.optString("DstInfo", null));
//
//                            try {
//                                tilkoService.savedataJItems(dataMap);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
                        System.out.println("dataJ 존재 :" + dataJ);
                    } else {
                        System.out.println("DataJ Array is null or empty");
                    }
                    if(dataG != null) {
                        // dataG 가 개별 컬럼을 가지고 있을경우
//                        Map<String, Object> dataGMap = new HashMap<>();
//                        dataGMap.put("Number", dataG.optString("Number", ""));
//
//                        // registerdatag 테이블 데이터 저장
//                        try{
//                            tilkoService.savedataJ(dataGMap);
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                        // registerdatagItems 접근
//                        org.json.JSONArray  Items = dataG.optJSONArray("Items");
//                        if(Items != null){
//                            for (int i = 0; i < TradeAmount.length(); i++) {
//                                org.json.JSONObject TradeAmountItem = TradeAmount.getJSONObject(i);
//
//                                // "IndiNo"와 같은 개별 항목 접근
//                                Map<String, Object> dataMap = new HashMap<>();
//                                dataMap.put("Amount", TradeAmountItem.optString("Amount", null));
//                                dataMap.put("UpdateResn", TradeAmountItem.optString("UpdateResn", null));
//                                dataMap.put("REALID", realMaxNum);
//
//                                try {
//                                    tilkoService.saveTradeAmount(dataMap);
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                        else{
//                            System.out.println("Regester DataJ Array is null or empty");
//                        }

                        // datag가 리스트로 하위 테이블정보만 가지고 있을경우
//                        for (int i = 0; i < dataJ.length(); i++) {
//                            org.json.JSONObject dataJItem = dataJ.getJSONObject(i);
//
//                            // "IndiNo"와 같은 개별 항목 접근
//                            Map<String, Object> dataMap = new HashMap<>();
//                            dataMap.put("IndiNo", dataJItem.optString("SeqNo", null));
//                            dataMap.put("EstateRightDisplay", dataJItem.optString("EstateRightDisplay", null));
//                            dataMap.put("OwnJuris", dataJItem.optString("OwnJuris", null));
//                            dataMap.put("RankNo", dataJItem.optString("RankNo", null));
//                            dataMap.put("CrtResn", dataJItem.optString("CrtResn", null));
//                            dataMap.put("DstInfo", dataJItem.optString("DstInfo", null));
//
//                            try {
//                                tilkoService.savedataJItems(dataMap);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
                        System.out.println("dataG 존재 :" + dataG);
                    } else {
                        System.out.println("DataJ Array is null or empty");
                    }
                    if(dataE != null){
                        for (int i = 0; i < dataE.length(); i++) {
                            org.json.JSONObject dataEItem = dataE.getJSONObject(i);

                            // 개별 항목 접근
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("RankNo", dataEItem.optString("RankNo", ""));
                            dataMap.put("RgsAimCont", dataEItem.optString("RgsAimCont", ""));
                            dataMap.put("Receve", dataEItem.optString("Receve", ""));
                            dataMap.put("RgsCaus", dataEItem.optString("RgsCaus", ""));
                            dataMap.put("NomprsAndEtc", dataEItem.optString("NomprsAndEtc", ""));
                            dataMap.put("REALID", realMaxNum);

                            try {
                                testTilkoService.savedataE(dataMap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("DatE Array is null or empty");
                    }
                }  else {
                    System.out.println("register is null or empty");
                }
                if (summaryObject != null) {
                    Map<String, Object> summaryData = new HashMap<>();
                    summaryData.put("UniqueNo", summaryObject.get("UniqueNo").toString());
                    summaryData.put("Gubun", summaryObject.get("Gubun").toString());
                    summaryData.put("Address", summaryObject.get("Address").toString());
                    summaryData.put("PrintDate", summaryObject.get("PrintDate").toString());
                    summaryData.put("REALID", realMaxNum);

                    // summary 데이터 저장
                    try{
                        testTilkoService.saveSummary(summaryData);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    // Data 키 접근()
                    // 소유지분현황 갑구 (TB_SummaryDataA)
                    org.json.JSONArray dataAArray = summaryObject.optJSONArray("DataA");
                    // 소유지분을 제외한소유권에 관한 사항 갑구 (TB_SUMMARYDATAK)
                    org.json.JSONArray dataKArray = summaryObject.optJSONArray("DataK");
                    // (근)저당권 및 전세권등(을구) (TB_SUMMARYDATAE)
                    org.json.JSONArray dataEArray = summaryObject.optJSONArray("DataE");

                    if (dataAArray != null) {
                        for (int i = 0; i < dataAArray.length(); i++) {
                            org.json.JSONObject dataAItem = dataAArray.getJSONObject(i);

                            // "IndiNo"와 같은 개별 항목 접근
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("RegisteredHolder", dataAItem.optString("RegisteredHolder", ""));
                            dataMap.put("RegistrationNumber", dataAItem.optString("RegistrationNumber", ""));
                            dataMap.put("FinalShare", dataAItem.optString("FinalShare", ""));
                            dataMap.put("Address", dataAItem.optString("Address", ""));
                            dataMap.put("RankNo", dataAItem.optString("RankNo", ""));
                            dataMap.put("REALID", realMaxNum);

                            // dataa 개별 저장
                            try {
                                testTilkoService.saveSummaryDataA(dataMap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("Summary DataA Array is null or empty");
                    }
                    if(dataKArray != null){
                        for (int i = 0; i < dataKArray.length(); i++) {
                            org.json.JSONObject dataKItem = dataKArray.getJSONObject(i);

                            //  개별 항목 접근
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("Purpose", dataKItem.optString("Purpose", ""));
                            dataMap.put("ReceiptInfo", dataKItem.optString("ReceiptInfo", ""));
                            dataMap.put("Information", dataKItem.optString("Information", ""));
                            dataMap.put("TargetOwner", dataKItem.optString("TargetOwner", ""));
                            dataMap.put("RankNo", dataKItem.optString("RankNo", ""));
                            dataMap.put("REALID", realMaxNum);

                            // datak 개별 저장
                            try {
                                testTilkoService.saveSummaryDataK(dataMap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("Summary DataK Array is null or empty");
                    }
                    if(dataEArray != null){
                        for (int i = 0; i < dataEArray.length(); i++) {
                            org.json.JSONObject dataEItem = dataEArray.getJSONObject(i);

                            // "IndiNo"와 같은 개별 항목 접근
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("Purpose", dataEItem.optString("Purpose", ""));
                            dataMap.put("ReceiptInfo", dataEItem.optString("ReceiptInfo", ""));
                            dataMap.put("Information", dataEItem.optString("Information", ""));
                            dataMap.put("TargetOwner", dataEItem.optString("TargetOwner", ""));
                            dataMap.put("RankNo", dataEItem.optString("RankNo", ""));
                            dataMap.put("REALID", realMaxNum);

                            try {
                                testTilkoService.saveSummaryDataE(dataMap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else{
                        System.out.println("summary dataE Object is null");
                    }
                } else {
                    System.out.println("Summary Object is null");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
