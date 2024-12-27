package mes.domain.repository.actasRepository;

import mes.domain.entity.actasEntity.TB_XUSERS;
import mes.domain.entity.actasEntity.TB_XUSERSId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TB_XuserRepository extends JpaRepository<TB_XUSERS, TB_XUSERSId> {
}
