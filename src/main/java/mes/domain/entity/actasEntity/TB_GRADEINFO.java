package mes.domain.entity.actasEntity;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TB_GRADEINFO")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TB_GRADEINFO {

    @Id
    @Column(name = "GRID")
    private String grId;

    @Column(name="GRSCORE01")
    private int grScore01;

    @Column(name="GRSCORE02")
    private int grScore02;

    @Column(name="GRFLAG")
    private String grFlag;

    @Column(name="GRCOLOR")
    private String grColor;

    @Column(name="GRFACE")
    private String grFace;

    @Column(name="GRMOOD")
    private String grMood;

    @Column(name="GRICON")
    private String grIcon;

}
