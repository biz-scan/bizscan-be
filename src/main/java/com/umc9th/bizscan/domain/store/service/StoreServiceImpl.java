package com.umc9th.bizscan.domain.store.service;

import com.umc9th.bizscan.domain.aiAnalysis.repository.AnalysisRepository;
import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.member.repository.MemberRepository;
import com.umc9th.bizscan.domain.store.dto.request.StoreCreateRequest;
import com.umc9th.bizscan.domain.store.dto.request.StoreUpdateRequest;
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
  private final AnalysisRepository analysisRepository;

  @Override
  public StoreResponse createStore(String email, StoreCreateRequest request) {

    if (storeRepository.existsByAddress(request.getAddress())) {
      throw new GeneralException(StoreErrorCode.ADDRESS_DUPLICATED);
    }

    Member member =
        memberRepository
            .findByEmail(email)
            .orElseThrow(() -> new GeneralException(StoreErrorCode.MEMBER_NOT_FOUND));

    GeoPoint geoPoint = geocode(request.getAddress());

    Store saved =
        storeRepository.save(storeMapper.toEntity(member, request, geoPoint.lat(), geoPoint.lon()));

    List<Tag> tags = resolveTags(request.getTags(), saved.getId());

    storeTagRepository.saveAll(tags.stream().map(tag -> StoreTag.of(saved, tag)).toList());

    return storeMapper.toCreateResponse(saved, tags);
  }

  @Override
  @Transactional(readOnly = true)
  public List<StoreResponse> getStores() {

    List<Store> stores = storeRepository.findAll();

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
        .map(
            store ->
                storeMapper.toCreateResponse(
                    store, tagsByStoreId.getOrDefault(store.getId(), List.of())))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public StoreResponse getStore(Long storeId) {

    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

    List<Tag> tags =
        storeTagRepository.findAllByStoreFetchTag(store).stream().map(StoreTag::getTag).toList();

    return storeMapper.toCreateResponse(store, tags);
  }

  @Override
  @Transactional(readOnly = true)
  public StoreResponse getMyStore(String email) {

    Member member =
        memberRepository
            .findByEmail(email)
            .orElseThrow(() -> new GeneralException(StoreErrorCode.MEMBER_NOT_FOUND));

    Store store =
        storeRepository
            .findByMember(member)
            .orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

    List<Tag> tags =
        storeTagRepository.findAllByStoreFetchTag(store).stream().map(StoreTag::getTag).toList();

    Long analysisId = analysisRepository.findLatestAnalysisIdByStoreId(store.getId()).orElse(null);

    return storeMapper.toCreateResponse(store, tags, analysisId);
  }

  @Override
  public StoreResponse updateStore(Long storeId, String email, StoreUpdateRequest request) {

    Store store = validateStoreOwner(storeId, email);

    String newAddress = Optional.ofNullable(request.getAddress()).orElse(store.getAddress());

    BigDecimal lat = store.getLat();
    BigDecimal lon = store.getLon();

    if (!store.getAddress().equals(newAddress)) {

      if (storeRepository.existsByAddressAndIdNot(newAddress, storeId)) {
        throw new GeneralException(StoreErrorCode.ADDRESS_DUPLICATED);
      }

      GeoPoint geoPoint = geocode(newAddress);
      lat = geoPoint.lat();
      lon = geoPoint.lon();
    }

    store.update(
        Optional.ofNullable(request.getName()).orElse(store.getName()),
        newAddress,
        lat,
        lon,
        Optional.ofNullable(request.getCategory()).orElse(store.getCategory()),
        Optional.ofNullable(request.getCategoryDetail()).orElse(store.getCategoryDetail()),
        Optional.ofNullable(request.getSignature()).orElse(store.getSignature()),
        Optional.ofNullable(request.getPrice()).orElse(store.getPrice()),
        Optional.ofNullable(request.getTarget()).orElse(store.getTarget()),
        Optional.ofNullable(request.getPainPoint()).orElse(store.getPainPoint()));

    List<Tag> tags =
        storeTagRepository.findAllByStoreFetchTag(store).stream().map(StoreTag::getTag).toList();

    Long analysisId = analysisRepository.findLatestAnalysisIdByStoreId(storeId).orElse(null);

    return storeMapper.toCreateResponse(store, tags, analysisId);
  }

  @Override
  public StoreResponse updateStoreTags(Long storeId, String email, List<String> tagStrings) {

    Store store = validateStoreOwner(storeId, email);

    List<TagCode> tagCodes = parseTagCodes(tagStrings);
    List<Tag> tags = resolveTags(tagCodes, storeId);

    storeTagRepository.deleteAllByStoreId(storeId);
    storeTagRepository.saveAll(tags.stream().map(tag -> StoreTag.of(store, tag)).toList());

    Long analysisId = analysisRepository.findLatestAnalysisIdByStoreId(storeId).orElse(null);

    return storeMapper.toCreateResponse(store, tags, analysisId);
  }

  @Override
  public StoreDeleteResponse deleteStore(Long storeId) {

    storeRepository
        .findById(storeId)
        .orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

    storeTagRepository.deleteAllByStoreId(storeId);
    storeRepository.deleteById(storeId);

    return StoreDeleteResponse.of(storeId);
  }

  private Store validateStoreOwner(Long storeId, String email) {

    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

    Member loginMember =
        memberRepository
            .findByEmail(email)
            .orElseThrow(() -> new GeneralException(StoreErrorCode.MEMBER_NOT_FOUND));

    if (!store.getMember().getId().equals(loginMember.getId())) {
      throw new GeneralException(StoreErrorCode.FORBIDDEN);
    }

    return store;
  }

  private List<TagCode> parseTagCodes(List<String> tagStrings) {

    if (tagStrings == null || tagStrings.isEmpty()) {
      throw new GeneralException(StoreErrorCode.TAG_REQUIRED);
    }

    try {
      return tagStrings.stream()
          .filter(Objects::nonNull)
          .map(String::trim)
          .map(TagCode::valueOf)
          .toList();

    } catch (IllegalArgumentException e) {
      throw new GeneralException(StoreErrorCode.TAG_CODE_INVALID);
    }
  }

  private List<Tag> resolveTags(List<TagCode> tagCodes, Long storeId) {

    if (tagCodes == null || tagCodes.isEmpty()) {
      throw new GeneralException(StoreErrorCode.TAG_REQUIRED);
    }

    LinkedHashSet<TagCode> distinct = new LinkedHashSet<>(tagCodes);

    if (distinct.size() > TAG_MAX_COUNT) {
      throw new GeneralException(StoreErrorCode.TAG_LIMIT_EXCEEDED);
    }

    List<TagCode> ordered = new ArrayList<>(distinct);

    List<Tag> candidates =
        tagRepository.findAllByTypeInAndNameIn(
            ordered.stream().map(TagCode::getType).toList(),
            ordered.stream().map(TagCode::getName).toList());

    Map<String, Tag> map =
        candidates.stream()
            .collect(Collectors.toMap(t -> t.getType().name() + ":" + t.getName().name(), t -> t));

    return ordered.stream()
        .map(
            code -> {
              Tag tag = map.get(code.getType().name() + ":" + code.getName().name());
              if (tag == null) {
                throw new GeneralException(StoreErrorCode.TAG_NOT_FOUND);
              }
              return tag;
            })
        .toList();
  }

  private GeoPoint geocode(String address) {

    KakaoApiResponse response = kakaoClient.searchAddress(address);

    if (response == null || response.getDocuments().isEmpty()) {
      throw new GeneralException(StoreErrorCode.ADDRESS_INVALID);
    }

    var addr = response.getDocuments().get(0).getAddress();

    return new GeoPoint(new BigDecimal(addr.getY()), new BigDecimal(addr.getX()));
  }
}
