package com.umc9th.bizscan.domain.store.service;

import com.umc9th.bizscan.domain.store.dto.request.StoreCreateRequest;
import com.umc9th.bizscan.domain.store.dto.request.StoreUpdateRequest;
import com.umc9th.bizscan.domain.store.dto.response.StoreDeleteResponse;
import com.umc9th.bizscan.domain.store.dto.response.StoreResponse;
import java.util.List;

public interface StoreService {

  StoreResponse createStore(String email, StoreCreateRequest request);

  List<StoreResponse> getStores();

  StoreResponse getStore(Long storeId);

  StoreResponse updateStore(Long storeId, String email, StoreUpdateRequest request);

  StoreResponse updateStoreTags(Long storeId, String email, List<String> tags);

  StoreDeleteResponse deleteStore(Long storeId);
}
