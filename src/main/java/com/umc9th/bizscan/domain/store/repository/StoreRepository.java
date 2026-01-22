package com.umc9th.bizscan.domain.store.repository;

import com.umc9th.bizscan.domain.store.entity.Store;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

  Optional<Store> findById(Long id);

  boolean existsByAddress(String address);
}