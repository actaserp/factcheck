package mes.domain.repository.factcheckRepository;

import mes.domain.entity.factcheckEntity.TB_PDFSEQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PDFSEQRepository extends JpaRepository<TB_PDFSEQ, Integer> {

}
