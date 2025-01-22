package mes.app.tilko;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TilkoController2 {

    String apiHost	= "https://api.tilko.net/";
    String apiKey	= "4846bd87087041bcb210305ecbbb888b";


    // AES 암호화 함수
    public String aesEncrypt(byte[] key, byte[] iv, String plainText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher cipher						= Cipher.getInstance("AES/CBC/PKCS5Padding");	// JAVA의 PKCS5Padding은 PKCS7Padding과 호환
        SecretKeySpec keySpec				= new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec				= new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] byteEncryptedData			= cipher.doFinal(plainText.getBytes("UTF-8"));

        // Base64로 인코딩
        String encryptedData				= new String(Base64.getEncoder().encodeToString(byteEncryptedData));

        return encryptedData;
    }


    // RSA 암호화 함수
    public static String rsaEncrypt(String rsaPublicKey, byte[] aesKey) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
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


    public static void main(String[] args) throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        TilkoController2 tc = new TilkoController2();
        String irosID = "aarmani";
        String irosPWD = "jky@6400";
        String irosNUM1 = "O3275071";
        String irosNUM2 = "3112";
        String irosNUM3 = "jky6400";
        // 등기물건 고유번호(GoyuNUM)
        String GoyuAddressNUM = "11511996172289";

        // 기타 데이터 (공백일경우 기본값 IsSummary제외 "N")
        String JoinYn = "N";
        String CostsYn = "N";
        String DataYn = "N";
        String ValidYn = "N";
        String IsSummary = "Y";

        // RSA Public Key 조회
        String rsaPublicKey = tc.getPublicKey();
        System.out.println("rsaPublicKey: " + rsaPublicKey);


        // AES Secret Key 및 IV 생성
        byte[] aesKey = new byte[16];
        new Random().nextBytes(aesKey);

        byte[] aesIv = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        // AES Key를 RSA Public Key로 암호화
        String aesCipherKey = rsaEncrypt(rsaPublicKey, aesKey);
        System.out.println("aesCipherKey: " + aesCipherKey);


        // API URL 설정
        String url = tc.apiHost + "api/v2.0/IrosIdLogin/RISURetrieve";


        // API 요청 파라미터 설정
        JSONObject json = new JSONObject();

        JSONObject auth = new JSONObject();
        auth.put("UserId", tc.aesEncrypt(aesKey, aesIv, irosID));
        auth.put("UserPassword", tc.aesEncrypt(aesKey, aesIv, irosPWD));

        json.put("Auth", auth);
        json.put("EmoneyNo1", tc.aesEncrypt(aesKey, aesIv, irosNUM1));
        json.put("EmoneyNo2", tc.aesEncrypt(aesKey, aesIv, irosNUM2));
        json.put("EmoneyPwd", tc.aesEncrypt(aesKey, aesIv, irosNUM3));
        json.put("UniqueNo", GoyuAddressNUM);
        json.put("JoinYn", JoinYn);
        json.put("CostsYn", JoinYn);
        json.put("DataYn", JoinYn);
        json.put("ValidYn", JoinYn);
        json.put("IsSummary", IsSummary);


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
        Object xmlDataObject = responseJson.opt("XmlData");
        if (xmlDataObject instanceof String) {
            // XML 데이터를 JSON 객체로 변환
            org.json.JSONObject jsonData = XML.toJSONObject((String) xmlDataObject);

            // 트랜잭션 키 가져오기
            Object transactionKeyObject = responseJson.opt("TransactionKey");
            org.json.JSONObject transactionKey = transactionKeyObject instanceof org.json.JSONObject
                    ? (org.json.JSONObject) transactionKeyObject
                    : null;

            // 결과 저장
            System.out.println("jsonData: " + jsonData);
        } else {
            System.out.println("데이터 저장 실패");
        }
    }

}
