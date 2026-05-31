package kh.ddeonabom.member.model.mappers;

import org.apache.ibatis.annotations.Mapper;

import kh.ddeonabom.member.model.vo.Member;
@Mapper
public interface MemberMapper {

	int insertMember(Member m);

	Member login(Member m);

	int existsByEmail(String email);

}
