package com.umc9th.bizscan.domain.member.service;

import com.umc9th.bizscan.domain.member.dto.RegisterMemberDto;
import com.umc9th.bizscan.domain.member.dto.request.MemberUpdateRequestDto;

public interface MemberCommandService {
  Long registerMember(RegisterMemberDto registerMemberDto);
  void updateMember(Long memberId, MemberUpdateRequestDto dto);
  void deleteMember(Long memberId);
}
