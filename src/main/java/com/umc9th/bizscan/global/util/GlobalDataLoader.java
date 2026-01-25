package com.umc9th.bizscan.global.util;

import com.opencsv.CSVReader;
import com.umc9th.bizscan.domain.commercial.entity.SalesEstimate;
import com.umc9th.bizscan.domain.commercial.repository.SalesRepository;
import com.umc9th.bizscan.domain.competitor.entity.CompetitorStore;
import com.umc9th.bizscan.domain.competitor.repository.CompetitorRepository;
import com.umc9th.bizscan.domain.hinterland.entity.HousingStat;
import com.umc9th.bizscan.domain.hinterland.entity.IncomeStat;
import com.umc9th.bizscan.domain.hinterland.repository.HousingRepository;
import com.umc9th.bizscan.domain.hinterland.repository.IncomeRepository;
import com.umc9th.bizscan.domain.population.entity.SdotPop;
import com.umc9th.bizscan.domain.population.repository.SdotRepository;
import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import com.umc9th.bizscan.domain.region.repository.RegionRepository;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.proj4j.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalDataLoader implements CommandLineRunner {

  private final RegionRepository regionRepository;
  private final SalesRepository salesRepository;
  private final CompetitorRepository competitorRepository;
  private final IncomeRepository incomeRepository;
  private final HousingRepository housingRepository;
  private final SdotRepository sdotRepository;

  // 서울시 공공데이터 좌표계 (보정된 중부원점)
  private static final String EPSG_5174_PARAM =
      "+proj=tmerc +lat_0=38 +lon_0=127.0028902777778 +k=1 +x_0=200000 +y_0=500000 +ellps=bessel +units=m +no_defs +towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43";

  // 구글/네이버 지도 좌표계 (위도/경도)
  private static final String WGS84_PARAM = "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs";

  @Override
  public void run(String... args) throws Exception {
    // 1. 데이터가 이미 존재하는지 체크 (가장 핵심인 RegionMaster 기준)
    long count = regionRepository.count();

    if (count > 0) {
      log.info("✅ DB에 데이터가 이미 존재합니다 (총 {}건). 로딩을 건너뜁니다.", count);
      return;
    }

    // 2. 데이터가 없을 경우에만 아래 로직 실행
    log.info("🚀 DB가 비어있습니다. 초기 데이터 적재를 시작합니다...");

    try {
      loadRegionMaster(); // 1. 상권 마스터
      loadSalesEstimate(); // 2. 매출
      loadCompetitors(); // 3. 경쟁업체
      loadIncomeStat(); // 4. 소득
      loadHousingStat(); // 5. 주거
      loadSdotPop(); // 6. 유동인구

      log.info("✅ 모든 초기 데이터 적재 완료!");
    } catch (Exception e) {
      log.error("❌ 데이터 적재 중 오류 발생: {}", e.getMessage());
      // 필요하다면 여기서 초기화 실패 시 DB를 비우는 로직을 추가할 수 있습니다.
    }
  }

  private void loadRegionMaster() {
    try {
      List<String[]> lines = readCsv("data/region_master.csv");
      List<RegionMaster> list = new ArrayList<>();

      // 좌표 변환기 생성
      CRSFactory factory = new CRSFactory();
      CoordinateReferenceSystem srcCrs = factory.createFromParameters("EPSG:5174", EPSG_5174_PARAM);
      CoordinateReferenceSystem destCrs = factory.createFromParameters("EPSG:4326", WGS84_PARAM);
      CoordinateTransform transform = new BasicCoordinateTransform(srcCrs, destCrs);

      for (String[] line : lines) {
        double tmX = parseDouble(line[4]);
        double tmY = parseDouble(line[5]);

        // 변환 수행
        ProjCoordinate srcCoord = new ProjCoordinate(tmX, tmY);
        ProjCoordinate destCoord = new ProjCoordinate();
        transform.transform(srcCoord, destCoord);

        list.add(
            RegionMaster.builder()
                .trdarCd(line[2])
                .trdarCdNm(line[3])
                .xCoord(tmX)
                .yCoord(tmY)
                .lat(destCoord.y) // 변환된 위도
                .lon(destCoord.x) // 변환된 경도
                .guNm(line[7])
                .adstrdCd(line[8])
                .adstrdNm(line[9])
                .build());
      }
      regionRepository.saveAll(list);
      log.info("RegionMaster {}건 저장 (좌표 변환 완료)", list.size());
    } catch (Exception e) {
      log.error("RegionMaster 로딩 실패", e);
    }
  }

  private void loadSalesEstimate() {
    try {
      List<String[]> lines = readCsv("data/sdot_age.csv");
      List<SalesEstimate> list = new ArrayList<>();
      for (String[] line : lines) {
        String trdarCd = line[3]; // 상권코드
        // DB에서 부모(상권) 찾기
        Optional<RegionMaster> regionOpt = regionRepository.findByTrdarCd(trdarCd);

        if (regionOpt.isPresent()) {
          list.add(
              SalesEstimate.builder()
                  .regionMaster(regionOpt.get())
                  .stdQuarter(line[0]) // 기준분기
                  .totalSaleCnt(parseLong(line[7]))
                  .maleCount(parseLong(line[23]))
                  .femaleCount(parseLong(line[24]))
                  .age10Count(parseLong(line[25]))
                  .age20Count(parseLong(line[26]))
                  .age30Count(parseLong(line[27]))
                  .age40Count(parseLong(line[28]))
                  .age50Count(parseLong(line[29]))
                  .age60Count(parseLong(line[30]))
                  .build());
        }
      }
      salesRepository.saveAll(list);
      log.info("SalesEstimate {}건 저장", list.size());
    } catch (Exception e) {
      log.error("SalesEstimate Error", e);
    }
  }

  private void loadCompetitors() {
    try {
      List<String[]> lines = readCsv("data/competition.csv");
      List<CompetitorStore> list = new ArrayList<>();
      for (String[] line : lines) {
        // RegionMaster 연결 없이 행정동코드(adstrdCd)만 저장 (유연성 확보)
        list.add(
            CompetitorStore.builder()
                .storeNm(line[1])
                .branchNm(line[2])
                .categoryLg(line[4])
                .categoryMd(line[6])
                .categorySm(line[8])
                .adstrdCd(line[15]) // 행정동코드
                .address(line[31])
                .lat(parseDouble(line[38]))
                .lon(parseDouble(line[37]))
                .build());
      }
      competitorRepository.saveAll(list);
      log.info("CompetitorStore {}건 저장", list.size());
    } catch (Exception e) {
      log.error("Competitor Error", e);
    }
  }

  private void loadSdotPop() {
    try {
      List<String[]> lines = readCsv("data/sdot_pop.csv");

      // 알려주신 포맷 적용: 2023-01-01 12:00:00
      DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

      List<SdotPop> list = new ArrayList<>();

      for (String[] line : lines) {
        // 데이터가 부족한 라인은 건너뛰기
        if (line.length < 8) continue;

        try {
          list.add(
              SdotPop.builder()
                  .measureDate(LocalDateTime.parse(line[2], fmt)) // index 2번 측정시간
                  .adstrdNm(line[6]) // index 6번 행정동명
                  .popCount(parseLong(line[7])) // index 7번 방문자수
                  .build());
        } catch (Exception e) {
          log.warn("파싱 실패 데이터: {} | 에러: {}", line[2], e.getMessage());
        }
      }
      sdotRepository.saveAll(list);
      log.info("✅ SdotPop {}건 저장 완료!", list.size());
    } catch (Exception e) {
      log.error("❌ SdotPop 전체 로딩 실패", e);
    }
  }

  // 4. 소득 통계 로딩 구현
  private void loadIncomeStat() {
    try {
      // income_stat.csv 파일 읽기
      List<String[]> lines = readCsv("data/income_stat.csv");
      List<IncomeStat> list = new ArrayList<>();

      for (String[] line : lines) {
        // CSV 구조: 0:기준년분기, 1:행정동코드, 2:행정동명, 3:월평균소득, 4:소득구간
        list.add(
            IncomeStat.builder()
                .stdDate(line[0])
                .adstrdCd(line[1])
                .adstrdNm(line[2])
                .avgMonIncome(parseLong(line[3]))
                .incomeDecile(line[4]) // 소득구간 (1~10분위)
                .build());
      }
      incomeRepository.saveAll(list);
      log.info("IncomeStat {}건 저장 완료", list.size());
    } catch (Exception e) {
      log.error("IncomeStat 로딩 실패", e);
    }
  }

  // 5. 주거 통계 로딩 구현
  private void loadHousingStat() {
    try {
      List<String[]> lines = readCsv("data/house_stat.csv");
      List<HousingStat> list = new ArrayList<>();

      for (String[] line : lines) {
        // "5권역별" 데이터만 추출
        if (line[0].equals("5권역별")) {

          // 비아파트 합산 (일반단독 + 다가구 + 다세대 + 연립)
          double single = parseDouble(line[3]);
          double multi = parseDouble(line[4]);
          double multiplex = parseDouble(line[5]);
          double town = parseDouble(line[6]);
          double nonApt = single + multi + multiplex + town;

          list.add(
              HousingStat.builder()
                  .zoneNm(line[1]) // 권역명
                  .stdDate("2020") // 기준년도
                  .singleFamRatio(single)
                  .multiFamRatio(multi)
                  .multiplexHouseRatio(multiplex)
                  .townHouseRatio(town)
                  .aptRatio(parseDouble(line[7]))
                  .nonAptRatio(nonApt) // 합산값
                  .officetelRatio(parseDouble(line[8]))
                  .studioRatio(parseDouble(line[9]))
                  .build());
        }
      }
      housingRepository.saveAll(list);
      log.info("HousingStat {}건 저장 완료", list.size());
    } catch (Exception e) {
      log.error("HousingStat 로딩 실패", e);
    }
  }

  // CSV 읽기 유틸 메소드 수정 (EUC-KR -> UTF-8)
  private List<String[]> readCsv(String path) throws Exception {
    ClassPathResource res = new ClassPathResource(path);
    CSVReader reader =
        new CSVReader(
            new InputStreamReader(res.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
    List<String[]> lines = reader.readAll();
    if (!lines.isEmpty()) lines.remove(0); // 헤더 제거
    return lines;
  }

  private Long parseLong(String v) {
    try {
      return Long.parseLong(v);
    } catch (Exception e) {
      return 0L;
    }
  }

  private Double parseDouble(String v) {
    try {
      return Double.parseDouble(v);
    } catch (Exception e) {
      return 0.0;
    }
  }
}
