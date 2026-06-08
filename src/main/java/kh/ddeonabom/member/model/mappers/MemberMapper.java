package kh.ddeonabom.member.model.mappers;

import org.apache.ibatis.annotations.Mapper;

import kh.ddeonabom.member.model.vo.Member;
@Mapper
public interface MemberMapper {

	int insertMember(Member m);

	Member login(Member m);

	int existsByEmail(String email);

	int updateMemberWithPassword(Member m);

	int updateMemberWithoutPassword(Member m);

	Member selectOneMember(String id);

	String findIdByEmail(String email);

	int updatePasswordOnly(Member m);


	int withdrawMember(String id);

	int existsByNickName(String nickName);

}
