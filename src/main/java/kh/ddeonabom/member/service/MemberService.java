package kh.ddeonabom.member.service;

import org.springframework.stereotype.Service;

import kh.ddeonabom.member.model.mappers.MemberMapper;
import kh.ddeonabom.member.model.vo.Member;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberMapper mapper;
	
	public int insertMember(Member m) {
		
		return mapper.insertMember(m);
	}

	public Member login(Member m) {
		return mapper.login(m);
	}

	public boolean existsByEmail(String email) {
		 return mapper.existsByEmail(email) > 0;
	}

}
