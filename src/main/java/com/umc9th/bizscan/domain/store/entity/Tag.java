package com.umc9th.bizscan.domain.store.entity;

import com.umc9th.bizscan.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
    name = "tag",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_tag_type_name",
          columnNames = {"type", "name"})
    })
public class Tag extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TagCode.Type type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private TagCode.Name name;

  public static Tag of(TagCode.Type type, TagCode.Name name) {
    return Tag.builder().type(type).name(name).build();
  }

  public void updateName(TagCode.Name name) {
    this.name = name;
  }
}
