package mes.domain.entity.factcheckEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name="TB_USERQST")
@Setter
@Getter
@NoArgsConstructor
public class TB_USERQST {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    Integer QSTSEQ;

    @Column
    String USERID;
    @Column
    String QSTTEXT;
    @Column
    String FLAG;
    @Column
    int QSTSORT;
    @Column
    int CHSEQ;
    @Column
    String QSTTEL;
    @Column
    String INUSERID;
    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd") // 형식 지정
    LocalDate INDATEM;
}
