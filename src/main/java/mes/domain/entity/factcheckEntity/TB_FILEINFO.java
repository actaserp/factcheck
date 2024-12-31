package mes.domain.entity.factcheckEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="TB_FILEINFO")
@Setter
@Getter
@NoArgsConstructor
public class TB_FILEINFO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    Integer BBSSEQ;     // 순번

    @Column
    String BBSDATE;    // 작성일자
    @Column
    String CHECKSEQ;   // 순번
    @Column
    String FILEPATH;
    @Column
    String FILESVNM;
    @Column
    String FILEEXTNS;
    @Column
    String FILEURL;
    @Column
    String FILEORNM;
    @Column
    BigDecimal FILESIZE;
    @Column
    String FILEREM;
    @Column
    String REPTN;
    @Column
    Date INDATEM;
    @Column
    String INUSERID;

}
