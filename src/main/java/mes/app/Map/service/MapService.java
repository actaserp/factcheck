package mes.app.Map.service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MapService {
  @Autowired
  SqlRunner sqlRunner;

  public List<Map<String, Object>> getMarkersForRegion(String region, String gugun) {
    //MapSqlParameterSource 사용하여 SQL 파라미터 설정
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("region", region);
    params.addValue("gugun", gugun);

    return sqlRunner.getRows("""
       SELECT 
       RESIDO, REGUGUN, REALSCORE 
       FROM TB_REALINFO
      WHERE RESIDO like :region + '%'
      AND (:gugun IS NULL OR REGUGUN like :gugun + '%');
    """, params);
  }


}
