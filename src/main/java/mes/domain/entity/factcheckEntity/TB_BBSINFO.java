package mes.domain.entity.factcheckEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="TB_BBSINFO")
@Setter
@Getter
@NoArgsConstructor
public class TB_BBSINFO {

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
    Date INDATEM;
    @Column
    String INUSERID;
}
