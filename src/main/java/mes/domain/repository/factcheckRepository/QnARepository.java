package mes.domain.repository.factcheckRepository;

import mes.domain.entity.factcheckEntity.TB_FAQINFO;
import mes.domain.entity.factcheckEntity.TB_USERQST;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
@Repository
public interface QnARepository extends JpaRepository<TB_USERQST, Integer> {
    Optional<TB_USERQST> findByQSTSEQ(Integer id);
    Optional<TB_USERQST> findByCHSEQ(Integer id);


    List<TB_USERQST> findAllByUSERID(String userid);
}
