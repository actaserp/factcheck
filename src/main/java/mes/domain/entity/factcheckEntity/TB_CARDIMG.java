package mes.domain.entity.factcheckEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="TB_CARDIMG")
@Setter
@Getter
@NoArgsConstructor
public class TB_CARDIMG {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    Integer imgseq;

    @Column
    String imgflag;
    @Column
    String imgfilename;
    @Column
    LocalDateTime indatem;
    @Column
    String inuserid;
}
