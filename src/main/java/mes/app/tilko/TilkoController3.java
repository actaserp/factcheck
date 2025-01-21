package mes.app.tilko;

import okhttp3.*;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;

public class TilkoController3 {

    String apiHost = "https://api.tilko.net/";
    String apiKey = "a2768417999c45978d5cefdc12adf588";

    // RSA 암호화 함수
    public static String rsaEncrypt(String rsaPublicKey, byte[] aesKey) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String encryptedData = null;

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] keyBytes = Base64.getDecoder().decode(rsaPublicKey.getBytes("UTF-8"));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        PublicKey fileGeneratedPublicKey = keyFactory.generatePublic(spec);
        RSAPublicKey key = (RSAPublicKey) (fileGeneratedPublicKey);

        // 만들어진 공개키객체를 기반으로 암호화모드로 설정하는 과정
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        // 평문을 암호화하는 과정
        byte[] byteEncryptedData = cipher.doFinal(aesKey);

        // Base64로 인코딩
        encryptedData = new String(Base64.getEncoder().encodeToString(byteEncryptedData));

        return encryptedData;
    }


    // RSA 공개키(Public Key) 조회 함수
    public String getPublicKey() throws IOException, ParseException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(apiHost + "/api/Auth/GetPublicKey?APIkey=" + apiKey)
                .header("Content-Type", "application/json").build();

        // API 호출 및 응답 처리
        Response response = client.newCall(request).execute();
        String responseStr = response.body().string();

        // JSON 응답 파싱
        JSONObject jsonObject = new JSONObject(responseStr);

        // "PublicKey" 추출
        String rsaPublicKey = jsonObject.getString("PublicKey");

        return rsaPublicKey;
    }


    public static void main(String[] args) throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        TilkoController3 tc = new TilkoController3();


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

            // API URL 설정(인터넷 등기소 등기물건 주소검색: https://tilko.net/Help/Api/POST-api-apiVersion-Iros-RISUConfirmSimpleC)
            // 고유번호 조회 엔드포인트
            String url = tc.apiHost + "api/v2.0/irosidlogin/getpdffile";

            // API 요청 파라미터 설정
            JSONObject json = new JSONObject();
            String testKey = "9bdd83c6-84a0-45df-abfe-8af3757d81e7";
            json.put("TransactionKey", testKey);
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
            // JSON 파싱
            JSONObject responseJson = new JSONObject(responseStr);

            // "PdfData" 섹션 가져오기 (PdfData의 길이가 너무 길어 String 선언 없이 저장)
            String pdfBase64 = responseJson.optString("PdfData", null);

            if (pdfBase64 == null) {
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
                    outputFilePath = uploadDir + " 등기부등본.pdf";

                    try (OutputStream os = new FileOutputStream(outputFilePath)) {
                        os.write(pdfBytes);
                        System.out.println("PDF 파일이 성공적으로 저장되었습니다: " + outputFilePath);
                    }
                    // 결과에 추가
                } catch (IllegalArgumentException e) {
                } catch (IOException e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
