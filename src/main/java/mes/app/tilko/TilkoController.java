package mes.app.tilko;

import mes.app.actas_inspec.LTSA.LTSAController;
import mes.app.tilko.service.TilkoService;
import mes.domain.entity.User;
import mes.domain.entity.factcheckEntity.TB_PDFSEQ;
import mes.domain.entity.factcheckEntity.TB_REALINFO;
import mes.domain.model.AjaxResult;
import mes.domain.repository.SystemOptionRepository;
import mes.domain.repository.factcheckRepository.PDFSEQRepository;
import mes.domain.repository.factcheckRepository.REALINFORepository;
import okhttp3.*;
import org.json.JSONException;
import org.json.XML;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.transaction.Transactional;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("api/tilko")
public class TilkoController {

    private static final String apiHost	= "https://api.tilko.net/";
    private static final String apiKey	= "9f32eeb2d5404e69b57d23c137961d64";
    TilkoParsing tilkoParsing;

    @Autowired
    TilkoService tilkoService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    PDFSEQRepository pdfseqRepository;

    @Autowired
    REALINFORepository realinfoRepository;

    // 등기부등본 갑을구 주요정보 데이터 파싱 api 통신
    public Map<String, Object> searchParsingData(String TRKey, String GoyuNUM, int realMaxNum){
        TilkoController tc = new TilkoController();
        Map<String, Object> returnMap = new HashMap<>();
        try {
            //RSA Public Key 조회
            String rsaPublicKey = getPublicKey();
            // AES Secret Key 및 IV 생성
            // AES Secret Key 및 IV 생성
            byte[] aesKey			= new byte[16];
            new Random().nextBytes(aesKey);

            byte[] aesIv			= new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

            // AES Key를 RSA Public Key로 암호화
            String aesCipherKey = rsaEncrypt(aesKey, rsaPublicKey);

            // api 엔드포인트
            String url = apiHost + "api/v2.0/IrosArchive/ParseXml";

            // API 요청 파라미터 설정
            JSONObject json			= new JSONObject();
            json.put("TransactionKey"				, TRKey);

            System.out.println("Request Payload: " + json);

            // API 호출
            OkHttpClient client		= new OkHttpClient();

            Request request			= new Request.Builder()
                    .url(url)
                    .addHeader("API-KEY"			, apiKey)
                    .addHeader("ENC-KEY"			, aesCipherKey)
                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString())).build();

            Response response		= client.newCall(request).execute();
            String responseStr		= response.body().string();
            System.out.println("responseStr: " + responseStr);
            // JSON 파싱
            org.json.JSONObject responseJson = new org.json.JSONObject(responseStr);
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
                    tilkoService.saveTilkoXML(registerData);
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
                                tilkoService.saveRegisterDataK(dataMap);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        // REALINFO 테이블에 등기 목적/원인 위해 출력 return
                        org.json.JSONObject lastData = dataK.getJSONObject(dataK.length()-1);
                        String RgsAimCont = lastData.optString("RgsAimCont",null);
                        String RgsCaus = lastData.optString("RgsCaus",null);
                        returnMap.put("RgsAimCont", RgsAimCont);
                        returnMap.put("RgsCaus", RgsCaus);
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
                                tilkoService.savedataHItems(dataMap);
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
//                            // 개별 항목 접근
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
                        // dataJ 데이터중 거래가액 / 일련번호 추출 return
//                        org.json.JSONObject lastData = dataJ.getJSONObject(dataJ.length()-1);
//                        String Amount = lastData.optString("Amount",null);
//                        returnMap.put("Amount", Amount);
//                        String SeqNo = lastData.optString("SeqNo",null);
//                        returnMap.put("SeqNo", SeqNo);
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
                            // REALINFO 테이블에 채권최고액 위해 출력 return
                            org.json.JSONObject lastData = dataE.getJSONObject(dataE.length()-1);
                            String NomprsAndEtc = lastData.optString("NomprsAndEtc",null);
                            returnMap.put("NomprsAndEtc", NomprsAndEtc);

