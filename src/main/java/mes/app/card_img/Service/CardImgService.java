package mes.app.card_img.Service;

import lombok.extern.slf4j.Slf4j;
import mes.config.Settings;
import mes.domain.entity.factcheckEntity.TB_CARDIMG;
import mes.domain.entity.factcheckEntity.TB_FILEINFO;
import mes.domain.entity.factcheckEntity.TB_MARKETING;
import mes.domain.repository.factcheckRepository.FILEINFORepository;
import mes.domain.repository.factcheckRepository.TB_CARDIMGRepository;
import mes.domain.repository.factcheckRepository.TB_MARKETINGRepository;
import mes.domain.services.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.io.Files.getFileExtension;

@Slf4j
@Service
public class CardImgService {

    @Autowired
    TB_CARDIMGRepository cardimgRepository;

    @Autowired
    FILEINFORepository fileinfoRepository;

    @Autowired
    private Settings settings;

    @Autowired
    SqlRunner sqlRunner;

    @Transactional
    public Integer saveOrUpdateMarketingData(Map<String, Object> formData, String userid, List<MultipartFile> files, String imgfilenm) {
        Integer imgseq = null;
        if (formData.get("imgseq") != null && !formData.get("imgseq").toString().trim().isEmpty()) {
            imgseq = Integer.parseInt(formData.get("imgseq").toString());
        }
        TB_CARDIMG cardimg;

        if (imgseq != null && cardimgRepository.existsById(imgseq)) {
            // 기존 데이터가 있는 경우 (UPDATE)
            cardimg = new TB_CARDIMG();
            cardimg.setImgseq(imgseq);  // 기존 ID 유지
        } else {
            // 새로운 데이터 저장 (INSERT)
            cardimg = new TB_CARDIMG();
            //log.info("🆕 새로운 마케팅 데이터 저장 시작");
        }

        // 공통 필드 값 설정
        cardimg.setImgflag((String) formData.get("imgflag"));
        cardimg.setImgfilename(imgfilenm);
        cardimg.setInuserid(userid);
        cardimg.setIndatem(LocalDateTime.now());

        // 저장 or 수정 실행
        TB_CARDIMG savedCardImg = cardimgRepository.save(cardimg);
        imgseq = savedCardImg.getImgseq(); // 새롭게 저장된 이미지 데이터의 ID

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = savedCardImg.getIndatem().format(formatter);

        String currentDate = formattedDate;

        // 파일 처리 (수정 시 기존 파일 삭제 후 재업로드)
        handleMarketingFiles(imgseq, files, userid, currentDate);

        return imgseq;  // 저장된 또는 수정된 마케팅 데이터의 ID 반환
    }


    private void handleMarketingFiles(Integer imgseq, List<MultipartFile> files, String userid, String currentDate) {
        String fileUploadPath = settings.getProperty("file_upload_path") + "이미지관리";
        File saveDir = new File(fileUploadPath);
        // 파일 저장
        if (files == null || files.isEmpty()) {
            //log.info("📁 업로드된 파일 없음.");
            return;
        }
        if (!saveDir.exists()) {
            saveDir.mkdirs(); // 디렉토리 생성
        }

        // 기존 파일 목록 조회 (파일명 비교를 위해)
        List<TB_FILEINFO> existingFiles = fileinfoRepository.findAllByCheckseqAndBbsseq("04",imgseq);
        Set<String> existingFileNames = existingFiles.stream()
                .map(TB_FILEINFO::getFILESVNM)
                .collect(Collectors.toSet());

        Set<String> newFileNames = files.stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toSet());

        for (TB_FILEINFO fileInfo : existingFiles) {
            String filePath = fileInfo.getFILEPATH() + File.separator + fileInfo.getFILESVNM();
            File file = new File(filePath);

            if (!newFileNames.contains(fileInfo.getFILEORNM())) { // 새로운 파일에 포함되지 않으면 삭제
                if (file.exists() && file.delete()) {
                    log.info("📁 기존 파일 삭제 성공: {}", filePath);
                } else {
                    log.warn("⚠ 기존 파일 삭제 실패 또는 존재하지 않음: {}", filePath);
                }
                fileinfoRepository.delete(fileInfo); // DB에서도 삭제
            }
        }

