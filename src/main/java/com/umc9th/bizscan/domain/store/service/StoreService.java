package com.umc9th.bizscan.domain.store.service;

import com.umc9th.bizscan.domain.store.dto.request.StoreRequest;
import com.umc9th.bizscan.domain.store.dto.response.StoreResponse;

public interface StoreService {

  StoreResponse createStore(StoreRequest request);
}