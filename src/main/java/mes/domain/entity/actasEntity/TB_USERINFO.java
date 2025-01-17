package mes.domain.entity.actasEntity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_USERINFO")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//모바일 사용자
public class TB_USERINFO {

    @Id
    @Column(name = "USERID")
    private String userId;      //사용자 ID

    @Column(name = "USERNM")
    private String userNm;      //사용자명

    @Column(name = "AGENUM")
    private Integer ageNum;     //연령

    @Column(name = "SEXYN")
    private String sexYn;       //성별

    @Column(name = "BIRTHYEAR")
    private String  birthYear;      //생년

    @Column(name = "USERHP")
    private String userHp;      //핸드폰 번호

    @Column(name = "POSTCD")
    private String postCd;      //우편번호

    @Column(name = "USERADDR")
    private String userAddr;    //주소

    @Column(name = "USERMAIL")
    private String userMail;    //email

    @Column(name = "LOGINPW")
    private String loginPw;     //비밀번호

    @Column(name = "USEYN")
    private String useYn;       //사용여부

    @Column(name = "SSOCD")
    private String ssoCd;       //인증코드

    @Column(name = "INDATEM")
    private LocalDateTime inDatem;      //입력일시(등록일자)

    @Column(name = "INUSERID")
    private String inUserId;    //아이디

    @Column(name = "USERSIDO")
    private String userIDO;     //시

    @Column(name="USERGU")
    private String userGU;      //구군

    @Column(name = "INUSERNM")
    private String inUserNm;    //입력자명

    @Column(name = "TOKENINFO")
    private String tokenInfo;   //토큰 정보

    @Column(name = "WDRAWDATE")
    private String wDrawDate;   //계정탈퇴일자
}
