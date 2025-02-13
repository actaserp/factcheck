package mes.domain.entity.factcheckEntity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
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
  private int realId;

  @Column(name = "USERID")
  private String userId;

  @Column(name = "REQDATE")
  private String reqDate;

  @Column(name = "REALADD")
  private String realAdd;

  @Column(name = "REGDATE")
  private String regDate;

  @Column(name = "RESIDO", length = 20)
  private String resido;

  @Column(name = "REGUGUN", length = 30)
  private String reguGun;

  @Column(name = "REMOK", length = 50)
  private String remok;

  @Column(name = "REWON", length = 50)
  private String rewon;

  @Column(name = "REWONDATE", length = 8)
  private String rewonDate;

  @Column(name = "REJIMOK", length = 20)
  private String rejimok;

  @Column(name = "REAREA", precision = 18, scale = 3)
  private BigDecimal reArea;

  @Column(name = "REAMOUNT")
  private Float reAmount;

  @Column(name = "RESEQ")
  private Integer reSeq;

  @Column(name = "REMAXAMT")
  private Float reMaxAmt;

  @Column(name = "INDATEM", nullable = false)
  private LocalDateTime inDatem = LocalDateTime.now();

  @Column(name = "REALSCORE")
  private Integer realScore;

  @Column(name = "REALPOINT")
  private Integer realPoint;

  @Column(name = "RELASTDATE", length = 8)
  private String realLastDate;

  @Column(name = "REALGUBUN", length = 50)
  private String realGubun;

  @Column(name = "PDFFILENAME", length = 100)
  private String pdfFilename;

}
