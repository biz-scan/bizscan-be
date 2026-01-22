package com.umc9th.bizscan.domain.store.client.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class KakaoGeoResponse {

  private List<Document> documents;

  @Getter
  public static class Document {

    private Address address;
  }

  @Getter
  public static class Address {

    private String x; // longitude
    private String y; // latitude
  }
}