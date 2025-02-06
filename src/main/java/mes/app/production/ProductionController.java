package mes.app.production;


import mes.app.production.service.ProductionService;
import mes.domain.model.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile_production")
public class ProductionController {

    @Autowired
    ProductionService productionService;

    @GetMapping("/read_all")
    public AjaxResult getList(@RequestParam(value = "search_startDate", required = false) String startDate,
                              @RequestParam(value = "search_endDate", required = false) String endDate,
                              @RequestParam(value = "search_property", required = false) String property) {

        AjaxResult result = new AjaxResult();

        // 날짜 변환 로직
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String formattedStartDate = (startDate != null) ? LocalDate.parse(startDate, inputFormatter).format(outputFormatter) : null;
        String formattedEndDate = (endDate != null) ? LocalDate.parse(endDate, inputFormatter).format(outputFormatter) : null;

        // 변환된 날짜 값을 사용하여 데이터 조회
        List<Map<String, Object>> productionData = productionService.getlist(formattedStartDate, formattedEndDate, property);

        result.data = productionData;

        return result;
    }

}
