package com.umc9th.bizscan.domain.hinterland.repository;
import com.umc9th.bizscan.domain.hinterland.entity.IncomeStat;
import com.umc9th.bizscan.domain.hinterland.entity.HousingStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HousingRepository extends JpaRepository<HousingStat, Long> {
}
