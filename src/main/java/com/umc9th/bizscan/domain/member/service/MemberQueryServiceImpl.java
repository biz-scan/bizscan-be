package com.umc9th.bizscan.domain.member.service;

import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.member.repository.MemberRepository;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) //queryservice에는 readonly
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;

    @Override
    public Member getMemberById(Long Id){
        return memberRepository.findById(Id)
                .orElseThrow(()->new GeneralException(ErrorCode.MEMBER_NOT_FOUND)) ;
    };

    @Override
    public Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(()-> new GeneralException(ErrorCode.MEMBER_NOT_FOUND)) ;
    };
}
