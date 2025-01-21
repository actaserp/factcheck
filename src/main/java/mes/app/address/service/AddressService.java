package mes.app.address.service;

import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AddressService {
    @Autowired
    SqlRunner sqlRunner;

    // 등기부등본 발급내역 조회
    public List<Map<String, Object>> searchHistory(String address, String ID) {
        MapSqlParameterSource params = new MapSqlParameterSource();


        String sql = """
                SELECT FROM 
                """;

        List<Map<String, Object>> result = this.sqlRunner.getRows(sql,params);
        return result;
    }
}
