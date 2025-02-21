package mes.domain.entity.factcheckEntity;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_MARKETING")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TB_MARKETING { //마케팅 관리

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "makseq")
  private Integer makseq; // 일련번호

  @Column(name = "makdate")
  private String makdate; // 의뢰일자 (YYYYMMDD)

  @Column(name = "makcltnm")
  private String makcltnm; // 기관명

  @Column(name = "maksubject")
  private String maksubject; // 제목

  @Column(name = "makcondyul")
  private Integer makcondyul; // 추출 조건

  @Column(name = "makuseyul")
  private Integer makuseyul; // 활용 수치

  @Column(name = "indatem")
  private LocalDateTime indatem; // 등록일자(입력 일자)

  @Column(name = "inuserid")
  private String inuserid; // 입력자 ID
}
