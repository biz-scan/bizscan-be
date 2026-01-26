package com.umc9th.bizscan.global.common.consts;

public class StaticVariable {
  public static final String SWAGGER_JWT = "JWT";
  public static final String SWAGGER_BEARER = "Bearer";
  public static final String BEARER = "Bearer ";
  public static final String AUTHORIZATION = "Authorization";
  public static final String GRANT_TYPE = "authorization_code";
  public static final String REISSUE_ENDPOINT = "/api/v1/tokens/reissue";
  public static final String HEALTH_CHECK_ENDPOINT = "/api/v1/test/health-check";
  public static final String CREATED_DATE = "createdDate";
  public static final String LAST_MODIFIED_DATE = "lastModifiedDate";
  public static final String ADVICE_ID = "id";
  public static final String NOTIFICATION_READ = "isRead";
  public static final String PAGINATION_SORTING_BY_ID = "id";

  // JWT
  // TODO 변경하기 (알림 때문에 데모데이에서 이렇게 사용)
  public static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; // 1일
  public static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7; // 7일
}
