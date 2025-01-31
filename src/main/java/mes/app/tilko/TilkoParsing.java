package mes.app.tilko;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TilkoParsing {
    // 주소 시도 / 구군 파싱 메서드
    public static Map<String, String> parseAddress(String address) {
        Map<String, String> result = new HashMap<>();

        // Patterns for RESIDO and REGUGUN
        Pattern residoPattern = Pattern.compile("(\\S+도\\s*)?\\S+시");
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
    // 카드점수 파싱 메서드 (카드 등급, 점수, 비고 모두 리턴)
    public static Map<String, Object> calScore(List<Map<String, Object>> summaryData,
                                               List<Map<String, Object>> comcode,
                                               Integer lessScore) {
        int finalScore = 100; // 기본 점수
        String Grade = "";
        List<String> comment = new ArrayList<>();

        // REGNM(등기명칭)을 키로 REGSTAND(분류기준), REGSTAMT(기준금액), REGMAXNUM(최대 차감점수) 저장
        Map<String, Map<String, Object>> regMap = new HashMap<>();
        for (Map<String, Object> code : comcode) {
            String regnm = code.get("REGNM").toString();
            Map<String, Object> values = new HashMap<>();
            values.put("REGSTAND", code.get("REGSTAND").toString());
            values.put("REGSTAMT", code.get("REGSTAMT") != null ? Double.parseDouble(code.get("REGSTAMT").toString()) : 1.0);
            values.put("REGMAXNUM", code.get("REGMAXNUM") != null ? Integer.parseInt(code.get("REGMAXNUM").toString()) : 0);
            regMap.put(regnm, values);
        }

        // 정규식 패턴 (채권최고액과 근저당권자 추출)
        Pattern amountPattern = Pattern.compile("채권최고액\\s+금([0-9,]+)원");
        Pattern creditorPattern = Pattern.compile("근저당권자\\s+(.+)");

        // 근저당권자별 마지막 채권최고액 저장
        Map<String, Long> creditorAmounts = new HashMap<>();

        // 등기명칭과 summaryData의 Purpose 비교
        for (Map<String, Object> summary : summaryData) {
            String purpose = summary.get("Purpose").toString();
            if (!regMap.containsKey(purpose)) continue;

            Map<String, Object> regData = regMap.get(purpose);
            String regstand = regData.get("REGSTAND").toString();
            double regStAmt = (double) regData.get("REGSTAMT");
            int regMaxNum = (int) regData.get("REGMAXNUM");

            String information = summary.get("Information").toString();

            // 채권최고액 추출
            Matcher amountMatcher = amountPattern.matcher(information);
            long bondAmount = 0;
            if (amountMatcher.find()) {
                bondAmount = Long.parseLong(amountMatcher.group(1).replace(",", ""));
            }

            // 근저당권자 추출
            Matcher creditorMatcher = creditorPattern.matcher(information);
            String creditor = "";
            if (creditorMatcher.find()) {
                creditor = creditorMatcher.group(1).trim();
            }

            if ("A1".equals(regstand) || ("A3".equals(regstand) && bondAmount > 0)) {
                // A1 또는 A3(채권최고액이 있는 경우) 로직
                creditorAmounts.put(creditor, bondAmount);
            } else if ("A2".equals(regstand) || ("A3".equals(regstand) && bondAmount == 0)) {
                // A2 또는 A3(채권최고액이 없는 경우) 로직
                finalScore -= regMaxNum;
            }
        }

        // A1, A3(A1 방식) 점수 차감 계산
        for (Map.Entry<String, Long> entry : creditorAmounts.entrySet()) {
            long amount = entry.getValue();

            // 안전하게 REGSTAMT 값을 가져오기 (null 체크 포함)
            double regStAmt = regMap.containsKey("근저당권설정") && regMap.get("근저당권설정").get("REGSTAMT") != null
                    ? (double) regMap.get("근저당권설정").get("REGSTAMT")
                    : 1.0; // 기본값 1.0 설정 (안전장치)

            // 채권최고금액 / REGSTAMT 계산 (1을 넘지 않도록 제한)
            double ratio = Math.min(1.0, amount / regStAmt);
            int deduction = (int) (ratio * (int) regMap.get("근저당권설정").get("REGMAXNUM"));
            finalScore -= deduction;
        }

        // 최저 점수 보정
        if (finalScore < lessScore) {
            finalScore = lessScore;
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("REALSCORE", finalScore);
        resultMap.put("GRADE", Grade);
        resultMap.put("COMMENT", comment);
        return resultMap;
    }



}
