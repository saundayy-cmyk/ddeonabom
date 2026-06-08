package kh.ddeonabom.member.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Member {
	private int memberNo;
	private String id;
	private String pwd;
	private String nickName;
	private String email;
	private String phone;
	private String isAdmin;
	private String status;
	private Date enrollDate;
	private Date modifyDate;
	
	
}
