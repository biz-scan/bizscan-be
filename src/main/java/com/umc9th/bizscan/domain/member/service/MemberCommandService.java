package com.umc9th.bizscan.domain.member.service;

import com.umc9th.bizscan.domain.member.dto.RegisterMemberDto;

public interface MemberCommandService {
    Long registerMember(RegisterMemberDto registerMemberDto);
}
