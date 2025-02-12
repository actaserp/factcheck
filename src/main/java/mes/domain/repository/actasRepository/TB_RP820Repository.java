package mes.domain.repository.actasRepository;

import mes.domain.entity.actasEntity.TB_RP820;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository 
public interface TB_RP820Repository extends JpaRepository<TB_RP820, Integer> {

    @Query(value = "SELECT MAX(t.tketnum) FROM TB_RP820 t WHERE t.spworkcd = :spworkcd AND t.spcompcd = :spcompcd AND t.spplancd = :spplancd", nativeQuery = true)
    Optional<String> findMaxChecknoBySpplancd(@Param("spworkcd") String spworkcd,
                                              @Param("spcompcd") String spcompcd,
                                              @Param("spplancd") String spplancd);
	
}