                            try {
                                tilkoService.savedataE(dataMap);
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
                        tilkoService.saveSummary(summaryData);
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
                                tilkoService.saveSummaryDataA(dataMap);
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
                                tilkoService.saveSummaryDataK(dataMap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("Summary DataK Array is null or empty");
                    }
                    if(dataEArray != null){
                        List<Map<String, Object>> dataList = new ArrayList<>();
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
                            dataList.add(dataMap);

                            try {
                                tilkoService.saveSummaryDataE(dataMap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // REALINFO 테이블에 점수계산 위해 return
                            returnMap.put("summaryData", dataList);
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
        return returnMap;
    }

    // 주소 고유번호 조회 api 통신
    @GetMapping("/searchGoyuList")
    public AjaxResult getGoyuNUM(@RequestParam(value = "address1")String address){
        TilkoController tc = new TilkoController();
        AjaxResult result = new AjaxResult();

        try {
            //RSA Public Key 조회
            String rsaPublicKey = getPublicKey();
            // AES Secret Key 및 IV 생성
            // AES Secret Key 및 IV 생성
            byte[] aesKey			= new byte[16];
            new Random().nextBytes(aesKey);

            byte[] aesIv			= new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

            // AES Key를 RSA Public Key로 암호화
            String aesCipherKey = rsaEncrypt(aesKey, rsaPublicKey);

            // API URL 설정(인터넷 등기소 등기물건 주소검색: https://tilko.net/Help/Api/POST-api-apiVersion-Iros-RISUConfirmSimpleC)
            // 고유번호 조회 엔드포인트
            String url = apiHost + "api/v2.0/Iros2/RetrieveSmplSrchList";

            // API 요청 파라미터 설정
            JSONObject json			= new JSONObject();
            json.put("Address"				, address);
            json.put("Sangtae"				, "2");
            json.put("KindClsFlag"			, "0");
            json.put("Region"				, "0");
            json.put("Page"					, "1");

            System.out.println("Request Payload: " + json);

            // API 호출
            OkHttpClient client		= new OkHttpClient();

            Request request			= new Request.Builder()
                    .url(url)
                    .addHeader("API-KEY"			, apiKey)
                    .addHeader("ENC-KEY"			, aesCipherKey)
                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString())).build();

            Response response		= client.newCall(request).execute();
            String responseStr		= response.body().string();
            System.out.println("responseStr: " + responseStr);

            // JSON 파싱
            JSONObject responseJson = new JSONObject(responseStr);
            // "Result" 객체에 접근
            JSONObject resultObject = responseJson.optJSONObject("Result");

            // "DataList" 배열 가져오기
            JSONArray dataList = resultObject != null ? resultObject.optJSONArray("DataList") : new JSONArray();

//            if (dataList != null) {
//                for (int i = 0; i < dataList.length(); i++) {
//                    JSONObject dataItem = dataList.getJSONObject(i);
//                    System.out.println("Data Item " + i + ": " + dataItem.toString());
//                }
//            }
            // 데이터 설정
            if (dataList != null && dataList.length() > 0) {
                result.data = dataList.toList();
            } else {
                result.data = new ArrayList<>(); // 빈 배열 설정
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.success = false;
            result.message = "오류 발생: " + e.getMessage();
        }
        return result;
    }
    // 등기부등본 xml데이터 api 통신
    @org.springframework.web.bind.annotation.ResponseBody
    @GetMapping("/searchaddress")
    public AjaxResult searchaddress(@RequestParam(value = "GoyuNUM")String GoyuNUM,
                                    @RequestParam(value = "address")String address,
                                    Authentication authentication) throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        AjaxResult result = new AjaxResult();
        TilkoController2 tc = new TilkoController2();
        String irosID = "aarmani";
        String irosPWD = "jky@6400";
        String irosNUM1 = "O3275071";
        String irosNUM2 = "3112";
        String irosNUM3 = "jky6400";
        // 등기물건 고유번호(GoyuNUM)
        String GoyuAddressNUM = GoyuNUM;

        // 기타 데이터 (공백일경우 기본값 IsSummary제외 "N")
        String CmortFlag = "N";
        String TradeSeqFlag = "N";
        String AbsCls = "11";
        String RgsMttrSmry = "1"; // 유효사항만 포함 여부

        // RSA Public Key 조회
        String rsaPublicKey = tc.getPublicKey();
        System.out.println("rsaPublicKey: " + rsaPublicKey);


        // AES Secret Key 및 IV 생성
        byte[] aesKey = new byte[16];
        new Random().nextBytes(aesKey);

        byte[] aesIv = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        // AES Key를 RSA Public Key로 암호화
        String aesCipherKey = rsaEncrypt(aesKey, rsaPublicKey);
        System.out.println("aesCipherKey: " + aesCipherKey);


        // API URL 설정
        // pdf data + Txkey(트랜잭션 key)
        String url = tc.apiHost + "api/v2.0/Iros2IdLogin/RealtyRegistry";


        // API 요청 파라미터 설정
        org.json.simple.JSONObject json = new org.json.simple.JSONObject();

        org.json.simple.JSONObject auth = new org.json.simple.JSONObject();
        auth.put("UserId", tc.aesEncrypt(aesKey, aesIv, irosID));
        auth.put("UserPassword", tc.aesEncrypt(aesKey, aesIv, irosPWD));

        json.put("Auth", auth);
        json.put("EmoneyNo1", tc.aesEncrypt(aesKey, aesIv, irosNUM1));
        json.put("EmoneyNo2", tc.aesEncrypt(aesKey, aesIv, irosNUM2));
        json.put("EmoneyPwd", tc.aesEncrypt(aesKey, aesIv, irosNUM3));
        json.put("Pin", GoyuAddressNUM);
        json.put("CmortFlag", CmortFlag);
        json.put("TradeSeqFlag", TradeSeqFlag);
        json.put("AbsCls", AbsCls);
        json.put("RgsMttrSmry", RgsMttrSmry);

        System.out.println("Request Payload: " + json.toJSONString());

        // API 호출
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("API-KEY", tc.apiKey)
                .addHeader("ENC-KEY", aesCipherKey)
                .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toJSONString())).build();

        Response response = client.newCall(request).execute();
        String responseStr = response.body().string();
        System.out.println("responseStr: " + responseStr);
        // JSON 파싱
        org.json.JSONObject responseJson = new org.json.JSONObject(responseStr);

        // "XmlData" 섹션 가져오기
//        Object xmlDataObject = responseJson.opt("XmlData");
//        if (xmlDataObject instanceof String && !((String) xmlDataObject).isEmpty()) {
//            // XML 데이터를 JSON 객체로 변환
//            org.json.JSONObject jsonData = XML.toJSONObject((String) xmlDataObject);

            // 트랜잭션 키 가져오기
            Object transactionKeyObject = responseJson.opt("ApiTxKey");
            org.json.JSONObject transactionKey = transactionKeyObject instanceof org.json.JSONObject
                    ? (org.json.JSONObject) transactionKeyObject
                    : null;

            // api 결과 저장
            Map<String, Object> resultData = new HashMap<>();
//            Map<String, Object> jsonDataMap = jsonData.toMap();

//            resultData.put("jsonData", jsonDataMap);
            assert transactionKey != null;
            resultData.put("transactionKey", transactionKeyObject.toString());

//            System.out.println("jsonDataMap : " +  jsonDataMap);

            // REALINFO 테이블 id 최대값 확인
            Map<String, Object> maxRealNum = tilkoService.getMaxOfRealinfo();
            int maxNum = (Integer) maxRealNum.get("NextID");

            // xml 파싱 데이터 조회 및 저장
            Map<String, Object> returnData = new HashMap<>();
            try {
                returnData = searchParsingData(transactionKeyObject.toString(), GoyuNUM, maxNum);
            }catch (Exception e){
                e.printStackTrace();
            }

            // REALINFO 테이블 데이터 저장 데이터 처리
            Map<String, Object> dataMap = new HashMap<>();
            User user = (User) authentication.getPrincipal();
            // 오늘 날짜 가져오기
            LocalDate today = LocalDate.now();

            // 날짜를 YYYYMMDD 형식으로 포맷팅
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String formattedDate = today.format(formatter);
            dataMap.put("USERID", user.getUsername());
            dataMap.put("REQDATE", formattedDate);
            dataMap.put("REALADD", address);
            dataMap.put("REGDATE", formattedDate);

            //시도 (address 파싱)
            Map<String, String> parsedResult = tilkoParsing.parseAddress(address);
            dataMap.put("RESIDO", parsedResult.get("RESIDO"));
            dataMap.put("REGUGUN", parsedResult.get("REGUGUN"));

            dataMap.put("REMOK", returnData.get("RgsAimCont")); // 등기목적
            // 등기원인일자 파싱
            Map<String, String> parsedDateAndRemaining = tilkoParsing.parseDateAndRemaining(returnData.get("RgsCaus").toString());
            dataMap.put("RgsCaus", parsedDateAndRemaining.get("DATE"));
            dataMap.put("RgsAimCont", parsedDateAndRemaining.get("REMAINING"));

            dataMap.put("REJIMOK", ""); //지목내역
            dataMap.put("REAREA", ""); // 면적
            dataMap.put("REAMOUNT", ""); // 거래가액 dataMap.put("REAMOUNT", returnData.get("Amount"));

