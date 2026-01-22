package com.umc9th.bizscan.domain.store.repository;

import com.umc9th.bizscan.domain.store.entity.Tag;
import com.umc9th.bizscan.domain.store.entity.TagCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

  List<Tag> findAllByTypeInAndNameIn(List<TagCode.Type> types, List<TagCode.Name> names);

  boolean existsByTypeAndName(TagCode.Type type, TagCode.Name name);
}
