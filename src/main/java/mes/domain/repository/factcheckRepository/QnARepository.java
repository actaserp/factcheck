package mes.domain.repository.factcheckRepository;

import mes.domain.entity.factcheckEntity.TB_FAQINFO;
import mes.domain.entity.factcheckEntity.TB_USERQST;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QnARepository extends JpaRepository<TB_USERQST, Integer> {
}
