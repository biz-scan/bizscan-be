package com.umc9th.bizscan.domain.member.dto.response;

import com.umc9th.bizscan.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponseDto {

  private Long id;
  private String email;
  private String nickname;

  public static MemberResponseDto from(Member member) {
    return new MemberResponseDto(member.getId(), member.getEmail(), member.getNickname());
  }
}
