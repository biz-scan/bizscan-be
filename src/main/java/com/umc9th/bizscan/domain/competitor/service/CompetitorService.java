package com.umc9th.bizscan.domain.competitor.service;

import com.umc9th.bizscan.domain.competitor.repository.CompetitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompetitorService {
  private final CompetitorRepository competitorRepository;
}
