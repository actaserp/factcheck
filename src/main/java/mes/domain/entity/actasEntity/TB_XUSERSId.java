package mes.domain.entity.actasEntity;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class TB_XUSERSId implements Serializable {
    private String custcd;
    private String userid;

}