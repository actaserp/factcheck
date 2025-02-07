//package mes.app.actas_inspec.LTSA;
//
//
//import mes.domain.DTO.TB_RP620Dto;
//import mes.domain.DTO.TB_RP621Dto;
//import mes.domain.DTO.TB_RP622Dto;
//import mes.domain.entity.actasEntity.*;
//import mes.domain.repository.TB_RP620Repository;
//import mes.domain.repository.TB_RP621Repository;
//import mes.domain.repository.TB_RP622Repository;
//import mes.domain.services.SqlRunner;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//public class LTSAListService {
//
//    @Autowired
//    SqlRunner sqlRunner;
//
//    @Autowired
//    TB_RP622Repository tbRp622Repository;
//    @Autowired
//    TB_RP620Repository tbRp620Repository;
//    @Autowired
//    TB_RP621Repository tbRp621Repository;
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
//
//
//
//
//
//    public List<Map<String, Object>> getSearchCondition_Outline(String spworkcd, String spcompcd, String spplancd){
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//        String sql = """
//                    select distinct SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) as year from tb_rp620
//                    where spworkcd = :spworkcd
//                    and spcompcd = :spcompcd
//                    and spplancd = :spplancd
//                    order by SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) desc;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//        return items;
//    }
//
//    public List<Map<String, Object>> getList(String standqy, String spworkcd, String spcompcd, String spplancd) {
//
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("standqy", standqy);
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//
//        String sql = """
//                    select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy, standym, TRUNC(elecres) as elecres, TRUNC(elecqupt) as elecqupt,
//                    TRUNC(setresq) as setresq, RTRIM(TO_CHAR(resuyul, 'FM9990.9999'), '.') as resuyul
//                    from tb_rp622 where standqy = :standqy
//                    and spworkcd = :spworkcd
//                    and spcompcd = :spcompcd
//                    and spplancd = :spplancd
//                    order by termseq;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//        return items;
//    }
//
//    public List<Map<String, Object>> getOutLineList(String year, String spworkcd, String spcompcd, String spplancd) {
//
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("year",year + "%");
//        dicParam.addValue("spworkcd",spworkcd);
//        dicParam.addValue("spcompcd",spcompcd);
//        dicParam.addValue("spplancd",spplancd);
//
//        String sql = """
//                    select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy, purpose, projectnm, equipstat, reportout, indatem from tb_rp620
//                    where standqy like :year
//                    and spworkcd = :spworkcd
//                    and spcompcd = :spcompcd
//                    and spplancd = :spplancd
//                    order by standqy desc;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//        return items;
//    }
//
//    @Transactional
//    public void saveOutline(TB_RP620Dto dto) {
//
//        try{
//
//            TB_RP620Id Id = new TB_RP620Id();
//            Id.setSpcompcd(dto.getSpcompcd());
//            Id.setSpworkcd(dto.getSpworkcd());
//            Id.setSpplancd(dto.getSpplancd());
//            Id.setStandqy(dto.getStandqy());
//
//            Optional<TB_RP620> find = tbRp620Repository.findById(Id); //영속 상태
//
//            if(find.isPresent()){
//                find.get().setPurpose(dto.getPurpose());
//                find.get().setProjectnm(dto.getProjectnm());
//                find.get().setEquipstat(dto.getEquipstat());
//                find.get().setReportout(dto.getReportout());
//                find.get().setInusernm(dto.getInusernm());
//                find.get().setInuserid(dto.getInuserid());
//
//            }
//
//        }catch (Exception e){
//            System.err.println("Error1 : " + e.getMessage());
//
//        }
//    }
//
//    public List<Map<String, Object>> getWcAndSummaryList(String year, String spworkcd, String spcompcd, String spplancd) {
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("year",year + "%");
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//        String sql = """
//                select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy,\s
//                resultsdt, resultedt, purpose, projectnm, equipstat, reportout, yratescond, yratewcond, yraterem, yeffiscond,
//                yeffiwcond, yeffirem
//                from tb_rp620
//                where standqy like :year
//                and spworkcd = :spworkcd
//                and spcompcd = :spcompcd
//                and spplancd = :spplancd
//                order by standqy desc;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//        return items;
//    }
//
//    @Transactional
//    public void saveWcAndSummary(TB_RP620Dto dto) {
//
//        try{
//
//            TB_RP620Id Id = new TB_RP620Id();
//            Id.setSpcompcd(dto.getSpcompcd());
//            Id.setSpworkcd(dto.getSpworkcd());
//            Id.setSpplancd(dto.getSpplancd());
//            Id.setStandqy(dto.getStandqy());
//
//            Optional<TB_RP620> find = tbRp620Repository.findById(Id); //영속 상태
//
//            if(find.isPresent()){
//                find.get().setPurpose(dto.getPurpose());
//                find.get().setProjectnm(dto.getProjectnm());
//                find.get().setEquipstat(dto.getEquipstat());
//                find.get().setReportout(dto.getReportout());
//                find.get().setInusernm(dto.getInusernm());
//                find.get().setInuserid(dto.getInuserid());
//                find.get().setResultsdt(dto.getResultsdt());
//                find.get().setResultedt(dto.getResultedt());
//                find.get().setYratescond(dto.getYratescond());
//                find.get().setYratewcond(dto.getYratewcond());
//                find.get().setYraterem(dto.getYraterem());
//                find.get().setYeffiscond(dto.getYeffiscond());
//                find.get().setYeffiwcond(dto.getYeffiwcond());
//                find.get().setYeffirem(dto.getYeffirem());
//
//            }
//
//        }catch (Exception e){
//            System.err.println("Error1 : " + e.getMessage());
//
//        }
//    }
//
//    public List<Map<String, Object>> getWcAndSummaryList2(String year, String spworkcd, String spcompcd, String spplancd) {
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("year",year + "%");
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//        String sql = """
//               select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy,
//               yratescond, yratesexec, yratesrerm, yeffiscond, yeffisrerm, yratewcond, yeffisexec, yeffiwrerm, yeffiwcond
//               from tb_rp621
//               where standqy like :year
//               and spworkcd = :spworkcd
//               and spcompcd = :spcompcd
//               and spplancd = :spplancd
//               order by standqy desc;
//               """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//        return items;
//    }
//
//    @Transactional
//    public void saveWcAndSummary2(TB_RP621Dto dto) {
//
//        try{
//
//            TB_RP621Id Id = new TB_RP621Id();
//            Id.setSpcompcd(dto.getSpcompcd());
//            Id.setSpworkcd(dto.getSpworkcd());
//            Id.setSpplancd(dto.getSpplancd());
//            Id.setStandqy(dto.getStandqy());
//
//            Optional<TB_RP621> find = tbRp621Repository.findById(Id); //영속 상태
//
//            if(find.isPresent()){
//                find.get().setInusernm(dto.getInusernm());
//                find.get().setInuserid(dto.getInuserid());
//                find.get().setYratescond(dto.getYratescond());
//                find.get().setYratewcond(dto.getYratewcond());
//                find.get().setYratesrerm(dto.getYratesrerm());
//                find.get().setYeffiscond(dto.getYeffiscond());
//                find.get().setYeffiwcond(dto.getYeffiwcond());
//                find.get().setYeffisrerm(dto.getYeffisrerm());
//                //save호출안해도 db반영됨 트랜잭션 끝나서 flush를 호출할것임
//            }
//
//        }catch (Exception e){
//            System.err.println("Error1 : " + e.getMessage());
//
//        }
//    }
//
//
//    public List<Map<String, Object>> getSystemUtil(String year, String spworkcd, String spcompcd, String spplancd) {
//
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("year",year + "%");
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//
//        String sql = """
//                    select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy,
//                    standym, TRUNC(dayscnt) AS dayscnt, TO_CHAR( TRUNC(elecres), 'FM999,999,999,999') as elecres,
//                    TO_CHAR(TRUNC(elecqupt), 'FM999,999,999,999') as elecqupt,
//                    TO_CHAR(TRUNC(setresq), 'FM999,999,999,999') as setresq,
//                    TO_CHAR(resuyul, 'FM999,999,990.9') AS resuyul,
//                    termseq,
//                    case when remark = 'null' then '' else remark end as remark
//                    from tb_rp622
//                    where standqy like :year
//                    and spworkcd = :spworkcd
//                    and spcompcd = :spcompcd
//                    and spplancd = :spplancd
//                    order by standqy, termseq asc;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//        return items;
//    }
//
//    @Transactional
//    public void saveSystemUtil(TB_RP622Dto dto) {
//
//        try{
//
//            TB_RP622Id Id = new TB_RP622Id();
//            Id.setSpcompcd(dto.getSpcompcd());
//            Id.setSpworkcd(dto.getSpworkcd());
//            Id.setSpplancd(dto.getSpplancd());
//            Id.setStandqy(dto.getStandqy());
//            Id.setTermseq(dto.getTermseq());
//
//            Optional<TB_RP622> find = tbRp622Repository.findById(Id); //영속 상태
//
//            if(find.isPresent()){
//                find.get().setInusernm(dto.getInusernm());
//                find.get().setInuserid(dto.getInuserid());
//                find.get().setDayscnt(dto.getDayscnt());
//                find.get().setElecres(dto.getElecres());
//                find.get().setElecqupt(dto.getElecqupt());
//                find.get().setSetresq(dto.getSetresq());
//                find.get().setResuyul(dto.getResuyul());
//                find.get().setRemark(dto.getRemark());
//                //save호출안해도 db반영됨 트랜잭션 끝나서 flush를 호출할것임
//            }
//
//        }catch (Exception e){
//            System.err.println("Error1 : " + e.getMessage());
//
//        }
//    }
//
//    public List<Map<String, Object>> getPerformanceGuarantee(String year, String spworkcd, String spcompcd, String spplancd) {
//
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("year",year + "%");
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//        String sql = """
//                    select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy,
//                    standym,
//                    TO_CHAR(TRUNC(perguconeleq), 'FM999,999,999,999') as perguconeleq,
//                    round(perguconelnd, 1) as perguconelnd,
//                    TO_CHAR(TRUNC(perguconelnq), 'FM999,999,999,999') as perguconelnq,
//                    TO_CHAR(TRUNC(perguconseleq), 'FM999,999,999,999') as perguconseleq,
//                    case when perguconpsfa = '1' then 'PASS' else 'FAIL' END as perguconpsfa,
//                    ROUND(perguconelyul, 1) as perguconelyul,
//                    TO_CHAR(TRUNC(perguconbkg), 'FM999,999,999,999') as perguconbkg, termseq
//                    from tb_rp623
//                    where standqy like :year
//                    and spworkcd = :spworkcd
//                    and spcompcd = :spcompcd
//                    and spplancd = :spplancd
//                    order by standqy, termseq asc
//                    ;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//        return items;
//    }
//
//
//    @Transactional
//    public void savePerformancdGuarantee(TB_RP623Dto dto) {
//
//        try{
//            TB_RP623Id Id = new TB_RP623Id();
//            Id.setSpcompcd(dto.getSpcompcd());
//            Id.setSpworkcd(dto.getSpworkcd());
//            Id.setSpplancd(dto.getSpplancd());
//            Id.setStandqy(dto.getStandqy());
//            Id.setTermseq(dto.getTermseq());
//
//            Optional<TB_RP623> find = tbRp623Repository.findById(Id); //영속 상태
//
//            if(find.isPresent()){
//                find.get().setInusernm(dto.getInusernm());
//                find.get().setInuserid(dto.getInuserid());
//                find.get().setPerguconeleq(dto.getPerguconeleq());
//                find.get().setPerguconelnd(dto.getPerguconelnd());
//                find.get().setPerguconelnq(dto.getPerguconelnq());
//                find.get().setPerguconseleq(dto.getPerguconseleq());
//                find.get().setPerguconelyul(dto.getPerguconelyul());
//                find.get().setPerguconbkg(dto.getPerguconbkg());
//                find.get().setPerguconpsfa((dto.getPerguconpsfa().equals("PASS")) ? "1" : "2");
//            }
//
//        }catch (Exception e){
//            System.err.println("Error1 : " + e.getMessage());
//
//        }
//    }
//
//    public List<Map<String, Object>> getPerformanceWarranty(String year, String spworkcd, String spcompcd, String spplancd) {
//
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("year",year + "%");
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//        String sql = """
//                    select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy,
//                    standym,
//                    termseq,
//                    TO_CHAR(TRUNC(warconeleq), 'FM999,999,999,999') as warconeleq,
//                    ROUND(warconelnd, 1) as warconelnd,
//                    TO_CHAR(TRUNC(warconelnq), 'FM999,999,999,999') as warconelnq,
//                    TO_CHAR(TRUNC(warconseleq), 'FM999,999,999,999') as warconseleq,
//                    ROUND(warconelyul, 1) as warconelyul,
//                    case when warconpsfa = '1' then 'PASS' else 'FAIL' END as warconpsfa,
//                    TO_CHAR(TRUNC(warconbkg), 'FM999,999,999,999') as warconbkg
//                    from tb_rp624
//                    where standqy like :year
//                    and spworkcd = :spworkcd
//                    and spcompcd = :spcompcd
//                    and spplancd = :spplancd
//                    order by standqy, termseq asc;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//        return items;
//    }
//
//    @Transactional
//    public void savePerformancdWarranty(TB_RP624Dto dto) {
//
//        try{
//            TB_RP624Id Id = new TB_RP624Id();
//            Id.setSpcompcd(dto.getSpcompcd());
//            Id.setSpworkcd(dto.getSpworkcd());
//            Id.setSpplancd(dto.getSpplancd());
//            Id.setStandqy(dto.getStandqy());
//            Id.setTermseq(dto.getTermseq());
//
//            Optional<TB_RP624> find = tbRp624Repository.findById(Id); //영속 상태
//
//            if(find.isPresent()){
//                find.get().setInusernm(dto.getInusernm());
//                find.get().setInuserid(dto.getInuserid());
//                find.get().setWarconeleq(dto.getWarconeleq());
//                find.get().setWarconelnd(dto.getWarconelnd());
//                find.get().setWarconelnq(dto.getWarconelnq());
//                find.get().setWarconseleq(dto.getWarconseleq());
//                find.get().setWarconelyul(dto.getWarconelyul());
//                find.get().setWarconbkg(dto.getWarconbkg());
//                find.get().setWarconpsfa((dto.getWarconpsfa().equals("PASS")) ? "1" : "2");
//                //save호출안해도 db반영됨 트랜잭션 끝나서 flush를 호출할것임
//            }
//
//        }catch (Exception e){
//            System.err.println("Error1 : " + e.getMessage());
//
//        }
//    }
//
//    public List<Map<String, Object>> getFuelEfficiency(String year, String spworkcd, String spcompcd, String spplancd) {
//
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("year",year + "%");
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//        String sql = """
//                    select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy,
//                    standym, termseq,
//                    TO_CHAR(TRUNC(fuelcqty), 'FM999,999,999,999') as fuelcqty,
//                    TO_CHAR(TRUNC(dataupq), 'FM999,999,999,999') as dataupq,
//                    TO_CHAR(TRUNC(exngcqty), 'FM999,999,999,999') as exngcqty,
//                    TO_CHAR(TRUNC(lpafeqty), 'FM999,999,999,999') as lpafeqty,
//                    TO_CHAR(TRUNC(setngcqty), 'FM999,999,999,999') as setngcqty,
//                    TO_CHAR(TRUNC(setexsqty), 'FM999,999,999,999') as setexsqty,
//                    TO_CHAR(TRUNC(setexfqty), 'FM999,999,999,999') as setexfqty
//                    from tb_rp625
//                    where standqy like :year
//                    and spworkcd = :spworkcd
//                    and spcompcd = :spcompcd
//                    and spplancd = :spplancd
//                    order by standqy, termseq asc;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//        return items;
//    }
//
//    @Transactional
//    public void saveFuelEfficiencySave(TB_RP625Dto dto) {
//
//        try{
//            TB_RP625Id Id = new TB_RP625Id();
//            Id.setSpcompcd(dto.getSpcompcd());
//            Id.setSpworkcd(dto.getSpworkcd());
//            Id.setSpplancd(dto.getSpplancd());
//            Id.setStandqy(dto.getStandqy());
//            Id.setTermseq(dto.getTermseq());
//
//            Optional<TB_RP625> find = tbRp625Repository.findById(Id); //영속 상태
//
//            if(find.isPresent()){
//                find.get().setInusernm(dto.getInusernm());
//                find.get().setInuserid(dto.getInuserid());
//                find.get().setFuelcqty(dto.getFuelcqty());
//                find.get().setDataupq(dto.getDataupq());
//                find.get().setExngcqty(dto.getExngcqty());
//                find.get().setLpafeqty(dto.getLpafeqty());
//                find.get().setSetngcqty(dto.getSetngcqty());
//                find.get().setSetexsqty(dto.getSetexsqty());
//                find.get().setSetexfqty(dto.getSetexfqty());
//
//                //save호출안해도 db반영됨 트랜잭션 끝나서 flush를 호출할것임
//            }
//
//        }catch (Exception e){
//            System.err.println("Error1 : " + e.getMessage());
//
//        }
//    }
//
//    public List<Map<String, Object>> getFuelPerformRead(String year, String spworkcd, String spcompcd, String spplancd) {
//
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("year",year + "%");
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//        String sql = """
//                    select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy,
//                    standym, termseq,
//                    TO_CHAR(TRUNC(perguconsesq), 'FM999,999,999,999') as perguconsesq,
//                    TO_CHAR(TRUNC(perguconc1q), 'FM999,999,999,999') as perguconc1q,
//                    TO_CHAR(TRUNC(perguconc2q), 'FM999,999,999,999') as perguconc2q,
//                    TO_CHAR(TRUNC(perguconcu1q), 'FM999,999,999,999') as perguconcu1q,
//                    TO_CHAR(TRUNC(perguconfc1q), 'FM999,999,999,999') as perguconfc1q,
//                    TO_CHAR(TRUNC(pergucons1bkg), 'FM999,999,999,999') as pergucons1bkg
//                    from tb_rp626
//                    where standqy like :year
//                    and spworkcd = :spworkcd
//                    and spcompcd = :spcompcd
//                    and spplancd = :spplancd
//                    order by standqy, termseq asc;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//        return items;
//    }
//
//
//    @Transactional
//    public void saveFuelPerformSave(TB_RP626Dto dto) {
//
//        try{
//            TB_RP626Id Id = new TB_RP626Id();
//            Id.setSpcompcd(dto.getSpcompcd());
//            Id.setSpworkcd(dto.getSpworkcd());
//            Id.setSpplancd(dto.getSpplancd());
//            Id.setStandqy(dto.getStandqy());
//            Id.setTermseq(dto.getTermseq());
//
//            Optional<TB_RP626> find = tbRp626Repository.findById(Id); //영속 상태
//
//            if(find.isPresent()){
//                find.get().setInusernm(dto.getInusernm());
//                find.get().setInuserid(dto.getInuserid());
//                find.get().setPerguconsesq(dto.getPerguconsesq());
//                find.get().setPerguconc1Q(dto.getPerguconc1Q());
//                find.get().setPerguconc2q(dto.getPerguconc2q());
//                find.get().setPerguconcu1q(dto.getPerguconcu1q());
//                find.get().setPerguconfc1q(dto.getPerguconfc1q());
//                find.get().setPergucons1bkg(dto.getPergucons1bkg());
//
//                //save호출안해도 db반영됨 트랜잭션 끝나서 flush를 호출할것임
//            }
//
//        }catch (Exception e){
//            System.err.println("Error1 : " + e.getMessage());
//
//        }
//    }
//
//
//    public List<Map<String, Object>> getFuelWarrantyRead(String year, String spworkcd, String spcompcd, String spplancd) {
//
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("year",year + "%");
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//        String sql = """
//                    select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy,
//                    standym, termseq,
//                    TO_CHAR(TRUNC(warconseleq), 'FM999,999,999,999') as warconseleq,
//                    TO_CHAR(TRUNC(warconfuecq), 'FM999,999,999,999') as warconfuecq,
//                    TO_CHAR(TRUNC(warconwfucq), 'FM999,999,999,999') as warconwfucq,
//                    TO_CHAR(TRUNC(perguconcu2q), 'FM999,999,999,999') as perguconcu2q,
//                    TO_CHAR(TRUNC(perguconfc2q), 'FM999,999,999,999') as perguconfc2q,
//                    TO_CHAR(TRUNC(pergucons2bkg), 'FM999,999,999,999') as pergucons2bkg
//                    from tb_rp627
//                    where standqy like :year
//                    and spworkcd = :spworkcd
//                    and spcompcd = :spcompcd
//                    and spplancd = :spplancd
//                    order by standqy, termseq asc;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//        return items;
//    }
//
//    @Transactional
//    public void saveFuelWarrantySave(TB_RP627Dto dto) {
//
//        try{
//            TB_RP627Id Id = new TB_RP627Id();
//            Id.setSpcompcd(dto.getSpcompcd());
//            Id.setSpworkcd(dto.getSpworkcd());
//            Id.setSpplancd(dto.getSpplancd());
//            Id.setStandqy(dto.getStandqy());
//            Id.setTermseq(dto.getTermseq());
//
//            Optional<TB_RP627> find = tbRp627Repository.findById(Id); //영속 상태
//
//            if(find.isPresent()){
//                find.get().setInusernm(dto.getInusernm());
//                find.get().setInuserid(dto.getInuserid());
//                find.get().setWarconseleq(dto.getWarconseleq());
//                find.get().setWarconfuecq(dto.getWarconfuecq());
//                find.get().setWarconwfucq(dto.getWarconwfucq());
//                find.get().setPerguconcu2q(dto.getPerguconcu2q());
//                find.get().setPerguconfc2q(dto.getPerguconfc2q());
//                find.get().setPergucons2bkg(dto.getPergucons2bkg());
//
//                //save호출안해도 db반영됨 트랜잭션 끝나서 flush를 호출할것임
//            }
//
//        }catch (Exception e){
//            System.err.println("Error1 : " + e.getMessage());
//
//        }
//    }
//
//    public List<Map<String, Object>> getSystemUtil_Period(String startDate, String endDate, String spworkcd, String spcompcd, String spplancd) {
//
//
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("startDate", startDate);
//        dicParam.addValue("endDate", endDate);
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//
//        String sql = """
//                    select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy,
//                    standym, TRUNC(dayscnt) AS dayscnt, TO_CHAR( TRUNC(elecres), 'FM999,999,999,999') as elecres,
//                    TO_CHAR(TRUNC(elecqupt), 'FM999,999,999,999') as elecqupt,
//                    TO_CHAR(TRUNC(setresq), 'FM999,999,999,999') as setresq,
//                    TO_CHAR(resuyul, 'FM999,999,990.9') AS resuyul,
//                    termseq,
//                    case when remark = 'null' then '' else remark end as remark
//                    from tb_rp622
//                    where standqy between :startDate and :endDate
//                    and spworkcd = :spworkcd
//                    and spcompcd = :spcompcd
//                    and spplancd = :spplancd
//                    and termseq not in ('1', '6')
//                    order by standqy, termseq asc;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//
//        if(!items.isEmpty()){
//            Map<String, BigDecimal> sumMap = calculateSum(items, List.of("dayscnt", "elecres", "elecqupt", "setresq", "resuyul"));
//
//
//            Map<String, Object> item = Map.of("standym", "총계(분기 계 제외)",
//                    "dayscnt", sumMap.get("dayscnt"),
//                    "elecres", sumMap.get("elecres"),
//                    "elecqupt", sumMap.get("elecqupt"),
//                    "setresq", sumMap.get("setresq"),
//                    "resuyul", sumMap.get("resuyul")
//            );
//
//            items.add(item);
//
//        }
//
//
//        return items;
//    }
//
//    public Map<String, BigDecimal> calculateSum(List<Map<String, Object>> items, List<String> keys) {
//        // 초기값: 키에 해당하는 합계를 저장할 Map 생성
//        Map<String, BigDecimal> result = keys.stream()
//                .collect(Collectors.toMap(key -> key, key -> BigDecimal.ZERO)); // 각 키의 초기값을 BigDecimal.ZERO로 설정
//
//        // "termseq" 키가 5인 데이터 제외
//        items.stream()
//                .filter(item -> !"5".equals(String.valueOf(item.getOrDefault("termseq", "")))) // termseq가 5인 데이터 제외
//                .forEach(item -> {
//                    keys.forEach(key -> {
//                        Object value = item.getOrDefault(key, BigDecimal.ZERO); // 기본값 BigDecimal.ZERO 사용
//                        BigDecimal bigDecimalValue = BigDecimal.ZERO;
//
//                        try {
//                            if (value instanceof BigDecimal) {
//                                bigDecimalValue = (BigDecimal) value;
//                            } else if (value instanceof String) {
//                                // 쉼표 제거 후 BigDecimal로 변환
//                                bigDecimalValue = new BigDecimal(((String) value).replace(",", ""));
//                            } else if (value instanceof Number) {
//                                bigDecimalValue = BigDecimal.valueOf(((Number) value).doubleValue());
//                            }
//                        } catch (NumberFormatException | NullPointerException e) {
//                            System.err.println("Invalid number format for key: " + key + ", value: " + value);
//                        }
//
//                        // 기존 값에 누적
//                        result.put(key, result.get(key).add(bigDecimalValue));
//                    });
//                });
//
//        return result;
//    }
//
//
//    public List<Map<String, Object>> getFuelEfficiency_Period(String startDate, String endDate, String spworkcd, String spcompcd, String spplancd) {
//
//
//        MapSqlParameterSource dicParam = new MapSqlParameterSource();
//        dicParam.addValue("startDate", startDate);
//        dicParam.addValue("endDate", endDate);
//        dicParam.addValue("spworkcd", spworkcd);
//        dicParam.addValue("spcompcd", spcompcd);
//        dicParam.addValue("spplancd", spplancd);
//
//
//        String sql = """
//                select SUBSTRING(standqy FROM 1 FOR LENGTH(standqy) - 1) || '.' || SUBSTRING(standqy FROM LENGTH(standqy)) AS standqy,
//                standym, termseq,
//                TO_CHAR(TRUNC(fuelcqty), 'FM999,999,999,999') as fuelcqty,
//                TO_CHAR(TRUNC(dataupq), 'FM999,999,999,999') as dataupq,
//                TO_CHAR(TRUNC(exngcqty), 'FM999,999,999,999') as exngcqty,
//                TO_CHAR(TRUNC(lpafeqty), 'FM999,999,999,999') as lpafeqty,
//                TO_CHAR(TRUNC(setngcqty), 'FM999,999,999,999') as setngcqty,
//                TO_CHAR(TRUNC(setexsqty), 'FM999,999,999,999') as setexsqty,
//                TO_CHAR(TRUNC(setexfqty), 'FM999,999,999,999') as setexfqty
//                from tb_rp625
//                where standqy between :startDate and :endDate
//                and spworkcd = :spworkcd
//                and spcompcd = :spcompcd
//                and spplancd = :spplancd
//                and termseq not in ('1', '6')
//                order by standqy, termseq asc;
//                """;
//        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
//
//
//        if(!items.isEmpty()){
//            Map<String, BigDecimal> sumMap = calculateSum(items, List.of("fuelcqty", "dataupq", "exngcqty", "lpafeqty", "setngcqty", "setexsqty", "setexfqty"));
//
//
//            Map<String, Object> item = Map.of("standym", "총계(분기 계 제외)",
//                    "fuelcqty", sumMap.get("fuelcqty"),
//                    "dataupq", sumMap.get("dataupq"),
//                    "exngcqty", sumMap.get("exngcqty"),
//                    "lpafeqty", sumMap.get("lpafeqty"),
//                    "setngcqty", sumMap.get("setngcqty"),
//                    "setexsqty", sumMap.get("setexsqty"),
//                    "setexfqty", sumMap.get("setexfqty")
//            );
//
//            items.add(item);
//        }
//
//
//        return items;
//    }
//}
