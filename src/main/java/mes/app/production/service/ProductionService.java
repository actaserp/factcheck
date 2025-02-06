package mes.app.production.service;

import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductionService {

    @Autowired
    SqlRunner sqlRunner;

    public List<Map<String, Object>> getlist(String startDate, String endDate, String property) {
        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("startDate", startDate);
        dicParam.addValue("endDate", endDate);
        dicParam.addValue("property", property);

        String sql = """
             SELECT
                     subquery.REALID,
                     MAX(subquery.RELASTDATE) AS RLASTDATE,
                     MAX(subquery.USERID) AS USERID,
                     MAX(subquery.REQDATE) AS REQDATE,
                     MAX(subquery.REALADD) AS REALADD,
                     MAX(subquery.REGDATE) AS REGDATE,
                     MAX(subquery.RESIDO) AS RESIDO,
                     MAX(subquery.REGUGUN) AS REGUGUN,
                     MAX(subquery.REMOK) AS REMOK,
                     MAX(subquery.REWON) AS REWON,
                     MAX(subquery.REWONDATE) AS REWONDATE,
                     MAX(subquery.REJIMOK) AS REJIMOK,
                     MAX(subquery.REAREA) AS REAREA,
                     MAX(subquery.REAMOUNT) AS REAMOUNT,
                     MAX(subquery.RESEQ) AS RESEQ,
                     MAX(subquery.REMAXAMT) AS REMAXAMT,
                     MAX(subquery.INDATEM) AS INDATEM,
                     MAX(subquery.REALSCORE) AS REALSCORE,
                     MAX(subquery.REALPOINT) AS REALPOINT,
                     MAX(subquery.REALGUBUN) AS REALGUBUN,
                     MAX(subquery.S_REQDATE) AS S_REQDATE,
                     MAX(subquery.S_USERID) AS S_USERID,
                     MAX(subquery.S_REALID) AS S_REALID,
                     MAX(subquery.view_count) AS view_count
                 FROM (
                     SELECT
                         R.REALID,
                         R.RELASTDATE,
                         R.USERID,
                         R.REQDATE,
                         R.REALADD,
                         R.REGDATE,
                         R.RESIDO,
                         R.REGUGUN,
                         R.REMOK,
                         R.REWON,
                         R.REWONDATE,
                         R.REJIMOK,
                         R.REAREA,
                         R.REAMOUNT,
                         R.RESEQ,
                         R.REMAXAMT,
                         R.INDATEM,
                         R.REALSCORE,
                         R.REALPOINT,
                         R.REALGUBUN,
                         S.REQDATE AS S_REQDATE,
                         S.USERID AS S_USERID,
                         S.REALID AS S_REALID,
                         COUNT(*) OVER (PARTITION BY R.REALID) AS view_count
                     FROM
                         TB_REALINFO R
                     JOIN
                         TB_SEARCHINFO S
                     ON
                         R.USERID = S.USERID
                     WHERE
                         S.REQDATE BETWEEN :startDate AND :endDate
                         AND (
                             (:property = 'my_property' AND S.USERID = R.USERID AND R.REQDATE BETWEEN :startDate AND :endDate)
                             OR (:property = 'recent_property' AND R.REQDATE BETWEEN :startDate AND :endDate)
                             OR (:property = 'most_viewed_property' AND R.REQDATE BETWEEN :startDate AND :endDate)
                             OR (:property = 'popular_property' AND R.REQDATE BETWEEN :startDate AND :endDate)
                         )
                 ) AS subquery
                 GROUP BY subquery.REALID
                 ORDER BY
                     CASE
                         WHEN :property = 'my_property' THEN MAX(subquery.RELASTDATE)
                         WHEN :property = 'recent_property' THEN MAX(subquery.RELASTDATE)
                         WHEN :property IN ('most_viewed_property', 'popular_property') THEN MAX(subquery.view_count)
                         ELSE NULL
                     END DESC,
                     MAX(subquery.RELASTDATE) DESC;
            """;

       /* // 'most_viewed_property'일 경우 LIMIT 100 추가
        if ("most_viewed_property".equals(property)) {
            sql += " OFFSET 0 ROWS FETCH NEXT 100 ROWS ONLY";
        }*/

        List<Map<String, Object>> items = this.sqlRunner.getRows(sql.toString(), dicParam);
        return items;
    }




   /* AND (
                                  (:property = 'my_property' AND S.USERID = R.USERID)
    OR (:property = 'recent_property' AND S.REQDATE >= DATEADD(MONTH, -1, GETDATE()))
    OR (:property = 'most_viewed_property' AND S.VIEW_COUNT >= (SELECT AVG(VIEW_COUNT) FROM TB_SEARCHINFO))
    OR (:property = 'popular_property' AND S.REQDATE >= DATEADD(WEEK, -1, GETDATE()))
            )*/

}
