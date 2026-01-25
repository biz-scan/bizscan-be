package com.umc9th.bizscan.domain.store.service;

import com.umc9th.bizscan.domain.store.entity.StoreCrawlingData;
import com.umc9th.bizscan.domain.store.repository.StoreCrawlingDataRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewCrawlerService {

  private final StoreCrawlingDataRepository storeCrawlingDataRepository;

  public StoreCrawlingData getStoreCrawlingData(String placeId, String storeName) {
    Optional<StoreCrawlingData> existingOpt = storeCrawlingDataRepository.findByPlaceId(placeId);

    if (existingOpt.isPresent()) {
      StoreCrawlingData existing = existingOpt.get();
      // 리뷰가 0개면 데이터 불완전으로 보고 재크롤링
      if (existing.getReviewCount() == 0) {
        log.info("⚠️ 데이터 불완전(리뷰 0개). 재크롤링... ID: {}", placeId);
        StoreCrawlingDataDto dto = crawlAllData(placeId);
        return updateExistingData(existing, dto);
      }
      log.info("✅ DB 데이터 사용 (리뷰 {}개, 별점 {})", existing.getReviewCount(), existing.getRating());
      return existing;
    }

    log.info("DB에 데이터 없음. 신규 크롤링... ID: {}", placeId);
    StoreCrawlingDataDto dto = crawlAllData(placeId);
    return saveCrawlingData(placeId, storeName, dto);
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
            .reviewContents(dto.getReviewContents())
            .build();
    return storeCrawlingDataRepository.save(newData);
  }

  @Transactional
  protected StoreCrawlingData updateExistingData(StoreCrawlingData data, StoreCrawlingDataDto dto) {
    // 별점과 내용도 함께 업데이트
    data.updateData(dto.getReviewCount(), dto.getRating(), dto.getReviewContents());
    return storeCrawlingDataRepository.save(data);
  }

  public String findPlaceId(String address, String storeName) {
    io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--remote-allow-origins=*");
    options.addArguments("--headless");
    options.addArguments("--disable-gpu");
    options.addArguments("--no-sandbox");
    options.addArguments(
        "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

    WebDriver driver = new ChromeDriver(options);
    String placeId = "";

    try {
      String[] addrParts = address.split(" ");
      String shortAddr = (addrParts.length >= 2) ? addrParts[0] + " " + addrParts[1] : address;
      String query = shortAddr + " " + storeName;

      String searchUrl = "https://m.place.naver.com/place/list?query=" + query;
      log.info("검색 URL: {}", searchUrl);

      driver.get(searchUrl);
      Thread.sleep(3000);

      String currentUrl = driver.getCurrentUrl();
      placeId = extractIdFromUrl(currentUrl);

      if (placeId.isEmpty()) {
        String pageSource = driver.getPageSource();
        Pattern p = Pattern.compile("[\"']id[\"']\\s*:\\s*[\"'](\\d{7,})[\"']");
        Matcher m = p.matcher(pageSource);
        if (m.find()) {
          placeId = m.group(1);
        } else {
          p = Pattern.compile("/place/(\\d{7,})");
          m = p.matcher(pageSource);
          if (m.find()) placeId = m.group(1);
        }
      }
      log.info("최종 추출 ID: {}", placeId);
    } catch (Exception e) {
      log.error("ID 찾기 에러", e);
    } finally {
      if (driver != null) driver.quit();
    }
    return placeId;
  }

  public StoreCrawlingDataDto crawlAllData(String placeId) {
    io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--remote-allow-origins=*");
    options.addArguments("--headless");
    options.addArguments(
        "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

    WebDriver driver = new ChromeDriver(options);
    int reviewCount = 0;
    double rating = 0.0;
    List<String> reviews = new ArrayList<>();

    try {
      // A. 홈 탭
      String homeUrl = "https://m.place.naver.com/restaurant/" + placeId + "/home";
      driver.get(homeUrl);
      Thread.sleep(2000);

      String bodyText = driver.findElement(By.tagName("body")).getText().replace("\n", " ");
      String pageSource = driver.getPageSource();

      // 전략 1: aria-label 속성 탐색 (접근성 태그에 별점이 숫자로 들어있는 경우가 많음)
      // 예: <span aria-label="별점 4.5"></span>
      try {
        WebElement ratingEl = driver.findElement(By.xpath("//*[contains(@aria-label, '별점')]"));
        String ariaText = ratingEl.getAttribute("aria-label"); // "별점 4.5"
        Pattern p = Pattern.compile("(\\d+(\\.\\d+)?)");
        Matcher m = p.matcher(ariaText);
        if (m.find()) {
          rating = Double.parseDouble(m.group(1));
        }
      } catch (Exception ignored) {
      }

      // 전략 2: JSON-LD 구조화 데이터 직접 파싱 (가장 정확함)
      // 네이버는 검색 엔진을 위해 스크립트 태그 안에 별점 정보를 숨겨둠
      if (rating == 0.0) {
        // "ratingValue":"4.5" 또는 "ratingValue":4.5 패턴 추출
        Pattern jsonLdPattern = Pattern.compile("\"ratingValue\"\\s*:\\s*\"?(\\d+(\\.\\d+)?)\"?");
        Matcher jsonLdMatcher = jsonLdPattern.matcher(pageSource);
        if (jsonLdMatcher.find()) {
          rating = Double.parseDouble(jsonLdMatcher.group(1));
        }
      }

      // 전략 3: 텍스트 기반 (기존 유지하되 패턴 확장)
      if (rating == 0.0) {
        Pattern textPattern = Pattern.compile("(별점|평점|점수)[^0-9]*(\\d\\.\\d{1,2})");
        Matcher textMatcher = textPattern.matcher(bodyText);
        if (textMatcher.find()) {
          rating = Double.parseDouble(textMatcher.group(2));
        }
      }

      // 리뷰수 추출
      Pattern reviewPattern = Pattern.compile("방문자\\s*리뷰\\s*([\\d,]+)");
      Matcher reviewMatcher = reviewPattern.matcher(bodyText);
      if (reviewMatcher.find()) {
        reviewCount = Integer.parseInt(reviewMatcher.group(1).replaceAll(",", ""));
      }

      log.info("상세 크롤링 결과 -> 별점: {}, 리뷰수: {}", rating, reviewCount);

      // B. 리뷰 탭 (내용 수집) - 기존과 동일
      String reviewUrl = "https://m.place.naver.com/restaurant/" + placeId + "/review/visitor";
      driver.get(reviewUrl);
      Thread.sleep(2000);

      JavascriptExecutor js = (JavascriptExecutor) driver;
      for (int i = 0; i < 2; i++) {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(1000);
      }

      try {
        List<WebElement> moreBtns = driver.findElements(By.cssSelector("a.fvwqf, a.TvfTp"));
        for (WebElement btn : moreBtns) {
          if (btn.isDisplayed()) {
            btn.click();
            Thread.sleep(500);
          }
        }
      } catch (Exception ignored) {
      }

      List<WebElement> reviewEls = driver.findElements(By.cssSelector("li div span"));
      if (reviewEls.isEmpty()) reviewEls = driver.findElements(By.xpath("//a[@role='button']"));

      for (WebElement el : reviewEls) {
        String text = el.getText().trim();
        // 필터링
        if (text.length() < 5) continue;
        if (text.contains("이 키워드를")) continue;
        if (text.contains("방문자 리뷰")) continue;
        if (text.contains("별점")) continue;
        if (text.contains("접기")) continue;
        if (text.matches("^[0-9]+$")) continue;

        if (!reviews.contains(text)) {
          reviews.add(text.replaceAll("[\r\n]+", " "));
        }
        if (reviews.size() >= 15) break;
      }

    } catch (Exception e) {
      log.error("크롤링 에러", e);
    } finally {
      if (driver != null) driver.quit();
    }

    return new StoreCrawlingDataDto(reviewCount, rating, String.join("|", reviews));
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
    private String reviewContents;
  }
}
