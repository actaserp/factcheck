package mes.app.account.service;

import lombok.extern.slf4j.Slf4j;
import mes.domain.DTO.UserDto;
import mes.domain.entity.actasEntity.TB_USERINFO;
import mes.domain.repository.actasRepository.TB_USERINFORepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TB_USERINFOService {
    @Autowired
    TB_USERINFORepository tb_userinfoRepository;

    public void save(TB_USERINFO tbUserinfo) {
        tb_userinfoRepository.save(tbUserinfo);
    }

    public TB_USERINFO getUserInfo(String userid){

        TB_USERINFO user = tb_userinfoRepository.findByUserIdAndUseYn(userid, "1").orElseThrow(() ->
        {
            return new EntityNotFoundException("해당 엔티티를 찾을 수 없습니다.");
        });

        return user;
    }
}
