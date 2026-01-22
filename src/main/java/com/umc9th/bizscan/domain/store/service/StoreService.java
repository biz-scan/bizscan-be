package com.umc9th.bizscan.domain.store.service;

import com.umc9th.bizscan.domain.store.dto.request.StoreRequest;
import com.umc9th.bizscan.domain.store.dto.response.StoreDeleteResponse;
import com.umc9th.bizscan.domain.store.dto.response.StoreResponse;
import java.util.List;

public interface StoreService {

  StoreResponse createStore(StoreRequest request);

  List<StoreResponse> getStores();

  StoreResponse getStore(Long storeId);

  StoreDeleteResponse deleteStore(Long storeId);
}
