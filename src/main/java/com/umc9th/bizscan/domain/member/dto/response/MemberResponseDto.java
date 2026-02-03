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
  private Long storeId;

    public static MemberResponseDto from(Member member) {
        // member.getStore()가 null인지 확인하여 storeId를 할당합니다.
        Long storeId = (member.getStore() != null) ? member.getStore().getId() : null;

        return new MemberResponseDto(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                storeId
        );
    }
}
