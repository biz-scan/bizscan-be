package com.umc9th.bizscan.domain.member.repository;

import com.umc9th.bizscan.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByNickname(String nickname);

  Optional<Member> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  String email(String email);

  @Query(
      "select m from Member m "
          + "left join fetch m.store s "
          + "left join fetch s.analysis a "
          + "left join fetch a.analysisRequest "
          + "where m.email = :email")
  Optional<Member> findByEmailWithStoreAndAnalysis(@Param("email") String email);

  @Modifying(clearAutomatically = true) // 쿼리 실행 후 영속성 컨텍스트를 자동으로 비워줌
  @Query("delete from Member m where m.id = :memberId")
  void deleteByIdBulk(@Param("memberId") Long memberId);
}
