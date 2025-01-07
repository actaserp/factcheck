package mes.domain.entity.factcheckEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name="TB_FAQINFO")
@Setter
@Getter
@NoArgsConstructor
public class TB_FAQINFO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    Integer FAQSEQ;

    @Column
    String FAQTEXT;

    @Column
    String FLAG;

    @Column
    String FAQFLAG;

    @Column
    int FASORT;

    @Column
    int CHSEQ;

    @Column
    String REMARK;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd") // 형식 지정
    LocalDate INDATEM;

    @Column
    String INUSERID;

}
