package mes.domain.entity.actasEntity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "TB_REGISTER")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TB_REGISTER {  //등기분류관리

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "REGSEQ")
    private int regSeq;     //순번

    @Column(name = "REGNM")
    private String regNm;   //등기명칭

    @Column(name = "REGGROUP")
    private String regGroup;    //상위그룹

    @Column(name = "REGSTAND")
    private String regStand;    //판정기준

    @Column(name = "REGMAXNUM")
    private Integer regMaxNum;  //최대위험점수

    @Column(name = "REGYUL")
    private BigDecimal regYul;   //발샐비율

    @Column(name = "REGSTAMT")
    private Float regStAmt;     //기준금액

    @Column(name = "RISKCLASS")
    private String riskClass;   //위험분류

    @Column(name = "SUBSCORE")
    private Integer subScore;   //후순위조건점수

    @Column(name = "SENSCORE")
    private Integer senScore;   //선순위조건점수

    @Column(name = "REGASNAME")
    private String regAsName;   //순화명칭

    @Column(name = "EXFLAG")
    private String exFlag;      //판점기준

    @Column(name = "REGCOMMENT")
    private String regComment;  //설명

    @Column(name = "REMARK")
    private String remark;      //비고

}
