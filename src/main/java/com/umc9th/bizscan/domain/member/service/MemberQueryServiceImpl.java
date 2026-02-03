package com.umc9th.bizscan.domain.member.service;

import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.member.repository.MemberRepository;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // queryservice에는 readonly
public class MemberQueryServiceImpl implements MemberQueryService {

  private final MemberRepository memberRepository;

  @Override
  public Member getMemberById(Long Id) {
    return memberRepository
        .findById(Id)
        .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));
  }

  @Override
  public Member getMemberByEmail(String email) {
    return memberRepository
        .findByEmailWithStore(email)    // store도 같이 조회하도록 변경 (FetchJoin)
        .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));
  }

  @Override
  public List<Member> getAllMembers() {
    return memberRepository.findAll();
  }

  @Override
  public boolean validateExistNickname(String nickname) {
    return memberRepository.existsByNickname(nickname);
  }

  @Override
  public boolean validateExistEmail(String email) {
    return memberRepository.existsByEmail(email);
  }
}
