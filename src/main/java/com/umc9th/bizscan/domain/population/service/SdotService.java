package com.umc9th.bizscan.domain.population.service;

import com.umc9th.bizscan.domain.population.repository.SdotRepository;
import com.umc9th.bizscan.domain.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SdotService {
  private final SdotRepository sdotRepository;
  private final RegionRepository regionRepository; // 부모 찾기용
}
