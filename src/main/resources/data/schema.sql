// 지역 마스터
create TABLE `tb_region_master` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,

    /* 핵심 매핑 키 (Key) */
    `trdar_cd`      VARCHAR(50)     NULL COMMENT '상권코드 (자식 연결용)',
    `gu_cd`         VARCHAR(10)     NULL     COMMENT '자치구코드 (S-DoT 연결용 - 예: 11200)',
    `adstrd_cd`     VARCHAR(50)     NULL     COMMENT '행정동코드 (배후지 연결용 - 예: 11200520)',

    `full_address`  VARCHAR(255)    NULL,

    /* 텍스트 정보 */
    `trdar_cd_nm`   VARCHAR(255)    NULL     COMMENT '상권명',
    `trdar_se_cd`   VARCHAR(50)     NULL,
    `trdar_se_cd_nm` VARCHAR(50)    NULL,
    `gu_nm`         VARCHAR(50)     NULL     COMMENT '자치구명 (예: 성동구 -> 권역 매핑용)',
    `adstrd_nm`     VARCHAR(50)     NULL,

    /* 좌표 정보 */
    `x_coord`       INT             NULL,
    `y_coord`       INT             NULL,
    `lat`           DECIMAL(10,7)   NULL,
    `lon`           DECIMAL(10,7)   NULL,
    `area_size`     DECIMAL(15,2)   NULL
);


// 유동인구 수
create TABLE `tb_sdot_pop` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `region_id`    BIGINT          NULL,
    `adstrd_nm` VARCHAR(100) NULL,
    `model_nm` VARCHAR(50) NULL,
    `serial_no` VARCHAR(50) NULL,
    `place_nm` VARCHAR(255) NULL,
    `measure_date` DATETIME NULL,
    `pop_count` BIGINT NULL,
    CONSTRAINT `FK_region_TO_sdot` FOREIGN KEY (`region_id`) REFERENCES `tb_region_master`(`id`)
);



