package mes.app.tilko;

import mes.domain.model.AjaxResult;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
            String url = apiHost + "api/v2.0/irosidlogin/risuconfirmsimplec";

            // API 요청 파라미터 설정
            JSONObject json			= new JSONObject();
            json.put("Address"				, address);
            json.put("Sangtae"				, "2");
            json.put("KindClsFlag"			, "0");
            json.put("Region"				, "0");
            json.put("Page"					, "1");

            System.out.println("Request Payload: " + json.toJSONString());

            // API 호출
            OkHttpClient client		= new OkHttpClient();

            Request request			= new Request.Builder()
                    .url(url)
                    .addHeader("API-KEY"			, tc.apiKey)
                    .addHeader("ENC-KEY"			, aesCipherKey)
                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toJSONString())).build();

            Response response		= client.newCall(request).execute();
            String responseStr		= response.body().string();

            // JSON 파싱
            JSONParser parser = new JSONParser();
            JSONObject responseJson = (JSONObject) parser.parse(responseStr);

            // "Result" 섹션 가져오기
            JSONObject JsonResult = (JSONObject) responseJson.get("Result");

            // "Result" 배열 가져오기
            JSONArray resultArray = (JSONArray) JsonResult.get("Result");

            result.data = resultArray;
        } catch (Exception e) {
        e.printStackTrace();
        }
        return result;
    }



    // 등기부등본 xml데이터 api 통신
    @GetMapping("/searchaddress")
    public AjaxResult main(@RequestParam(value = "GoyuNUM")String GoyuNUM) {
        TilkoController tc = new TilkoController();
        AjaxResult result = new AjaxResult();
        // 인터넷 등기소 정보
        String irosID = "aarmani";
        String irosPWD = "jky@6400";
        String irosNUM1 = "O3275071";
        String irosNUM2 = "3112";
        String irosNUM3 = "min@0727#@!";
        // 등기물건 고유번호(GoyuNUM)
        String GoyuAddressNUM = String.valueOf(GoyuNUM).replace("-","");

        // 기타 데이터 (공백일경우 기본값 IsSummary제외 "N")
        String JoinYn = "N";
        String CostsYn = "N";
        String DataYn = "N";
        String ValidYn = "N";
        String IsSummary = "Y";

        try {
            // 1. RSA 공개키 가져오기
            String rsaPublicKey = getPublicKey();

            // 2. AES 키와 IV 생성
//            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
//            keyGen.init(128); // AES 128비트 키 생성
//            SecretKey aesKey = keyGen.generateKey();
//            byte[] iv = new byte[16]; // IV는 16바이트
//            new java.security.SecureRandom().nextBytes(iv);

            // AES Secret Key 및 IV 생성
            byte[] aesKey			= new byte[16];
            new Random().nextBytes(aesKey);

            byte[] iv			= new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

            // 3. AES 키를 RSA 공개키로 암호화
            String encryptedAesKey = rsaEncrypt(aesKey, rsaPublicKey);
//            String encryptedAesKey		= rsaEncrypt(aesKey, rsaPublicKey);

            // 등기부등본 조회 엔드포인트
            String url = apiHost + "api/v2.0/irosidlogin/risuretrieve";

            // 각 필드 개별 암호화
            String encryptedIrosID = Base64.getEncoder().encodeToString(aesEncrypt(irosID, aesKey, iv).getBytes());
            String encryptedIrosPWD = Base64.getEncoder().encodeToString(aesEncrypt(irosPWD, aesKey, iv).getBytes());
            String encryptedIrosNUM1 = Base64.getEncoder().encodeToString(aesEncrypt(irosNUM1, aesKey, iv).getBytes());
            String encryptedIrosNUM2 = Base64.getEncoder().encodeToString(aesEncrypt(irosNUM2, aesKey, iv).getBytes());
            String encryptedIrosNUM3 = Base64.getEncoder().encodeToString(aesEncrypt(irosNUM3, aesKey, iv).getBytes());

            // API 요청 파라미터 설정
            JSONObject json = new JSONObject();
            JSONObject auth = new JSONObject();

            //암호화된 바이너리 데이터를 그대로 전달
//            auth.put("UserId", encryptedIrosID);
//            auth.put("UserPassword", encryptedIrosPWD);
//            json.put("Auth" , auth);
            json.put("Auth.UserId", encryptedIrosID);
            json.put("Auth.UserPassword", encryptedIrosPWD);

            json.put("EmoneyNo1"				, encryptedIrosNUM1);
            json.put("EmoneyNo2"			, encryptedIrosNUM2);
            json.put("EmoneyPwd"				, encryptedIrosNUM3);
            json.put("UniqueNo"					, GoyuAddressNUM);
            json.put("JoinYn"					, JoinYn);
            json.put("CostsYn"					, CostsYn);
            json.put("DataYn"					, DataYn);
            json.put("ValidYn"					, ValidYn);
            json.put("IsSummary"					, IsSummary);

            // JSON을 String 형식으로 변환
            String jsonString = json.toJSONString();  // JSON 문자열 생성

            // JSON 데이터 암호화 확인
            System.out.println("Request Payload: " + jsonString);

            // API 호출
            OkHttpClient client		= new OkHttpClient();

            Request request			= new Request.Builder()
                    .url(url)
                    .addHeader("API-KEY"			, tc.apiKey)
                    .addHeader("ENC-KEY"			, encryptedAesKey)
                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonString)).build();

            Response response		= client.newCall(request).execute();
            String responseStr		= response.body().string();
            System.out.println("responseStr: " + responseStr);

//            // JSON 파싱
//            JSONParser parser = new JSONParser();
//            JSONObject responseJson = (JSONObject) parser.parse(responseStr);
//
//            // "Result" 섹션 가져오기
//            JSONObject JsonResult = (JSONObject) responseJson.get("Result");
//
//            // "Result" 배열 가져오기
//            JSONArray resultArray = (JSONArray) JsonResult.get("Result");
//
//            result.data = resultArray;

        } catch (Exception e) {
            e.printStackTrace();
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

        JSONParser jsonParser	= new JSONParser();
        JSONObject jsonObject	= (JSONObject) jsonParser.parse(responseStr);

        String rsaPublicKey		= (String) jsonObject.get("PublicKey");

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

//    // 데이터를 AES로 암호화
//    private static byte[] encryptWithAes(String data, SecretKey aesKey, byte[] iv) throws Exception {
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        IvParameterSpec ivSpec = new IvParameterSpec(iv);
//        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
//        return cipher.doFinal(data.getBytes("UTF-8"));
//    }
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
