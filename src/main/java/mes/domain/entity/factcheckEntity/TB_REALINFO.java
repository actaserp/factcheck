package mes.domain.entity.factcheckEntity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_REALINFO")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TB_REALINFO {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "REALID")
  private Integer realId;

  @Column(name = "REALADD")
  private String realAddress; // 실제 주소 (마커 좌표 변환 필요)

  @Column(name = "REGDATE")
  private String regDate;

  @Column(name = "RESIDO")
  private String region; // 시/도 정보

  @Column(name = "REGUGUN")
  private String district; // 구/군 정보

  @Column(name = "REAREA")
  private Double area; // 면적 정보

  @Column(name = "REAMOUNT")
  private Float amount; // 금액 정보

  @Column(name = "INDATEM")
  private LocalDateTime createdAt;

}
