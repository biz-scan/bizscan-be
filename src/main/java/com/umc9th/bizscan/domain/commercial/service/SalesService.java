package com.umc9th.bizscan.domain.commercial.service;

import com.umc9th.bizscan.domain.commercial.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesService {
  private final SalesRepository salesRepository;
  // ...
}
