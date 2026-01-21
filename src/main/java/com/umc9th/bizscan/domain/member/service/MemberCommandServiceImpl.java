package com.umc9th.bizscan.domain.member.service;

import com.umc9th.bizscan.domain.member.dto.RegisterMemberDto;
import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.member.repository.MemberRepository;
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

        // 1. 이메일 중복 검사
        if (memberRepository.existsByEmail(registerMemberDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2. 닉네임 중복 검사
        if (memberRepository.existsByNickname(registerMemberDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 3. 엔티티 생성
        Member member = Member.builder()
                .email(registerMemberDto.getEmail())
                .nickname(registerMemberDto.getNickname())
                .password(passwordEncoder.encode(registerMemberDto.getPassword()))
                .build();

        // 4. 저장
        return memberRepository.save(member).getId();
    }
}

