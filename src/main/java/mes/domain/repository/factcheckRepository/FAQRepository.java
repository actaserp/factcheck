package mes.domain.repository.factcheckRepository;

import mes.domain.entity.actasEntity.TB_RP920;
import mes.domain.entity.factcheckEntity.TB_FAQINFO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface FAQRepository extends JpaRepository<TB_FAQINFO, Integer> {

}
