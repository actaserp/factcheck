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
    // RankNo에서 숫자 부분만 추출하는 메서드 (예: "1-1" → "1")
    public static String extractRankNumber(String rankNo) {
        return rankNo.split("-")[0]; // "-" 기준으로 앞부분만 추출
    }
    // Information에서 금액이 포함되어 있는지 확인하는 메서드
    public static boolean containsAmount(String information) {
        if (information == null) return false;
        Pattern pattern = Pattern.compile("금[0-9,.]+원"); // "금000,000원" 형태 검사
        Matcher matcher = pattern.matcher(information);
        return matcher.find();
    }

    // Information에서 금액 추출하는 메서드
    public static String extractAmount(String information) {
        if (information == null) return null;
        Pattern pattern = Pattern.compile("금([0-9,.]+)원"); // "금000,000원" 형태
        Matcher matcher = pattern.matcher(information);
        return matcher.find() ? matcher.group(1) : null;
    }
    // ✅ RankNo가 같은 그룹인지 확인하는 메서드
    public static boolean isSameRankGroup(String existingRank, String newRank) {
        String baseExistingRank = extractRankNumber(existingRank); // 기존 데이터에서 숫자 부분 추출
        String baseNewRank = extractRankNumber(newRank); // 새로운 데이터에서 숫자 부분 추출

        return baseExistingRank.equals(baseNewRank) || existingRank.startsWith(baseNewRank);
    }
    // ReceiptDate에서 날자 전처리 메서드
    public static String convertReceiptDate(String receiptInfo) {
        if (receiptInfo == null || receiptInfo.isEmpty()) return null;

        // "YYYY년M월D일" 형식의 날짜 추출
        Pattern pattern = Pattern.compile("(\\d{4})년(\\d{1,2})월(\\d{1,2})일");
        Matcher matcher = pattern.matcher(receiptInfo);

        if (matcher.find()) {
            String year = matcher.group(1);
            String month = String.format("%02d", Integer.parseInt(matcher.group(2))); // 1자리 월을 2자리로 변환
            String day = String.format("%02d", Integer.parseInt(matcher.group(3)));   // 1자리 일을 2자리로 변환
            return year + month + day; // YYYYMMDD 형식 반환
        }

        return null; // 날짜가 없으면 null 반환
    }
    // 점수계산위한 summarydata 수집 메서드
    public List<Map<String, Object>> collectSummaryData(
            List<Map<String, Object>> summaryData,
            List<Map<String, Object>> comcode) {
        System.out.println("점수계산 데이터 수집 메서드 진입 : " + summaryData);

        List<Map<String, Object>> collectedData = new ArrayList<>();

        for (Map<String, Object> summary : summaryData) {
            String purpose = summary.getOrDefault("Purpose", "").toString();
            String rankNo = summary.getOrDefault("RankNo", "").toString();  // 원본 RankNo 유지
            if (rankNo.isEmpty()) continue; // ✅ RankNo가 없으면 건너뜀
            boolean hasAmountInInfo = containsAmount(summary.getOrDefault("Information", "").toString());
            String extractedAmount = extractAmount(summary.getOrDefault("Information", "").toString());

            // ✅ ReceiptInfo 변환 (YYYYMMDD 형식으로)
            String receiptInfo = summary.getOrDefault("ReceiptInfo", "").toString();
            String receiptDate = convertReceiptDate(receiptInfo); // YYYYMMDD 변환 적용

            // REGWORD 포함 여부 확인 및 REGSEQ, REGREMARK 가져오기
            for (Map<String, Object> code : comcode) {
                String regWord = code.get("REGWORD").toString();
                if (purpose.contains(regWord)) {
                    String regSeq = code.get("REGSEQ").toString(); // REGSEQ 가져오기
                    String regRemark = code.getOrDefault("REGREMARK", "").toString(); // REGREMARK 가져오기

                    // ✅ 같은 Rank 그룹 내에서 같은 REGSEQ 데이터 찾기 (금액 포함 여부별로 구분)
                    Optional<Map<String, Object>> existingData = collectedData.stream()
                            .filter(d -> isSameRankGroup(d.get("RankNo").toString(), rankNo) &&  // 같은 Rank 그룹 판별
                                    d.get("REGSEQ").equals(regSeq) &&
                                    d.get("containsAmount").equals(hasAmountInInfo ? 1 : 0)) // 같은 금액 상태(1: 있음, 0: 없음)만 비교
                            .findFirst();

                    if (existingData.isPresent()) {
                        if (hasAmountInInfo) {
                            // ✅ 같은 RankNo + REGSEQ + 금액 여부가 같은 데이터 존재 → 최신화
                            existingData.get().put("Amount", extractedAmount); // 금액 덮어쓰기
                            existingData.get().put("Information", summary.get("Information")); // Information 최신화
                        } else {
                            existingData.get().putAll(summary); // 기존 금액 없는 데이터 최신화
                            existingData.get().put("REGREMARK", regRemark);
                            existingData.get().put("REGWORD", regWord);
                            existingData.get().put("REGNM", code.getOrDefault("REGNM", ""));
                            existingData.get().put("REGASNAME", code.getOrDefault("REGASNAME", ""));
                            existingData.get().put("ReceiptDate", receiptDate);
                        }
                    } else {
                        // ✅ 새로운 데이터 추가 (containsAmount 포함, REGSEQ 포함, REGREMARK 포함)
                        Map<String, Object> newData = new HashMap<>(summary);
                        newData.put("REGSEQ", regSeq);
                        newData.put("REGREMARK", regRemark);
                        newData.put("REGWORD", regWord);
                        newData.put("REGNM", code.getOrDefault("REGNM", ""));
                        newData.put("REGSTAND", code.getOrDefault("REGSTAND", ""));
                        newData.put("REGMAXNUM", code.getOrDefault("REGMAXNUM", ""));
                        newData.put("REGSTAMT", code.getOrDefault("REGSTAMT", ""));
                        newData.put("SUBSCORE", code.getOrDefault("SUBSCORE", ""));
                        newData.put("SENSCORE", code.getOrDefault("SENSCORE", ""));
                        newData.put("REGASNAME", code.getOrDefault("REGASNAME", ""));

                        newData.put("containsAmount", hasAmountInInfo ? 1 : 0); // 금액 여부 플래그 추가
                        newData.put("Amount", hasAmountInInfo ? extractedAmount : null); // 초기 Amount 설정
                        newData.put("ReceiptDate", receiptDate); // ✅ YYYYMMDD 변환값 저장
                        collectedData.add(newData);
                    }
                    break;
                }
            }
        }
        // ✅ 안전한 정렬 적용 (혹시 정렬이 깨졌다면 다시 정렬)
        sortByReceiptDate(collectedData);
        System.out.println("점수계산 데이터 수집 메서드 out : " + collectedData);
        return collectedData;
    }




    public Map<String, Object> calculateFinalScore(List<Map<String, Object>> collectedData,
                                                   Integer lessScore,
                                                   List<Map<String, Object>> gradeInfo) {
        System.out.println("점수차감 main메서드 진입 : " + collectedData);
        int finalScore = 100; // 기본 점수
        String Grade = "";
        List<Map<String, Object>> comment = new ArrayList<>();
        List<String> Deductions = new ArrayList<>();
        List<Map<String, Object>> deductionDetails = new ArrayList<>();
        AtomicBoolean firstDeductionFound = new AtomicBoolean(false);

        // ✅ REGSEQ별 최대 차감 한도 저장
        Map<String, Integer> maxDeductionLimits = new HashMap<>();

        for (Map<String, Object> data : collectedData) {
            String regSeq = data.get("REGSEQ").toString();
            String regStand = data.get("REGSTAND").toString();

            int maxDeduction = 0;

            if ("A1".equals(regStand)) {
                Object regMaxNum = data.get("REGMAXNUM");
                maxDeduction = (regMaxNum != null) ? Integer.parseInt(regMaxNum.toString()) : 0;
            } else if ("A2".equals(regStand)) {
                Object subScore = data.get("SUBSCORE");
                maxDeduction = (subScore != null) ? Integer.parseInt(subScore.toString()) : 0;
            } else if ("A3".equals(regStand)) {
                Object regMaxNum = data.get("REGMAXNUM");
                maxDeduction = (regMaxNum != null) ? Integer.parseInt(regMaxNum.toString()) : 0;
            }

            maxDeductionLimits.put(regSeq, maxDeduction);
        }

        // ✅ 차감 개수 카운트
        int totalDeductionsCount = 0;

        // ✅ 점수 차감 로직 실행
        for (Map<String, Object> summary : collectedData) {
            String regSeq = summary.get("REGSEQ").toString();
            String regStand = summary.get("REGSTAND").toString();
            boolean hasAmount = summary.get("containsAmount").equals(1);
            int bondAmount = summary.get("Amount") != null ? Integer.parseInt(summary.get("Amount").toString().replace(",", "")) : 0;

            int deduction = 0;

            if ("A1".equals(regStand) && hasAmount) {
                deduction = calculateDeduction(bondAmount);
            } else if ("A2".equals(regStand) || ("A3".equals(regStand) && !hasAmount)) {
                Object subScore = summary.get("SUBSCORE");
                deduction = (subScore != null) ? Integer.parseInt(subScore.toString()) : 0;
            } else if ("A3".equals(regStand)) {
                deduction = calculateDeduction(bondAmount);
            }

            // ✅ REGSEQ별 최대 차감 한도에서 차감 적용
            int maxDeduction = maxDeductionLimits.getOrDefault(regSeq, 0);
            int actualDeduction = Math.min(deduction, maxDeduction);
            maxDeductionLimits.put(regSeq, maxDeduction - actualDeduction);

            // ✅ 첫 번째 데이터 감점 (SENSCORE 존재 시, 추가 차감 적용)
            boolean isFirstDeduction = !firstDeductionFound.get()
                    && summary.containsKey("SENSCORE")
                    && summary.get("SENSCORE") != null
                    && !summary.get("SENSCORE").toString().isEmpty();
            if (isFirstDeduction) {
                actualDeduction += 5; // ✅ 첫 번째 감점이면 추가로 5점 차감
            }
            firstDeductionFound.set(true);

            // ✅ 점수 차감 적용
            finalScore -= actualDeduction;

            // ✅ 차감 내역 저장
            Map<String, Object> deductionEntry = new HashMap<>();

            deductionEntry.put("HISNM", summary.get("REGWORD"));
            deductionEntry.put("HISAMT", bondAmount);
            deductionEntry.put("HISPOINT", actualDeduction);
            deductionEntry.put("REGSTAND", regStand);
            deductionEntry.put("HISFLAG", isFirstDeduction ? "1" : "0"); // ✅ 첫 번째 차감 데이터에만 1 적용
            deductionEntry.put("REMARK", summary.get("REGREMARK"));
            deductionEntry.put("HISDATE", summary.get("ReceiptDate"));

            Map<String, Object> cardData = new HashMap<>();
            cardData.put("DATE", summary.get("ReceiptDate"));
            cardData.put("REGREMARK", summary.get("REGREMARK"));
            comment.add(cardData);
            Deductions.add((String) summary.get("REGASNAME"));
            deductionDetails.add(deductionEntry);

            totalDeductionsCount++;
        }



        // ✅ 추가 감점 (항목 개수당 1점)
        finalScore -= totalDeductionsCount;
        Map<String, Object> CountEntry = new HashMap<>();
        CountEntry.put("HISNM", "항목차감");
        CountEntry.put("HISPOINT", totalDeductionsCount);
        CountEntry.put("REMARK", "마지막 항목별 추가 차감점수입니다.");
        deductionDetails.add(CountEntry);

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


        // ✅ 최종 점수 출력
        System.out.println("🔥 최종 점수: " + finalScore);
        System.out.println("📌 감점 내역: " + deductionDetails);

        Map<String, Object> result = new HashMap<>();
        result.put("COMMENT", comment);
        result.put("REGASNAME", Deductions);
        result.put("GRADE", Grade);
        result.put("REALSCORE", finalScore);
        result.put("DEDUCTION_DETAILS", deductionDetails);

        return result;
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
            rankNoCount = 1;
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
