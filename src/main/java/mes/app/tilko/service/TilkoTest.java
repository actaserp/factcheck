package mes.app.tilko.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import mes.app.tilko.TilkoController;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TilkoTest {

    static String apiHost	= "https://api.tilko.net/";
    static String apiKey	= "a2768417999c45978d5cefdc12adf588";


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
//        TilkoController tc = new TilkoController();
//        Map<String, Object> map = new HashMap<>();
//        try {
//            //RSA Public Key 조회
//            String rsaPublicKey = tc.getPublicKey();
//            // AES Secret Key 및 IV 생성
//            // AES Secret Key 및 IV 생성
//            byte[] aesKey			= new byte[16];
//            new Random().nextBytes(aesKey);
//
//            byte[] aesIv			= new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
//
//            // AES Key를 RSA Public Key로 암호화
//            String aesCipherKey = rsaEncrypt(rsaPublicKey, aesKey);
//
//            // api 엔드포인트
//            String url = apiHost + "api/v2.0/IrosArchive/ParseXml";
//            String TRKey = "f02f448f-d56c-4836-b8c2-744bf1fc0b31";
//            // API 요청 파라미터 설정
//            org.json.JSONObject json			= new org.json.JSONObject();
//            json.put("TransactionKey"				, TRKey);
//
//            System.out.println("Request Payload: " + json);
//
//            // API 호출
//            OkHttpClient client		= new OkHttpClient();
//
//            Request request			= new Request.Builder()
//                    .url(url)
//                    .addHeader("API-KEY"			, apiKey)
//                    .addHeader("ENC-KEY"			, aesCipherKey)
//                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString())).build();
//
//            Response response		= client.newCall(request).execute();
//            String responseStr		= response.body().string();
//            System.out.println("responseStr: " + responseStr);
//            // JSON 파싱
//            org.json.JSONObject responseJson = new org.json.JSONObject(responseStr);
//            JSONArray resultArray = responseJson.optJSONArray("ResultList");
//            // 데이터 설정
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        TilkoController tc = new TilkoController();
        try {
            //RSA Public Key 조회
            String rsaPublicKey = tc.getPublicKey();
            // AES Secret Key 및 IV 생성
            // AES Secret Key 및 IV 생성
            byte[] aesKey = new byte[16];
            new Random().nextBytes(aesKey);

            byte[] aesIv = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

            // AES Key를 RSA Public Key로 암호화
            String aesCipherKey = rsaEncrypt(rsaPublicKey, aesKey);

            // api 엔드포인트
            String url = apiHost + "api/v2.0/IrosArchive/Analyze";
            String TRKey = "f02f448f-d56c-4836-b8c2-744bf1fc0b31";
            int realMaxNum = 999;
            // API 요청 파라미터 설정
            org.json.JSONObject json = new org.json.JSONObject();
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

//            if (resultObject != null) {
//                // "Rights" 키 접근
//                org.json.JSONArray rightsArray = resultObject.optJSONArray("Rights");
//
//                // "Seniority" 키 접근
//                org.json.JSONArray seniorityArray = resultObject.optJSONArray("Seniority");
//                if (rightsArray != null) {
//                    for (int i = 0; i < rightsArray.length(); i++) {
//                        org.json.JSONObject rightsItem = rightsArray.getJSONObject(i);
//
//                        // 개별 항목 접근
//                        Map<String, Object> dataMap = new HashMap<>();
//                        dataMap.put("RankNo", rightsItem.optString("RankNo", null));
//                        dataMap.put("Gubun", rightsItem.optString("Gubun", null));
//                        dataMap.put("TargetOwner", rightsItem.optString("TargetOwner", null));
//                        dataMap.put("Information", rightsItem.optString("Information", null));
//                        dataMap.put("OriginalText", rightsItem.optString("OriginalText", null));
//                        dataMap.put("REALID", realMaxNum);
//
//                        try {
//                            tilkoService.saveRights(dataMap);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                } else {
//                    System.out.println("Rights Array is null or empty");
//                }
//                if (seniorityArray != null) {
//                    for (int i = 0; i < seniorityArray.length(); i++) {
//                        org.json.JSONObject seniorityItem = seniorityArray.getJSONObject(i);
//
//                        // 개별 항목 접근
//                        Map<String, Object> dataMap = new HashMap<>();
//                        dataMap.put("RankNo", seniorityItem.optString("RankNo", null));
//                        dataMap.put("Gubun", seniorityItem.optString("Gubun", null));
//                        dataMap.put("TargetOwner", seniorityItem.optString("TargetOwner", null));
//                        dataMap.put("Amount", seniorityItem.optString("Amount", null));
//                        dataMap.put("CurCode", seniorityItem.optString("CurCode", null));
//                        dataMap.put("Warning", seniorityItem.optString("Warning", null));
//                        dataMap.put("OriginalText", seniorityItem.optString("OriginalText", null));
//                        dataMap.put("REALID", realMaxNum);
//
//                        try {
//                            tilkoService.saveSeniority(dataMap);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                } else {
//                    System.out.println("Seniority Array is null or empty");
//                }
//            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

}