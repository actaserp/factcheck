package mes.domain.entity.actasEntity;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Data
public class TB_RP870_PK implements Serializable {

    private String spworkcd;    // 관할지역코드
    private String spcompcd;    // 발전산단코드
    private String spplancd;    // 발전소코드
    private String registdt;    // 점검일자
    private String checkseq;    // 순번

}