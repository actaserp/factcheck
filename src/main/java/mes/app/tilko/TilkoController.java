package mes.app.tilko;

import mes.domain.model.AjaxResult;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RestController
@RequestMapping("api/tilko")
public class TilkoController {

    private static final String apiHostSUB = "https://dev.tilko.net";

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
            // 2. AES 키와 IV 생성
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // AES 128비트 키 생성
            SecretKey aesKey = keyGen.generateKey();
            byte[] iv = new byte[16]; // IV는 16바이트
            new java.security.SecureRandom().nextBytes(iv);

            // AES Key를 RSA Public Key로 암호화
            byte[] encryptedAesKey = encryptAesKeyWithRsa(aesKey.getEncoded(), rsaPublicKey);

            // API URL 설정(인터넷 등기소 등기물건 주소검색: https://tilko.net/Help/Api/POST-api-apiVersion-Iros-RISUConfirmSimpleC)
            // 고유번호 조회 엔드포인트
            String url = apiHostSUB + "api/v2.0/irosidlogin/risuconfirmsimplec";

            // API 요청 파라미터 설정
            JSONObject json			= new JSONObject();
            json.put("Address"				, address);
            json.put("Sangtae"				, "2");
            json.put("KindClsFlag"			, "0");
            json.put("Region"				, "0");
            json.put("Page"					, "1");

            // API 호출
            OkHttpClient client		= new OkHttpClient();

            Request request			= new Request.Builder()
                    .url(url)
                    .addHeader("API-KEY"			, tc.apiKey)
                    .addHeader("ENC-KEY"			, Base64.getEncoder().encodeToString(encryptedAesKey))
                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toJSONString())).build();

            Response response		= client.newCall(request).execute();
            String responseStr		= response.body().string();

//            // JSON 파싱
//            JSONObject responseJson = new JSONObject(responseStr);
//
//            // Result 섹션 가져오기
//            JSONObject result = responseJson.getJSONObject("Result");
//            JSONArray resultArray = result.getJSONArray("Result");
//
//            System.out.println("responseStr: " + resultArray);

        } catch (Exception e) {
        e.printStackTrace();
        }
        return result;
    }



    // 등기부등본 xml데이터 api 통신
    @GetMapping("/searchaddress")
    public AjaxResult main(@RequestParam(value = "GoyuNUM")String GoyuNUM) {
        AjaxResult result = new AjaxResult();
        // 인터넷 등기소 정보
        String irosID = "aarmani";
        String irosPWD = "jky@6400";
        String irosNUM1 = "O3275071";
        String irosNUM2 = "3112";
        String irosNUM3 = "min@0727#@!";

        String irosID2 = "1111111";
        String irosPWD2 = "2222222";
        String irosNUM12 = "3333333";
        String irosNUM22 = "4444";
        String irosNUM32 = "5555555";
        // 등기물건 고유번호(GoyuNUM)
        // 기타 데이터 (공백일경우 기본값 IsSummary제외 "N")
        String JoinYn = "";
        String CostsYn = "";
        String DataYn = "";
        String ValidYn = "";
        String IsSummary = "";

        try {
            // 1. RSA 공개키 가져오기
            String rsaPublicKey = getPublicKey();

            // 2. AES 키와 IV 생성
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // AES 128비트 키 생성
            SecretKey aesKey = keyGen.generateKey();
            byte[] iv = new byte[16]; // IV는 16바이트
            new java.security.SecureRandom().nextBytes(iv);

            // 3. AES 키를 RSA 공개키로 암호화
            byte[] encryptedAesKey = encryptAesKeyWithRsa(aesKey.getEncoded(), rsaPublicKey);

            // 4. 요청 데이터 AES로 암호화
            String requestData = "{ \"IrosID\": \"your_id\", \"IrosPwd\": \"your_password\" }";
            byte[] encryptedData = encryptWithAes(requestData, aesKey, iv);

            // 5. POST 요청 보내기
            sendEncryptedRequest(encryptedAesKey, encryptedData, iv);

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

    // AES 키를 RSA 공개키로 암호화
    private static byte[] encryptAesKeyWithRsa(byte[] aesKey, String rsaPublicKey) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(rsaPublicKey);
        PublicKey publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(aesKey);
    }

    // 데이터를 AES로 암호화
    private static byte[] encryptWithAes(String data, SecretKey aesKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        return cipher.doFinal(data.getBytes("UTF-8"));
    }

    // 암호화된 요청 보내기
    private static void sendEncryptedRequest(byte[] encryptedAesKey, byte[] encryptedData, byte[] iv) throws Exception {
        URL url = new URL("https://example.com/api/SomeEndpoint");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // 요청 데이터 생성
        String jsonPayload = String.format("{\"key\": \"%s\", \"iv\": \"%s\", \"data\": \"%s\"}",
                Base64.getEncoder().encodeToString(encryptedAesKey),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(encryptedData));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes("UTF-8"));
        }

        // 응답 확인
        if (conn.getResponseCode() == 200) {
            try (java.util.Scanner scanner = new java.util.Scanner(conn.getInputStream())) {
                System.out.println("응답: " + scanner.useDelimiter("\\A").next());
            }
        } else {
            throw new Exception("POST 요청 실패: " + conn.getResponseCode());
        }
    }
}
