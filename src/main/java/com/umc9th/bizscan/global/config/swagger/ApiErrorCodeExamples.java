package com.umc9th.bizscan.global.config.swagger;

import com.umc9th.bizscan.domain.analysis.exception.SwotAnalysisErrorCode;
import com.umc9th.bizscan.domain.store.exception.StoreErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.security.exception.SecurityErrorStatus;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExamples {

  // ErrorCode
  ErrorCode[] value() default {};

  // CustomErrorCode: store={} 명시 필요
  StoreErrorCode[] store() default {};

  SwotAnalysisErrorCode[] dataAnalysis() default {};

  SecurityErrorStatus[] security() default {};
}
