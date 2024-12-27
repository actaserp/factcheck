package mes.domain.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.*;

@Entity
@Table(name="auth_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//auth_user --> 유저테이블, user_profile --> 유저에 대한 정보 테이블, TB_RP940 --> 회원가입 신청 목록 테이블, TB_RP945 --> 유저들의 산단정보(산단, 발전소, 지역) 테이블 이 4개가 관련되어있음
public class User implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;

	String password;

	//	@JsonIgnore : 제이슨으로 해당 값을 못가져오게함
	String username;

	String email;

	@Column(name = "is_superuser")
	Boolean superUser;

	@Column(name = "is_active")
	Boolean active;

	@Column(name = "first_name")
	String first_name;

	@Column(name = "last_name")
	String last_name;

	@Column(name = "is_staff")
	Boolean is_staff;

	@Column(name = "last_login")
	Timestamp lastLogin;

	@Column(name = "date_joined")
	Timestamp date_joined;


	@Column(name = "tel")
	String tel;

	@Column(name = "Phone")
	String phone;

	@Column(name = "agencycd")
	String agencycd;

	@Column(name = "agencynm")
	String agencynm;


	@Column(name = "divinm")
	String divinm;

	@Column(name = "spjangcd")
	String spjangcd;	//사업장 코드

	@JsonManagedReference
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
	private UserProfile userProfile;

}