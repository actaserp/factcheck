package mes.app.system.service;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.util.StringUtils;
import mes.domain.services.SqlRunner;

@Service
public class LoginLogService {

    @Autowired
    SqlRunner sqlRunner;

    public List<Map<String, Object>> getLoginLogList(Timestamp start, Timestamp end, String keyword, String type) {

        MapSqlParameterSource dicParam = new MapSqlParameterSource();
        dicParam.addValue("start", start);
        dicParam.addValue("end", end);

        String sql = """
                select row_number() over (order by CONVERT(varchar, ll._created, 23) desc, up."Name" asc, ll._created desc) as row_number
                , ll.id
                , ll."Type" as type
                , ll."IPAddress" as addr
                , au.username as login_id
                , up."Name" as name
                , CONVERT(varchar, ll._created, 120) as created
                from login_log ll
                left join auth_user au ON au.id = ll."User_id" 
                left join user_profile up on up."User_id" = ll."User_id" 
                where ll._created between :start and :end
                """;

        // 'login', 'logout' 타입을 적용할 경우 필터 추가
        if (StringUtils.isNotEmpty(type)) {
            sql += " and ll.\"Type\" = :type ";
            dicParam.addValue("type", type);
        } else {
            sql += " and (ll.\"Type\" = 'login' or ll.\"Type\" = 'logout')";
        }

        // 키워드 검색 추가 조건
        if (StringUtils.isNotEmpty(keyword)) {
            sql += """ 
                    and (au.username LIKE '%' + :keyword + '%'
                        or up."Name" LIKE '%' + :keyword + '%' 
                    )
                    """;
            dicParam.addValue("keyword", keyword); // keyword가 있을 때만 파라미터 추가
        }

        // 정렬 조건은 항상 동일하게 적용
        sql += " order by" +
                "    CONVERT(varchar, ll._created, 23) desc," +
                "    up.\"Name\" asc, " +
                "    ll._created desc";
        System.out.println();
        List<Map<String, Object>> items = this.sqlRunner.getRows(sql, dicParam);
        return items;
    }

}
