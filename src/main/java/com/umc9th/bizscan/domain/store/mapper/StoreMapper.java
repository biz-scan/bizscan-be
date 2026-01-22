package com.umc9th.bizscan.domain.store.mapper;

import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.store.dto.request.StoreRequest;
import com.umc9th.bizscan.domain.store.dto.response.StoreResponse;
import com.umc9th.bizscan.domain.store.entity.Store;
import com.umc9th.bizscan.domain.store.entity.Tag;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StoreMapper {

  public Store toEntity(Member member, StoreRequest request, BigDecimal lat, BigDecimal lon) {
    return Store.builder()
        .member(member)
        .name(request.getName())
        .address(request.getAddress())
        .lat(lat)
        .lon(lon)
        .category(request.getCategory())
        .categoryDetail(request.getCategoryDetail())
        .signature(request.getSignature())
        .price(request.getPrice())
        .target(request.getTarget())
        .painPoint(request.getPainPoint())
        .build();
  }

  public StoreResponse toCreateResponse(Store store, List<Tag> tags) {
    List<StoreResponse.TagInfo> tagInfos =
        tags.stream()
            .map(
                t ->
                    StoreResponse.TagInfo.builder()
                        .id(t.getId())
                        .type(t.getType().name())
                        .name(t.getName().name())
                        .build())
            .toList();

    return StoreResponse.of(
        store.getId(),
        store.getName(),
        store.getAddress(),
        store.getLat(),
        store.getLon(),
        store.getCategory().name(),
        store.getCategoryDetail().name(),
        store.getSignature(),
        store.getPrice().name(),
        store.getTarget().name(),
        store.getPainPoint().name(),
        tagInfos);
  }
}
