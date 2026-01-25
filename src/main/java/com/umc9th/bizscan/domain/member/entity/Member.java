package com.umc9th.bizscan.domain.member.entity;

import com.umc9th.bizscan.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity {
  // TODO : 추후 implements UserDetails(security 의존성 추가)
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 이메일
  @Column(name = "email", nullable = false, unique = true)
  private String email;

  // 닉네임
  @Column(name = "nickname", nullable = false, unique = true)
  private String nickname;

  // 비밀번호 : 영어와 숫자를 조합하여 6~15자로 입력하세요.
  private String password;
}
