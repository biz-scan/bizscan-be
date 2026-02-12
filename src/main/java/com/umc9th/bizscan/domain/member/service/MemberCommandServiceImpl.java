package com.umc9th.bizscan.domain.member.service;

import com.umc9th.bizscan.domain.member.dto.RegisterMemberDto;
import com.umc9th.bizscan.domain.member.dto.request.MemberUpdateRequestDto;
import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.member.exception.MemberException;
import com.umc9th.bizscan.domain.member.repository.MemberRepository;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public Long registerMember(RegisterMemberDto registerMemberDto) {

    boolean emailExists = memberRepository.existsByEmail(registerMemberDto.getEmail());
    boolean nicknameExists = memberRepository.existsByNickname(registerMemberDto.getNickname());

    // 1. 둘 다 중복일 경우 이미 등록된 사용자
    if (emailExists && nicknameExists) {
      throw new MemberException(ErrorCode.MEMBER_ALREADY_REGISTERED);
    }

    // 2. 이메일 중복 검사
    if (emailExists) {
      throw new MemberException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    // 3. 닉네임 중복 검사
    if (nicknameExists) {
      throw new MemberException(ErrorCode.NICKNAME_ALREADY_EXISTS);
    }

    // 4. 엔티티 생성
    Member member =
        Member.builder()
            .email(registerMemberDto.getEmail())
            .nickname(registerMemberDto.getNickname())
            .password(passwordEncoder.encode(registerMemberDto.getPassword()))
            .build();

    // 5. 저장
    return memberRepository.save(member).getId();
  }

  @Override
  public void updateMember(Long memberId, MemberUpdateRequestDto dto) {
    // 회원 조회
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

    // 1. 닉네임 수정 (입력값이 있을 경우에만)
    if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
      member.updateNickname(dto.getNickname());
    }

    // 2. 비밀번호 수정 (새 비밀번호 입력값이 있을 경우에만)
    if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
      // 현재 비밀번호 입력 확인
      if (dto.getCurrentPassword() == null
          || !passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
        throw new GeneralException(ErrorCode.INVALID_PASSWORD);
      }

      // 기존 비밀번호와 동일한지 확인
      if (passwordEncoder.matches(dto.getNewPassword(), member.getPassword())) {
        throw new GeneralException(ErrorCode.SAME_AS_OLD_PASSWORD);
      }

      String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
      member.updatePassword(encodedNewPassword);
    }
  }

  @Override
  public void deleteMember(Long memberId) {

    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

    memberRepository.deleteByIdBulk(member.getId());
  }
}
