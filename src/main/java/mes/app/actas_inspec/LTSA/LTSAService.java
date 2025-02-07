//package mes.app.actas_inspec.LTSA;
//
//
//import mes.app.UtilClass;
//import mes.domain.DTO.TB_RP620Dto;
//import mes.domain.DTO.TB_RP621Dto;
//import mes.domain.DTO.TB_RP622Dto;
//import mes.domain.entity.actasEntity.TB_RP620;
//import mes.domain.entity.actasEntity.TB_RP620Id;
//import mes.domain.entity.actasEntity.TB_RP622;
//import mes.domain.repository.TB_RP620Repository;
//import mes.domain.repository.TB_RP621Repository;
//import mes.domain.repository.TB_RP622Repository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//public class LTSAService {
//
//    @Autowired
//    TB_RP620Repository tbRp620Repository;
//    @Autowired
//    TB_RP621Repository tbRp621Repository;
//    @Autowired
//    TB_RP622Repository tbRp622Repository;
//    @Autowired
//    TB_RP623Repository tbRp623Repository;
//    @Autowired
//    TB_RP624Repository tbRp624Repository;
//    @Autowired
//    TB_RP625Repository tbRp625Repository;
//    @Autowired
//    TB_RP626Repository tbRp626Repository;
//    @Autowired
//    TB_RP627Repository tbRp627Repository;
//    @Autowired
//    TB_RP850Repository tbRp850Repository;
//    @Autowired
//    TB_RP840Repository tbRp840Repository;
//
//
//    public void saveRP620(TB_RP620Dto dto, Map<String, String> dtoValue, String year, String month){
//
//        TB_RP620Id id = new TB_RP620Id();
//        id.setSpworkcd(dtoValue.get("spworkcd"));
//        id.setSpcompcd(dtoValue.get("spcompcd"));
//        id.setSpplancd(dtoValue.get("spplancd"));
//        id.setStandqy(year + month);
//
//        Optional<TB_RP620> find = tbRp620Repository.findById(id);
//
//        TB_RP620 entity;
//
//        if(find.isPresent()){
//            entity = find.get();
//
//            if(dto.getPurpose() != null) entity.setPurpose(dto.getPurpose());
//            if(dto.getProjectnm() != null) entity.setProjectnm(dto.getProjectnm());
//            if(dto.getEquipstat() != null) entity.setEquipstat(dto.getEquipstat());
//            if(dto.getReportout() != null) entity.setReportout(dto.getReportout());
//            if(dto.getYratescond() != null) entity.setYratescond(dto.getYratescond());
//            if(dto.getYratewcond() != null) entity.setYratewcond(dto.getYratewcond());
//            if(dto.getYraterem() != null) entity.setYraterem(dto.getYraterem());
//            if(dto.getYeffiscond() != null) entity.setYeffiscond(dto.getYeffiscond());
//            if(dto.getYeffiwcond() != null) entity.setYeffiwcond(dto.getYeffiwcond());
//            if(dto.getYeffirem() != null) entity.setYeffirem(dto.getYeffirem());
//            if(dto.getResultsdt() != null) entity.setResultsdt(dto.getResultsdt());
//            if(dto.getResultedt() != null) entity.setResultedt(dto.getResultedt());
//            if(dto.getInuserid() != null) entity.setInuserid(dto.getInuserid());
//            if(dto.getInusernm() != null) entity.setInusernm(dto.getInusernm());
//
//        }else{
//            entity = dto.toEntity(dto);
//        }
//
//        tbRp620Repository.save(entity);
//    }
//
//    public void saveRP621(TB_RP621Dto dto){
//        tbRp621Repository.save(dto.toEntity());
//    }
//
//    public void saveRP622(List<TB_RP622Dto> dto){
//        List<TB_RP622> entityList = new ArrayList<>();
//
//        for(TB_RP622Dto dto2 : dto){
//
//            TB_RP622 entity = new TB_RP622();
//            entity = dto2.toEntity();
//            entityList.add(entity);
//        }
//        tbRp622Repository.saveAll(entityList);
//    }
//
//    public void saveRP623(List<TB_RP623Dto> dto) {
//
//        List<TB_RP623> entityList = new ArrayList<>();
//
//        for(TB_RP623Dto dto2 : dto){
//
//            TB_RP623 entity = new TB_RP623();
//            entity = dto2.toEntity();
//            entityList.add(entity);
//        }
//        tbRp623Repository.saveAll(entityList);
//    }
//
//    public void saveRP624(List<TB_RP624Dto> dto) {
//
//        List<TB_RP624> entityList = new ArrayList<>();
//
//        for(TB_RP624Dto dto2 : dto){
//
//            TB_RP624 entity = new TB_RP624();
//            entity = dto2.toEntity();
//            entityList.add(entity);
//        }
//        tbRp624Repository.saveAll(entityList);
//    }
//
//    public void saveRP625(List<TB_RP625Dto> dto) {
//
//        List<TB_RP625> entityList = new ArrayList<>();
//
//        for(TB_RP625Dto dto2 : dto){
//
//            TB_RP625 entity = new TB_RP625();
//            entity = dto2.toEntity();
//            entityList.add(entity);
//        }
//        tbRp625Repository.saveAll(entityList);
//    }
//
//    public void saveRP626(List<TB_RP626Dto> dto) {
//
//        List<TB_RP626> entityList = new ArrayList<>();
//
//        for(TB_RP626Dto dto2 : dto){
//
//            TB_RP626 entity = new TB_RP626();
//            entity = dto2.toEntity();
//            entityList.add(entity);
//        }
//        tbRp626Repository.saveAll(entityList);
//    }
//
//    public void saveRP627(List<TB_RP627Dto> dto) {
//
//        List<TB_RP627> entityList = new ArrayList<>();
//
//        for(TB_RP627Dto dto2 : dto){
//
//            TB_RP627 entity = new TB_RP627();
//            entity = dto2.toEntity();
//            entityList.add(entity);
//        }
//        tbRp627Repository.saveAll(entityList);
//    }
//
//    public void saveRP840(List<TB_RP840Dto> dto) {
//
//        List<TB_RP840> entityList = new ArrayList<>();
//
//        for(TB_RP840Dto dto2 : dto){
//
//            TB_RP840 entity = new TB_RP840();
//            entity = dto2.toEntity();
//            entityList.add(entity);
//        }
//        tbRp840Repository.saveAll(entityList);
//    }
//
//    public void saveRP850(List<TB_RP850Dto> dto) {
//
//        List<TB_RP850> entityList = new ArrayList<>();
//
//        for(TB_RP850Dto dto2 : dto){
//
//            TB_RP850 entity = new TB_RP850();
//            entity = dto2.toEntity();
//            entityList.add(entity);
//        }
//        tbRp850Repository.saveAll(entityList);
//    }
//    public TB_RP620Dto TB_RP620DtoSet(Map<String, List<String>> result) {
//
//
//        TB_RP620Dto rp620Dto = new TB_RP620Dto();
//
//        String yrate = result.get("row1").get(0);
//        List<String> yrateparsed = Arrays.asList(yrate.split("\\|"));
//
//        String yeffi = result.get("row2").get(0);
//        List<String> yeffipared = Arrays.asList(yeffi.split("\\|"));
//
//
//        rp620Dto.setYratescond(yrateparsed.get(2));
//        rp620Dto.setYratewcond(yrateparsed.get(3));
//        rp620Dto.setYraterem(yrateparsed.get(4));
//        rp620Dto.setYeffiscond(yeffipared.get(2));
//        rp620Dto.setYeffiwcond(yeffipared.get(3));
//        rp620Dto.setYeffirem(yeffipared.get(4));
//
//
//
//        return rp620Dto;
//    }
//
//    public TB_RP621Dto TB_RP621DtoSet(Map<String, List<String>> result, Map<String, String> dtoValue, String year, String quarter){
//
//        String yrate = result.get("row3").get(0);
//        List<String> yrateparsed = Arrays.asList(yrate.split("\\|"));
//
//        String yeffis = result.get("row4").get(0);
//        List<String> yeffispared = Arrays.asList(yeffis.split("\\|"));
//
//        String yratew = result.get("row5").get(0);
//        List<String> yratewparsed = Arrays.asList(yratew.split("\\|"));
//
//        String yeffiw = result.get("row6").get(0);
//        List<String> yeffiwparsed = Arrays.asList(yeffiw.split("\\|"));
//
//
//        TB_RP621Dto RP621Dto = new TB_RP621Dto();
//        RP621Dto.setSpworkcd(dtoValue.get("spworkcd"));
//        RP621Dto.setSpcompcd(dtoValue.get("spcompcd"));
//        RP621Dto.setSpplancd(dtoValue.get("spplancd"));
//        RP621Dto.setSpworknm(dtoValue.get("spworknm"));
//        RP621Dto.setSpcompnm(dtoValue.get("spcompnm"));
//        RP621Dto.setSpplannm(dtoValue.get("spplannm"));
//        RP621Dto.setStandqy(year + quarter);
//        RP621Dto.setYratescond(getOrDefault(yrateparsed, 1, ""));
//        RP621Dto.setYratesexec(getOrDefault(yrateparsed, 2, ""));
//        RP621Dto.setYratesrerm(getOrDefault(yrateparsed, 3, ""));
//        RP621Dto.setYeffiscond(getOrDefault(yeffispared, 1, ""));
//        RP621Dto.setYeffisexec(getOrDefault(yeffispared, 2, ""));
//        RP621Dto.setYeffisrerm(getOrDefault(yeffispared, 3, ""));
//
//        RP621Dto.setYratewcond(getOrDefault(yratewparsed, 1, ""));
//        RP621Dto.setYratewexec(getOrDefault(yratewparsed, 2, ""));
//        RP621Dto.setYratewrerm(getOrDefault(yratewparsed, 3, ""));
//        RP621Dto.setYeffiwcond(getOrDefault(yeffiwparsed, 1, ""));
//        RP621Dto.setYeffiwexec(getOrDefault(yeffiwparsed, 2, ""));
//        RP621Dto.setYeffiwrerm(getOrDefault(yeffiwparsed, 3, ""));
//
//        return RP621Dto;
//    }
//
//    public List<TB_RP622Dto> TB_RP622DtoSet(Map<String, List<String>> result, Map<String, String> dtoValue, String year, String quarter){
//
//        List<TB_RP622Dto> RP622Dto = new ArrayList<>();
//
//
//        for (int i = 1; i < 7; i++) {
//            TB_RP622Dto dto = new TB_RP622Dto();
//
//            Integer sequence = i;
//
//            List<String> row = result.get("row" + (i));
//
//            // 공통 문자열 처리 로직을 메서드로 분리
//            String Dayscnt = cleanValue(row.get(1));
//            String Elecres = cleanValue(row.get(2));
//            String Elecqupt = cleanValue(row.get(3));
//            String Setresq = cleanValue(row.get(4));
//            String Resutul = cleanValue(row.get(5));
//
//            // DTO 값 설정
//            dto.setSpworkcd(dtoValue.get("spworkcd"));
//            dto.setSpcompcd(dtoValue.get("spcompcd"));
//            dto.setSpplancd(dtoValue.get("spplancd"));
//            dto.setSpworknm(dtoValue.get("spworknm"));
//            dto.setSpcompnm(dtoValue.get("spcompnm"));
//            dto.setSpplannm(dtoValue.get("spplannm"));
//            dto.setStandqy(year + quarter);
//
//            dto.setTermseq(sequence.toString());
//            dto.setStandym(row.get(0));
//            // 숫자 필드 설정
//            dto.setDayscnt(parseLongOrDefault(Dayscnt, 0L));
//            dto.setElecres(parseLongOrDefault(Elecres, 0L));
//            dto.setElecqupt(parseLongOrDefault(Elecqupt, 0L));
//            dto.setSetresq(parseLongOrDefault(Setresq, 0L));
//            dto.setResuyul(parseDoubleOrDefault(Resutul, 0.0));
//
//
//            // 나머지 값 설정
//            dto.setRemark(row.get(6));
//
//            RP622Dto.add(dto);
//        }
//        return RP622Dto;
//
//    }
//
//    public List<TB_RP623Dto> TB_RP623DtoSet(Map<String, List<String>> result, Map<String, String> dtoValue, String year, String quarter){
//
//
//
//        List<TB_RP623Dto> RP623Dto = new ArrayList<>();
//
//
//        for(int i=1; i < 7; i++){
//            TB_RP623Dto dto = new TB_RP623Dto();
//
//            Integer sequence = i;
//
//            List<String> row = result.get("row" + (i));
//
//            // 3페이지의 첫그래프라서 다시 row1부터 시작함
//            String STANDYM = cleanValue(result.get("row"+ (i)).get(0));
//
//            String PERGUCONELEQ = cleanValue(row.get(1));
//            String PERGUCONELND = cleanValue(row.get(2));
//            String PERGUCONELNQ = cleanValue(row.get(3));
//            String PERGUCONSELEQ = cleanValue(row.get(4));
//            String PERGUCONELYUL = cleanValue(row.get(5));
//            String PERGUCONPSFA = cleanValue(row.get(6));
//            String PERGUCONBKG = cleanValue(row.get(7));
//
//
//            dto.setSpworkcd(dtoValue.get("spworkcd"));
//            dto.setSpcompcd(dtoValue.get("spcompcd"));
//            dto.setSpplancd(dtoValue.get("spplancd"));
//            dto.setSpworknm(dtoValue.get("spworknm"));
//            dto.setSpcompnm(dtoValue.get("spcompnm"));
//            dto.setSpplannm(dtoValue.get("spplannm"));
//            dto.setStandqy(year+quarter);
//            dto.setTermseq(sequence.toString());
//            dto.setStandym(STANDYM);
//            dto.setPerguconeleq(parseDoubleOrDefault(PERGUCONELEQ, 0L));
//            dto.setPerguconelnd(parseDoubleOrDefault(PERGUCONELND, 0L));
//            dto.setPerguconelnq(parseDoubleOrDefault(PERGUCONELNQ, 0L));
//            dto.setPerguconseleq(parseDoubleOrDefault(PERGUCONSELEQ, 0L));
//            dto.setPerguconelyul(parseDoubleOrDefault(PERGUCONELYUL, 0L));
//            dto.setPerguconpsfa(PERGUCONPSFA.equals("PASS") ? "1" : "2");
//            dto.setPerguconbkg(parseDoubleOrDefault(PERGUCONBKG, 0L));
//
//            RP623Dto.add(dto);
//        }
//
//        return RP623Dto;
//    }
//
//
//    public List<TB_RP624Dto> TB_RP624DtoSet(Map<String, List<String>> result, Map<String, String> dtoValue, String year, String quarter){
//
//
//        List<TB_RP624Dto> RP624Dto = new ArrayList<>();
//
//
//        for(int i=1; i < 7; i++){
//            TB_RP624Dto dto = new TB_RP624Dto();
//
//            Integer sequence = i;
//
//            List<String> row = result.get("row"+(i+6));
//
//            // 3페이지의 첫그래프라서 다시 row1부터 시작함
//            String STANDYM = cleanValue(row.get(0));
//
//            String PERGUCONELEQ = cleanValue(row.get(1));
//            String PERGUCONELND = cleanValue(row.get(2));
//            String PERGUCONELNQ = cleanValue(row.get(3));
//            String PERGUCONSELEQ = cleanValue(row.get(4));
//            String PERGUCONELYUL = cleanValue(row.get(5));
//            String PERGUCONPSFA = cleanValue(row.get(6));
//            String PERGUCONBKG = cleanValue(row.get(7));
//
//
//            dto.setSpworkcd(dtoValue.get("spworkcd"));
//            dto.setSpcompcd(dtoValue.get("spcompcd"));
//            dto.setSpplancd(dtoValue.get("spplancd"));
//            dto.setSpworknm(dtoValue.get("spworknm"));
//            dto.setSpcompnm(dtoValue.get("spcompnm"));
//            dto.setSpplannm(dtoValue.get("spplannm"));
//            dto.setStandqy(year+quarter);
//            dto.setTermseq(sequence.toString());
//            dto.setStandym(STANDYM);
//            dto.setWarconeleq(parseDoubleOrDefault(PERGUCONELEQ, 0L));
//            dto.setWarconelnd(parseDoubleOrDefault(PERGUCONELND, 0L));
//            dto.setWarconelnq(parseDoubleOrDefault(PERGUCONELNQ, 0L));
//            dto.setWarconseleq(parseDoubleOrDefault(PERGUCONSELEQ, 0L));
//            dto.setWarconelyul(parseDoubleOrDefault(PERGUCONELYUL, 0L));
//            dto.setWarconpsfa(PERGUCONPSFA.equals("PASS") ? "1" : "2");
//            dto.setWarconbkg(parseDoubleOrDefault(PERGUCONBKG, 0L));
//
//            RP624Dto.add(dto);
//        }
//
//        return RP624Dto;
//    }
//
//    public List<TB_RP625Dto> TB_RP625DtoSet(Map<String, List<String>> result, Map<String, String> dtoValue, String year, String quarter){
//
//
//        List<TB_RP625Dto> RP625Dto = new ArrayList<>();
//
//
//        for(int i=1; i < 7; i++){
//            TB_RP625Dto dto = new TB_RP625Dto();
//
//            List<String> row = result.get("row" + (i+12));
//
//            Integer sequence = i;
//
//            // 3페이지의 첫그래프라서 다시 row1부터 시작함
//            String STANDYM = cleanValue(row.get(0));
//            String FUELCQTY = cleanValue(row.get(1));
//            String DATAUPQ = cleanValue(row.get(3));
//            String LPAFEQTY = cleanValue(row.get(2));
//            String SETNGCQTY = cleanValue(row.get(4));
//            String SETEXSQTY = cleanValue(row.get(5));
//            String SETEXFQTY = cleanValue(row.get(6));
//
//
//            dto.setSpworkcd(dtoValue.get("spworkcd"));
//            dto.setSpcompcd(dtoValue.get("spcompcd"));
//            dto.setSpplancd(dtoValue.get("spplancd"));
//            dto.setSpworknm(dtoValue.get("spworknm"));
//            dto.setSpcompnm(dtoValue.get("spcompnm"));
//            dto.setSpplannm(dtoValue.get("spplannm"));
//            dto.setStandqy(year+quarter);
//            dto.setTermseq(sequence.toString());
//            dto.setStandym(STANDYM);
//            dto.setFuelcqty(parseDoubleOrDefault(FUELCQTY, 0L));
//            dto.setDataupq(parseDoubleOrDefault(DATAUPQ, 0L));
//            dto.setLpafeqty(parseDoubleOrDefault(LPAFEQTY, 0L));
//            dto.setSetngcqty(parseDoubleOrDefault(SETNGCQTY, 0L));
//            dto.setSetexsqty(parseDoubleOrDefault(SETEXSQTY, 0L));
//            dto.setSetexfqty(parseDoubleOrDefault(SETEXFQTY, 0L));
//
//            RP625Dto.add(dto);
//        }
//
//        return RP625Dto;
//    }
//
//    public List<TB_RP626Dto> TB_RP626DtoSet(Map<String, List<String>> result, Map<String, String> dtoValue, String year, String quarter){
//
//
//        List<TB_RP626Dto> RP626Dto = new ArrayList<>();
//
//
//        for(int i=1; i < 7; i++){
//            TB_RP626Dto dto = new TB_RP626Dto();
//
//            List<String> row = result.get("row" + (i+18));
//
//            Integer sequence = i;
//
//            // 3페이지의 첫그래프라서 다시 row1부터 시작함
//            String STANDYM = cleanValue(row.get(0));
//
//            String perguconsesq = cleanValue(row.get(1));
//            String perguconc1Q = cleanValue(row.get(2));
//            String perguconc2q = cleanValue(row.get(3));
//            String perguconcu1q = cleanValue(row.get(4));
//            String perguconfc1q = cleanValue(row.get(5));
//            String pergucons1bkg = cleanValue(row.get(6));
//
//
//            dto.setSpworkcd(dtoValue.get("spworkcd"));
//            dto.setSpcompcd(dtoValue.get("spcompcd"));
//            dto.setSpplancd(dtoValue.get("spplancd"));
//            dto.setSpworknm(dtoValue.get("spworknm"));
//            dto.setSpcompnm(dtoValue.get("spcompnm"));
//            dto.setSpplannm(dtoValue.get("spplannm"));
//            dto.setStandqy(year+quarter);
//            dto.setTermseq(sequence.toString());
//            dto.setStandym(STANDYM);
//            dto.setPerguconsesq(parseDoubleOrDefault(perguconsesq, 0L));
//            dto.setPerguconc1Q(parseDoubleOrDefault(perguconc1Q, 0L));
//            dto.setPerguconc2q(parseDoubleOrDefault(perguconc2q, 0L));
//            dto.setPerguconcu1q(parseDoubleOrDefault(perguconcu1q, 0L));
//            dto.setPerguconfc1q(parseDoubleOrDefault(perguconfc1q, 0L));
//            dto.setPergucons1bkg(parseDoubleOrDefault(pergucons1bkg, 0L));
//
//            RP626Dto.add(dto);
//        }
//
//        return RP626Dto;
//    }
//
//    public List<TB_RP627Dto> TB_RP627DtoSet(Map<String, List<String>> result, Map<String, String> dtoValue, String year, String quarter){
//
//
//        List<TB_RP627Dto> RP627Dto = new ArrayList<>();
//
//
//        for(int i=1; i < 7; i++){
//            TB_RP627Dto dto = new TB_RP627Dto();
//
//            //4페이지 시작하므로 다시 1부터시작
//            List<String> row = result.get("row"+(i+24));
//
//            Integer sequence = i;
//
//            String STANDYM = cleanValue(row.get(0));
//
//            String warconseleq = cleanValue(row.get(1));
//            String warconfuecq = cleanValue(row.get(2));
//            String warconwfucq = cleanValue(row.get(3));
//            String perguconcu2q = cleanValue(row.get(4));
//            String perguconfc2q = cleanValue(row.get(5));
//            String pergucons2bkg = cleanValue(row.get(6));
//
//
//            dto.setSpworkcd(dtoValue.get("spworkcd"));
//            dto.setSpcompcd(dtoValue.get("spcompcd"));
//            dto.setSpplancd(dtoValue.get("spplancd"));
//            dto.setSpworknm(dtoValue.get("spworknm"));
//            dto.setSpcompnm(dtoValue.get("spcompnm"));
//            dto.setSpplannm(dtoValue.get("spplannm"));
//            dto.setStandqy(year+quarter);
//            dto.setTermseq(sequence.toString());
//            dto.setStandym(STANDYM);
//            dto.setWarconseleq(parseDoubleOrDefault(warconseleq, 0L));
//            dto.setWarconfuecq(parseDoubleOrDefault(warconfuecq, 0L));
//
//            dto.setWarconwfucq(parseDoubleOrDefault(warconwfucq, 0L));
//            dto.setPerguconcu2q(parseDoubleOrDefault(perguconcu2q, 0L));
//            dto.setPerguconfc2q(parseDoubleOrDefault(perguconfc2q, 0L));
//            dto.setPergucons2bkg(parseDoubleOrDefault(pergucons2bkg, 0L));
//
//            RP627Dto.add(dto);
//        }
//
//        return RP627Dto;
//    }
//
//
//    public List<TB_RP850Dto> TB_RP850DtoSet(Map<String, Object> result, Map<String, String> dtoValue){
//
//        List<TB_RP850Dto> RP850Dto = new ArrayList<>();
//        for (Map.Entry<String, Object> entry : result.entrySet()) {
//            // 현재 키와 값이 List<String> 인지 확인
//            if (entry.getValue() instanceof List) {
//                List<String> values = (List<String>) entry.getValue();
//
//                // "년"이 포함된 첫 번째 값 가져오기
//                String year = getFirstYearEntry(values);
//                System.out.println("year: " + year);
//
//
//                for (String value : values) {
//                    List<String> splitValues = Arrays.asList(value.split("\\|"));
//
//                    StringBuilder month = new StringBuilder(splitValues.get(1).replace("월", ""));
//
//                    if(month.length() < 2){
//                        month.insert(0, "0");
//                    }
//
//                    TB_RP850Dto dto = new TB_RP850Dto();
//                    dto.setSpworkcd(dtoValue.get("spworkcd"));
//                    dto.setSpcompcd(dtoValue.get("spcompcd"));
//                    dto.setSpplancd(dtoValue.get("spplancd"));
//                    dto.setSpworknm(dtoValue.get("spworknm"));
//                    dto.setSpcompnm(dtoValue.get("spcompnm"));
//                    dto.setSpplannm(dtoValue.get("spplannm"));
//                    dto.setStandym(year.substring(0,4) + month);
//                    dto.setElecresq(parseDoubleOrDefault(splitValues.get(2), 0));
//                    dto.setInuserid(UtilClass.getUserId());
//                    dto.setInusernm(UtilClass.getUsername());
//
//                    RP850Dto.add(dto);
//                }
//            }
//        }
//
//        return RP850Dto;
//    }
//
//    public List<TB_RP840Dto> TB_RP840DtoSet(Map<String, Object> result, Map<String, String> dtoValue){
//
//        List<TB_RP840Dto> TB_RP840Dto = new ArrayList<>();
//        for (Map.Entry<String, Object> entry : result.entrySet()) {
//            // 현재 키와 값이 List<String> 인지 확인
//            if (entry.getValue() instanceof List) {
//                List<String> values = (List<String>) entry.getValue();
//
//
//
//
//                for (String value : values) {
//
//                    List<String> splitValues = Arrays.asList(value.split("\\|"));
//
//
//                    double corrnm = 0;
//                    if(splitValues.size() > 8){
//                        corrnm = parseDoubleOrDefault(splitValues.get(8), 0);
//                    }else{
//                        corrnm = 0;
//
//                    }
//
//
//                    TB_RP840Dto dto = new TB_RP840Dto();
//                    dto.setSpworkcd(dtoValue.get("spworkcd"));
//                    dto.setSpcompcd(dtoValue.get("spcompcd"));
//                    dto.setSpplancd(dtoValue.get("spplancd"));
//                    dto.setSpworknm(dtoValue.get("spworknm"));
//                    dto.setSpcompnm(dtoValue.get("spcompnm"));
//                    dto.setSpplannm(dtoValue.get("spplannm"));
//                    dto.setStandym(splitValues.get(0).replace(".", ""));
//                    dto.setElecreaq(parseDoubleOrDefault(splitValues.get(2), 0));
//                    dto.setHhvmjq(parseDoubleOrDefault(splitValues.get(3), 0));
//                    dto.setHhvkcalq(parseDoubleOrDefault(splitValues.get(4), 0));
//                    dto.setHhvnm3(parseDoubleOrDefault(splitValues.get(5), 0));
//                    dto.setLhvkcalq(parseDoubleOrDefault(splitValues.get(6), 0));
//                    dto.setLhvkcalmq(parseDoubleOrDefault(splitValues.get(7), 0));
//                    dto.setCorrnm3(corrnm);
//                    dto.setInuserid(UtilClass.getUserId());
//                    dto.setInusernm(UtilClass.getUsername());
//
//                    TB_RP840Dto.add(dto);
//
//                }
//            }
//        }
//
//        return TB_RP840Dto;
//    }
//
//
//
//    //얘는 사실 별로 필요없다.
//    private String cleanValue(String value) {
//        return value.replaceAll("[,\\s%]", "");
//    }
//
//    private long parseLongOrDefault(String value, long defaultValue) {
//        try {
//            return !value.isEmpty() ? Long.parseLong(value) : defaultValue;
//        } catch (NumberFormatException e) {
//            return defaultValue;
//        }
//    }
//
//    private double parseDoubleOrDefault(String value, double defaultValue) {
//        try {
//            // 콤마, 괄호, 공백, %, 및 기타 불필요한 문자들을 제거한 후 Double로 변환
//            String cleanedValue = value.replaceAll("[,\\s()%]", "");
//
//            return !cleanedValue.isEmpty() ? Double.parseDouble(cleanedValue) : defaultValue;
//        } catch (NumberFormatException e) {
//            return defaultValue;
//        }
//    }
//
//    public String getFirstYearEntry(List<String> result) {
//        // Map의 각 엔트리를 순회하면서 "년"이 포함된 항목을 찾기
//
//        String year = "";
//
//        for(String value : result){
//            if(value.contains("년") && !value.contains("년월")){
//                year = value;
//            }
//
//        }
//
//        // "년"이 포함된 항목이 없으면 null 반환
//        return year;
//    }
//
//    public static String getOrDefault(List<String> list, int index, String defaultValue) {
//        try {
//            return list.get(index);
//        } catch (IndexOutOfBoundsException e) {
//            return defaultValue;
//        }
//    }
//
//
//
//}
