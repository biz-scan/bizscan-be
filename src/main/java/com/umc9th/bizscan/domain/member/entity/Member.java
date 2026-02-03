package com.umc9th.bizscan.domain.member.entity;

import com.umc9th.bizscan.domain.store.entity.Store;
import com.umc9th.bizscan.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.Collection;
import java.util.Collections;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity implements UserDetails {
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
  @Column(name = "password", nullable = false)
  private String password;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    private Store store;

  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singleton(new SimpleGrantedAuthority("ROLE_MEMBER"));
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public boolean isAccountNonExpired() {
    return false;
  }

  @Override
  public boolean isAccountNonLocked() {
    return false;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public String getUsername() {
    return this.email; // 로그인 ID로 email 사용
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }
}
