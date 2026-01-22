package com.umc9th.bizscan.domain.store.service;

import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.member.repository.MemberRepository;
import com.umc9th.bizscan.domain.store.client.KakaoGeoClient;
import com.umc9th.bizscan.domain.store.dto.request.StoreRequest;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreServiceImpl implements StoreService {

  private static final int TAG_MAX_COUNT = 3;

  private final StoreRepository storeRepository;
  private final TagRepository tagRepository;
  private final StoreTagRepository storeTagRepository;
  private final MemberRepository memberRepository;
  private final StoreMapper storeMapper;
  private final KakaoGeoClient kakaoGeoClient;

  @Override
  public StoreResponse createStore(StoreRequest request) {

    if (storeRepository.existsByAddress(request.getAddress())) {
      throw new GeneralException(StoreErrorCode.ADDRESS_DUPLICATED);
    }

    Member member =
        memberRepository
            .findById(request.getMemberId())
            .orElseThrow(() -> new GeneralException(StoreErrorCode.MEMBER_NOT_FOUND));

    KakaoGeoClient.GeoPoint geo;
    try {
      geo = kakaoGeoClient.getCoordinates(request.getAddress());
    } catch (Exception e) {
      throw new GeneralException(StoreErrorCode.ADDRESS_INVALID);
    }
    if (geo == null || geo.lat() == null || geo.lon() == null) {
      throw new GeneralException(StoreErrorCode.ADDRESS_INVALID);
    }

    Store saved =
        storeRepository.save(
            storeMapper.toEntity(member, request, geo.lat(), geo.lon()));

    List<TagCode> tagCodes =
        Optional.ofNullable(request.getTags()).orElseGet(List::of).stream()
            .filter(Objects::nonNull)
            .toList();

    if (tagCodes.isEmpty()) {
      throw new GeneralException(StoreErrorCode.TAG_REQUIRED);
    }

    LinkedHashSet<TagCode> distinct = new LinkedHashSet<>(tagCodes);

    if (distinct.size() != tagCodes.size()) {
      throw new GeneralException(StoreErrorCode.TAG_DUPLICATED);
    }
    if (distinct.size() > TAG_MAX_COUNT) {
      throw new GeneralException(StoreErrorCode.TAG_LIMIT_EXCEEDED);
    }

    List<TagCode> orderedDistinct = new ArrayList<>(distinct);

    List<TagCode.Type> types =
        orderedDistinct.stream().map(TagCode::getType).distinct().toList();

    List<TagCode.Name> names =
        orderedDistinct.stream().map(TagCode::getName).distinct().toList();

    List<Tag> candidates = tagRepository.findAllByTypeInAndNameIn(types, names);

    Map<String, Tag> tagMap =
        candidates.stream()
            .collect(Collectors.toMap(t -> key(t.getType(), t.getName()), t -> t));

    List<Tag> tags = new ArrayList<>();
    for (TagCode code : orderedDistinct) {
      Tag tag = tagMap.get(key(code.getType(), code.getName()));
      if (tag == null) {
        throw new GeneralException(StoreErrorCode.TAG_NOT_FOUND);
      }
      tags.add(tag);
    }

    storeTagRepository.saveAll(tags.stream().map(t -> StoreTag.of(saved, t)).toList());

    return storeMapper.toCreateResponse(saved, tags);
  }

  private String key(TagCode.Type type, TagCode.Name name) {
    return type.name() + ":" + name.name();
  }

  @Override
  @Transactional(readOnly = true)
  public List<StoreResponse> getStores() {
    return storeRepository.findAll().stream()
        .map(store -> {
          List<Tag> tags =
              storeTagRepository.findAllByStore(store).stream()
                  .map(StoreTag::getTag)
                  .toList();

          return storeMapper.toCreateResponse(store, tags);
        })
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
        storeTagRepository.findAllByStore(store).stream()
            .map(StoreTag::getTag)
            .toList();

    return storeMapper.toCreateResponse(store, tags);
  }
}