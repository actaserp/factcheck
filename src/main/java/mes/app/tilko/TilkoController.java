package mes.app.tilko;

import mes.domain.model.AjaxResult;
import okhttp3.*;
import org.json.XML;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@RestController
@RequestMapping("api/tilko")
public class TilkoController {

    private static final String apiHost	= "https://api.tilko.net/";
    private static final String apiKey	= "a2768417999c45978d5cefdc12adf588";
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
            String url = apiHost + "api/v1.0/Iros/RISUConfirmSimpleC";

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
            org.json.JSONObject responseJson = new org.json.JSONObject(responseStr);
            JSONArray resultArray = responseJson.optJSONArray("ResultList");
            // 데이터 설정
            if (resultArray != null && resultArray.length() > 0) {
                result.data = resultArray.toList();
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
    @GetMapping("/searchaddress")
    public AjaxResult searchaddress(@RequestParam(value = "GoyuNUM")String GoyuNUM) throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        TilkoController tc = new TilkoController();
        AjaxResult result = new AjaxResult();
        // 인터넷 등기소 정보
        String irosID = "aarmani";
        String irosPWD = "jky@6400";
        String irosNUM1 = "O3275071";
        String irosNUM2 = "3112";
        String irosNUM3 = "jky6400";
        // 등기물건 고유번호(GoyuNUM)
        String GoyuAddressNUM = String.valueOf(GoyuNUM).replace("-","");
        System.out.println("고유번호 : "+ GoyuAddressNUM);

        // 기타 데이터 (공백일경우 기본값 IsSummary제외 "N")
        String JoinYn = "N";
        String CostsYn = "N";
        String DataYn = "N";
        String ValidYn = "N";
        String IsSummary = "Y";

        // 1. RSA 공개키 가져오기
        String rsaPublicKey = tc.getPublicKey();

        // AES Secret Key 및 IV 생성
        byte[] aesKey			= new byte[16];
        new Random().nextBytes(aesKey);

        byte[] aesIv			= new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        // 3. AES 키를 RSA 공개키로 암호화
        String encryptedAesKey = rsaEncrypt(aesKey, rsaPublicKey);

        // 등기부등본 조회 엔드포인트
        String url = apiHost + "api/v2.0/IrosIdLogin/RISURetrieve";

        // API 요청 파라미터 설정
        JSONObject json			= new JSONObject();

        JSONObject auth			= new JSONObject();
        auth.put("UserId", tc.aesEncrypt(irosID, aesIv, aesKey));
        auth.put("UserPassword", tc.aesEncrypt(irosPWD, aesIv, aesKey));

        json.put("Auth", auth);
        json.put("EmoneyNo1", tc.aesEncrypt(irosNUM1, aesIv, aesKey));
        json.put("EmoneyNo2", tc.aesEncrypt(irosNUM2, aesIv, aesKey));
        json.put("EmoneyPwd", tc.aesEncrypt(irosNUM3, aesIv, aesKey));
        json.put("UniqueNo", GoyuAddressNUM);
        json.put("JoinYn", JoinYn);
        json.put("CostsYn", CostsYn);
        json.put("DataYn", DataYn);
        json.put("ValidYn", ValidYn);
        json.put("IsSummary", IsSummary);

        // API 호출
        OkHttpClient client		= new OkHttpClient();

        Request request			= new Request.Builder()
                .url(url)
                .addHeader("API-KEY"			, tc.apiKey)
                .addHeader("ENC-KEY"			, encryptedAesKey)
                .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString())).build();

        Response response		= client.newCall(request).execute();
        String responseStr		= response.body().string();
        System.out.println("responseStr: " + responseStr);

        // JSON 파싱
        JSONObject responseJson = new JSONObject(responseStr);

        // "XmlData" 섹션 가져오기
        Object xmlDataObject = responseJson.opt("XmlData");
        if (xmlDataObject instanceof String) {
            // XML 데이터를 JSON 객체로 변환
            JSONObject jsonData = XML.toJSONObject((String) xmlDataObject);

            // 트랜잭션 키 가져오기
            Object transactionKeyObject = responseJson.opt("TransactionKey");
            JSONObject transactionKey = transactionKeyObject instanceof JSONObject
                    ? (JSONObject) transactionKeyObject
                    : null;

            // 결과 저장
            result.message = transactionKey != null ? transactionKey.toString() : "TransactionKey 없음";
            result.data = jsonData;
        } else {
            result.data = "데이터 조회 실패";
        }
        return result;
    }

    // pdf파일 다운로드
    @GetMapping("/downloadPDF")
    public AjaxResult downloadPDF(@RequestParam(value = "TRKey")String TRKey,
                                  @RequestParam(value = "address")String address) throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        TilkoController tc = new TilkoController();
        AjaxResult result = new AjaxResult();

        try {
            //RSA Public Key 조회
            String rsaPublicKey = tc.getPublicKey();
            // AES Secret Key 및 IV 생성
            // AES Secret Key 및 IV 생성
            byte[] aesKey = new byte[16];
            new Random().nextBytes(aesKey);

            byte[] aesIv = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

            // AES Key를 RSA Public Key로 암호화
            String aesCipherKey = rsaEncrypt(aesKey, rsaPublicKey);

            // API URL 설정(인터넷 등기소 등기물건 주소검색: https://tilko.net/Help/Api/POST-api-apiVersion-Iros-RISUConfirmSimpleC)
            // 고유번호 조회 엔드포인트
            String url = tc.apiHost + "api/v2.0/irosidlogin/getpdffile";

            // API 요청 파라미터 설정
            JSONObject json = new JSONObject();
            String testKey = "5ec209d6-b4a8-4fd6-ad15-119aeba204b2";
            json.put("TransactionKey", TRKey);
            json.put("IsSummary", "Y");

            System.out.println("Request Payload: " + json.toString());

            // API 호출
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("API-KEY", tc.apiKey)
                    .addHeader("ENC-KEY", aesCipherKey)
                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString())).build();

            Response response = client.newCall(request).execute();
            String responseStr = response.body().string();
            System.out.println("responseStr: " + responseStr);
            // JSON 파싱
            JSONObject responseJson = new JSONObject(responseStr);

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
                    outputFilePath = uploadDir + address + " 등기부등본.pdf";

                    try (OutputStream os = new FileOutputStream(outputFilePath)) {
                        os.write(pdfBytes);
                        System.out.println("PDF 파일이 성공적으로 저장되었습니다: " + outputFilePath);
                    }
                    result.message = "PDF 파일이 성공적으로 저장되었습니다: " + outputFilePath;
                    // 결과에 추가
                } catch (IllegalArgumentException e) {
                    result.message = "";
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            return result;
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
}
