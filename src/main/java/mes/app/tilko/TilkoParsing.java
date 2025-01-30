package mes.app.tilko;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TilkoParsing {
    // 주소 시도 / 구군 파싱 메서드
    public static Map<String, String> parseAddress(String address) {
        Map<String, String> result = new HashMap<>();

        // Patterns for RESIDO and REGUGUN
        Pattern residoPattern = Pattern.compile("\\S+시");
        Pattern regugunPattern = Pattern.compile("시\\s(\\S+구)");

        // Match RESIDO
        Matcher residoMatcher = residoPattern.matcher(address);
        if (residoMatcher.find()) {
            result.put("RESIDO", residoMatcher.group());
        } else {
            result.put("RESIDO", null);
        }

        // Match REGUGUN
        Matcher regugunMatcher = regugunPattern.matcher(address);
        if (regugunMatcher.find()) {
            result.put("REGUGUN", regugunMatcher.group(1));
        } else {
            result.put("REGUGUN", null);
        }

        return result;
    }

    // 등기원인 / 원인일자 파싱 메서드
    public static Map<String, String> parseDateAndRemaining(String input) {
        Map<String, String> result = new HashMap<>();

        // Pattern to match date in YYYY년MM월DD일 format
        Pattern datePattern = Pattern.compile("(\\d{4})년(\\d{1,2})월(\\d{1,2})일");
        Matcher matcher = datePattern.matcher(input);

        if (matcher.find()) {
            String year = matcher.group(1);
            String month = String.format("%02d", Integer.parseInt(matcher.group(2)));
            String day = String.format("%02d", Integer.parseInt(matcher.group(3)));
            result.put("DATE", year + month + day);

            // Extract remaining text after the date
            String remainingText = input.substring(matcher.end()).trim();
            result.put("REMAINING", remainingText);
        } else {
            result.put("DATE", null);
            result.put("REMAINING", input.trim());
        }

        return result;
    }

    // 채권최고액 파싱 메서드
    public static String parseAmount(String input) {
        // Pattern to match monetary amount
        Pattern amountPattern = Pattern.compile("\\b\\d{1,3}(,\\d{3})*(?=원)\\b");
        Matcher matcher = amountPattern.matcher(input);

        if (matcher.find()) {
            return matcher.group().replace(",", ""); // Remove commas for consistent format
        }

        return null; // Return null if no amount is found
    }

    // 구축물별 구분 메서드
    public static String assortArchitec(String input) {
        // 1. 주요 카테고리 분류
        if (input.contains("단독주택")) {
            return checkType(input, new String[]{"단독주택", "다중주택", "다가구주택"}, "단독주택");
        }
        if (input.contains("공동주택")) {
            return checkType(input, new String[]{"아파트", "연립주택", "다세대주택", "기숙사"}, "공동주택");
        }
        if (input.contains("판매시설")) {
            return "판매시설";
        }
        if (input.contains("업무시설")) {
            return "업무시설";
        }
        if (input.contains("숙박시설")) {
            return "숙박시설";
        }
        if (input.contains("근린생활시설")) {
            return "근린생활시설";
        }
        if (input.contains("학교")) {
            return "학교";
        }
        if (input.contains("학원")) {
            return "학원";
        }

        return "분류되지 않음"; // 어떤 카테고리에도 해당하지 않는 경우
    }

    private static String checkType(String input, String[] types, String defaultType) {
        for (String type : types) {
            if (input.contains(type)) {
                return type;
            }
        }
        return defaultType; // 세부 유형이 없으면 기본 카테고리 반환
    }
}
