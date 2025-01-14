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

    public List<Map<String, Object>> getList(String startDate, String endDate, String searchUserNm) {
      MapSqlParameterSource params = new MapSqlParameterSource();
      StringBuilder sql = new StringBuilder("""
        SELECT
           tu.USERID,
           tu.USERGU AS userGU,
           tu.USERSIDO AS usersido,
           tu.USERNM AS userNm,
           tu.SEXYN AS sexYN,
           tu.USERHP AS userHP,
           tu.POSTCD,
           tu.USERADDR AS userAddr,
           tu.USERMAIL AS userEmail,
           tu.INDATEM AS indatem, -- 등록일자
           COALESCE(COUNT(se.REQDATE), 0) AS InquiryCount -- 조회 카운트
       FROM
           TB_USERINFO tu
       LEFT JOIN TB_SEARCHINFO se
       ON tu.USERID = se.USERID
           AND se.REQDATE >= :startDate
           AND se.REQDATE <= :endDate
       WHERE 1=1
       """);

      // WHERE 절 동적 조건 추가
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND tu.INDATEM >= :startDate "); // 공백 추가
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND tu.INDATEM <= :endDate "); // 공백 추가
            params.addValue("endDate", endDate);
        }
        if (searchUserNm != null && !searchUserNm.isEmpty()) {
            sql.append(" AND tu.USERNM LIKE :searchUserNm "); // 공백 추가
            params.addValue("searchUserNm", "%" + searchUserNm + "%");
        }


        // GROUP BY 및 ORDER BY 추가
        sql.append("""
        GROUP BY
          tu.USERID,
          tu.USERGU,
          tu.USERSIDO,
          tu.USERNM,
          tu.SEXYN,
          tu.USERHP,
          tu.POSTCD,
          tu.USERADDR,
          tu.USERMAIL,
          tu.INDATEM
        """);

            sql.append("""
        ORDER BY
          tu.USERNM ASC
        """);

      log.info("회원관리List SQL: {}", sql);
      log.info("SQL Parameters: {}", params.getValues());

      return sqlRunner.getRows(sql.toString(), params);
  }

}
