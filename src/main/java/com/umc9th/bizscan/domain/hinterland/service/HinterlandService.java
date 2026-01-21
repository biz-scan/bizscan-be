package com.umc9th.bizscan.domain.hinterland.service;

import com.umc9th.bizscan.domain.hinterland.repository.HousingRepository;
import com.umc9th.bizscan.domain.hinterland.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HinterlandService {
    private final IncomeRepository incomeRepository;
    private final HousingRepository housingRepository; // (Repository 따로 만들었다면)
}
