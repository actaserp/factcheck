package mes.domain.entity.factcheckEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
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
    int FAQSEQ;

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
    Date INDATEM;

    @Column
    String INUSERID;

}
