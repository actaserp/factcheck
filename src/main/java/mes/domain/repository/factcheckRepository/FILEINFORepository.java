package mes.domain.repository.factcheckRepository;

import mes.domain.entity.factcheckEntity.TB_BBSINFO;
import mes.domain.entity.factcheckEntity.TB_FILEINFO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FILEINFORepository extends JpaRepository<TB_FILEINFO, Integer> {
}