// 유동인구 연령대, 성별, 시간대 매출건수
create TABLE `tb_sales_estimate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `region_id` BIGINT NOT NULL,

    `trdar_cd` VARCHAR(20) NULL,      -- 상권 코드
    `trdar_cd_nm` VARCHAR(100) NULL,  -- 상권 코드 명
    `service_cd` VARCHAR(20) NULL,    -- 서비스 업종 코드
    `service_nm` VARCHAR(100) NULL,   -- 서비스 업종 코드 명
    `std_quarter` VARCHAR(10) NULL,

    `total_sale_cnt` BIGINT NULL DEFAULT 0,

    -- [성별]
    `male_count` BIGINT NULL,
    `female_count` BIGINT NULL,

    -- [연령대]
    `age_10_count` BIGINT NULL,
    `age_20_count` BIGINT NULL,
    `age_30_count` BIGINT NULL,
    `age_40_count` BIGINT NULL,
    `age_50_count` BIGINT NULL,
    `age_60_count` BIGINT NULL,

    -- 시간대별 매출 건수
    `tmzon_00_06_sale_cnt` BIGINT NULL,
    `tmzon_06_11_sale_cnt` BIGINT NULL,
    `tmzon_11_14_sale_cnt` BIGINT NULL,
    `tmzon_14_17_sale_cnt` BIGINT NULL,
    `tmzon_17_21_sale_cnt` BIGINT NULL,
    `tmzon_21_24_sale_cnt` BIGINT NULL,

    CONSTRAINT `FK_region_TO_sales` FOREIGN KEY (`region_id`) REFERENCES `tb_region_master`(`id`)
);



//경쟁업체
create TABLE `tb_competitor_store` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `store_uid` VARCHAR(50) NULL COMMENT '상가업소번호',
  `region_id` BIGINT NULL COMMENT '지역 마스터 ID (FK)',

  `store_nm` VARCHAR(100) NULL COMMENT '상호명',
  `branch_nm` VARCHAR(100) NULL COMMENT '지점명',
  `adstrd_cd` VARCHAR(20) NULL COMMENT '행정동코드',

  `category_lg` VARCHAR(50) NULL COMMENT '업종대분류명',
  `category_md` VARCHAR(50) NULL COMMENT '업종중분류명',
  `category_sm` VARCHAR(50) NULL COMMENT '업종소분류명',
  `address` VARCHAR(200) NULL COMMENT '도로명주소',

  `lat` DOUBLE NULL COMMENT '위도',
  `lon` DOUBLE NULL COMMENT '경도',

  -- 인덱스 (검색 속도 향상용)
  UNIQUE INDEX `uk_store_uid` (`store_uid`),  -- 중복 방지
  INDEX `idx_comp_loc` (`lat`, `lon`),        -- 거리 계산용
  INDEX `idx_comp_cat_sm` (`category_sm`)     -- 업종 검색용
);

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `tb_competitor_review`;
DROP TABLE IF EXISTS `tb_competitor_store`;
SET FOREIGN_KEY_CHECKS = 1;





// 소득 테이블
create TABLE `tb_income_stat` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `adstrd_cd` VARCHAR(20) NULL COMMENT '행정동 코드',
    `adstrd_nm` VARCHAR(50) NULL COMMENT '행정동 명',
    `std_date` VARCHAR(20) NULL COMMENT '기준 년분기',
    `avg_mon_income` BIGINT NULL COMMENT '월 평균 소득',
    `income_decile` VARCHAR(10) NULL COMMENT '소득구간',

    -- 검색 속도 향상 (행정동 명으로 조회할 것임)
    INDEX `idx_income_dong` (`adstrd_nm`),
    INDEX `idx_income_date` (`std_date`)
);


create TABLE `tb_housing_stat` (
    `id`                    BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `zone_nm`               VARCHAR(20)     NOT NULL COMMENT '권역명 (예: 동북권)',
    `std_date`              VARCHAR(6)      NULL COMMENT '기준년월 (예: 2020)',

    /* 주거 형태 비율 (%) */
    `single_fam_ratio`      DECIMAL(5,2)    NULL COMMENT '일반단독주택',
    `multi_fam_ratio`       DECIMAL(5,2)    NULL COMMENT '다가구용 단독주택',
    `multiplex_house_ratio` DECIMAL(5,2)    NULL COMMENT '다세대주택',
    `town_house_ratio`      DECIMAL(5,2)    NULL COMMENT '연립주택',
    `apt_ratio`             DECIMAL(5,2)    NULL COMMENT '아파트',
    `officetel_ratio`       DECIMAL(5,2)    NULL COMMENT '오피스텔',
    `studio_ratio`          DECIMAL(5,2)    NULL COMMENT '원룸',
    `non_apt_ratio`         DECIMAL(5,2)    NULL COMMENT '주택이 아닌 건물',

    INDEX `idx_housing_zone` (`zone_nm`)
);


create TABLE `tb_analysis_summary` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `region_id`         BIGINT          NOT NULL,
    `std_date`          DATE            NULL,

    /* 기회 */
    `main_age_group`    VARCHAR(20)     NULL,
    `main_gender`       VARCHAR(10)     NULL,
    `avg_daily_pop`     BIGINT          NULL,
    `peak_time`         VARCHAR(50)     NULL,

    /* 위협 */
    `competitor_count`  BIGINT          NULL,
    `competition_level` VARCHAR(10)     NULL,

    /* 강점 */
    `avg_mon_income`    BIGINT          NULL,

    /* 트렌드 */
    `housing_type`      VARCHAR(50)     NULL,
    `topHashtags`     TEXT            NULL,

    `my_review_count`       INT         NULL,
    `my_rating`             DOUBLE      NULL,
    `avg_comp_review_count` DOUBLE      NULL,
    `my_review_contents`    TEXT        NULL,

    `created_at`        DATETIME        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT `FK_region_TO_summary` FOREIGN KEY (`region_id`) REFERENCES `tb_region_master`(`id`)
);



-- 네이버 리뷰 크롤링
create TABLE `tb_store_crawling_data` (
    `id`                    BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,

    /* 가게 식별 및 정보 */
    `place_id`              VARCHAR(50)     NOT NULL COMMENT '네이버 플레이스 ID',
    `store_name`            VARCHAR(100)    NULL     COMMENT '가게명',

    /* 분석 데이터 */
    `review_count`          INT             DEFAULT 0 COMMENT '방문자 리뷰 수',
    `rating`                DOUBLE          DEFAULT 0.0 COMMENT '네이버 별점',
    /* 텍스트 데이터 (W 분석 핵심) */
    `review_contents`       TEXT            NULL     COMMENT '리뷰 텍스트 모음',

    /* 관리용 */
    `created_at`            DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `updated_at`            DATETIME        DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP,

    UNIQUE INDEX `uk_place_id` (`place_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



/*임시 테이블 */


//주거 형태 임시 테이블
create TABLE `temp_housing_stat` (
    `특성별(1)`           VARCHAR(50),
    `특성별(2)`           VARCHAR(50),
    `사례수 (명)`         VARCHAR(50),
    `일반단독주택 (%)`    DECIMAL(5,2),
    `다가구용 단독주택 (%)` DECIMAL(5,2),
    `다세대주택 (%)`      DECIMAL(5,2),
    `연립주택 (%)`        DECIMAL(5,2),
    `아파트 (%)`          DECIMAL(5,2),
    `오피스텔 (%)`        DECIMAL(5,2),
    `원룸 (%)`            DECIMAL(5,2),
    `주택이 아닌 건물 (%)` DECIMAL(5,2),
    `기타 (%)`            DECIMAL(5,2)
);

