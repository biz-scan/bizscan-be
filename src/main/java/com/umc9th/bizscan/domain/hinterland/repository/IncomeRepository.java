package com.umc9th.bizscan.domain.hinterland.repository;

import com.umc9th.bizscan.domain.hinterland.entity.IncomeStat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IncomeRepository extends JpaRepository<IncomeStat, Long> {
  // 행정동 명으로 조회하되, 가장 최신 데이터(분기) 1개만 가져옴
  Optional<IncomeStat> findFirstByAdstrdNmOrderByStdDateDesc(String adstrdNm);

  // csv 파일에 행정동이 없을 경우 성동구 전체의 월 평균 소득으로 대체
  @Query("SELECT AVG(i.avgMonIncome) FROM IncomeStat i")
  Double findAverageIncome();
}
