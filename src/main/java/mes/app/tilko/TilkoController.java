package mes.app.tilko;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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
    String apiHost	= "https://api.tilko.net/";
    String apiKey	= "a2768417999c45978d5cefdc12adf588";

    // 주소 고유번호 조회 api 통신


    // 등기부등본 xml데이터 api 통신

    public static void main(String[] args) {
        try {
            // 1. RSA 공개키 가져오기
            String rsaPublicKey = getPublicKeyFromServer();

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
    }

    // RSA 공개키 가져오기
    private static String getPublicKeyFromServer() throws Exception {
        URL url = new URL("https://example.com/api/Auth/GetPublicKey");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() == 200) {
            try (java.util.Scanner scanner = new java.util.Scanner(conn.getInputStream())) {
                return scanner.useDelimiter("\\A").next();
            }
        } else {
            throw new Exception("RSA 공개키를 가져오는 데 실패했습니다.");
        }
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
