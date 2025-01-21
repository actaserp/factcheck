package mes.app.address;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mes.app.address.service.AddressService;
import mes.app.customer_management.service.CM_QnaService;
import mes.domain.entity.User;
import mes.domain.entity.factcheckEntity.TB_FILEINFO;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/address")
public class AddressController {
    @Autowired
    AddressService addressService;

    // 등기부등본 내역 조회
    @GetMapping("/searchHistory")
    public AjaxResult deleteQnA(@RequestParam(value = "address")String address,
                                Authentication auth) {
        AjaxResult result = new AjaxResult();
        User user = (User) auth.getPrincipal();
        String ID = user.getUsername();

        try {
            addressService.searchHistory(address, ID);
            result.message = "성공적으로 내역 조회되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            result.message = "내역 조회 중 오류가 발생했습니다.";
        }

        return result;
    }
}
