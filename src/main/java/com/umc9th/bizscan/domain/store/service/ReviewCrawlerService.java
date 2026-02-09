package com.umc9th.bizscan.domain.store.service;

import com.umc9th.bizscan.domain.store.entity.StoreCrawlingData;
import com.umc9th.bizscan.domain.store.repository.StoreCrawlingDataRepository;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewCrawlerService {

  private final StoreCrawlingDataRepository storeCrawlingDataRepository;

  // 1. 메인 로직
  public StoreCrawlingData getStoreCrawlingData(String placeId, String storeName) {
    Optional<StoreCrawlingData> existingOpt = storeCrawlingDataRepository.findByPlaceId(placeId);

    if (existingOpt.isPresent()) {
      StoreCrawlingData existing = existingOpt.get();
      // 0개면 재시도
      if (existing.getReviewCount() == 0) {
        log.info("리뷰 0개 발견 -> 재크롤링 시도 ID: {}", placeId);
        StoreCrawlingDataDto dto = crawlDataPureJava(placeId);
        return updateExistingData(existing, dto);
      }
      log.info("DB 캐시 사용: {} (리뷰 {}개)", storeName, existing.getReviewCount());
      return existing;
    }

    log.info("신규 수집 시작: {} (ID:{})", storeName, placeId);
    StoreCrawlingDataDto dto = crawlDataPureJava(placeId);
    return saveCrawlingData(placeId, storeName, dto);
  }

  // 2. ID 찾기
  public String findPlaceId(String address, String storeName) {
    String placeId = "";
    try {
      Thread.sleep((long) (Math.random() * 300) + 200);

      String[] addrParts = address.split(" ");
      String shortAddr = (addrParts.length >= 2) ? addrParts[0] + " " + addrParts[1] : address;
      String query = shortAddr + " " + storeName;

      // Googlebot
      Document doc =
          Jsoup.connect("https://m.search.naver.com/search.naver?query=" + query)
              .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
              .referrer("https://www.google.com")
              .timeout(10000)
              .get();

      // 링크 우선 탐색
      Elements links =
          doc.select("a[href*='place.naver.com/restaurant'], a[href*='place.naver.com/place']");
      for (Element link : links) {
        String href = link.attr("href");
        placeId = extractIdFromUrl(href);
        if (!placeId.isEmpty()) break;
      }

      // JSON 데이터 탐색
      if (placeId.isEmpty()) {
        Pattern p = Pattern.compile("(\"id\"|\"cid\")\\s*:\\s*\"?(\\d{7,})\"?");
        Matcher m = p.matcher(doc.html());
        if (m.find()) placeId = m.group(2);
      }

      // data-cid 탐색
      if (placeId.isEmpty()) {
        Element cidEl = doc.selectFirst("[data-cid]");
        if (cidEl != null) placeId = cidEl.attr("data-cid");
      }

      if (!placeId.isEmpty()) log.info("ID 찾기 성공: {}", placeId);

    } catch (Exception e) {
      log.error("ID 찾기 에러: {}", e.getMessage());
    }
    return placeId;
  }

  // =========================================================
  // 3. 데이터 수집 (Googlebot + 2중 탐색)
  // =========================================================
  public StoreCrawlingDataDto crawlDataPureJava(String placeId) {
    int reviewCount = 0;
    double rating = 0.0;

    try {
      // 1차 시도: 홈 탭 (/home)
      // Googlebot UserAgent
      String homeUrl = "https://m.place.naver.com/restaurant/" + placeId + "/home";

      Document doc =
          Jsoup.connect(homeUrl)
              .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
              .referrer("https://m.place.naver.com")
              .timeout(10000)
              .ignoreHttpErrors(true)
              .get();

      String html = doc.html();

      // 별점 파싱
      Matcher rateM = Pattern.compile("\"ratingValue\"\\s*:\\s*\"?([0-9.]+)\"?").matcher(html);
      if (rateM.find()) rating = Double.parseDouble(rateM.group(1));

      // 리뷰 수 파싱 (JSON)
      Matcher countM =
          Pattern.compile("(\"visitorReviewsTotal\"|\"reviewCount\")\\s*:\\s*(\\d+)").matcher(html);
      if (countM.find()) {
        reviewCount = Integer.parseInt(countM.group(2));
      }

      // -----------------------------------------------------------
      // [2차 시도] 홈에서 못 찾았으면 '리뷰 탭'으로 직접 이동
      // -----------------------------------------------------------
      if (reviewCount == 0) {
        log.info("홈 탭에서 리뷰 못 찾음. 리뷰 탭으로 이동... ID: {}", placeId);
        String reviewUrl = "https://m.place.naver.com/restaurant/" + placeId + "/review/visitor";

        Document reviewDoc =
            Jsoup.connect(reviewUrl)
                .userAgent(
                    "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                .timeout(10000)
                .ignoreHttpErrors(true)
                .get();

        String reviewHtml = reviewDoc.html();
        String reviewText = reviewDoc.text();

        // 텍스트에서 "방문자리뷰 1,234" 찾기 (가장 강력함)
        Matcher textM = Pattern.compile("(방문자|인증)\\s*리뷰\\s*([0-9,]+)").matcher(reviewText);
        if (textM.find()) {
          reviewCount = Integer.parseInt(textM.group(2).replace(",", ""));
        }
      }

      // 별점 보정 (리뷰는 있는데 별점이 0이면)
      if (rating == 0.0 && reviewCount > 0) {
        rating = calculateEstimatedRating(reviewCount);
      }

      log.info("최종 크롤링 결과(ID:{}) -> 리뷰: {}, 별점: {}", placeId, reviewCount, rating);

    } catch (Exception e) {
      log.error("크롤링 실패 (ID: {}): {}", placeId, e.getMessage());
    }

    return new StoreCrawlingDataDto(reviewCount, rating);
  }

  private double calculateEstimatedRating(int reviewCount) {
    if (reviewCount == 0) return 0.0;
    if (reviewCount < 10) return 3.5;
    if (reviewCount < 50) return 4.0;
    if (reviewCount < 100) return 4.2;
    if (reviewCount < 500) return 4.4;
    if (reviewCount < 1000) return 4.5;
    return 4.7;
  }

  @Transactional
  protected StoreCrawlingData saveCrawlingData(
      String placeId, String storeName, StoreCrawlingDataDto dto) {
    StoreCrawlingData newData =
        StoreCrawlingData.builder()
            .placeId(placeId)
            .storeName(storeName)
            .reviewCount(dto.getReviewCount())
            .rating(dto.getRating())
            .build();
    return storeCrawlingDataRepository.save(newData);
  }

  @Transactional
  protected StoreCrawlingData updateExistingData(StoreCrawlingData data, StoreCrawlingDataDto dto) {
    data.updateData(dto.getReviewCount(), dto.getRating());
    return storeCrawlingDataRepository.save(data);
  }

  private String extractIdFromUrl(String url) {
    if (url == null) return "";
    Pattern pattern = Pattern.compile("(restaurant|place)/(\\d+)");
    Matcher matcher = pattern.matcher(url);
    if (matcher.find()) return matcher.group(2);
    return "";
  }

  @Getter
  @AllArgsConstructor
  public static class StoreCrawlingDataDto {
    private int reviewCount;
    private double rating;
  }
}
