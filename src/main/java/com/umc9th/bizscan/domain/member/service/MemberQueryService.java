package com.umc9th.bizscan.domain.member.service;

import com.umc9th.bizscan.domain.member.entity.Member;

import java.util.List;

public interface MemberQueryService {
    Member getMemberById(Long Id);
    Member getMemberByEmail(String email);
    List<Member> getAllMembers();
    boolean validateExistNickname(String nickname);
    boolean validateExistEmail(String email);
}
