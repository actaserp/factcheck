package mes.app.account.service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.entity.actasEntity.TB_XUSERS;
import mes.domain.entity.actasEntity.TB_XUSERSId;
import mes.domain.repository.actasRepository.TB_XuserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class TB_xuserService {
    @Autowired
    TB_XuserRepository xuserstRepository;

    public void save(TB_XUSERS xusers) {
        xuserstRepository.save(xusers);
    }

    public Optional<TB_XUSERS> findById(TB_XUSERSId tbXusersId) {
        return xuserstRepository.findById(tbXusersId);
    }

    public void PasswordChange(String pw, String userid, String password) {

        xuserstRepository.PasswordChange(pw, userid, password);
    }
}
