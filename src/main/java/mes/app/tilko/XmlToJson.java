package mes.app.tilko;

import org.json.XML;
import org.json.JSONObject;

public class XmlToJson {
    // XML 데이터를 JSON으로 변환하는 메서드
    public static String convertXmlToJson(String xmlData) {
        try {
            // XML 데이터를 JSON으로 변환
            JSONObject json = XML.toJSONObject(xmlData);
            // JSON을 문자열로 반환
            return json.toString(4); // 4는 들여쓰기(Indentation) 설정
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void xmlToJson(String xmlData) {

        // XML을 JSON으로 변환
        String jsonResult = convertXmlToJson(xmlData);

        // 결과 출력
        if (jsonResult != null) {
            System.out.println("Converted JSON:");
            System.out.println(jsonResult);
        } else {
            System.out.println("XML 변환 실패!");
        }
    }

}
