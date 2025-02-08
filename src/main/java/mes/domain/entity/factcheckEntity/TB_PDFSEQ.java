package mes.domain.entity.factcheckEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="TB_PDFSEQ")
@Setter
@Getter
@NoArgsConstructor
public class TB_PDFSEQ {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer seq;     // 순번

    @Column(name = "PDFFILENAME")
    private String pdfFilename;

    @Column(name = "indatem")
    private LocalDateTime inDate = LocalDateTime.now();
}
