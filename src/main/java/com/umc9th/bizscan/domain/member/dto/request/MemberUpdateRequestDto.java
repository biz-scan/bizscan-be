package com.umc9th.bizscan.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberUpdateRequestDto {
    @Schema(description = "변경할 닉네임", example = "UMC")
    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    @Schema(description = "현재 비밀번호 (본인 확인용)", example = "oldPassword123")
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @Schema(description = "새로운 비밀번호", example = "newPassword123")
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,15}$",
            message = "비밀번호는 영어와 숫자를 조합하여 6~15자로 입력하세요."
    )
    private String newPassword;
}
