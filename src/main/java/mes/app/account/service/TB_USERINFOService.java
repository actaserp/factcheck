package mes.app.account.service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.entity.actasEntity.TB_USERINFO;
import mes.domain.repository.actasRepository.TB_USERINFORepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TB_USERINFOService {
    @Autowired
    TB_USERINFORepository tb_userinfoRepository;

    public void save(TB_USERINFO tbUserinfo) {
        tb_userinfoRepository.save(tbUserinfo);
    }
}
