package com.umc9th.bizscan.domain.hinterland.repository;

import com.umc9th.bizscan.domain.hinterland.entity.IncomeStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeRepository extends JpaRepository<IncomeStat, Long> {}
