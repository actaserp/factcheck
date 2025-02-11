package mes.app.tilko;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TilkoParsing {
    // 주소 시도 / 구군 파싱 메서드
    public static Map<String, String> parseAddress(String address) {
        Map<String, String> result = new HashMap<>();

        // Patterns for RESIDO and REGUGUN
        Pattern residoPattern = Pattern.compile("^(\\S+(?:\\s특별시|\\s광역시|도|시))");
        Pattern regugunPattern = Pattern.compile("(\\S+시|특별시)\\s(\\S+구|군)");

        // Match RESIDO
        Matcher residoMatcher = residoPattern.matcher(address);
        if (residoMatcher.find()) {
            result.put("RESIDO", residoMatcher.group(1));
        } else {
            result.put("RESIDO", null);
        }

        // Match REGUGUN
        Matcher regugunMatcher = regugunPattern.matcher(address);
        if (regugunMatcher.find()) {
            result.put("REGUGUN", regugunMatcher.group(2));
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
        // 금액 추출 (금이 있거나 없거나 상관없이 매칭)
        Pattern amountPattern = Pattern.compile("(금)?\\d{1,3}(,\\d{3})*(?=원)");
        Matcher matcher = amountPattern.matcher(input);

        if (matcher.find()) {
            return matcher.group().replace(",", ""); // 콤마 제거 후 반환
        }

        return null; // 매칭 실패 시 null 반환
    }

    // 건축물 분류 메서드
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
        if (input.contains("도서관")) {
            return "도서관";
        }
        if (input.contains("연구소")) {
            return "연구소";
        }
        if (input.contains("문화 및 집회시설")) {
            return "문화 및 집회시설";
        }
        if (input.contains("종교시설")) {
            return "종교시설";
        }
        if (input.contains("의료시설")) {
            return "의료시설";
        }
        if (input.contains("노유자시설")) {
            return "노유자시설";
        }
        if (input.contains("공장")) {
            return "공장";
        }
        if (input.contains("창고시설")) {
            return "창고시설";
        }
        if (input.contains("운동시설")) {
            return "운동시설";
        }
        if (input.contains("상가주택")) {
            return "상가주택";
        }
        if (input.contains("오피스텔")) {
            return "오피스텔";
        }
        if (input.contains("복합건축물")) {
            return "복합건축물";
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
        List<String> Deductions = new ArrayList<>();

        // REGNM(등기명칭)을 키로 REGSTAND(분류기준), REGSTAMT(기준금액), REGMAXNUM(최대 차감점수) 저장
        Map<String, Map<String, Object>> regMap = new HashMap<>();
        for (Map<String, Object> code : comcode) {
            String regnm = code.get("REGNM").toString();
            Map<String, Object> values = new HashMap<>();
            values.put("REGSTAND", code.get("REGSTAND").toString());
            values.put("REGSTAMT", code.get("REGSTAMT") != null ? Double.parseDouble(code.get("REGSTAMT").toString()) : 1.0);
            values.put("REGMAXNUM", code.get("REGMAXNUM") != null ? Integer.parseInt(code.get("REGMAXNUM").toString()) * 10000 : 0);
            values.put("REGCOMMENT", code.get("REGCOMMENT").toString());
            values.put("REGASNAME", code.get("REGASNAME").toString());
            values.put("REGNM", code.get("REGNM").toString());
            regMap.put(regnm, values);
        }
        System.out.println("regMap : "  + regMap);

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

            // 코멘트 추가
            String regComment = regData.get("REGCOMMENT").toString();
            comment.add(regComment);
            // 감점사항 추가
            String Deduction = regData.get("REGASNAME").toString();
            Deductions.add(Deduction);

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

        // 등급 설정
        if (finalScore >= 90) {
            Grade = "S";
        } else if (finalScore >= 80) {
            Grade = "A";
        } else if (finalScore >= 70) {
            Grade = "B";
        } else if (finalScore >= 60) {
            Grade = "C";
        } else if (finalScore >= 50) {
            Grade = "D";
        } else if (finalScore >= 40) {
            Grade = "E";
        } else {
            Grade = "F";
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("REALSCORE", finalScore);
        resultMap.put("GRADE", Grade);
        resultMap.put("COMMENT", comment);
        resultMap.put("REGASNAME",Deductions);
        return resultMap;
    }

    // Register 공동담보목록(TB_REGISTERDATAG) 파싱 메서드
    public static Map<String, String> REGDATAGparsing(Map<String, String> RegisterList) {

        Map<String, String> resultMap = new HashMap<>();
        return resultMap;
    }


    // 날짜 추출 메서드 (YYYY/MM/DD 형식)
    public static String extractDate(String text) {
        Pattern pattern = Pattern.compile("(\\d{4})년(\\d{2})월(\\d{2})일");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1) + "/" + matcher.group(2) + "/" + matcher.group(3);
        }
        return null;
    }

    // 시간 추출 메서드 (HHMMSS 형식)
    public static String extractTime(String text) {
        Pattern pattern = Pattern.compile("(\\d{2})시(\\d{2})분(\\d{2})초");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1) + matcher.group(2) + matcher.group(3);
        }
        return null;
    }
    // 관할등기소 추출
    public static String extractJurisdictionOffice(String text) {
        // 개행을 포함한 모든 문자를 매칭하도록 (?s) 추가
        Pattern pattern = Pattern.compile("(?s)\\|\\s*관할등기소\\s*\\|\\s*([\\s\\S]+?)\\s*\\|\\s*([\\s\\S]+?)\\s*\\|");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim() + " " + matcher.group(2).trim();
        } else {
            return "정보 없음";
        }
    }
    // 갑구 소유권에 관한 사항 수집
    public static Map<String, Object> parseGabguTable(List<String> tableData) {
        System.out.println("넘어온 Resister 갑구 파싱 데이터" + tableData);
        List<Map<String, Object>> parsedData = new ArrayList<>();
        List<Map<String, Object>> TradeAmount = new ArrayList<>(); // 내부에서 생성
        Map<String, Object> currentRow = new HashMap<>();
        String lastTradeAmount = null; // 마지막 매매 거래가액 저장

        for (String row : tableData) {
            String[] columns = row.split("\\|");

            // 첫 번째 컬럼이 "순위번호"인 경우 해당 행을 무시
            if (columns.length > 0 && columns[0].trim().equals("순위번호")) {
                continue;
            }

            if (columns.length < 5) continue;  // 유효한 데이터인지 체크

            if (!columns[0].trim().isEmpty()) {
                // 이전 데이터 저장 후 초기화
                if (!currentRow.isEmpty()) {
                    parsedData.add(new HashMap<>(currentRow));
                }
                currentRow.clear();
                currentRow.put("RankNo", columns[0].trim());
                currentRow.put("RgsAimCont", columns[1].trim());
                currentRow.put("Receve", columns[2].trim());
                currentRow.put("RgsCaus", columns[3].trim());
                currentRow.put("NomprsAndEtc", columns[4].trim());

                // "매매" 포함 여부 확인 → TradeAmount에 추가
                if (columns[3].contains("매매")) {
                    Map<String, Object> tradeEntry = new HashMap<>();
                    tradeEntry.put("RgsCaus", columns[3].trim());

                    // 거래가액(Amount) 추출
                    String[] details = columns[4].split(" ");
                    for (String detail : details) {
                        if (detail.startsWith("금")) {  // 금액 패턴 찾기
                            try {
                                String amountStr = detail.replaceAll("[^0-9]", "");  // 숫자만 추출
                                tradeEntry.put("Amount", Long.parseLong(amountStr));
                                lastTradeAmount = amountStr;  // 마지막 거래가액 저장
                                break;
                            } catch (NumberFormatException e) {
                                // 숫자 변환 실패시 무시
                            }
                        }
                    }
                    TradeAmount.add(tradeEntry);
                }
            } else {
                // 순위번호가 없는 경우 → 이전 데이터와 병합
                currentRow.put("NomprsAndEtc", currentRow.get("NomprsAndEtc") + " " + columns[4].trim());
            }
        }

        // 마지막 데이터 저장
        if (!currentRow.isEmpty()) {
            parsedData.add(currentRow);
        }

        // 마지막 거래가액 설정
        if (!TradeAmount.isEmpty() && lastTradeAmount != null) {
            Map<String, Object> lastTradeEntry = TradeAmount.get(TradeAmount.size() - 1);
            lastTradeEntry.put("Amount", Long.parseLong(lastTradeAmount));
        }

        // 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("parsedData", parsedData);
        result.put("TradeAmount", TradeAmount);
        System.out.println("Resister 갑구 파싱 데이터" + result);

        return result;
    }

    // 을구 데이터 수집
    public static Map<String, Object> parseeulguTable(List<String> tableData) {
        System.out.println("넘어온 Resister 을구 파싱 데이터: " + tableData);
        List<Map<String, Object>> parsedData = new ArrayList<>();
        List<Map<String, Object>> collateralData = new ArrayList<>(); // "담보" 데이터 저장
        List<Map<String, Object>> leaseData = new ArrayList<>(); // "전세" 데이터 저장
        Map<String, Object> currentRow = new HashMap<>();

        for (String row : tableData) {
            String[] columns = row.split("\\|");

            // 순위번호가 없는 행 또는 "순위번호" 헤더인 경우 건너뜁니다.
            if (columns.length < 5 || columns[0].trim().isEmpty() || columns[0].trim().equals("순위번호")) {
                continue;
            }

            // 순위번호가 있는 새로운 데이터 시작 시
            if (!columns[0].trim().isEmpty()) {
                // 이전 데이터 저장 후 초기화
                if (!currentRow.isEmpty()) {
                    parsedData.add(new HashMap<>(currentRow));
                }
                currentRow.clear();

                currentRow.put("RankNo", columns[0].trim());
                currentRow.put("RgsAimCont", columns[1].trim());
                currentRow.put("Receve", columns[2].trim());
                currentRow.put("RgsCaus", columns[3].trim());
                currentRow.put("NomprsAndEtc", columns[4].trim());

                // "담보" 데이터 수집
                if (columns[3].contains("담보")) {
                    Map<String, Object> collateralEntry = new HashMap<>();
                    collateralEntry.put("RankNo", columns[0].trim());
                    collateralEntry.put("CrtResn", ""); // 추가 이유는 상위 로직에서 보완
                    collateralEntry.put("DstInfo", ""); // 목적 정보 필요 시 상위 로직에서 채움
                    collateralEntry.put("SeqNo", "1");
                    collateralEntry.put("EstateRightDisplay", columns[4].trim());
                    collateralData.add(collateralEntry);
                }

                // "전세" 데이터 수집
                if (columns[3].contains("전세")) {
                    Map<String, Object> leaseEntry = new HashMap<>();
                    leaseEntry.put("RankNo", columns[0].trim());
                    leaseEntry.put("CrtResn", columns[3].trim());
                    leaseEntry.put("DstInfo", "");
                    leaseEntry.put("SeqNo", "1");
                    leaseEntry.put("EstateRightDisplay", columns[4].trim());
                    leaseData.add(leaseEntry);
                }
            } else {
                // 순위번호가 없는 경우 → 이전 데이터와 병합
                currentRow.put("NomprsAndEtc", currentRow.get("NomprsAndEtc") + " " + columns[4].trim());
            }
        }

        // 마지막 데이터 저장
        if (!currentRow.isEmpty()) {
            parsedData.add(currentRow);
        }

        // 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("parsedData", parsedData);
        result.put("collateralData", collateralData);
        result.put("leaseData", leaseData);
        System.out.println("Resister 을구 파싱 결과: " + result);
        return result;
    }


    // 파싱데이터에서 특정 키워드 갑구 을구 의 시작 인덱스 찾는 메서드
    public static int findStartIndex(List<String> tableData, String keyword) {
        for (int i = 0; i < tableData.size(); i++) {
            if (tableData.get(i).contains(keyword)) {
                return i;
            }
        }
        return -1;
    }
    // Summary 데이터 파싱 메서드
    public static Map<String, Object> parseSummaryTable(List<String> tableData, String nomData) {
        List<Map<String, Object>> summaryDataA = new ArrayList<>();
        List<Map<String, Object>> summaryDataK = new ArrayList<>();
        List<Map<String, Object>> summaryDataE = new ArrayList<>();

        boolean isParsingA = false, isParsingK = false, isParsingE = false;
        boolean hasGabguData = false, hasEulguData = false;
        boolean isGabguComplete = false;  // 갑구 데이터 종료 플래그

        // **갑구와 을구 데이터 존재 여부 판단**
        if (!nomData.replaceAll("[|\\s\\n\\t\\r]+", " ").contains("2. 소유지분을 제외한 소유권에 관한 사항 (갑구) - 기록사항 없음")) {
            System.out.println("✅ 갑구 데이터 수집 진행");
            hasGabguData = true;
        } else {
            System.out.println("❌ 갑구 데이터 없음");
            isGabguComplete = true;  // **갑구 데이터가 없으면 즉시 종료 플래그 설정**
        }

        if (!nomData.replaceAll("[|\\s\\n\\t\\r]+", " ").contains("3. (근)저당권 및 전세권 등 ( 을구 ) - 기록사항 없음")) {
            System.out.println("✅ 을구 데이터 수집 진행");
            hasEulguData = true;
        } else {
            System.out.println("❌ 을구 데이터 없음");
        }

        for (String row : tableData) {
            String[] columns = row.split("\\|");
            if (columns.length < 2) continue;

            System.out.println("현재 행 데이터: " + Arrays.toString(columns));

            // **등기명의인 데이터 감지**
            if (columns[0].contains("등기명의인")) {
                System.out.println("🔍 등기명의인 데이터 감지");
                isParsingA = true;
                isParsingK = false;
                isParsingE = false;
                continue;
            }

            // **갑구 데이터 감지 시작**
            if (columns[0].equals("순위번호") && hasGabguData && !isGabguComplete && !isParsingE) {
                System.out.println("🔍 갑구 데이터 감지 시작");
                isParsingA = false;
                isParsingK = true;
                isParsingE = false;
                continue;
            }

            // **갑구 데이터 종료 및 을구 데이터 감지 시작**
            if (columns[0].equals("순위번호") && hasEulguData && isGabguComplete) {
                System.out.println("🔍 을구 데이터 감지 시작");
                isParsingA = false;
                isParsingK = false;
                isParsingE = true;
                continue;
            }

            // **등기명의인 데이터 저장**
            if (isParsingA && columns.length >= 5) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("RegisteredHolder", columns[0].trim());
                entry.put("RegistrationNumber", columns[1].trim());
                entry.put("FinalShare", columns[2].trim());
                entry.put("Address", columns[3].trim());
                entry.put("RankNo", columns[4].trim());
                summaryDataA.add(entry);
            }

            // **갑구 데이터 저장**
            if (hasGabguData && isParsingK && columns.length >= 5) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("RankNo", columns[0].trim());
                entry.put("Purpose", columns[1].trim());
                entry.put("ReceiptInfo", columns[2].trim());
                entry.put("Information", columns[3].trim());
                entry.put("TargetOwner", columns[4].trim());
                summaryDataK.add(entry);
            }

            // **갑구 구간 종료 플래그 설정**
            if (isParsingK && !columns[0].equals("순위번호") && columns[0].matches("\\d+-\\d*")) {
                System.out.println("🚩 갑구 데이터 종료, 을구 데이터 감지 가능");
                isGabguComplete = true;
                isParsingK = false;
                isParsingE = true;
                continue;
            }

            // **을구 데이터 저장**
            if (hasEulguData && isParsingE && columns.length >= 5) {
                    System.out.println("✅ 을구 데이터 저장 진행: " + Arrays.toString(columns));
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("RankNo", columns[0].trim());
                    entry.put("Purpose", columns[1].trim());
                    entry.put("ReceiptInfo", columns[2].trim());
                    entry.put("Information", columns[3].trim());
                    entry.put("TargetOwner", columns[4].trim());
                    summaryDataE.add(entry);
            }
        }

        // **최종 결과 반환**
        Map<String, Object> result = new HashMap<>();
        result.put("summaryDataA", summaryDataA);
        result.put("summaryDataK", summaryDataK);
        result.put("summaryDataE", summaryDataE);
        System.out.println("result : " + result);


        return result;
    }



    // 구축물 파싱
    public static Map<String, Object> extractGubun(List<String> tableData) {
        Map<String, Object> buildingData = new HashMap<>();
        StringBuilder buildingDetails = new StringBuilder();
        boolean collecting = false;

        System.out.println("extractGubun() 호출됨, tableData 크기: " + tableData.size());

        if (tableData.size() > 1) {
            tableData = tableData.subList(1, tableData.size());
        } else {
            return new HashMap<>();
        }

        for (String row : tableData) {
            String[] columns = row.split("\\|");

            if (columns.length < 4) continue; // 최소 4개 필드 존재해야 유효

            // 건물 내역 시작점 확인
            if (!columns[0].trim().isEmpty() && !collecting) {
                collecting = true;
                buildingData.put("seq", columns[0].trim());
                buildingData.put("address", columns[1].trim());
                buildingDetails.append(columns[3].trim());
            }
            // 건물 내역이 이어지는 경우 계속 추가
            else if (collecting) {
                buildingDetails.append(" ").append(columns[3].trim());
            }
        }

        // 최종 병합된 건물 내역 저장
        buildingData.put("buildingDetails", buildingDetails.toString().trim());

        return buildingData;
    }




}
