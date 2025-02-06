package mes.app.MobileUsr.AppInfo.version;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class BuildInfoService {

    private final String filepath = "src/main/resources/static/build-info.json";

    public BuildInfoService() {
        generateBuildInfo();

    }

    private void generateBuildInfo(){
        Map<String, String> buildInfo = new HashMap<>();
        buildInfo.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        buildInfo.put("version", "version 1.0"); //TODO: 버전정보 수정해줘야 됨
        ObjectMapper objectMapper = new ObjectMapper();

        try{

            objectMapper.writeValue(new File(filepath), buildInfo);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
