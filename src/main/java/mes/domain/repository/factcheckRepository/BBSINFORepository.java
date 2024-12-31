package mes.domain.repository.factcheckRepository;

import mes.domain.entity.factcheckEntity.TB_BBSINFO;
import mes.domain.entity.factcheckEntity.TB_FAQINFO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BBSINFORepository extends JpaRepository<TB_BBSINFO, Integer> {

}
