package com.umc9th.bizscan.domain.member.service;

import com.umc9th.bizscan.domain.member.dto.RegisterMemberDto;
import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.member.exception.MemberException;
import com.umc9th.bizscan.domain.member.repository.MemberRepository;
import com.umc9th.bizscan.global.apiPayload.code.BaseErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl {

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
        Member member = Member.builder()
                .email(registerMemberDto.getEmail())
                .nickname(registerMemberDto.getNickname())
                .password(passwordEncoder.encode(registerMemberDto.getPassword()))
                .build();

        // 5. 저장
        return memberRepository.save(member).getId();
    }
}

