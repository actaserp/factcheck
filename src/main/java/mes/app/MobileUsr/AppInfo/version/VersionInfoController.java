package mes.app.MobileUsr.AppInfo.version;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/app/info")
public class VersionInfoController {

    private final BuildInfoService buildInfoService;

    public VersionInfoController(BuildInfoService buildInfoService) {
        this.buildInfoService = buildInfoService;
    }


}
