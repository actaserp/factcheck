package mes.domain.entity.factcheckEntity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "TB_REGWORD")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TB_REGWORD {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "REGNO")
  private Integer regNo; // 키워드 고유 ID

  @Column(name = "REGSEQ", nullable = false)
  private Integer regSeq; // 등록 정보 ID (TB_REGISTER의 PK와 연결)

  @Column(name = "REGWORD", length = 100)
  private String regWord; // 등기목적 키워드

  @Column(name = "USEYN", length = 1)
  private String useYn; // 사용 여부 (Y/N)
}