insert into tb_housing_stat (
    zone_nm,
    std_date,
    single_fam_ratio,
    multi_fam_ratio,
    multiplex_house_ratio,
    town_house_ratio,
    apt_ratio,
    officetel_ratio,
    studio_ratio,
    non_apt_ratio
)
select
    t.`특성별(2)`,           -- 권역명 (도심권, 동북권 등)
    '2020',                 -- 기준년도 (파일에 없으므로 직접 입력)
    t.`일반단독주택 (%)`,
    t.`다가구용 단독주택 (%)`,
    t.`다세대주택 (%)`,
    t.`연립주택 (%)`,
    t.`아파트 (%)`,
    t.`오피스텔 (%)`,
    t.`원룸 (%)`,
    t.`주택이 아닌 건물 (%)`
from temp_housing_stat t
where t.`특성별(1)` = '5권역별';  -- 권역별 데이터만 추출




// 유동인구 수 임시 테이블
create TABLE `temp_sdot_pop` (
    `모델번호`      VARCHAR(50),
    `시리얼`        VARCHAR(50),
    `측정시간`      VARCHAR(50),  -- 예: 2025-11-30_23:56:00 (형식 변환 필요)
    `지역`          VARCHAR(50),
    `자치구코드`    VARCHAR(20),
    `자치구`        VARCHAR(20),
    `행정동`        VARCHAR(50),  -- 예: 성수2가3동 (매핑 핵심)
    `방문자수`      INT,
    `등록일`        VARCHAR(50)
);

insert into tb_sdot_pop (
    model_nm,
    serial_no,
    measure_date,
    place_nm,
    adstrd_nm,
    pop_count,
    region_id
)
select
    t.모델번호,
    t.시리얼,
    -- 날짜 변환 (YYYY-MM-DD_HH:MM:SS -> DATETIME)
    str_to_date(t.측정시간, '%Y-%m-%d_%H:%i:%s'),
    t.지역,
    t.행정동,
    t.방문자수,
    -- 행정동 이름으로 region_id 찾기 (매핑 안 되면 NULL)
    (SELECT id from tb_region_master where adstrd_nm = t.행정동 limit 1)
FROM temp_sdot_pop t;



// 소득 - 임시테이블
create TABLE `temp_income_stat` (
    `기준_년분기_코드` VARCHAR(20),
    `행정동_코드` VARCHAR(20),
    `행정동_코드_명` VARCHAR(50),
    `월_평균_소득_금액` VARCHAR(50),
    `소득_구간_코드` VARCHAR(10)
);

// 소득 - 임시에서 실제 db로 옮김
insert into tb_income_stat (
    std_date,
    adstrd_cd,
    adstrd_nm,
    avg_mon_income,
    income_decile
)
select
    `기준_년분기_코드`,
    `행정동_코드`,
    `행정동_코드_명`,
    CAST(`월_평균_소득_금액` as UNSIGNED),
    `소득_구간_코드`
FROM temp_income_stat;





//유동인구 성별, 연령, 시간대 담을 임시 테이블
create TABLE `temp_sales` (
    `기준_년분기_코드` VARCHAR(10),
    `상권_구분_코드` VARCHAR(10),
    `상권_구분_코드_명` VARCHAR(50),
    `상권_코드` VARCHAR(20),
    `상권_코드_명` VARCHAR(50),
    `서비스_업종_코드` VARCHAR(20),
    `서비스_업종_코드_명` VARCHAR(50),
    `당월_매출_건수` BIGINT,
    `주중_매출_건수` BIGINT,
    `주말_매출_건수` BIGINT,
    `월요일_매출_건수` BIGINT,
    `화요일_매출_건수` BIGINT,
    `수요일_매출_건수` BIGINT,
    `목요일_매출_건수` BIGINT,
    `금요일_매출_건수` BIGINT,
    `토요일_매출_건수` BIGINT,
    `일요일_매출_건수` BIGINT,
    `시간대_건수~06_매출_건수` BIGINT,
    `시간대_건수~11_매출_건수` BIGINT,
    `시간대_건수~14_매출_건수` BIGINT,
    `시간대_건수~17_매출_건수` BIGINT,
    `시간대_건수~21_매출_건수` BIGINT,
    `시간대_건수~24_매출_건수` BIGINT,
    `남성_매출_건수` BIGINT,
    `여성_매출_건수` BIGINT,
    `연령대_10_매출_건수` BIGINT,
    `연령대_20_매출_건수` BIGINT,
    `연령대_30_매출_건수` BIGINT,
    `연령대_40_매출_건수` BIGINT,
    `연령대_50_매출_건수` BIGINT,
    `연령대_60_이상_매출_건수` BIGINT
);

