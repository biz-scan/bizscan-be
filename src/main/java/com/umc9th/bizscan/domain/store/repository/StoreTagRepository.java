package com.umc9th.bizscan.domain.store.repository;

import com.umc9th.bizscan.domain.store.entity.Store;
import com.umc9th.bizscan.domain.store.entity.StoreTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreTagRepository extends JpaRepository<StoreTag, Long> {

  List<StoreTag> findAllByStore(Store store);
}