        for (MultipartFile file : files) {
            try {
                String storedFileName = UUID.randomUUID().toString() + "." + getFileExtension(file.getOriginalFilename());
                File saveFile = new File(fileUploadPath + File.separator + storedFileName);
                file.transferTo(saveFile);

                if (!saveFile.exists()) {
                    log.error("파일 저장 실패! 저장된 파일이 존재하지 않음: {}", storedFileName);
                    continue;
                }

                TB_FILEINFO fileInfo = new TB_FILEINFO();
                fileInfo.setBbsseq(imgseq);
                fileInfo.setCHECKSEQ("04"); // 04: 이미지관리 관련 파일
                fileInfo.setFILEPATH(fileUploadPath);
                fileInfo.setFILESVNM(storedFileName);
                fileInfo.setFiledate(currentDate);
                fileInfo.setFILEORNM(file.getOriginalFilename());
                fileInfo.setFILESIZE(BigDecimal.valueOf(file.getSize()));
                fileInfo.setFILEEXTNS(getFileExtension(file.getOriginalFilename()));
                fileInfo.setFILEURL(fileUploadPath);
                fileInfo.setINUSERID(userid);
                fileInfo.setINDATEM(LocalDateTime.now());

                fileinfoRepository.save(fileInfo);
                //log.info("파일 저장 완료: {}", storedFileName);

            } catch (IOException e) {
                log.error("파일 저장 중 오류 발생: {}", file.getOriginalFilename(), e);
            }
        }
    }


    // parseNumericField 메서드 추가
    private BigDecimal parseNumericField(String value, String... suffixesToRemove) {
        //log.info("숫자 필드 파싱 중: 값={}, 제거할 접미사={}", value, suffixesToRemove);

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            String sanitizedValue = value.trim();

            // 여러 접미사를 제거
            for (String suffix : suffixesToRemove) {
                sanitizedValue = sanitizedValue.replace(suffix, "").trim();
            }

            // 숫자만 남았는지 검증
            if (!sanitizedValue.matches("^-?\\d+(\\.\\d+)?$")) {
                throw new NumberFormatException("유효한 숫자 형식이 아님: " + sanitizedValue);
            }

            return new BigDecimal(sanitizedValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자 형식 변환 중 오류 발생: " + value, e);
        }
    }

    public List<Map<String, Object>> getList(String startDate, String endDate, String searchUserNm) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
        SELECT
             CI.*,
             COALESCE((
                 SELECT JSON_QUERY((
                     SELECT
                         FI.fileseq AS fileseq,
                         FI.FILESIZE AS fileSize,
                         FI.fileornm AS fileornm,
                         FI.filesvnm AS filesvnm,
                         FI.filepath AS filepath,
                         FI.fileextns AS fileextns
                     FROM TB_FILEINFO FI
                     WHERE FI.bbsseq = CI.imgseq
                     AND FI.CHECKSEQ = '04'
                     FOR JSON PATH
                 ))
             ), '[]') AS fileinfos
         FROM TB_CARDIMG CI
         WHERE 1=1
        """);
        if (startDate != null && !startDate.isEmpty()) {
            sql.append("  AND CI.indatem >= :startDate ");
            params.addValue("startDate", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND CI.indatem <= :endDate ");
            params.addValue("endDate", endDate);
        }
        if (searchUserNm != null && !searchUserNm.isEmpty()) {
            sql.append(" AND CI.makcltnm LIKE :searchUserNm ");
            params.addValue("searchUserNm", "%" + searchUserNm + "%");
        }
        sql.append(" ORDER BY CI.indatem DESC");

//      log.info("마케팅관리 List SQL: {}", sql);
//      log.info("SQL Parameters: {}", params.getValues());

        return sqlRunner.getRows(sql.toString(), params);
    }

    //삭제
    public void deleteRegisterById(Integer imgseq) {
        if (!cardimgRepository.existsById(imgseq)) {
            throw new EntityNotFoundException("삭제할 데이터가 존재하지 않습니다. ID: " + imgseq);
        }

        // 마케팅 관련 파일만 조회
        List<TB_FILEINFO> fileList = fileinfoRepository.findAllByCheckseqAndBbsseq("03", imgseq);

        if (!fileList.isEmpty()) {
            log.info("마케팅 파일 삭제 시작 (총 {} 개)", fileList.size());

            for (TB_FILEINFO fileInfo : fileList) {
                try {
                    String filePath = fileInfo.getFILEPATH();
                    String fileName = fileInfo.getFILESVNM();
                    File file = new File(filePath, fileName);

                    if (file.exists()) {
                        if (file.delete()) {
                            // log.info("파일 삭제 성공: {}", filePath + File.separator + fileName);
                        } else {
                            log.error("파일 삭제 실패: {}", filePath + File.separator + fileName);
                        }
                    } else {
                        log.warn("삭제할 파일이 존재하지 않음: {}", filePath + File.separator + fileName);
                    }
                } catch (Exception e) {
                    log.error("파일 삭제 중 오류 발생: {}", e.getMessage(), e);
                }
            }

            // CHECKSEQ = '03'인 파일만 DB에서 삭제
            fileinfoRepository.deleteByBbsseqAndCheckseq(imgseq, "04");
        } else {
            log.warn("해당 마케팅 데이터에 파일이 없습니다. (ID: {})", imgseq);
        }

        // 마케팅 데이터 삭제
        cardimgRepository.deleteById(imgseq);
    }

}
