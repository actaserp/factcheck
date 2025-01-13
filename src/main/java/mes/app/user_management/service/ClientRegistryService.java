package mes.app.user_management.service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.repository.actasRepository.TB_USERINFORepository;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ClientRegistryService {

    @Autowired
    TB_USERINFORepository tb_userinfoRepository;

    @Autowired
    SqlRunner sqlRunner;

   /* public List<Map<String, Object>> getList(String startDate, String endDate, String searchUserNm) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
        SELECT
            tu.USERID,
            tu.USERNM AS userNm,
            tu.SEXYN AS sexYN,
            tu.USERHP AS userHP,
            tu.POSTCD,
            tu.USERADDR AS userAddr,
            tu.USERMAIL AS userEmail,
            tu.INDATEM AS indatem,
            COUNT(se.REQDATE) AS InquiryCount -- 사용자의 검색 횟수 카운트
        FROM
            TB_USERINFO tu
        LEFT JOIN
            TB_SEARCHINFO se
            ON se.USERID = tu.USERID
        """);

        // WHERE 절 동적 조건 추가
        sql.append("WHERE 1=1 "); // 기본 조건 (추가 조건 연결을 용이하게 하기 위함)

        if (startDate != null && !startDate.isEmpty()) {
            sql.append("AND se.REQDATE >= :startDate ");
            params.addValue("startDate", startDate);
        }

        if (endDate != null && !endDate.isEmpty()) {
            sql.append("AND se.REQDATE <= :endDate ");
            params.addValue("endDate", endDate);
        }

        if (searchUserNm != null && !searchUserNm.isEmpty()) {
            sql.append("AND tu.USERNM LIKE :searchUserNm ");
            params.addValue("searchUserNm", "%" + searchUserNm + "%");
        }

        // GROUP BY 및 ORDER BY 추가
        sql.append("""
        GROUP BY
            tu.USERID,
            tu.USERNM,
            tu.SEXYN,
            tu.USERHP,
            tu.POSTCD,
            tu.USERADDR,
            tu.USERMAIL,
            tu.INDATEM
        ORDER BY
            InquiryCount DESC
    """);

        //log.info("회원관리List SQL: {}", sql);
        //log.info("SQL Parameters: {}", params.getValues());

        // 결과 반환
        return sqlRunner.getRows(sql.toString(), params);
    }*/
   public List<Map<String, Object>> getList(String startDate, String endDate, String searchUserNm) {
       MapSqlParameterSource params = new MapSqlParameterSource();
       StringBuilder sql = new StringBuilder("""
        SELECT
            tu.USERID,
            tu.USERNM AS userNm,
            tu.SEXYN AS sexYN,
            tu.USERHP AS userHP,
            tu.POSTCD,
            tu.USERADDR AS userAddr,
            tu.USERMAIL AS userEmail,
            tu.INDATEM AS indatem,
            COUNT(se.REQDATE) AS InquiryCount -- 사용자의 검색 횟수 카운트
        FROM
            TB_USERINFO tu
        LEFT JOIN
            TB_SEARCHINFO se
            ON se.USERID = tu.USERID
            AND (:startDate IS NULL OR se.REQDATE >= :startDate)
            AND (:endDate IS NULL OR se.REQDATE <= :endDate)
    """);

       // WHERE 절 동적 조건 추가
       sql.append("WHERE 1=1 "); // 기본 조건 (추가 조건 연결을 용이하게 하기 위함)
       if (startDate != null && !startDate.isEmpty()) {
           sql.append("AND (:startDate IS NULL OR tu.INDATEM >= :startDate) ");
           params.addValue("startDate", startDate);
       }
       if (endDate != null && !endDate.isEmpty()) {
           sql.append("AND (:endDate IS NULL OR tu.INDATEM <= :endDate) ");
           params.addValue("endDate", endDate);
       }
       if (searchUserNm != null && !searchUserNm.isEmpty()) {
           sql.append("AND tu.USERNM LIKE :searchUserNm ");
           params.addValue("searchUserNm", "%" + searchUserNm + "%");
       }

       // GROUP BY 및 ORDER BY 추가
       sql.append("""
        GROUP BY
            tu.USERID,
            tu.USERNM,
            tu.SEXYN,
            tu.USERHP,
            tu.POSTCD,
            tu.USERADDR,
            tu.USERMAIL,
            tu.INDATEM
        ORDER BY
            InquiryCount DESC
    """);

      // log.info("회원관리List SQL: {}", sql);
       //log.info("SQL Parameters: {}", params.getValues());

       // 결과 반환
       return sqlRunner.getRows(sql.toString(), params);
   }


}
