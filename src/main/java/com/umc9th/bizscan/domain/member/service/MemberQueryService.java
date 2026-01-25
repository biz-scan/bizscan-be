package com.umc9th.bizscan.domain.member.service;

import com.umc9th.bizscan.domain.member.entity.Member;

public interface MemberQueryService {
    Member getMemberById(Long Id);
    Member getMemberByEmail(String email);
}
