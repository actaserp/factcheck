package mes.app.tilko;

import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;

public class TilkoController3 {

    String apiHost	= "https://api.tilko.net/";
    String apiKey	= "a2768417999c45978d5cefdc12adf588";


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


    public static void main(String[] args) throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        TilkoController3 tc			= new TilkoController3();


        // RSA Public Key 조회
        String rsaPublicKey		= tc.getPublicKey();
        System.out.println("rsaPublicKey: " + rsaPublicKey);


        // AES Secret Key 및 IV 생성
        byte[] aesKey			= new byte[16];
        new Random().nextBytes(aesKey);

        byte[] aesIv			= new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };


        // AES Key를 RSA Public Key로 암호화
        String aesCipherKey		= rsaEncrypt(rsaPublicKey, aesKey);
        System.out.println("aesCipherKey: " + aesCipherKey);


        // API URL 설정(인터넷 등기소 등기물건 주소검색: https://tilko.net/Help/Api/POST-api-apiVersion-Iros-RISUConfirmSimpleC)
        String url				= tc.apiHost + "api/v1.0/Iros/RISUConfirmSimpleC";


        // API 요청 파라미터 설정
        JSONObject json			= new JSONObject();
        json.put("Address"				, "서울특별시 중구 무교동 11");
        json.put("Sangtae"				, "2");
        json.put("KindClsFlag"			, "0");
        json.put("Region"				, "0");
        json.put("Page"					, "1");


        // API 호출
        OkHttpClient client		= new OkHttpClient();

        Request request			= new Request.Builder()
                .url(url)
                .addHeader("API-KEY"			, tc.apiKey)
                .addHeader("ENC-KEY"			, aesCipherKey)
                .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toJSONString())).build();

        Response response		= client.newCall(request).execute();
        String responseStr		= response.body().string();
        System.out.println("responseStr: " + responseStr);
        }
}
