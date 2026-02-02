package com.umc9th.bizscan.domain.store.repository;

import com.umc9th.bizscan.domain.store.entity.Store;
import com.umc9th.bizscan.domain.store.entity.StoreTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreTagRepository extends JpaRepository<StoreTag, Long> {

  @Query("""
      select st
      from StoreTag st
      join fetch st.tag
      where st.store in :stores
      """)
  List<StoreTag> findAllByStoreInFetchTag(@Param("stores") List<Store> stores);

  @Query("""
      select st
      from StoreTag st
      join fetch st.tag
      where st.store = :store
      """)
  List<StoreTag> findAllByStoreFetchTag(@Param("store") Store store);

  void deleteAllByStore_Id(Long storeId);
}