package com.umc9th.bizscan.domain.store.init;

import com.umc9th.bizscan.domain.store.entity.Tag;
import com.umc9th.bizscan.domain.store.entity.TagCode;
import com.umc9th.bizscan.domain.store.repository.TagRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TagInitializer {

  private final TagRepository tagRepository;

  @PostConstruct
  @Transactional
  public void init() {
    List<TagCode> tags = List.of(
        // MOOD
        TagCode.MOOD_VIEW,
        TagCode.MOOD_HIP,
        TagCode.MOOD_QUIET,
        TagCode.MOOD_RETRO,
        TagCode.MOOD_LUXURY,
        TagCode.MOOD_LIVELY,

        // FEATURE
        TagCode.FEATURE_GOOD_VALUE,
        TagCode.FEATURE_SOLO_FRIENDLY,
        TagCode.FEATURE_GROUP_SEAT,
        TagCode.FEATURE_PET_FRIENDLY,
        TagCode.FEATURE_PHOTO_SPOT,

        // OPERATION
        TagCode.OPERATION_HALL_SERVICE,
        TagCode.OPERATION_DELIVERY_AVAILABLE,
        TagCode.OPERATION_TAKEOUT_ONLY
    );

    for (TagCode code : tags) {
      if (!tagRepository.existsByTypeAndName(code.getType(), code.getName())) {
        tagRepository.save(Tag.of(code.getType(), code.getName()));
      }
    }
  }
}