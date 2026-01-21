package com.umc9th.bizscan.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterMemberDto {

  // 이메일
  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "이메일이 올바르지 않습니다.")
  private String email;

  // 닉네임
  @NotBlank(message = "닉네임은 필수입니다.")
  private String nickname;

  // 비밀번호: 영어와 숫자를 조합하여 6~15자로 입력하세요.
  @NotBlank(message = "비밀번호는 필수입니다.")
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,15}$",
      message = "영어와 숫자를 조합하여 6~15자로 입력하세요.")
  private String password;
}
