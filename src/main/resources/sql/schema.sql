SELECT * FROM tb_region_trend;

COMMIT;


CREATE TABLE `tb_region_master` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,

    /* 핵심 매핑 키 (Key) */
    `trdar_cd`      VARCHAR(50)     NOT NULL COMMENT '상권코드 (자식 연결용)',
    `gu_cd`         VARCHAR(10)     NULL     COMMENT '자치구코드 (S-DoT 연결용 - 예: 11200)',
    `adstrd_cd`     VARCHAR(50)     NULL     COMMENT '행정동코드 (배후지 연결용 - 예: 11200520)',

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


CREATE TABLE `tb_sdot_pop` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `region_id` BIGINT NOT NULL,
    `model_nm` VARCHAR(50) NULL,
    `serial_no` VARCHAR(50) NULL,
    `place_nm` VARCHAR(255) NULL,
    `measure_date` DATETIME NULL,
    `pop_count` BIGINT NULL,
    CONSTRAINT `FK_region_TO_sdot` FOREIGN KEY (`region_id`) REFERENCES `tb_region_master`(`id`)
);


CREATE TABLE `tb_sales_estimate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `region_id` BIGINT NOT NULL,
    `std_quarter` VARCHAR(10) NULL,
    `total_sale_cnt` BIGINT NULL,
    `age_10_count` BIGINT NULL,
    `age_20_count` BIGINT NULL,
    `age_30_count` BIGINT NULL,
    `age_40_count` BIGINT NULL,
    `age_50_count` BIGINT NULL,
    `age_60_count` BIGINT NULL,
    `male_count` BIGINT NULL,
    `female_count` BIGINT NULL,
    CONSTRAINT `FK_region_TO_sales` FOREIGN KEY (`region_id`) REFERENCES `tb_region_master`(`id`)
);


CREATE TABLE `tb_region_trend` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `region_id` BIGINT NOT NULL,
    `std_date` DATE NULL,
    `keyword` VARCHAR(255) NULL,
    `search_vol` BIGINT NULL,
    `rank` BIGINT NULL,
    CONSTRAINT `FK_region_TO_trend` FOREIGN KEY (`region_id`) REFERENCES `tb_region_master`(`id`)
);


CREATE TABLE `tb_competitor_store` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `region_id` BIGINT NOT NULL,
    `store_nm` VARCHAR(255) NULL,
    `branch_nm` VARCHAR(255) NULL,
    `adstrd_cd` VARCHAR(50) NULL,
    `category_lg` VARCHAR(50) NULL,
    `category_md` VARCHAR(50) NULL,
    `category_sm` VARCHAR(50) NULL,
    `address` VARCHAR(255) NULL,
    `lat` DECIMAL(10, 7) NULL,
    `lon` DECIMAL(10, 7) NULL,
    CONSTRAINT `FK_region_TO_competitor` FOREIGN KEY (`region_id`) REFERENCES `tb_region_master`(`id`)
);


CREATE TABLE `tb_competitor_review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `competitor_id` BIGINT NOT NULL,
    `reviewer_name` VARCHAR(255) NULL,
    `content` TEXT NULL,
    `rating` DECIMAL(2,1) NULL,
    `writing_date` DATE NULL,
    CONSTRAINT `FK_competitor_TO_review` FOREIGN KEY (`competitor_id`) REFERENCES `tb_competitor_store`(`id`)
);


CREATE TABLE `tb_income_stat` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `adstrd_cd` VARCHAR(50) NOT NULL,
    `adstrd_nm` VARCHAR(50) NULL,
    `std_date` VARCHAR(6) NULL,
    `avg_mon_income` BIGINT NULL,
    `income_sec_cd` BIGINT NULL,
    INDEX `idx_income_adstrd` (`adstrd_cd`)
);

DROP TABLE IF EXISTS `tb_housing_stat`;

CREATE TABLE `tb_housing_stat` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `zone_nm` VARCHAR(20) NOT NULL COMMENT '권역명 (예: 동북권, 동남권)',
    `std_date` VARCHAR(6) NULL COMMENT '기준년월',

    /* 비율 데이터 */
    `single_fam_ratio` DECIMAL(5,2) NULL,
    `multi_fam_ratio` DECIMAL(5,2) NULL,
    `multiplex_house_ratio` DECIMAL(5,2) NULL,
    `town_house_ratio` DECIMAL(5,2) NULL,
    `apt_ratio` DECIMAL(5,2) NULL,
    `officetel_ratio` DECIMAL(5,2) NULL,
    `studio_ratio` DECIMAL(5,2) NULL,

    /* 빠른 검색을 위해 인덱스 */
    INDEX `idx_housing_zone` (`zone_nm`)
);


CREATE TABLE `tb_analysis_summary` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `region_id` BIGINT NOT NULL,
    `std_date` DATE NULL,
    `main_age_group` VARCHAR(20) NULL,
    `main_gender` ENUM('M', 'F', 'MIX') NULL,
    `avg_daily_pop` BIGINT NULL,
    `peak_time` VARCHAR(50) NULL,
    `competitor_count` BIGINT NULL,
    `competition_level` ENUM('HIGH', 'MID', 'LOW') NULL,
    `avg_mon_income` BIGINT NULL,
    `housing_type` VARCHAR(50) NULL,
    `trend_keyword` TEXT NULL,
    CONSTRAINT `FK_region_TO_summary` FOREIGN KEY (`region_id`) REFERENCES `tb_region_master`(`id`)
);