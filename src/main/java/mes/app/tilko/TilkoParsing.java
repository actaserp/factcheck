package mes.app.tilko;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
        Pattern amountPattern = Pattern.compile("(금)?(\\d{1,3}(,\\d{3})*)(?=원)");
        Matcher matcher = amountPattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(2).replace(",", ""); // 콤마 제거 후 반환
        }

        return null; // 매칭 실패 시 null 반환
    }

    // 건축물 분류 메서드
    public static String assortArchitec(String input) {
        System.out.println("건축물 분류 텍스트 : " + input);

        if (matchesWord(input, "단독주택")) {
            return checkType(input, new String[]{"단독주택", "다중주택", "다가구주택"}, "단독주택");
        }
        if (matchesWord(input, "공동주택")) {
            return checkType(input, new String[]{"아파트", "연립주택", "다세대주택", "기숙사"}, "공동주택");
        }
        if (matchesWord(input, "판매시설")) {
            return "판매시설";
        }
        if (matchesWord(input, "업무시설")) {
            return "업무시설";
        }
        if (matchesWord(input, "숙박시설")) {
            return "숙박시설";
        }
        if (matchesWord(input, "근린생활시설")) {
            return "근린생활시설";
        }
        if (matchesWord(input, "학교")) {
            return "학교";
        }
        if (matchesWord(input, "학원")) {
            return "학원";
        }
        if (matchesWord(input, "도서관")) {
            return "도서관";
        }
        if (matchesWord(input, "연구소")) {
            return "연구소";
        }
        if (matchesWord(input, "문화 및 집회시설")) {
            return "문화 및 집회시설";
        }
        if (matchesWord(input, "종교시설")) {
            return "종교시설";
        }
        if (matchesWord(input, "의료시설")) {
            return "의료시설";
        }
        if (matchesWord(input, "노유자시설")) {
            return "노유자시설";
        }
        if (matchesWord(input, "공장")) {
            return "공장";
        }
        if (matchesWord(input, "창고시설")) {
            return "창고시설";
        }
        if (matchesWord(input, "운동시설")) {
            return "운동시설";
        }
        if (matchesWord(input, "상가주택")) {
            return "상가주택";
        }
        if (matchesWord(input, "오피스텔")) {
            return "오피스텔";
        }
        if (matchesWord(input, "복합건축물")) {
            return "복합건축물";
        }
        if (matchesWord(input, "아파트")) {
            return "아파트";
        }

        return "기타"; // 어떤 카테고리에도 해당하지 않는 경우
    }
    private static String checkType(String input, String[] types, String defaultType) {
        for (String type : types) {
            if (input.contains(type)) {
                return type;
            }
        }
        return defaultType; // 세부 유형이 없으면 기본 카테고리 반환
    }
    // 정규식을 사용하여 정확한 단어 탐지
    private static boolean matchesWord(String input, String word) {
        Pattern pattern = Pattern.compile("\\b" + word + "\\b");
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }
    public static List<Map<String, Object>> collectSummaryData(
            List<Map<String, Object>> summaryData,
            List<Map<String, Object>> comcode) {

        // 결과 리스트 (수집된 데이터 저장)
        List<Map<String, Object>> collectedData = new ArrayList<>();

        // ✅ REGWORD 포함된 데이터 매칭
        for (Map<String, Object> summary : summaryData) {
            String purpose = summary.get("Purpose").toString();
            String rankNo = summary.get("RankNo").toString();
            String amount = summary.getOrDefault("Amount", "").toString(); // 금액 (없을 수도 있음)
            boolean isAmountEmpty = amount.isEmpty();

            // REGWORD 포함 여부 확인 및 REQSEQ 가져오기
            for (Map<String, Object> code : comcode) {
                String regWord = code.get("REGWORD").toString();
                if (purpose.contains(regWord)) {
                    String regSeq = code.get("REGSEQ").toString(); // REQSEQ 가져오기

                    // 기존에 같은 RankNo와 REQSEQ가 있는지 확인
                    Optional<Map<String, Object>> existingData = collectedData.stream()
                            .filter(d -> d.get("RankNo").equals(rankNo) && d.get("REGSEQ").equals(regSeq))
                            .findFirst();

                    if (existingData.isPresent()) {
                        // 기존 데이터 존재 → 금액 덮어씌우기
                        if (!isAmountEmpty) {
                            existingData.get().put("Amount", amount); // 금액 갱신
                        }
                    } else {
                        // 기존 데이터 없음 → 새로운 데이터 추가
                        Map<String, Object> newData = new HashMap<>(summary);
                        newData.put("REGSEQ", regSeq); // REQSEQ 추가
                        collectedData.add(newData);
                    }
                    break; // 첫 번째 매칭만 사용
                }
            }
        }
        return collectedData;
    }


    // ✅ 점수계산 메서드
    public Map<String, Object> calScore(List<Map<String, Object>> summaryData,
                                        List<Map<String, Object>> comcode,
                                        Integer lessScore,
                                        List<Map<String, Object>> gradeInfo) {

        System.out.println("계산에 사용되는 SummaryData : " + summaryData);
        int finalScore = 100; // 기본 점수
        String Grade = "";
        List<String> comment = new ArrayList<>();
        List<String> Deductions = new ArrayList<>();
        List<Map<String, Object>> deductionDetails = new ArrayList<>(); // 차감 정보 저장
        Map<String, Integer> regSeqMap = new HashMap<>(); // 각 데이터의 REGSEQ 저장

        // ✅ 첫 번째 차감 요소 감지 플래그 (전역적으로 유지)
        AtomicBoolean firstDeductionFound = new AtomicBoolean(false);

        // ✅ 정규식 패턴 (모든 금액 추출)
        Pattern amountPattern = Pattern.compile("금([0-9,]+)원");

        // ✅ 각 SummaryData에서 REGWORD가 포함된 경우 REGSEQ 저장
        for (Map<String, Object> summary : summaryData) {
            String purpose = summary.get("Purpose").toString();
            Integer matchedRegSeq = null;

            for (Map<String, Object> code : comcode) {
                String regWord = code.get("REGNM").toString();
                if (purpose.contains(regWord)) { // Purpose에 REGWORD 포함 여부 확인
                    matchedRegSeq = Integer.parseInt(code.get("REGSEQ").toString()); // REGSEQ 매칭
                    break;
                }
            }

            if (matchedRegSeq != null) {
                regSeqMap.put(purpose, matchedRegSeq);
            }
        }

        // ✅ RankNo 기준으로 같은 그룹인 데이터 찾기 (접두사로 그룹화)
        Map<String, List<Map<String, Object>>> groupedByRank = new HashMap<>();

        for (Map<String, Object> summary : summaryData) {
            String rankNo = summary.get("RankNo").toString();

            String mainRankNo = rankNo.split("-")[0]; // "-" 이전 숫자만 추출하여 그룹화

            groupedByRank.putIfAbsent(mainRankNo, new ArrayList<>());
            groupedByRank.get(mainRankNo).add(summary);
        }

        // ✅ 같은 RankNo 그룹 내에서 REGSEQ가 동일하면 각각 차감 적용
        for (String rankNo : groupedByRank.keySet()) {
            List<Map<String, Object>> dataList = groupedByRank.get(rankNo);
            dataList.sort(Comparator.comparing(d -> d.get("RankNo").toString())); // RankNo 정렬

            Map<Integer, Map<String, Object>> regSeqLastEntries = new HashMap<>(); // ✅ REGSEQ별 마지막 데이터 저장

            for (Map<String, Object> entry : dataList) {
                String purpose = entry.get("Purpose").toString();
                if (regSeqMap.containsKey(purpose)) {
                    Integer regSeq = regSeqMap.get(purpose);

                    // ✅ 같은 REGSEQ가 여러 개 있어도 가장 마지막 데이터를 저장
                    regSeqLastEntries.put(regSeq, entry);
                }
            }

            // ✅ 각 REGSEQ별로 차감 로직 적용
            for (Integer regSeq : regSeqLastEntries.keySet()) {
                Map<String, Object> lastEntry = regSeqLastEntries.get(regSeq);
                finalScore = applyDeduction(lastEntry, regSeq, comcode, amountPattern, firstDeductionFound, finalScore, deductionDetails, comment, Deductions);
            }
        }
        // ✅ 모든 차감이 끝난 후 추가 차감
        int deductionCount = deductionDetails.size();
        finalScore -= deductionCount;  // ✅ 추가 차감 적용
        System.out.println("📉 REGNM 개수만큼 추가 1점 차감: " + deductionCount);

        // **최저 점수 보정**
        if (finalScore < lessScore) {
            finalScore = lessScore;
        }

        // **등급 설정**
        for (Map<String, Object> grade : gradeInfo) {
            int maxScore = (Integer) grade.get("GRSCORE01");
            int minScore = (Integer) grade.get("GRSCORE02");

            if (minScore <= finalScore && maxScore >= finalScore) {
                Grade = grade.get("GRID").toString();
                break;
            }
        }

        if (Grade.isEmpty()) {
            Grade = "F";
        }

        // **결과 저장**
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("REALSCORE", finalScore);
        resultMap.put("GRADE", Grade);
        resultMap.put("COMMENT", comment);
        resultMap.put("REGASNAME", Deductions);
        resultMap.put("DEDUCTION_DETAILS", deductionDetails);
        System.out.println("점수 반환 데이터 : " + resultMap);

        return resultMap;
    }

    // ✅ 금액별 점수 차감 로직
    private int applyDeduction(Map<String, Object> summary, Integer regSeq, List<Map<String, Object>> comcode,
                               Pattern amountPattern, AtomicBoolean firstDeductionFound, int finalScore,
                               List<Map<String, Object>> deductionDetails, List<String> comment, List<String> Deductions) {
        for (Map<String, Object> code : comcode) {
            if (Integer.parseInt(code.get("REGSEQ").toString()) == regSeq) {
                String regstand = code.get("REGSTAND").toString();
                String information = summary.get("Information") != null ? summary.get("Information").toString() : "";

                // ✅ 정규식으로 금액 추출
                Matcher amountMatcher = amountPattern.matcher(information);
                long bondAmount = amountMatcher.find() ? Long.parseLong(amountMatcher.group(1).replace(",", "")) : 0;

                // ✅ 첫 번째 차감 데이터인지 확인 후 SENSCORE 감점 적용
                boolean isFirstDeduction = !firstDeductionFound.get() && code.containsKey("SENSCORE");

                if (isFirstDeduction) {
                    finalScore -= 5;
                    System.out.println("🔥 첫 번째 차감 데이터 SENSCORE 존재 → 5점 차감!");
                    firstDeductionFound.set(true);
                }

                // ✅ SUBSCORE + 금액 차감 적용
                int subScore = code.get("SUBSCORE") != null ? Integer.parseInt(code.get("SUBSCORE").toString()) : 0;
                int amountDeduction = calculateDeduction(bondAmount);  // ✅ 금액 차감 적용

                finalScore -= amountDeduction;

                // ✅ A1, A2, A3 점수 차감 로직 추가(금액이 없는 점수기준(복합기준))
                boolean isComplexDeduction = "A2".equals(regstand) || ("A3".equals(regstand) && bondAmount == 0);
                if (isComplexDeduction) {
                    finalScore -= subScore;
                    System.out.println("🔥 A2 또는 A3 채권최고액 없음 → " + subScore + "점 차감!");
                }
                // ✅ HISPOINT에 저장할 값 결정
                int deductionPoint = isComplexDeduction ? subScore : amountDeduction;

                // SummaryData 날자 스플릿
                String RecDate = Arrays.toString(summary.get("ReceiptInfo").toString().split("일"));
                // ✅ 차감 정보 저장
                Map<String, Object> deductionEntry = new HashMap<>();
                deductionEntry.put("HISNM", summary.get("Purpose"));
                deductionEntry.put("HISAMT", bondAmount);
                deductionEntry.put("HISPOINT", deductionPoint);
                deductionEntry.put("REGSTAND", regstand);
                deductionEntry.put("HISFLAG", isFirstDeduction ? "1" : "0"); // ✅ 첫 번째 차감 데이터에만 1 적용
                deductionEntry.put("REMARK", code.get("REGCOMMENT").toString());
                deductionDetails.add(deductionEntry);

                // ✅ 코멘트 및 감점사항 추가
                comment.add(code.get("REGCOMMENT").toString());
                Deductions.add(code.get("REGASNAME").toString());
                break;
            }
        }

        return finalScore;
    }

    // ✅ 금액별 점수 차감 로직
    private static int calculateDeduction(long amount) {
        if (amount <= 50000000) return 0;
        else if (amount <= 100000000) return getProportionalScore(amount, 50000001, 100000000, 1, 7);
        else if (amount <= 150000000) return getProportionalScore(amount, 100000001, 150000000, 8, 14);
        else if (amount <= 200000000) return getProportionalScore(amount, 150000001, 200000000, 15, 21);
        else if (amount <= 250000000) return getProportionalScore(amount, 200000001, 250000000, 22, 28);
        else if (amount <= 300000000) return getProportionalScore(amount, 250000001, 300000000, 29, 35);
        else if (amount <= 350000000) return getProportionalScore(amount, 300000001, 350000000, 36, 42);
        else if (amount <= 400000000) return getProportionalScore(amount, 350000001, 400000000, 43, 49);
        return 49;
    }

    // ✅ 비율 계산 함수
    private static int getProportionalScore(long amount, long min, long max, int minScore, int maxScore) {
        double ratio = (double) (amount - min) / (max - min);
        return (int) Math.round(minScore + ratio * (maxScore - minScore));
    }

    // ✅ ReceiptInfo 날짜 기준으로 정렬하는 메서드
    public void sortByReceiptDate(List<Map<String, Object>> dataList) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년M월d일");

        dataList.sort((d1, d2) -> {
            // ReceiptInfo 가져오기
            String receipt1 = d1.get("ReceiptInfo").toString();
            String receipt2 = d2.get("ReceiptInfo").toString();

            // ✅ 날짜 추출 (제74988호 같은 뒷부분 제거)
            receipt1 = receipt1.split("제")[0].trim(); // "2024년5월13일"
            receipt2 = receipt2.split("제")[0].trim(); // "2013년9월23일"

            // ✅ 문자열을 LocalDate로 변환
            LocalDate date1 = LocalDate.parse(receipt1, formatter);
            LocalDate date2 = LocalDate.parse(receipt2, formatter);

            // ✅ 날짜 비교 (오름차순 정렬)
            return date1.compareTo(date2);
        });
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
    public static String extractJurisdictionOffice(String text) {
        // 정규식 수정: | 관할등기소 | 다음 두 개의 | 로 구분된 값을 추출
        Pattern pattern = Pattern.compile("\\|\\s*관할등기소\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^|]+)\\s*\\|");
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

            // 순위번호가 없거나 "null"로 시작하는경우 이전 데이터와 병합
            if (!columns[0].trim().isEmpty() && !columns[0].trim().equalsIgnoreCase("null")) {

                if (!currentRow.isEmpty()) {
                    parsedData.add(new HashMap<>(currentRow));
                }
                currentRow.clear();

                currentRow.put("RankNo", columns[0].trim());
                currentRow.put("RgsAimCont", columns[1].trim());
                currentRow.put("Receve", columns[2].trim());
                currentRow.put("RgsCaus", columns[3].trim());
                currentRow.put("NomprsAndEtc", columns[4].trim());

//                // "매매" 포함 여부 확인 → TradeAmount에 추가
//                if (columns[3].contains("매매")) {
//                    Map<String, Object> tradeEntry = new HashMap<>();
//                    tradeEntry.put("RgsCaus", columns[3].trim());
//
//                    // 거래가액(Amount) 추출
//                    System.out.println("매매 데이터 columns[3](4번째 셀): " + tradeEntry);
//                    String[] details = columns[4].split(" ");
//                    System.out.println("매매 데이터 columns[4](5번째 셀): " + details);
//                    for (String detail : details) {
//                        if (detail.startsWith("금")) {  // 금액 패턴 찾기
//                            try {
//                                String amountStr = detail.replaceAll("[^0-9]", "");  // 숫자만 추출
//                                tradeEntry.put("Amount", Long.parseLong(amountStr));
//                                lastTradeAmount = amountStr;  // 마지막 거래가액 저장
//                                break;
//                            } catch (NumberFormatException e) {
//                                // 숫자 변환 실패시 무시
//                            }
//                        }
//                    }
//                    TradeAmount.add(tradeEntry);
//                }
            } else {
                currentRow.put("RgsAimCont", currentRow.get("RgsAimCont") + " " + columns[1].trim());
                currentRow.put("Receve", currentRow.get("Receve") + " " + columns[2].trim());
                currentRow.put("RgsCaus", currentRow.get("RgsCaus") + " " + columns[3].trim());
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
                if (!currentRow.isEmpty() && !columns[0].trim().equalsIgnoreCase("null")) {
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
    // 정규식 패턴 기반 검색 메서드
    public static int findStartIndexWithRegex(List<String> tableData, String regexPattern) {
        Pattern pattern = Pattern.compile(regexPattern);

        for (int i = 0; i < tableData.size(); i++) {
            Matcher matcher = pattern.matcher(tableData.get(i));
            if (matcher.find()) {
                return i;  // 첫 번째로 매칭되는 인덱스 반환
            }
        }
        return -1;  // 찾지 못하면 -1 반환
    }
    // Summary 데이터 파싱 메서드
    public static Map<String, Object> parseSummaryTable(List<String> tableData, String nomData) {
        List<Map<String, Object>> summaryDataA = new ArrayList<>();
        List<Map<String, Object>> summaryDataK = new ArrayList<>();
        List<Map<String, Object>> summaryDataE = new ArrayList<>();
        System.out.println("넘어온 Summary 테이블 데이터 : " + tableData);
        System.out.println("넘어온 Summary 일반 데이터 : " + nomData);

        boolean isParsingA = false, isParsingK = false, isParsingE = false;
        boolean hasGabguData = false, hasEulguData = false;
        int rankNoCount = 0;  // "순위번호" 등장 횟수 추적

        // **갑구와 을구 데이터 존재 여부 판단**
        if (!nomData.replaceAll("[|\\s\\n\\t\\r]+", " ").contains("2. 소유지분을 제외한 소유권에 관한 사항 (갑구) - 기록사항 없음")) {
            System.out.println("✅ 갑구 데이터 수집 진행");
            hasGabguData = true;
        } else {
            System.out.println("❌ 갑구 데이터 없음");
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

            // **순위번호 컬럼 감지 (첫 번째 & 두 번째)**
            if (columns[0].equals("순위번호")) {
                rankNoCount++;

                if (rankNoCount == 1 && hasGabguData) {
                    // ✅ 첫 번째 "순위번호" 감지 → 갑구 데이터 시작
                    System.out.println("🔍 첫 번째 순위번호 감지: 갑구 데이터 시작");
                    isParsingA = false;
                    isParsingK = true;
                    isParsingE = false;
                } else if (rankNoCount == 2 && hasEulguData) {
                    // ✅ 두 번째 "순위번호" 감지 → 갑구 데이터 종료 + 을구 데이터 시작
                    System.out.println("🔍 두 번째 순위번호 감지: 갑구 종료, 을구 데이터 시작");
                    isParsingK = false;
                    isParsingE = true;
                }
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

        if (tableData.size() > 1) {
            tableData = tableData.subList(1, tableData.size());
        } else {
            return new HashMap<>();
        }

        for (String row : tableData) {
            // 안전한 split 적용 (빈 필드 포함)
            String[] columns = row.split("\\|", -1);

            // "표시번호"로 시작하는 행은 무시
            if (columns[0].trim().equals("표시번호")) {
                continue;
            }

            System.out.println("구축물 파싱 진행중 행 : " + Arrays.toString(columns));

            // columns.length가 5인 경우
            if (columns.length == 5) {

                // 건물 내역 시작점 확인
                if (!columns[0].trim().isEmpty() && !collecting) {
                    collecting = true;
                    buildingData.put("seq", columns[0].trim());
                    buildingData.put("address", columns[2].trim());
                    buildingDetails.append(columns[4].trim());  // 5개일 때 마지막 컬럼은 [3]
                }
            }
            // columns.length가 6인 경우
            else if (columns.length == 6) {

                // 건물 내역 시작점 확인
                if (!columns[0].trim().isEmpty() && !collecting) {
                    collecting = true;
                    buildingData.put("seq", columns[0].trim());
                    buildingData.put("address", columns[2].trim());
                    buildingDetails.append(columns[4].trim()); // 6개일 때 마지막 컬럼은 [4]
                }
            }
            // columns.length가 7인 경우
            else if (columns.length == 7) {

                // 건물 내역 시작점 확인
                if (!columns[0].trim().isEmpty() && !collecting) {
                    collecting = true;
                    buildingData.put("seq", columns[0].trim());
                    buildingData.put("address", columns[2].trim());
                    buildingDetails.append(columns[4].trim()); // 6개일 때 마지막 컬럼은 [4]
                }
            }

            // columns.length가 5보다 작거나 6보다 큰 경우
            else {
                System.out.println("경고: 예상치 못한 컬럼 개수(" + columns.length + "), 데이터: " + Arrays.toString(columns));
            }
        }

        // 최종 병합된 건물 내역 저장
        buildingData.put("buildingDetails", buildingDetails.toString().trim());

        return buildingData;
    }





}
