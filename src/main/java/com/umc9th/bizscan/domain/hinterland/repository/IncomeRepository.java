package com.umc9th.bizscan.domain.hinterland.repository;

import com.umc9th.bizscan.domain.hinterland.entity.IncomeStat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IncomeRepository extends JpaRepository<IncomeStat, Long> {
  Optional<IncomeStat> findFirstByAdstrdNmOrderByStdDateDesc(String adstrdNm);

  @Query("SELECT AVG(i.avgMonIncome) FROM IncomeStat i")
  Double findAverageIncome();
}
