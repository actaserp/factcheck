//package mes.app.actas_inspec.LTSA;
//
//
//import mes.app.UtilClass;
//import mes.domain.DTO.TB_RP620Dto;
//import mes.domain.DTO.TB_RP621Dto;
//import mes.domain.DTO.TB_RP622Dto;
//import mes.domain.model.AjaxResult;
//import mes.domain.repository.TB_RP622Repository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/ltsaList")
//public class LTSAListController {
//
//    @Autowired
//    TB_RP622Repository tbRp622Repository;
//    @Autowired
//    LTSAListService ltsaListService;
//
//    //리시트 항목에 대한 검색조건 항목 조회
//    @GetMapping("/searchCondition")
//    public AjaxResult getSearchCondition(@RequestParam String spworkcd,
//                                         @RequestParam String spcompcd,
//                                         @RequestParam String spplancd
//                                         ){
//
//        AjaxResult result = new AjaxResult();
//
//        List<TB_RP622Dto> list =  tbRp622Repository.findDistinctStandqy(spworkcd, spcompcd, spplancd);
//
//        // LinkedHashSet을 사용하여 순서 보장
//        Set<String> setList = list.stream()
//                .map(TB_RP622Dto::getStandqy)
//                .collect(Collectors.toCollection(LinkedHashSet::new));
//
//        List<String> formattedList = setList.stream()
//                .map(value -> {
//                    if (value.length() >= 5) {
//                        String year = value.substring(0, 4);
//                        String quarter = value.substring(4);
//                        return year + "." + quarter + "Q";
//                    } else {
//                        return value;
//                    }
//                })
//                .toList();
//
//        result.success = true;
//        result.data = formattedList;
//
//        return result;
//    }
//
//    @GetMapping("/searchConditionYear")
//    public AjaxResult getSearchConditionYear(@RequestParam String spworkcd,
//                                             @RequestParam String spcompcd,
//                                             @RequestParam String spplancd){
//
//        AjaxResult result = new AjaxResult();
//
//        List<Map<String, Object>> items = new ArrayList<>();
//
//        items = ltsaListService.getSearchCondition_Outline(spworkcd, spcompcd, spplancd);
//
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//    @GetMapping("/ListRead")
//    public AjaxResult getList(@RequestParam String standqy,
//                              @RequestParam String spworkcd,
//                              @RequestParam String spcompcd,
//                              @RequestParam String spplancd){
//
//        AjaxResult result = new AjaxResult();
//
//        List<Map<String, Object>> items = new ArrayList<>();
//
//        items = ltsaListService.getList(standqy, spworkcd, spcompcd, spplancd);
//
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//    @GetMapping("/OutlineRead")
//    public AjaxResult getOutline(@RequestParam(value = "year") String year,
//                                 @RequestParam String spworkcd,
//                                 @RequestParam String spcompcd,
//                                 @RequestParam String spplancd){
//
//        AjaxResult result = new AjaxResult();
//
//        List<Map<String, Object>> items = new ArrayList<>();
//
//
//        items = ltsaListService.getOutLineList(year, spworkcd, spcompcd, spplancd);
//
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//    @PostMapping("/OutlineSave")
//    public AjaxResult saveOutline(
//            @RequestPart(value = "jsonData") TB_RP620Dto dto
//    ){
//        AjaxResult result = new AjaxResult();
//
//        String userid = UtilClass.getUserId();
//        String username = UtilClass.getUsername();
//
//        dto.setInuserid(userid);
//        dto.setInusernm(username);
//
//        ltsaListService.saveOutline(dto);
//
//
//        result.success = true;
//        result.message = "저장되었습니다.";
//
//        return result;
//    }
//
//    @GetMapping("/WcAndSummaryRead")
//    public AjaxResult getWcAndSummary(@RequestParam(value = "year") String year,
//                                      @RequestParam String spcompcd,
//                                      @RequestParam String spworkcd,
//                                      @RequestParam String spplancd){
//
//        AjaxResult result = new AjaxResult();
//
//        List<Map<String, Object>> items = new ArrayList<>();
//
//
//        items = ltsaListService.getWcAndSummaryList(year, spworkcd, spcompcd, spplancd);
//
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//    @PostMapping("/WcAndSummarySave")
//    public AjaxResult saveWcAndSummary(
//            @RequestPart(value = "jsonData") TB_RP620Dto dto
//    ){
//        AjaxResult result = new AjaxResult();
//
//        String userid = UtilClass.getUserId();
//        String username = UtilClass.getUsername();
//
//        dto.setInuserid(userid);
//        dto.setInusernm(username);
//
//        ltsaListService.saveWcAndSummary(dto);
//
//
//        result.success = true;
//        result.message = "저장되었습니다.";
//
//        return result;
//    }
//
//    @GetMapping("/WcAndSummary2Read")
//    public AjaxResult getWcAndSummary2(@RequestParam(value = "year") String year,
//                                       @RequestParam String spcompcd,
//                                       @RequestParam String spworkcd,
//                                       @RequestParam String spplancd){
//
//        AjaxResult result = new AjaxResult();
//
//        List<Map<String, Object>> items = new ArrayList<>();
//
//
//        items = ltsaListService.getWcAndSummaryList2(year, spworkcd, spcompcd, spplancd);
//
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//    @PostMapping("/WcAndSummarySave2")
//    public AjaxResult saveWcAndSummary2(
//            @RequestPart(value = "jsonData") TB_RP621Dto dto
//    ){
//        AjaxResult result = new AjaxResult();
//
//        String userid = UtilClass.getUserId();
//        String username = UtilClass.getUsername();
//
//        dto.setInuserid(userid);
//        dto.setInusernm(username);
//
//        ltsaListService.saveWcAndSummary2(dto);
//
//
//        result.success = true;
//        result.message = "저장되었습니다.";
//
//        return result;
//    }
//
//    @GetMapping("/systemutilRead")
//    public AjaxResult getSystemUtil(@RequestParam(value = "year") String year,
//                                    @RequestParam String spcompcd,
//                                    @RequestParam String spworkcd,
//                                    @RequestParam String spplancd){
//
//        AjaxResult result = new AjaxResult();
//
//        List<Map<String, Object>> items = new ArrayList<>();
//
//
//        items = ltsaListService.getSystemUtil(year, spworkcd, spcompcd, spplancd);
//
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//    @PostMapping("/SystemUtilSave")
//    public AjaxResult SystemUtilSave(
//            @RequestParam(value = "dayscnt") String dayscnt,
//            @RequestParam(value = "elecres") String elecres,
//            @RequestParam(value = "elecqupt") String elecqupt,
//            @RequestParam(value = "setresq") String setresq,
//            @RequestParam(value = "resuyul") String resuyul,
//            @RequestParam(value = "remark") String remark,
//            @RequestParam(value = "termseq") String termseq,
//            @RequestParam(value = "standqy") String standqy,
//            @RequestParam(value = "spworkcd") String spworkcd,
//            @RequestParam(value = "spworknm") String spworknm,
//            @RequestParam(value = "spcompcd") String spcompcd,
//            @RequestParam(value = "spcompnm") String spcompnm,
//            @RequestParam(value = "spplancd") String spplancd,
//            @RequestParam(value = "spplannm") String spplannm
//            ){
//        AjaxResult result = new AjaxResult();
//
//        String userid = UtilClass.getUserId();
//        String username = UtilClass.getUsername();
//
//        TB_RP622Dto dto = new TB_RP622Dto();
//        dto.setSpworkcd(spworkcd);
//        dto.setSpworknm(spworknm);
//        dto.setSpcompcd(spcompcd);
//        dto.setSpcompnm(spcompnm);
//        dto.setSpplancd(spplancd);
//        dto.setSpplannm(spplannm);
//        dto.setStandqy(standqy.replaceAll("\\.", ""));
//        dto.setInuserid(userid);
//        dto.setInusernm(username);
//        dto.setDayscnt(Long.parseLong(UtilClass.extractNum(dayscnt)));
//        dto.setElecres(Long.parseLong(UtilClass.extractNum(elecres)));
//        dto.setElecqupt(Long.parseLong(UtilClass.extractNum(elecqupt)));
//        dto.setSetresq(Long.parseLong(UtilClass.extractNum(setresq)));
//        dto.setResuyul(Double.parseDouble(UtilClass.extractNum(resuyul)));
//        dto.setRemark(remark);
//        dto.setTermseq(termseq);
//
//        ltsaListService.saveSystemUtil(dto);
//
//
//        result.success = true;
//        result.message = "저장되었습니다.";
//
//        return result;
//    }
//
//    @GetMapping("/PerformancdGuaranteeRead")
//    public AjaxResult getPerformancdGuaranteeRead(@RequestParam(value = "year") String year,
//                                                  @RequestParam String spcompcd,
//                                                  @RequestParam String spworkcd,
//                                                  @RequestParam String spplancd){
//
//        AjaxResult result = new AjaxResult();
//
//        List<Map<String, Object>> items = new ArrayList<>();
//
//
//        items = ltsaListService.getPerformanceGuarantee(year, spworkcd, spcompcd, spplancd);
//
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//    @PostMapping("/PerformancdGuaranteeSave")
//    public AjaxResult PerformancdGuaranteeSave(
//            @RequestParam(value = "perguconeleq_pg") String perguconeleq_pg,
//            @RequestParam(value = "perguconelnd_pg") String perguconelnd_pg,
//            @RequestParam(value = "perguconelnq_pg") String perguconelnq_pg,
//            @RequestParam(value = "perguconseleq_pg") String perguconseleq_pg,
//            @RequestParam(value = "perguconelyul_pg") String perguconelyul_pg,
//            @RequestParam(value = "perguconbkg_pg") String perguconbkg_pg,
//            @RequestParam(value = "perguconpsfa") String perguconpsfa,
//            @RequestParam(value = "termseq") String termseq,
//            @RequestParam(value = "standqy") String standqy,
//            @RequestParam(value = "spworkcd") String spworkcd,
//            @RequestParam(value = "spworknm") String spworknm,
//            @RequestParam(value = "spcompcd") String spcompcd,
//            @RequestParam(value = "spcompnm") String spcompnm,
//            @RequestParam(value = "spplancd") String spplancd,
//            @RequestParam(value = "spplannm") String spplannm
//    ){
//        AjaxResult result = new AjaxResult();
//
//        String userid = UtilClass.getUserId();
//        String username = UtilClass.getUsername();
//
//        TB_RP623Dto dto = new TB_RP623Dto();
//        dto.setSpworkcd(spworkcd);
//        dto.setSpworknm(spworknm);
//        dto.setSpcompcd(spcompcd);
//        dto.setSpcompnm(spcompnm);
//        dto.setSpplancd(spplancd);
//        dto.setSpplannm(spplannm);
//        dto.setStandqy(standqy.replaceAll("\\.", ""));
//        dto.setInuserid(userid);
//        dto.setInusernm(username);
//        dto.setPerguconeleq(Double.parseDouble(UtilClass.extractNum(perguconeleq_pg)));
//        dto.setPerguconelnd(Double.parseDouble(UtilClass.extractNum(perguconelnd_pg)));
//        dto.setPerguconelnq(Double.parseDouble(UtilClass.extractNum(perguconelnq_pg)));
//        dto.setPerguconseleq(Double.parseDouble(UtilClass.extractNum(perguconseleq_pg)));
//        dto.setPerguconelyul(Double.parseDouble(UtilClass.extractNum(perguconelyul_pg)));
//        dto.setPerguconbkg(Double.parseDouble(UtilClass.extractNum(perguconbkg_pg)));
//        dto.setPerguconpsfa(perguconpsfa);
//        dto.setTermseq(termseq);
//
//        ltsaListService.savePerformancdGuarantee(dto);
//
//
//        result.success = true;
//        result.message = "저장되었습니다.";
//
//        return result;
//    }
//
//
//    @GetMapping("/PerformancdWarrantyRead")
//    public AjaxResult getPerformancdWarrantyRead(@RequestParam(value = "year") String year,
//                                                 @RequestParam String spcompcd,
//                                                 @RequestParam String spworkcd,
//                                                 @RequestParam String spplancd){
//
//        AjaxResult result = new AjaxResult();
//
//        List<Map<String, Object>> items = new ArrayList<>();
//
//
//        items = ltsaListService.getPerformanceWarranty(year, spworkcd, spcompcd, spplancd);
//
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//
//    @PostMapping("/PerformancdWarrantySave")
//    public AjaxResult performancdWarrantySave(@RequestBody TB_RP624Dto dto) {
//        AjaxResult result = new AjaxResult();
//
//        dto.setInuserid(UtilClass.getUserId());
//        dto.setInusernm(UtilClass.getUsername());
//
//        dto.setStandqy(dto.getStandqy().replaceAll("\\.", ""));
//
//        ltsaListService.savePerformancdWarranty(dto);
//
//        result.success = true;
//        result.message = "저장되었습니다.";
//
//        return result;
//    }
//
//    @GetMapping("/FuelEfficiencyRead")
//    public AjaxResult getFuelEfficiencyRead(@RequestParam(value = "year") String year,
//                                            @RequestParam String spcompcd,
//                                            @RequestParam String spworkcd,
//                                            @RequestParam String spplancd){
//
//        AjaxResult result = new AjaxResult();
//
//        List<Map<String, Object>> items = new ArrayList<>();
//
//        items = ltsaListService.getFuelEfficiency(year, spworkcd, spcompcd, spplancd);
//
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//
//    @PostMapping("/FuelEfficiencySave")
//    public AjaxResult FuelEfficiencySaveSave(@RequestBody TB_RP625Dto dto) {
//        AjaxResult result = new AjaxResult();
//
//        dto.setInuserid(UtilClass.getUserId());
//        dto.setInusernm(UtilClass.getUsername());
//
//        dto.setStandqy(dto.getStandqy().replaceAll("\\.", ""));
//
//        ltsaListService.saveFuelEfficiencySave(dto);
//
//        result.success = true;
//        result.message = "저장되었습니다.";
//
//        return result;
//    }
//
//    @GetMapping("/FuelPerformRead")
//    public AjaxResult getFuelPerformReadRead(@RequestParam(value = "year") String year,
//                                             @RequestParam String spcompcd,
//                                             @RequestParam String spworkcd,
//                                             @RequestParam String spplancd){
//
//        AjaxResult result = new AjaxResult();
//
//        List<Map<String, Object>> items = new ArrayList<>();
//
//        items = ltsaListService.getFuelPerformRead(year, spworkcd, spcompcd, spplancd);
//
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//    @PostMapping("/FuelPerformSave")
//    public AjaxResult FuelPerformSave(@RequestBody TB_RP626Dto dto) {
//        AjaxResult result = new AjaxResult();
//
//        dto.setInuserid(UtilClass.getUserId());
//        dto.setInusernm(UtilClass.getUsername());
//
//        dto.setStandqy(dto.getStandqy().replaceAll("\\.", ""));
//
//        ltsaListService.saveFuelPerformSave(dto);
//
//        result.success = true;
//        result.message = "저장되었습니다.";
//
//        return result;
//    }
//
//    @GetMapping("/FuelWarrantyRead")
//    public AjaxResult getFuelWarrantyRead(@RequestParam(value = "year") String year,
//                                          @RequestParam String spcompcd,
//                                          @RequestParam String spworkcd,
//                                          @RequestParam String spplancd
//                                          ){
//
//        AjaxResult result = new AjaxResult();
//
//        List<Map<String, Object>> items = new ArrayList<>();
//
//        items = ltsaListService.getFuelWarrantyRead(year, spworkcd, spcompcd, spplancd);
//
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//    @PostMapping("/FuelWarrantySave")
//    public AjaxResult FuelWarrantySave(@RequestBody TB_RP627Dto dto) {
//        AjaxResult result = new AjaxResult();
//
//        dto.setInuserid(UtilClass.getUserId());
//        dto.setInusernm(UtilClass.getUsername());
//
//        dto.setStandqy(dto.getStandqy().replaceAll("\\.", ""));
//
//        ltsaListService.saveFuelWarrantySave(dto);
//
//        result.success = true;
//        result.message = "저장되었습니다.";
//
//        return result;
//    }
//
//    @GetMapping("/system-usage/period")
//    public AjaxResult getSystemUtil(@RequestParam String startDate,
//                                    @RequestParam String endDate,
//                                    @RequestParam String spcompcd,
//                                    @RequestParam String spworkcd,
//                                    @RequestParam String spplancd){
//
//        // 초기 값 설정
//        String stdate = formatDateWithQuarter(startDate, "19991");
//        String eddate = formatDateWithQuarter(endDate, "29991");
//
//        // 데이터 조회
//        List<Map<String, Object>> items = ltsaListService.getSystemUtil_Period(stdate, eddate, spworkcd, spcompcd, spplancd);
//
//        // 결과 반환
//        AjaxResult result = new AjaxResult();
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//    @GetMapping("/fuel-efficiency/period")
//    public AjaxResult getFuelEfficiency(@RequestParam String startDate,
//                                        @RequestParam String endDate,
//                                        @RequestParam String spcompcd,
//                                        @RequestParam String spworkcd,
//                                        @RequestParam String spplancd) {
//
//        // 초기 값 설정
//        String stdate = formatDateWithQuarter(startDate, "19991");
//        String eddate = formatDateWithQuarter(endDate, "29991");
//
//        // 데이터 조회
//        List<Map<String, Object>> items = ltsaListService.getFuelEfficiency_Period(stdate, eddate, spworkcd, spcompcd, spplancd);
//
//        // 결과 반환
//        AjaxResult result = new AjaxResult();
//        result.success = true;
//        result.data = items;
//
//        return result;
//    }
//
//    // 날짜와 분기 변환 로직을 별도 메서드로 추출
//    private String formatDateWithQuarter(String date, String defaultDate) {
//        if (date == null || date.isEmpty()) {
//            return defaultDate; // 기본값 반환
//        }
//
//        String formattedDate = date.replaceAll("-", "");
//        String quarter = String.valueOf(mapStringToValue(formattedDate.substring(4, 6))); // 월에서 분기 추출
//        return formattedDate.substring(0, 4) + quarter; // YYYY + 분기 조합
//    }
//
//    private String mapStringToValue(String input) {
//        // 기본값 설정 (매핑되지 않는 경우 0 반환)
//        String result = "0";
//
//        // 두 자리 문자열 검사
//        if (input == null || input.length() != 2 || !input.matches("\\d{2}")) {
//            throw new IllegalArgumentException("Input must be a two-digit numeric string");
//        }
//
//        // 문자열에 따라 값 매핑
//        switch (input) {
//            case "01": case "02": case "03":
//                result = "1";
//                break;
//            case "04": case "05": case "06":
//                result = "2";
//                break;
//            case "07": case "08": case "09":
//                result = "3";
//                break;
//            case "10": case "11": case "12":
//                result = "4";
//                break;
//            default:
//                // 매핑되지 않는 경우 기본값 유지 (0)
//                break;
//        }
//
//        return result;
//    }
//
//}
