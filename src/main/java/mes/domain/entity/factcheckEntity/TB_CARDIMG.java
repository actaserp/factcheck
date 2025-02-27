package mes.domain.entity.factcheckEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name="TB_CARDIMG")
@Setter
@Getter
@NoArgsConstructor
public class TB_CARDIMG {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    Integer IMGSEQ;

    @Column
    String IMGFLAG;
    @Column
    String IMGFILENM;
    @Column
    String INDATEM;
    @Column
    String INUSERID;
}
