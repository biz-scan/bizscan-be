package com.umc9th.bizscan.domain.store.service;

import com.umc9th.bizscan.domain.store.entity.StoreReview;
import com.umc9th.bizscan.domain.store.repository.StoreReviewRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // DB 저장을 위해 필요

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewCrawlerService {

  private final StoreReviewRepository storeReviewRepository;

  @Transactional // DB 저장/조회가 일어나므로 트랜잭션 처리
  public List<String> getReviews(String placeId) {

    // 1. DB 확인
    if (storeReviewRepository.existsByPlaceId(placeId)) {
      log.info("DB에서 리뷰 조회 중... placeId: {}", placeId);
      return storeReviewRepository.findByPlaceId(placeId).stream()
          .map(StoreReview::getContent)
          .toList();
    }

    // 2. DB에 없으면? -> 크롤링 메소드 호출
    log.info("DB에 없음. 크롤링 시작... placeId: {}", placeId);
    List<String> crawledReviews = crawlNaverReviews(placeId);

    // 3. 크롤링 결과가 있으면 DB에 저장
    if (!crawledReviews.isEmpty()) {
      List<StoreReview> entities =
          crawledReviews.stream()
              .map(text -> StoreReview.builder().placeId(placeId).content(text).build())
              .toList();

      storeReviewRepository.saveAll(entities); // 한방에 저장
      log.info("DB 저장 완료! 개수: {}", entities.size());
    }

    return crawledReviews;
  }

  // [2]실제 크롤링만 수행
  public List<String> crawlNaverReviews(String placeId) {
    List<String> reviews = new ArrayList<>();

    io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
    ChromeOptions options = new ChromeOptions();
    // options.addArguments("--headless");
    options.addArguments("--remote-allow-origins=*");
    WebDriver driver = new ChromeDriver(options);

    try {
      String url = "https://m.place.naver.com/restaurant/" + placeId + "/review/visitor";
      driver.get(url);
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
      }

      List<WebElement> reviewElements =
          driver.findElements(By.cssSelector(".pui__vn15t2 > a:first-child"));

      if (reviewElements.isEmpty()) {
        reviewElements = driver.findElements(By.cssSelector("li .pui__vn15t2 a"));
      }

      for (WebElement element : reviewElements) {
        String text = element.getText();
        if (!text.isEmpty() && !text.equals("접기") && text.length() > 5) {
          reviews.add(text);
        }
        if (reviews.size() >= 10) break;
      }
    } catch (Exception e) {
      log.error("크롤링 중 에러 발생", e);
    } finally {
      driver.quit();
    }

    return reviews;
  }

  public String findPlaceId(String address, String storeName) {
    // 1. 드라이버 설정
    io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
    ChromeOptions options = new ChromeOptions();
    // options.addArguments("--headless");
    options.addArguments("--remote-allow-origins=*");

    WebDriver driver = new ChromeDriver(options);
    String placeId = "";

    try {
      // 2. 검색 수행
      String query = address + " " + storeName;
      String searchUrl = "https://m.place.naver.com/place/list?query=" + query;
      log.info("검색 요청: {}", searchUrl);

      driver.get(searchUrl);
      Thread.sleep(2000);

      String currentUrl = driver.getCurrentUrl();
      log.info("현재 URL: {}", currentUrl);

      // 3. ID 추출 로직
      if (!currentUrl.contains("list")
          && (currentUrl.contains("/place/") || currentUrl.contains("/restaurant/"))) {
        // Case A: 바로 상세 페이지로 넘어간 경우
        // 예: https://m.place.naver.com/restaurant/12345678/home
        String[] parts = currentUrl.split("naver.com/")[1].split("/");
        if (parts.length >= 2) {
          placeId = parts[1];
        }
      } else {
        // Case B: 리스트가 뜬 경우 (현재 로그 상황)
        // 화면 안에서 첫 번째 가게를 찾아서 ID 추출
        try {
          // 리스트의 첫 번째 가게 링크 찾기
          // 보통 ul > li > a 구조
          WebElement firstResult = driver.findElement(By.cssSelector("ul > li:first-child a"));
          String href = firstResult.getAttribute("href");

          log.info("리스트에서 찾은 링크: {}", href);

          if (href != null && (href.contains("/place/") || href.contains("/restaurant/"))) {
            String[] parts = href.split("place/|restaurant/");
            if (parts.length > 1) {
              placeId = parts[1].split("\\?")[0];
            }
          }
        } catch (Exception e) {
          log.warn("리스트에서 요소를 찾을 수 없습니다.");
        }
      }

      // 혹시라도 ID에 이상한 문자가 섞였으면 제거 (숫자만 남기기)
      if (!placeId.isEmpty() && !placeId.matches("[0-9]+")) {
        log.warn("ID 형식이 올바르지 않아 정제 시도: {}", placeId);
        placeId = placeId.replaceAll("[^0-9]", ""); // 숫자 이외 제거
      }

      log.info("최종 추출된 ID: {}", placeId);

    } catch (Exception e) {
      log.error("ID 찾기 중 에러 발생", e);
    } finally {
      driver.quit();
    }

    return placeId;
  }
}