            dataMap.put("RESEQ", ""); // 일련번호 dataMap.put("RESEQ", returnData.get("SeqNo"))

            // 채권최고액 파싱
            String amount = tilkoParsing.parseAmount(returnData.get("RgsCaus").toString());
            dataMap.put("REMAXAMT", amount);
            // 입력일시 GETDATE()
            // 판정점수 계산 로직
            // 공통코드 데이터 불러오기
            List<Map<String, Object>> comcode = tilkoService.getComcode();
            // 차감 최저점수 불러오기
            Map<String, Object> lessScore = tilkoService.getLessScore();
            Map<String, Object> resultScore = tilkoParsing.calScore((List<Map<String, Object>>) returnData.get("summaryData"),
                    comcode,
                    (Integer)lessScore.get("Value"));
            dataMap.put("REALSCORE", resultScore.get("REALSCORE")); // 판정점수 (점수 로직 summary 데이터 기준 공통코드 텍스트 비교 후 로직)
            // 카드 출력시 필요 데이터
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("REALSCORE", resultScore.get("REALSCORE"));
            resultMap.put("GRADE", resultScore.get("GRADE"));
            resultMap.put("COMMENT", resultScore.get("COMMENT"));
            resultMap.put("ADDRESS", address);

            dataMap.put("REALPOINT", 1); // 조회수
            dataMap.put("RELASTDATE", formattedDate); // 최종 조회일
            dataMap.put("PinNo", GoyuNUM); // 조회고유번호
            // 구축물 데이터 파싱
            // 데이터 파싱(건축물 구축물별 구분)
//            Map<String, Object> register = (Map<String, Object>) jsonDataMap.get("register");
//            List<Map<String, Object>> itemList = (List<Map<String, Object>>) register.get("item");
//            StringBuilder wksbiData = new StringBuilder();
//            for (Map<String, Object> item : itemList) {
//                // 각 item별 건물내역 데이터
//                String wksbiAddress = (String) item.get("wksbw_buld_cont");
//                wksbiData.append(wksbiAddress);
//            }
////             구축물별 파싱 메서드 호출
//            String archtec = tilkoParsing.assortArchitec(String.valueOf(wksbiData));
//            dataMap.put("REALGUBUN", archtec); // 구축물


            tilkoService.saveTilko(dataMap);
            result.data = resultMap;

//        } else {
//            System.out.println("데이터 저장 실패");
//            result.message = "데이터 저장 실패";
//        }
        return result;
    }


    // pdf파일 다운로드
    @GetMapping("/downloadPDF")
    public AjaxResult downloadPDF(@RequestParam(value = "GoyuNUM")String GoyuNUM,
                                  @RequestParam(value = "address")String address,
                                  Authentication authentication) throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        TilkoController tc = new TilkoController();
        LTSAController ltsaCont = new LTSAController();
        AjaxResult result = new AjaxResult();
        User user = (User)authentication.getPrincipal();
        LocalDate today = LocalDate.now();
        // 카드 출력시 필요 데이터
        Map<String, Object> resultMap = new HashMap<>();

        // 기존 유저 조회정보에서 동일한 고유번호가 있는지 확인후 없다면 통신 있다면 자료 가져오기
        boolean savedGoyu = tilkoService.isGoyuNumMatched(user.getUsername(), address, GoyuNUM);
        if (savedGoyu) {
            result.message = "기존 조회데이터가 존재합니다.";
            resultMap.put("GoyuNum", GoyuNUM);
            result.data = resultMap;
            return result;
        }else{
            System.out.println("새로 데이터를 조회합니다.");
        }


        String irosID = "aarmani";
        String irosPWD = "jky@6400";
        String irosNUM1 = "Q9864909";
        String irosNUM2 = "6966";
        String irosNUM3 = "jky6400";
        // 등기물건 고유번호(GoyuNUM)
        String GoyuAddressNUM = GoyuNUM;

        // 기타 데이터 (공백일경우 기본값 IsSummary제외 "N")
        String CmortFlag = "N";
        String TradeSeqFlag = "N";
        String AbsCls = "11";
        String RgsMttrSmry = "1"; // 유효사항만 포함 여부

        // RSA Public Key 조회
        String rsaPublicKey = tc.getPublicKey();
        System.out.println("rsaPublicKey: " + rsaPublicKey);


        // AES Secret Key 및 IV 생성
        byte[] aesKey = new byte[16];
        new Random().nextBytes(aesKey);

        byte[] aesIv = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        try {
            // AES Key를 RSA Public Key로 암호화
            String aesCipherKey = rsaEncrypt(aesKey, rsaPublicKey);
            System.out.println("aesCipherKey: " + aesCipherKey);


            // API URL 설정
            // pdf data + Txkey(트랜잭션 key)
            String url = tc.apiHost + "api/v2.0/Iros2IdLogin/RealtyRegistry";


            // API 요청 파라미터 설정
            org.json.simple.JSONObject json = new org.json.simple.JSONObject();

            org.json.simple.JSONObject auth = new org.json.simple.JSONObject();
            auth.put("UserId", tc.aesEncrypt(irosID, aesKey, aesIv));
            auth.put("UserPassword", tc.aesEncrypt(irosPWD, aesKey, aesIv));

            json.put("Auth", auth);
            json.put("EmoneyNo1", tc.aesEncrypt(irosNUM1, aesKey, aesIv));
            json.put("EmoneyNo2", tc.aesEncrypt(irosNUM2, aesKey, aesIv));
            json.put("EmoneyPwd", tc.aesEncrypt(irosNUM3, aesKey, aesIv));
            json.put("Pin", GoyuAddressNUM);
            json.put("CmortFlag", CmortFlag);
            json.put("TradeSeqFlag", TradeSeqFlag);
            json.put("AbsCls", AbsCls);
            json.put("RgsMttrSmry", RgsMttrSmry);

            System.out.println("Request Payload: " + json.toJSONString());

            // API 호출
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("API-KEY", tc.apiKey)
                    .addHeader("ENC-KEY", aesCipherKey)
                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toJSONString())).build();

            Response response = client.newCall(request).execute();
            String responseStr = response.body().string();
//            System.out.println("responseStr: " + responseStr);
            // JSON 파싱
            org.json.JSONObject responseJson = new org.json.JSONObject(responseStr);

            // "XmlData" 섹션 가져오기
//        Object xmlDataObject = responseJson.opt("XmlData");
//        if (xmlDataObject instanceof String && !((String) xmlDataObject).isEmpty()) {
//            // XML 데이터를 JSON 객체로 변환
//            org.json.JSONObject jsonData = XML.toJSONObject((String) xmlDataObject);

            // 트랜잭션 키 가져오기
