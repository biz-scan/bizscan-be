package com.umc9th.bizscan.domain.population.repository;

import com.umc9th.bizscan.domain.population.entity.SdotPop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SdotRepository extends JpaRepository<SdotPop, Long> {
}
