package mes.app;
import mes.domain.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class UtilClass {

    // JSON 배열을 파싱하여 리스트로 변환하는 메서드
    public List<String> parseUserIds(String userid) {
        String cleanJson = userid.replaceAll("[\\[\\]\"]", "");
        return Arrays.asList(cleanJson.split(","));
    }

    public List<Integer> parseUserIdsToInt(String userid) {
        String cleanJson = userid.replaceAll("[\\[\\]\"]", ""); // 대괄호와 따옴표 제거
        return Arrays.stream(cleanJson.split(","))  // 쉼표로 분리
                .map(Integer::parseInt)       // 각 요소를 Integer로 변환
                .collect(Collectors.toList()); // 리스트로 수집
    }

    public static String removeBrackers(String input){
        return input.replaceAll("[\\[\\]\"]", "");
    }

    //월에 마지막날 구하기  , day값 형태는 yyyy-MM
    public String getLastDay(String day){
        int year = Integer.parseInt(day.substring(0,4));
        int month = Integer.parseInt(day.substring(4,6));

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate LastDay = yearMonth.atEndOfMonth();
        return LastDay.toString();
    }

    public String getUserId(){
        SecurityContext sc = SecurityContextHolder.getContext();
        Authentication auth = sc.getAuthentication();
        User user = (User) auth.getPrincipal();
        return user.getUsername();
    }

    public String getUsername(){
        SecurityContext sc = SecurityContextHolder.getContext();
        Authentication auth = sc.getAuthentication();
        User user = (User) auth.getPrincipal();
        return user.getFirst_name();
    }

    public static File saveFileToTemp(MultipartFile file) throws IOException {
        // 원본 파일명에서 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 확장자를 동적으로 설정하여 임시 파일 생성
        File tempFile = File.createTempFile("uploaded-", extension);
        file.transferTo(tempFile); // 파일을 임시 파일로 변환

        return tempFile;
    }

    public static File saveFileToTempAsFile(File file) throws IOException {
        // 임시파일을 저장할 명시적 디렉토리
        File tempDir = new File("C:\\temp");

        // 디렉토리 존재하지 않으면 생성
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // 해당 디렉토리에 임시파일 생성
        File tempFile = File.createTempFile("uploaded-", ".pdf", tempDir);

        // 원본 파일 내용을 복사
        Files.copy(file.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return tempFile;
    }

}