//            Object transactionKeyObject = responseJson.opt("ApiTxKey");
//            org.json.JSONObject transactionKey = transactionKeyObject instanceof org.json.JSONObject
//                    ? (org.json.JSONObject) transactionKeyObject
//                    : null;
            System.out.println("responseJson : " + responseJson);
            // "PdfData" 섹션 가져오기 (PdfData의 길이가 너무 길어 String 선언 없이 저장)
            String pdfBase64 = responseJson.optString("PdfData", null);

            if (pdfBase64 == null) {
                result.message = "pdf데이터가 존재하지 않습니다.";
            } else {
                try {
                    if (pdfBase64 == null || pdfBase64.isEmpty()) {
                        System.out.println("PDF 데이터가 존재하지 않습니다.");
                        responseJson.put("Message", "PDF 데이터가 존재하지 않습니다.");
                    }
                    // Base64 디코딩하여 PDF 파일 저장
                    byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);

                    String osName = System.getProperty("os.name").toLowerCase();

                    String uploadDir;
                    String outputFilePath;
                    String testFilePath = "";
                    String saveFileNM = "";

                    if (osName.contains("win")) {
                        // Windows 환경
                        uploadDir = "c:\\temp\\registerFiles\\";
                    } else if (osName.contains("android")) {
                        // Android 환경
                        String userHome = System.getProperty("user.home");
                        uploadDir = userHome + "/registerFiles/";
                    } else if (osName.contains("mac") || osName.contains("ios")) {
                        // macOS 또는 iOS 환경
                        String userHome = System.getProperty("user.home");
                        uploadDir = userHome + "/registerFiles/";
                    } else {
                        throw new UnsupportedOperationException("지원되지 않는 운영체제입니다: " + osName);
                    }
                    // 디렉토리 생성
                    File directory = new File(uploadDir);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    // 파일 이름 db 저장 및 생성 id 값 가져와 1.name.pdf 형식으로 서버에 저장
                    // "구"가 포함된 패턴 찾기
                    Pattern pattern = Pattern.compile(".*?구");
                    Matcher matcher = pattern.matcher(address);
                    // pdfFile table에 저장되는 이름(서버에는 id 포함된 saveFileNM 저장)
                    String pdfMidNM = matcher.find() ? matcher.group() + "_등기부등본.pdf" : "조회_등기부등본.pdf";
                    TB_PDFSEQ pdfRecord = new TB_PDFSEQ();
                    pdfRecord.setPdfFilename(pdfMidNM);
                    TB_PDFSEQ savedRecord = pdfseqRepository.save(pdfRecord);
                    saveFileNM = savedRecord.getSeq() + "." + user.getUsername()+ "_" + pdfMidNM;
                    outputFilePath = uploadDir + saveFileNM;

                    try (OutputStream os = new FileOutputStream(outputFilePath)) {
                        os.write(pdfBytes);
                        System.out.println("PDF 파일이 성공적으로 저장되었습니다: " + outputFilePath);
                    }
                    result.message = "PDF 파일이 성공적으로 저장되었습니다: " + outputFilePath;

                    // pdf 파일 데이터 파싱 api 호출
                    File pdfFile = new File(outputFilePath);
                    System.out.println("pdfFile : " + pdfFile);
                    Map<String, Object> pdfParsingMap = ltsaCont.uploadPDF(pdfFile);
                    System.out.println("parsingDATA : " + pdfParsingMap);

                    // 부동산 등기부등본 기본 데이터(Register) 파싱 로직
                    Map<String, Object> RegisterMap = (Map<String, Object>) pdfParsingMap.get("RegisterMap");
                    Map<String, Object> REALAOWNMap = (Map<String, Object>) pdfParsingMap.get("REALAOWNMap");
                    Map<String, Object> REALBOWNMap = (Map<String, Object>) pdfParsingMap.get("REALBOWNMap");
                    List<Map<String, Object>> REALAOWNDATA = (List<Map<String, Object>>) REALAOWNMap.get("REALAOWNDATA");
                    List<Map<String, Object>> REALBOWNDATA = (List<Map<String, Object>>) REALBOWNMap.get("REALBOWNDATA");
                    //담보
                    Map<String, Object> RegisterDataGMap = (Map<String, Object>) pdfParsingMap.get("RegisterDataGMap");
                    List<Map<String, Object>> RegisterDataGItemsList = (List<Map<String, Object>>) pdfParsingMap.get("RegisterDataGItemsList");
                    // 전세
                    Map<String, Object> RegisterDataHMap = (Map<String, Object>) pdfParsingMap.get("RegisterDataHMap");
                    List<Map<String, Object>> RegisterDataHItemsList = (List<Map<String, Object>>) pdfParsingMap.get("RegisterDataHItemsList");
                    // 매매
                    List<Map<String, Object>> TradeAmount = (List<Map<String, Object>>) pdfParsingMap.get("TradeAmount");

                    // 부동산 등기부등본 주요 등기사항 요약(Summary) 파싱 로직
                    Map<String, Object> SummaryData = new HashMap<>();
                    List<Map<String, Object>> SummaryDataEMap = (List<Map<String, Object>>) pdfParsingMap.get("SummaryDataEMap");
                    List<Map<String, Object>> SummaryDataKMap = (List<Map<String, Object>>) pdfParsingMap.get("SummaryDataKMap");
                    List<Map<String, Object>> SummaryDataAMap = (List<Map<String, Object>>) pdfParsingMap.get("SummaryDataAMap");

                    // 구축물
                    Map<String, Object> gubunDataAMap = (Map<String, Object>) pdfParsingMap.get("gubunData");
                    String archtec = tilkoParsing.assortArchitec(String.valueOf(gubunDataAMap.get("buildingDetails")));

                    // 파싱후 REALINFO 테이블에 우선 저장 후 id 값 가져오기
                    TB_REALINFO tbRealinfo = new TB_REALINFO();
                    tbRealinfo.setUserId(user.getUsername());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    String formattedDate = today.format(formatter);
                    tbRealinfo.setRegDate(formattedDate);
                    tbRealinfo.setReqDate(formattedDate);
                    tbRealinfo.setRealAdd(address);
                    Map<String, String> parsedResult = tilkoParsing.parseAddress(address);
                    tbRealinfo.setResido(parsedResult.get("RESIDO"));
                    tbRealinfo.setReguGun(parsedResult.get("REGUGUN"));
                    // REALAOWNDATA의 마지막 요소에서 "RgsAimCont" 가져와서 저장
                    if (REALAOWNDATA != null && !REALAOWNDATA.isEmpty()) {
                        Map<String, Object> lastGabguDATA = REALAOWNDATA.get(REALAOWNDATA.size() - 1);
                        tbRealinfo.setRemok((String) lastGabguDATA.get("RgsAimCont"));
                        tbRealinfo.setRewon((String) lastGabguDATA.get("RgsCaus"));
                    } else {
                        tbRealinfo.setRemok("");
                        tbRealinfo.setRewon("");
                    }
                    tbRealinfo.setRewonDate(formattedDate);
                    tbRealinfo.setRejimok("");
                    tbRealinfo.setReArea(null);
                    tbRealinfo.setReAmount(null);
                    tbRealinfo.setReSeq(null);
                    // REALBOWNDATA의 마지막 요소에서 "RgsCaus" 가져와서 저장
                    // 채권최고액 파싱
                    if (REALBOWNDATA != null && !REALBOWNDATA.isEmpty()) {
                        Map<String, Object> lasteulguDATA = REALBOWNDATA.get(REALBOWNDATA.size() - 1);
                        String rgsCausValue = (String) lasteulguDATA.get("NomprsAndEtc");
                        String amount = tilkoParsing.parseAmount(rgsCausValue);
                        if (amount != null && !amount.isEmpty()) {
                            try {
                                // 문자열을 Float로 변환
                                tbRealinfo.setReMaxAmt(Float.parseFloat(amount));
                            } catch (NumberFormatException e) {
                                // 변환 실패 시 처리
                                tbRealinfo.setReMaxAmt(null);
                                e.printStackTrace(); // 예외 로그 출력
                            }
                        } else {
                            tbRealinfo.setReMaxAmt(null);
                        }
                    } else {
                        tbRealinfo.setReMaxAmt(null);
                    }

                    // 점수계산 분류 데이터 불러오기
                    List<Map<String, Object>> comcode = tilkoService.getComcode();
                    // 차감 최저점수 불러오기
                    Map<String, Object> lessScore = tilkoService.getLessScore();
                    // 점수계산
                    Map<String, Object> resultScore = tilkoParsing.calScore(SummaryDataEMap,
                            comcode,
                            Integer.parseInt(lessScore.get("Value").toString()) // 안전한 변환 적용
                    );
                    tbRealinfo.setRealScore((Integer) resultScore.get("REALSCORE"));
                    tbRealinfo.setRealPoint(1);
                    tbRealinfo.setRealLastDate(formattedDate);
                    tbRealinfo.setRealGubun(archtec);
                    tbRealinfo.setPdfFilename(saveFileNM);
                    System.out.println("tbRealinfo : " + tbRealinfo);
                    // REALINFO 저장
                    TB_REALINFO saveinfo = realinfoRepository.save(tbRealinfo);
                    int REALID;
                    if (saveinfo != null && saveinfo.getRealId() != 0) {
                        REALID = saveinfo.getRealId();
                    } else {
                        throw new RuntimeException("REALINFO 저장 실패");
                    }
                    // searchinfo 테이블에 조회기록 저장
                    tilkoService.saveSearchInfo(user.getUsername(), REALID);
                    // RealSummaryData 저장
                    SummaryData.put("REALID", REALID);
                    SummaryData.put("UniqueNo", GoyuNUM);
                    SummaryData.put("Gubun", archtec);  // 건축물 구분
                    SummaryData.put("Address", address);
                    SummaryData.put("PrintDate", formattedDate);
                    tilkoService.saveSummary(SummaryData);

                    RegisterMap.put("REALID", REALID);
                    RegisterMap.put("PinNo", GoyuNUM);
                    RegisterMap.put("WksbiAddress", address);
                    RegisterMap.put("Address", address);
                    tilkoService.saveTilkoXML(RegisterMap);
                    for (Map<String, Object> item : REALAOWNDATA){
                        item.put("REALID", REALID);
                        tilkoService.saveRegisterDataK(item);
                    }
                    for (Map<String, Object> item : REALBOWNDATA){
                        item.put("REALID", REALID);
                        tilkoService.savedataE(item);
                    }
                    for (Map<String, Object> item : TradeAmount){
                        item.put("REALID", REALID);
                        tilkoService.saveTradeAmount(item);
                    }
                    for (Map<String, Object> item : RegisterDataGItemsList){
                        item.put("REALID", REALID);
                        tilkoService.saveRegisterDataG(item);
                    }
                    for (Map<String, Object> item : RegisterDataHItemsList){
                        item.put("REALID", REALID);
                        tilkoService.savedataH(item);
                    }
                    for (Map<String, Object> item : SummaryDataEMap){
                        item.put("REALID", REALID);
                        tilkoService.saveSummaryDataE(item);
                    }
                    for (Map<String, Object> item : SummaryDataKMap){
                        item.put("REALID", REALID);
                        tilkoService.saveSummaryDataK(item);
                    }
                    for (Map<String, Object> item : SummaryDataAMap){
                        item.put("REALID", REALID);
                        tilkoService.saveSummaryDataA(item);
                    }
                    // 카드 출력시 필요 데이터

                    resultMap.put("REALSCORE", resultScore.get("REALSCORE"));
                    resultMap.put("GRADE", resultScore.get("GRADE"));
                    resultMap.put("COMMENT", resultScore.get("COMMENT"));
                    resultMap.put("REGASNAME", resultScore.get("REGASNAME"));
                    resultMap.put("ADDRESS", address);
                    resultMap.put("PDFFILENAME", saveFileNM);
                    result.data = resultMap;
                    result.message = "등기부등본 정상조회";
                } catch (IllegalArgumentException e) {
                    result.message = "오류발생";
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // RSA 공개키(Public Key) 조회 함수
    public String getPublicKey() throws IOException, ParseException {
        OkHttpClient client		= new OkHttpClient();

        Request request			= new Request.Builder()
                .url(apiHost + "/api/Auth/GetPublicKey?APIkey=" + apiKey)
                .header("Content-Type", "application/json").build();

        Response response		= client.newCall(request).execute();
        String responseStr		= response.body().string();

        JSONObject jsonObject  = new JSONObject(responseStr);

        String rsaPublicKey = jsonObject.getString("PublicKey");

        return rsaPublicKey;
    }

    // AES -> RSA 암호화 함수
    public static String rsaEncrypt(byte[] aesKey, String rsaPublicKey) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String encryptedData				= null;

        KeyFactory keyFactory				= KeyFactory.getInstance("RSA");
        byte[] keyBytes						= Base64.getDecoder().decode(rsaPublicKey.getBytes("UTF-8"));
        X509EncodedKeySpec spec				= new X509EncodedKeySpec(keyBytes);
        PublicKey fileGeneratedPublicKey	= keyFactory.generatePublic(spec);
        RSAPublicKey key					= (RSAPublicKey)(fileGeneratedPublicKey);

        // 만들어진 공개키객체를 기반으로 암호화모드로 설정하는 과정
        Cipher cipher						= Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        // 평문을 암호화하는 과정
        byte[] byteEncryptedData			= cipher.doFinal(aesKey);

        // Base64로 인코딩
        encryptedData						= new String(Base64.getEncoder().encodeToString(byteEncryptedData));

        return encryptedData;
    }

    // 데이터 AES 암호화 함수
    public String aesEncrypt(String plainText, byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher cipher						= Cipher.getInstance("AES/CBC/PKCS5Padding");	// JAVA의 PKCS5Padding은 PKCS7Padding과 호환
        SecretKeySpec keySpec				= new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec				= new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] byteEncryptedData			= cipher.doFinal(plainText.getBytes("UTF-8"));

        // Base64로 인코딩
        String encryptedData				= new String(Base64.getEncoder().encodeToString(byteEncryptedData));

        return encryptedData;
    }

    // 권리분석 api 통신
    public void searchAnalyzeData(String TRKey, int realMaxNum) {
        TilkoController tc = new TilkoController();
        try {
            //RSA Public Key 조회
            String rsaPublicKey = getPublicKey();
            // AES Secret Key 및 IV 생성
            // AES Secret Key 및 IV 생성
            byte[] aesKey = new byte[16];
            new Random().nextBytes(aesKey);

            byte[] aesIv = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

            // AES Key를 RSA Public Key로 암호화
            String aesCipherKey = rsaEncrypt(aesKey, rsaPublicKey);

            // api 엔드포인트
            String url = apiHost + "api/v2.0/IrosArchive/Analyze";

            // API 요청 파라미터 설정
            JSONObject json = new JSONObject();
            json.put("TransactionKey", TRKey);

            System.out.println("Request Payload: " + json);

            // API 호출
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("API-KEY", apiKey)
                    .addHeader("ENC-KEY", aesCipherKey)
                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString())).build();

            Response response = client.newCall(request).execute();
            String responseStr = response.body().string();
            System.out.println("responseStr: " + responseStr);
            // JSON 파싱
            org.json.JSONObject responseJson = new org.json.JSONObject(responseStr);
            // "Result" 키 접근
            org.json.JSONObject resultObject = responseJson.optJSONObject("Result");

            if (resultObject != null) {
                // "Rights" 키 접근
                org.json.JSONArray rightsArray = resultObject.optJSONArray("Rights");

                // "Seniority" 키 접근
                org.json.JSONArray seniorityArray = resultObject.optJSONArray("Seniority");
                if (rightsArray != null) {
                    for (int i = 0; i < rightsArray.length(); i++) {
                        org.json.JSONObject rightsItem = rightsArray.getJSONObject(i);

                        // 개별 항목 접근
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("RankNo", rightsItem.optString("RankNo", null));
                        dataMap.put("Gubun", rightsItem.optString("Gubun", null));
                        dataMap.put("TargetOwner", rightsItem.optString("TargetOwner", null));
                        dataMap.put("Information", rightsItem.optString("Information", null));
                        dataMap.put("OriginalText", rightsItem.optString("OriginalText", null));
                        dataMap.put("REALID", realMaxNum);

                        try {
                            tilkoService.saveRights(dataMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("Rights Array is null or empty");
                }
                if (seniorityArray != null) {
                    for (int i = 0; i < seniorityArray.length(); i++) {
                        org.json.JSONObject seniorityItem = seniorityArray.getJSONObject(i);

                        // 개별 항목 접근
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("RankNo", seniorityItem.optString("RankNo", null));
                        dataMap.put("Gubun", seniorityItem.optString("Gubun", null));
                        dataMap.put("TargetOwner", seniorityItem.optString("TargetOwner", null));
                        dataMap.put("Amount", seniorityItem.optString("Amount", null));
                        dataMap.put("CurCode", seniorityItem.optString("CurCode", null));
                        dataMap.put("Warning", seniorityItem.optString("Warning", null));
                        dataMap.put("OriginalText", seniorityItem.optString("OriginalText", null));
                        dataMap.put("REALID", realMaxNum);

                        try {
                            tilkoService.saveSeniority(dataMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("Seniority Array is null or empty");
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    // pdf파일 다운로드(test)
    @GetMapping("/downloadPDF2")
    public AjaxResult downloadPDF2(@RequestParam(value = "GoyuNUM")String GoyuNUM,
                                  @RequestParam(value = "address")String address,
                                  Authentication authentication) throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        TilkoController tc = new TilkoController();
        LTSAController ltsaCont = new LTSAController();
        AjaxResult result = new AjaxResult();
        User user = (User)authentication.getPrincipal();
        LocalDate today = LocalDate.now();

        String irosID = "aarmani";
        String irosPWD = "jky@6400";
        String irosNUM1 = "O3275071";
        String irosNUM2 = "3112";
        String irosNUM3 = "jky6400";
        // 등기물건 고유번호(GoyuNUM)
        String GoyuAddressNUM = GoyuNUM;

        // 기타 데이터 (공백일경우 기본값 IsSummary제외 "N")
        String CmortFlag = "N";
        String TradeSeqFlag = "N";
        String AbsCls = "11";
        String RgsMttrSmry = "1"; // 유효사항만 포함 여부

        // RSA Public Key 조회
        String rsaPublicKey = tc.getPublicKey();
        System.out.println("rsaPublicKey: " + rsaPublicKey);


        // AES Secret Key 및 IV 생성
        byte[] aesKey = new byte[16];
        new Random().nextBytes(aesKey);

        byte[] aesIv = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        try {
            // AES Key를 RSA Public Key로 암호화
            String aesCipherKey = rsaEncrypt(aesKey, rsaPublicKey);
            System.out.println("aesCipherKey: " + aesCipherKey);


            // API URL 설정
            // pdf data + Txkey(트랜잭션 key)
            String url = tc.apiHost + "api/v2.0/Iros2IdLogin/RealtyRegistry";


            // API 요청 파라미터 설정
            org.json.simple.JSONObject json = new org.json.simple.JSONObject();

            org.json.simple.JSONObject auth = new org.json.simple.JSONObject();
            auth.put("UserId", tc.aesEncrypt(irosID, aesKey, aesIv));
            auth.put("UserPassword", tc.aesEncrypt(irosPWD, aesKey, aesIv));

            json.put("Auth", auth);
            json.put("EmoneyNo1", tc.aesEncrypt(irosNUM1, aesKey, aesIv));
            json.put("EmoneyNo2", tc.aesEncrypt(irosNUM2, aesKey, aesIv));
            json.put("EmoneyPwd", tc.aesEncrypt(irosNUM3, aesKey, aesIv));
            json.put("Pin", GoyuAddressNUM);
            json.put("CmortFlag", CmortFlag);
            json.put("TradeSeqFlag", TradeSeqFlag);
            json.put("AbsCls", AbsCls);
            json.put("RgsMttrSmry", RgsMttrSmry);

            System.out.println("Request Payload: " + json.toJSONString());

            // API 호출
//            OkHttpClient client = new OkHttpClient();
//
//            Request request = new Request.Builder()
//                    .url(url)
//                    .addHeader("API-KEY", tc.apiKey)
//                    .addHeader("ENC-KEY", aesCipherKey)
//                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toJSONString())).build();
//
//            Response response = client.newCall(request).execute();
//            String responseStr = response.body().string();
////            System.out.println("responseStr: " + responseStr);
//            // JSON 파싱
//            org.json.JSONObject responseJson = new org.json.JSONObject(responseStr);
//
//            // "XmlData" 섹션 가져오기
////        Object xmlDataObject = responseJson.opt("XmlData");
////        if (xmlDataObject instanceof String && !((String) xmlDataObject).isEmpty()) {
////            // XML 데이터를 JSON 객체로 변환
////            org.json.JSONObject jsonData = XML.toJSONObject((String) xmlDataObject);
//
//            // 트랜잭션 키 가져오기
//            Object transactionKeyObject = responseJson.opt("ApiTxKey");
//            org.json.JSONObject transactionKey = transactionKeyObject instanceof org.json.JSONObject
//                    ? (org.json.JSONObject) transactionKeyObject
//                    : null;
//
//
//
//            // "PdfData" 섹션 가져오기 (PdfData의 길이가 너무 길어 String 선언 없이 저장)
//            String pdfBase64 = responseJson.optString("PdfData", null);

//            if (pdfBase64 == null) {
//                result.message = "pdf데이터가 존재하지 않습니다.";
//            } else {
//                try {
//                    if (pdfBase64 == null || pdfBase64.isEmpty()) {
//                        System.out.println("PDF 데이터가 존재하지 않습니다.");
//                        responseJson.put("Message", "PDF 데이터가 존재하지 않습니다.");
//                    }
//                    // Base64 디코딩하여 PDF 파일 저장
//                    byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
//
//                    String osName = System.getProperty("os.name").toLowerCase();
//
//                    String uploadDir;
//                    String outputFilePath;
                    String testFilePath = "c:\\temp\\registerFiles\\2.서울특별시 금천구_등기부등본.pdf";
//                    String saveFileNM = "";
//
//                    if (osName.contains("win")) {
//                        // Windows 환경
//                        uploadDir = "c:\\temp\\registerFiles\\";
//                    } else if (osName.contains("android")) {
//                        // Android 환경
//                        String userHome = System.getProperty("user.home");
//                        uploadDir = userHome + "/registerFiles/";
//                    } else if (osName.contains("mac") || osName.contains("ios")) {
//                        // macOS 또는 iOS 환경
//                        String userHome = System.getProperty("user.home");
//                        uploadDir = userHome + "/registerFiles/";
//                    } else {
//                        throw new UnsupportedOperationException("지원되지 않는 운영체제입니다: " + osName);
//                    }
//                    // 디렉토리 생성
//                    File directory = new File(uploadDir);
//                    if (!directory.exists()) {
//                        directory.mkdirs();
//                    }
//                    // 파일 이름 db 저장 및 생성 id 값 가져와 1.name.pdf 형식으로 서버에 저장
//                    // "구"가 포함된 패턴 찾기
//                    Pattern pattern = Pattern.compile(".*?구");
//                    Matcher matcher = pattern.matcher(address);
                    // pdfFile table에 저장되는 이름(서버에는 id 포함된 saveFileNM 저장)
//                    String pdfMidNM = matcher.find() ? matcher.group() + "_등기부등본.pdf" : "조회_등기부등본.pdf";
//                    TB_PDFSEQ pdfRecord = new TB_PDFSEQ();
//                    pdfRecord.setPdfFilename(pdfMidNM);
//                    TB_PDFSEQ savedRecord = pdfseqRepository.save(pdfRecord);
//                    saveFileNM = savedRecord.getSeq() + "." + pdfMidNM;
//                    outputFilePath = uploadDir + saveFileNM;
//
//                    try (OutputStream os = new FileOutputStream(outputFilePath)) {
//                        os.write(pdfBytes);
//                        System.out.println("PDF 파일이 성공적으로 저장되었습니다: " + outputFilePath);
//                    }
//                    result.message = "PDF 파일이 성공적으로 저장되었습니다: " + outputFilePath;
//
//                    // pdf 파일 데이터 파싱 api 호출
//                    File pdfFile = new File(outputFilePath);
//                    System.out.println("pdfFile : " + pdfFile);
                    File file = new File(testFilePath);

                    Map<String, Object> pdfParsingMap = ltsaCont.uploadPDF(file);
                    System.out.println("parsingDATA : " + pdfParsingMap);
                    String testFileNM = "0.금천구 등기부등본.pdf";

                    // 부동산 등기부등본 기본 데이터(Register) 파싱 로직
                    Map<String, Object> RegisterMap = (Map<String, Object>) pdfParsingMap.get("RegisterMap");
                    Map<String, Object> REALAOWNMap = (Map<String, Object>) pdfParsingMap.get("REALAOWNMap");
                    Map<String, Object> REALBOWNMap = (Map<String, Object>) pdfParsingMap.get("REALBOWNMap");
                    List<Map<String, Object>> REALAOWNDATA = (List<Map<String, Object>>) REALAOWNMap.get("REALAOWNDATA");
                    List<Map<String, Object>> REALBOWNDATA = (List<Map<String, Object>>) REALBOWNMap.get("REALBOWNDATA");
                    //담보
                    Map<String, Object> RegisterDataGMap = (Map<String, Object>) pdfParsingMap.get("RegisterDataGMap");
                    List<Map<String, Object>> RegisterDataGItemsList = (List<Map<String, Object>>) pdfParsingMap.get("RegisterDataGItemsList");
                    // 전세
                    Map<String, Object> RegisterDataHMap = (Map<String, Object>) pdfParsingMap.get("RegisterDataHMap");
                    List<Map<String, Object>> RegisterDataHItemsList = (List<Map<String, Object>>) pdfParsingMap.get("RegisterDataHItemsList");
                    // 매매
                    List<Map<String, Object>> TradeAmount = (List<Map<String, Object>>) pdfParsingMap.get("TradeAmount");

                    // 부동산 등기부등본 주요 등기사항 요약(Summary) 파싱 로직
                    Map<String, Object> SummaryData = new HashMap<>();
                    List<Map<String, Object>> SummaryDataEMap = (List<Map<String, Object>>) pdfParsingMap.get("SummaryDataEMap");
                    List<Map<String, Object>> SummaryDataKMap = (List<Map<String, Object>>) pdfParsingMap.get("SummaryDataKMap");
                    List<Map<String, Object>> SummaryDataAMap = (List<Map<String, Object>>) pdfParsingMap.get("SummaryDataAMap");

                    // 구축물
            Map<String, Object> gubunDataAMap = (Map<String, Object>) pdfParsingMap.get("gubunData");
            String archtec = (gubunDataAMap != null && gubunDataAMap.get("buildingDetails") != null)
                    ? tilkoParsing.assortArchitec(String.valueOf(gubunDataAMap.get("buildingDetails")))
                    : "";


            // 파싱후 REALINFO 테이블에 우선 저장 후 id 값 가져오기
                    TB_REALINFO tbRealinfo = new TB_REALINFO();
                    tbRealinfo.setUserId(user.getUsername());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    String formattedDate = today.format(formatter);
                    tbRealinfo.setRegDate(formattedDate);
                    tbRealinfo.setReqDate(formattedDate);
                    tbRealinfo.setRealAdd(address);
                    Map<String, String> parsedResult = tilkoParsing.parseAddress(address);
                    tbRealinfo.setResido(parsedResult.get("RESIDO"));
                    tbRealinfo.setReguGun(parsedResult.get("REGUGUN"));
                    // REALAOWNDATA의 마지막 요소에서 "RgsAimCont" 가져와서 저장
                    if (REALAOWNDATA != null && !REALAOWNDATA.isEmpty()) {
                        Map<String, Object> lastGabguDATA = REALAOWNDATA.get(REALAOWNDATA.size() - 1);
                        tbRealinfo.setRemok((String) lastGabguDATA.get("RgsAimCont"));
                        tbRealinfo.setRewon((String) lastGabguDATA.get("RgsCaus"));
                    } else {
                        tbRealinfo.setRemok("");
                        tbRealinfo.setRewon("");
                    }
                    tbRealinfo.setRewonDate(formattedDate);
                    tbRealinfo.setRejimok("");
                    tbRealinfo.setReArea(null);
                    tbRealinfo.setReAmount(null);
                    tbRealinfo.setReSeq(null);
                    // REALBOWNDATA의 마지막 요소에서 "RgsCaus" 가져와서 저장
                    // 채권최고액 파싱
                    if (REALBOWNDATA != null && !REALBOWNDATA.isEmpty()) {
                        Map<String, Object> lasteulguDATA = REALBOWNDATA.get(REALBOWNDATA.size() - 1);
                        String rgsCausValue = (String) lasteulguDATA.get("NomprsAndEtc");
                        String amount = tilkoParsing.parseAmount(rgsCausValue);
                        if (amount != null && !amount.isEmpty()) {
                            try {
                                // 문자열을 Float로 변환
                                tbRealinfo.setReMaxAmt(Float.parseFloat(amount));
                            } catch (NumberFormatException e) {
                                // 변환 실패 시 처리
                                tbRealinfo.setReMaxAmt(null);
                                e.printStackTrace(); // 예외 로그 출력
                            }
                        } else {
                            tbRealinfo.setReMaxAmt(null);
                        }
                    } else {
                        tbRealinfo.setReMaxAmt(null);
                    }

                    // 점수계산 분류 데이터 불러오기
                    List<Map<String, Object>> comcode = tilkoService.getComcode();
                    // 차감 최저점수 불러오기
                    Map<String, Object> lessScore = tilkoService.getLessScore();
                    // 점수계산
                    Map<String, Object> resultScore = tilkoParsing.calScore(SummaryDataEMap,
                            comcode,
                            Integer.parseInt(lessScore.get("Value").toString()) // 안전한 변환 적용
                    );
                    tbRealinfo.setRealScore((Integer) resultScore.get("REALSCORE"));
                    tbRealinfo.setRealPoint(1);
                    tbRealinfo.setRealLastDate(formattedDate);
                    tbRealinfo.setRealGubun(archtec);
                    tbRealinfo.setPdfFilename(testFileNM);
                    // REALINFO 저장
                    TB_REALINFO saveinfo = realinfoRepository.save(tbRealinfo);
                    int REALID;
                    if (saveinfo != null && saveinfo.getRealId() != 0) {
                        REALID = saveinfo.getRealId();
                    } else {
                        throw new RuntimeException("REALINFO 저장 실패");
                    }
                    // RealSummaryData 저장
                    SummaryData.put("REALID", REALID);
                    SummaryData.put("UniqueNo", GoyuNUM);
                    SummaryData.put("Gubun", archtec);  // 건축물 구분
                    SummaryData.put("Address", address);
                    SummaryData.put("PrintDate", formattedDate);
                    tilkoService.saveSummary(SummaryData);

                    RegisterMap.put("REALID", REALID);
                    RegisterMap.put("PinNo", GoyuNUM);
                    RegisterMap.put("WksbiAddress", address);
                    RegisterMap.put("Address", address);
                    tilkoService.saveTilkoXML(RegisterMap);
                    for (Map<String, Object> item : REALAOWNDATA){
                        item.put("REALID", REALID);
                        tilkoService.saveRegisterDataK(item);
                    }
                    for (Map<String, Object> item : REALBOWNDATA){
                        item.put("REALID", REALID);
                        tilkoService.savedataE(item);
                    }
                    for (Map<String, Object> item : TradeAmount){
                        item.put("REALID", REALID);
                        tilkoService.saveTradeAmount(item);
                    }
                    for (Map<String, Object> item : RegisterDataGItemsList){
                        item.put("REALID", REALID);
                        tilkoService.saveRegisterDataG(item);
                    }
                    for (Map<String, Object> item : RegisterDataHItemsList){
                        item.put("REALID", REALID);
                        tilkoService.savedataH(item);
                    }
                    for (Map<String, Object> item : SummaryDataEMap){
                        item.put("REALID", REALID);
                        tilkoService.saveSummaryDataE(item);
                    }
                    for (Map<String, Object> item : SummaryDataKMap){
                        item.put("REALID", REALID);
                        tilkoService.saveRegisterDataK(item);
                    }
                    for (Map<String, Object> item : SummaryDataAMap){
                        item.put("REALID", REALID);
                        tilkoService.saveSummaryDataA(item);
                    }
                    // 카드 출력시 필요 데이터
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("REALSCORE", resultScore.get("REALSCORE"));
                    resultMap.put("GRADE", resultScore.get("GRADE"));
                    resultMap.put("COMMENT", resultScore.get("COMMENT"));
                    resultMap.put("ADDRESS", address);
                    result.data = resultMap;
                    result.message = "등기부등본 정상조회";
                } catch (IllegalArgumentException e) {
                    result.message = "오류발생" + e.getMessage();
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }
//            }
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
        return result;
    }
}