insert into tb_sales_estimate (
    region_id,
    trdar_cd, trdar_cd_nm,
    service_cd, service_nm,
    std_quarter,
    total_sale_cnt,

    -- 성별/연령
    male_count, female_count,
    age_10_count, age_20_count, age_30_count,
    age_40_count, age_50_count, age_60_count,

    -- ✨ [시간대 매핑 수정]
    tmzon_00_06_sale_cnt,
    tmzon_06_11_sale_cnt,
    tmzon_11_14_sale_cnt,
    tmzon_14_17_sale_cnt,
    tmzon_17_21_sale_cnt,
    tmzon_21_24_sale_cnt
)
select
    r.id,
    t.`상권_코드`,
    t.`상권_코드_명`,
    t.`서비스_업종_코드`,
    t.`서비스_업종_코드_명`,
    t.`기준_년분기_코드`,
    t.`당월_매출_건수`,

    -- 성별/연령
    t.`남성_매출_건수`,
    t.`여성_매출_건수`,
    t.`연령대_10_매출_건수`,
    t.`연령대_20_매출_건수`,
    t.`연령대_30_매출_건수`,
    t.`연령대_40_매출_건수`,
    t.`연령대_50_매출_건수`,
    t.`연령대_60_이상_매출_건수`,

    -- ✨ [시간대 컬럼명 수정 적용]
    t.`시간대_건수~06_매출_건수`,  -- 00~06시
    t.`시간대_건수~11_매출_건수`,  -- 06~11시
    t.`시간대_건수~14_매출_건수`,  -- 11~14시
    t.`시간대_건수~17_매출_건수`,  -- 14~17시
    t.`시간대_건수~21_매출_건수`,  -- 17~21시
    t.`시간대_건수~24_매출_건수`   -- 21~24시
from temp_sales t
join tb_region_master r on t.`상권_코드` = r.trdar_cd;




// 경쟁업체 데이터 담을 임시 테이블
create TABLE `temp_store_info` (
    `상가업소번호` VARCHAR(100),
    `상호명` TEXT,
    `지점명` VARCHAR(100),
    `업종대분류코드` VARCHAR(20),
    `업종대분류명` VARCHAR(100),
    `업종중분류코드` VARCHAR(20),
    `업종중분류명` VARCHAR(100),
    `업종소분류코드` VARCHAR(20),
    `업종소분류명` VARCHAR(100), -- ✨ 핵심 (핸드폰 소매업 등)
    `표준산업분류코드` VARCHAR(20),
    `표준산업분류명` VARCHAR(100),
    `시도코드` VARCHAR(20),
    `시도명` VARCHAR(50),
    `시군구코드` VARCHAR(20),
    `시군구명` VARCHAR(50),
    `행정동코드` VARCHAR(20),
    `행정동명` VARCHAR(50),
    `법정동코드` VARCHAR(20),
    `법정동명` VARCHAR(50),
    `PNU코드` VARCHAR(50),
    `대지구분코드` VARCHAR(10),
    `대지구분명` VARCHAR(20),
    `번지본번호` VARCHAR(10),
    `번지부번호` VARCHAR(10),
    `지번주소` TEXT,
    `도로명코드` VARCHAR(20),
    `도로명` VARCHAR(100),
    `건물번호` VARCHAR(10),
    `건물부번호` VARCHAR(10),
    `건물관리번호` VARCHAR(50),
    `건물명` VARCHAR(100),
    `도로명주소` TEXT,            -- ✨ 주소 정보
    `과거우편번호` VARCHAR(10),
    `신규우편번호` VARCHAR(10),
    `동정보` VARCHAR(50),
    `층정보` VARCHAR(50),
    `호정보` VARCHAR(50),
    `경도` VARCHAR(50),           -- ✨ X좌표
    `위도` VARCHAR(50)            -- ✨ Y좌표
);



insert into tb_competitor_store (
    store_uid, store_nm, branch_nm, adstrd_cd,
    category_lg, category_md, category_sm,
    address, lon, lat
)
select
    `상가업소번호`, `상호명`, `지점명`, `행정동코드`,
    `업종대분류명`, `업종중분류명`, `업종소분류명`,
    `도로명주소`,
    CAST(`경도` as DOUBLE), CAST(`위도` AS DOUBLE)
FROM temp_store_info
WHERE `경도` != '' AND `위도` != '';