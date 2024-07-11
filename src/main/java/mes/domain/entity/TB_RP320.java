package mes.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "TB_RP320")
public class TB_RP320 {
	
	@Column(name = "powerid")
	private String powerid;
	
	@Id
	@Column(name = "standdt")
	private String standdt;
	
	@Column(name = "powernm")
	private String powernm;
	
	@Column(name = "chargedv")
	private String chargedv;
	
	@Column(name = "insuernm")
	private String insuernm;
	
	@Column(name = "spworkcd")
	private String spworkcd;
	
	@Column(name = "inuserid")
	private String inuserid;
	
	@Column(name = "sppland")
	private String sppland;
	
	@Column(name = "spplanm")
	private String spplanm;
	
	@Column(name = "spworkm")
	private String spworkm;
	
	@Column(name = "spcompd")
	private String spcompd;
	
	@Column(name = "spcompm")
	private String spcompm;
	
	@Column(name = "updatem")
	private LocalDateTime updatem;
	
	@Column(name = "indatem")
	private LocalDateTime indatem;
	
	// 실제 값에 따라서 precision, scale 추후 추가하기)
	@Column(name = "mevalue01")
	private BigDecimal mevalue01;
	
	@Column(name = "mevalue02")
	private BigDecimal mevalue02;
	
	@Column(name = "mevalue03")
	private BigDecimal mevalue03;
	
	@Column(name = "mevalue04")
	private BigDecimal mevalue04;
	
	@Column(name = "mevalue05")
	private BigDecimal mevalue05;
	
	@Column(name = "mevalue06")
	private BigDecimal mevalue06;
	
	@Column(name = "mevalue07")
	private BigDecimal mevalue07;
	
	@Column(name = "mevalue08")
	private BigDecimal mevalue08;
	
	@Column(name = "mevalue09")
	private BigDecimal mevalue09;
	
	@Column(name = "mevalue10")
	private BigDecimal mevalue10;
	
	@Column(name = "mevalue11")
	private BigDecimal mevalue11;
	
	@Column(name = "mevalue12")
	private BigDecimal mevalue12;
	
	@Column(name = "mevalue13")
	private BigDecimal mevalue13;
	
	@Column(name = "mevalue14")
	private BigDecimal mevalue14;
	
	@Column(name = "mevalue15")
	private BigDecimal mevalue15;
	
	@Column(name = "mevalue16")
	private BigDecimal mevalue16;
	
	@Column(name = "mevalue17")
	private BigDecimal mevalue17;
	
	@Column(name = "mevalue18")
	private BigDecimal mevalue18;
	
	@Column(name = "mevalue19")
	private BigDecimal mevalue19;
	
	@Column(name = "mevalue20")
	private BigDecimal mevalue20;
	
	@Column(name = "mevalue21")
	private BigDecimal mevalue21;
	
	@Column(name = "mevalue22")
	private BigDecimal mevalue22;
	
	@Column(name = "mevalue23")
	private BigDecimal mevalue23;
	
	@Column(name = "mevalue24")
	private BigDecimal mevalue24;
	
	@Column(name = "mevaluet")
	private BigDecimal mevaluet;
	
}