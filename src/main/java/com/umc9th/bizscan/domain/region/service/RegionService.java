package com.umc9th.bizscan.domain.region.service;

import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import com.umc9th.bizscan.domain.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegionService {
  private final RegionRepository regionRepository;

  // TODO: CSV 파일 읽어서 RegionMaster 저장하는 로직 구현 예정
  public void saveRegionData(RegionMaster regionMaster) {
    regionRepository.save(regionMaster);
  }
}
