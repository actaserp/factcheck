package mes.app.MobileUsr.AppInfo.QnA;


import mes.domain.entity.User;
import mes.domain.entity.actasEntity.TB_USERINFO;
import mes.domain.entity.factcheckEntity.TB_USERQST;
import mes.domain.repository.actasRepository.TB_USERINFORepository;
import mes.domain.repository.factcheckRepository.QnARepository;
import mes.domain.services.SqlRunner;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@Service
public class QnAService {

    private final QnARepository qnARepository;
    private final TB_USERINFORepository userinfoRepository;
    private final SqlRunner sqlRunner;
    public QnAService(QnARepository qnARepository, TB_USERINFORepository userinfoRepository, SqlRunner sqlRunner) {
        this.qnARepository = qnARepository;
        this.userinfoRepository = userinfoRepository;
        this.sqlRunner = sqlRunner;
    }

    @Transactional
    public Boolean saveQnA(User user, String text){

        TB_USERINFO userinfo = userinfoRepository.findByUserIdAndUseYn(user.getUsername(), "1")
                .orElseThrow(() -> {
                    return new EntityNotFoundException("유저정보가 없습니다.");
                });


        TB_USERQST qna = new TB_USERQST();
        qna.setFLAG("0");
        qna.setQSTTEXT(text);
        qna.setUSERID(user.getUsername());
        qna.setQSTTEL(userinfo.getUserHp());
        qna.setINDATEM(LocalDate.now());

        qnARepository.save(qna);

        return true;
    }

    @Transactional(readOnly = true)
    public TB_USERQST QnASearch(Integer id){

        TB_USERQST qna = qnARepository.findByQSTSEQ(id).orElseThrow(() ->{
            return new EntityNotFoundException("해당 문의글을 찾을 수 없습니다.");
        });

        return qna;
    }

    @Transactional(readOnly = true)
    public Optional<TB_USERQST> AnswerSearchBy(Integer id){

        Optional<TB_USERQST> qna = qnARepository.findByCHSEQ(id);

        return qna;
    }

    @Transactional
    public void deleteQna(List<TB_USERQST> items){
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("삭제할 문의글이 없습니다.");
        }

        qnARepository.deleteAll(items);
    }

    // QnA 문의내역 리스트
    public List<Map<String, Object>> selectList(String searchText) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        StringBuilder sql = new StringBuilder("""
                   SELECT
                        Q.QSTSEQ,
                        Q.QSTTEXT,
                        Q.FLAG,
                        Q.QSTSORT,
                        Q.CHSEQ,
                        Q.INDATEM,
                        Q.INUSERID,
                        Q.USERID,
                        Q.INDATEM,
                        A.QSTSEQ AS answerNo,
                        A.QSTTEXT AS answer,
                        A.INDATEM AS answerdate,
                        A.QSTTEL AS answerTel
                    FROM
                        TB_USERQST Q
                    LEFT JOIN
                        TB_USERQST A
                    ON
                        Q.QSTSEQ = A.CHSEQ
                    WHERE
                        Q.FLAG = '0'
                """);
        // 문의내용필터
        if (searchText != null && !searchText.isEmpty()) {
            sql.append(" AND Q.USERID LIKE :searchText");
            params.addValue("searchText", "%" + searchText + "%");
        }
        //  GROUP BY 조건 추가
        sql.append(" GROUP BY" +
                "                    Q.QSTSEQ, Q.INDATEM, Q.USERID, Q.QSTTEXT, Q.FLAG, Q.QSTSORT, Q.CHSEQ, Q.INDATEM, Q.INUSERID, A.QSTSEQ, A.QSTTEXT, A.INDATEM, A.QSTTEL");
        // 정렬 조건 추가
        sql.append(" ORDER BY Q.INDATEM DESC");
        List<Map<String,Object>> items = this.sqlRunner.getRows(sql.toString(),params);
        return items;
    }
}
