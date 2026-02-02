package com.umc9th.bizscan.domain.store.service;

import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.member.repository.MemberRepository;
import com.umc9th.bizscan.domain.store.dto.request.StoreRequest;
import com.umc9th.bizscan.domain.store.dto.response.StoreDeleteResponse;
import com.umc9th.bizscan.domain.store.dto.response.StoreResponse;
import com.umc9th.bizscan.domain.store.entity.Store;
import com.umc9th.bizscan.domain.store.entity.StoreTag;
import com.umc9th.bizscan.domain.store.entity.Tag;
import com.umc9th.bizscan.domain.store.entity.TagCode;
import com.umc9th.bizscan.domain.store.exception.StoreErrorCode;
import com.umc9th.bizscan.domain.store.mapper.StoreMapper;
import com.umc9th.bizscan.domain.store.repository.StoreRepository;
import com.umc9th.bizscan.domain.store.repository.StoreTagRepository;
import com.umc9th.bizscan.domain.store.repository.TagRepository;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import com.umc9th.bizscan.global.client.kakao.KakaoClient;
import com.umc9th.bizscan.global.client.kakao.dto.GeoPoint;
import com.umc9th.bizscan.global.client.kakao.dto.KakaoApiResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreServiceImpl implements StoreService {

  private static final Logger log = LoggerFactory.getLogger(StoreServiceImpl.class);

  private static final int TAG_MAX_COUNT = 3;

  private final StoreRepository storeRepository;
  private final TagRepository tagRepository;
  private final StoreTagRepository storeTagRepository;
  private final MemberRepository memberRepository;
  private final StoreMapper storeMapper;
  private final KakaoClient kakaoClient;

  @Override
  public StoreResponse createStore(StoreRequest request) {
    log.info(
        "Create store request received. memberId={}, address={}",
        request.getMemberId(),
        request.getAddress());

    if (storeRepository.existsByAddress(request.getAddress())) {
      log.warn("Duplicated address detected. address={}", request.getAddress());
      throw new GeneralException(StoreErrorCode.ADDRESS_DUPLICATED);
    }

    Member member =
        memberRepository
            .findById(request.getMemberId())
            .orElseThrow(
                () -> {
                  log.warn("Member not found. memberId={}", request.getMemberId());
                  return new GeneralException(StoreErrorCode.MEMBER_NOT_FOUND);
                });

    GeoPoint geoPoint;
    try {
      KakaoApiResponse response = kakaoClient.searchAddress(request.getAddress());

      if (response == null
          || response.getDocuments() == null
          || response.getDocuments().isEmpty()) {
        log.warn("Geocode result is empty. address={}", request.getAddress());
        throw new GeneralException(StoreErrorCode.ADDRESS_INVALID);
      }

      KakaoApiResponse.Address addressInfo = response.getDocuments().get(0).getAddress();

      geoPoint =
          new GeoPoint(
              new BigDecimal(addressInfo.getY()), // 위도
              new BigDecimal(addressInfo.getX()) // 경도
          );

    } catch (Exception e) {
      log.warn(
          "Failed to geocode address. address={}, reason={}", request.getAddress(), e.getMessage());
      throw new GeneralException(StoreErrorCode.ADDRESS_INVALID);
    }

    Store saved =
        storeRepository.save(storeMapper.toEntity(member, request, geoPoint.lat(), geoPoint.lon()));

    log.info(
        "Store saved. storeId={}, address={}, lat={}, lon={}",
        saved.getId(),
        saved.getAddress(),
        saved.getLat(),
        saved.getLon());

    // tags null 방어 + null 요소 제거
    List<TagCode> tagCodes =
        Optional.ofNullable(request.getTags()).orElseGet(List::of).stream()
            .filter(Objects::nonNull)
            .toList();

    if (tagCodes.isEmpty()) {
      log.warn("Tag is required but empty. storeId={}", saved.getId());
      throw new GeneralException(StoreErrorCode.TAG_REQUIRED);
    }

    // 중복 제거 + 순서 보존
    LinkedHashSet<TagCode> distinct = new LinkedHashSet<>(tagCodes);

    if (distinct.size() != tagCodes.size()) {
      log.warn("Duplicated tags detected. storeId={}, tags={}", saved.getId(), tagCodes);
      throw new GeneralException(StoreErrorCode.TAG_DUPLICATED);
    }
    if (distinct.size() > TAG_MAX_COUNT) {
      log.warn("Tag limit exceeded. storeId={}, size={}", saved.getId(), distinct.size());
      throw new GeneralException(StoreErrorCode.TAG_LIMIT_EXCEEDED);
    }

    List<TagCode> orderedDistinct = new ArrayList<>(distinct);

    // 필요한 type/name만 뽑아서 한번에 조회
    List<TagCode.Type> types = orderedDistinct.stream().map(TagCode::getType).distinct().toList();

    List<TagCode.Name> names = orderedDistinct.stream().map(TagCode::getName).distinct().toList();

    List<Tag> candidates = tagRepository.findAllByTypeInAndNameIn(types, names);

    // DB 데이터가 중복되어 있어도 서버가 죽지 않도록 merge 함수 추가
    Map<String, Tag> tagMap =
        candidates.stream()
            .collect(Collectors.toMap(t -> key(t.getType(), t.getName()), t -> t, (a, b) -> a));

    // 요청 순서대로 검증 + 태그 리스트 구성
    List<Tag> tags = new ArrayList<>();
    for (TagCode code : orderedDistinct) {
      Tag tag = tagMap.get(key(code.getType(), code.getName()));
      if (tag == null) {
        log.warn("Tag not found in DB. storeId={}, requestedTag={}", saved.getId(), code);
        throw new GeneralException(StoreErrorCode.TAG_NOT_FOUND);
      }
      tags.add(tag);
    }

    storeTagRepository.saveAll(tags.stream().map(t -> StoreTag.of(saved, t)).toList());
    log.info("Store tags saved. storeId={}, tagCount={}", saved.getId(), tags.size());

    return storeMapper.toCreateResponse(saved, tags);
  }

  @Override
  @Transactional(readOnly = true)
  public List<StoreResponse> getStores() {
    log.info("Get stores request received.");

    List<Store> stores = storeRepository.findAll();
    log.info("Stores loaded. count={}", stores.size());

    if (stores.isEmpty()) {
      return List.of();
    }

    List<StoreTag> storeTags = storeTagRepository.findAllByStoreInFetchTag(stores);

    Map<Long, List<Tag>> tagsByStoreId =
        storeTags.stream()
            .collect(
                Collectors.groupingBy(
                    st -> st.getStore().getId(),
                    Collectors.mapping(StoreTag::getTag, Collectors.toList())));

    return stores.stream()
        .map(store -> storeMapper.toCreateResponse(store,
            tagsByStoreId.getOrDefault(store.getId(), List.of())))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public StoreResponse getStore(Long storeId) {
    log.info("Get store request received. storeId={}", storeId);

    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(
                () -> {
                  log.warn("Store not found. storeId={}", storeId);
                  return new GeneralException(StoreErrorCode.STORE_NOT_FOUND);
                });

    List<Tag> tags =
        storeTagRepository.findAllByStoreFetchTag(store).stream()
            .map(StoreTag::getTag)
            .toList();

    return storeMapper.toCreateResponse(store, tags);
  }

  @Override
  public StoreDeleteResponse deleteStore(Long storeId) {
    log.info("Delete store request received. storeId={}", storeId);

    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(
                () -> {
                  log.warn("Store not found for delete. storeId={}", storeId);
                  return new GeneralException(StoreErrorCode.STORE_NOT_FOUND);
                });

    // FK 때문에 store_tag 먼저 삭제
    storeTagRepository.deleteAllByStore_Id(storeId);

    storeRepository.delete(store);
    log.info("Store deleted. storeId={}", storeId);

    return StoreDeleteResponse.of(storeId);
  }

  private String key(TagCode.Type type, TagCode.Name name) {
    return type.name() + ":" + name.name();
  }
}
