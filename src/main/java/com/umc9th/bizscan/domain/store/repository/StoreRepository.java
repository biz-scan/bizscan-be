package com.umc9th.bizscan.domain.store.repository;

import com.umc9th.bizscan.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

  boolean existsByAddress(String address);
}