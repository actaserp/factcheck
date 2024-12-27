package mes.domain.entity.actasEntity;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TB_XUSERS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TB_XUSERS {

    @EmbeddedId
    private TB_XUSERSId id;

    @Column(name = "rnum")
    private String rnum;

    @Column(name = "passwd1")
    private String passwd1;

    @Column(name = "passwd2")
    private String passwd2;

    @Column(name = "shapass")
    private String shapass;

    @Column(name = "custnm")
    private String custnm;

    @Column(name = "pernm")
    private String pernm;

    @Column(name = "useyn")
    private String useyn;

    @Column(name = "perid")
    private String perid;

    @Column(name = "sysmain")
    private String sysmain;

    @Column(name = "grpid")
    private String grpid;

    @Column(name = "domcls")
    private String domcls;

    @Column(name = "spjangcd")
    private String spjangcd;
}

