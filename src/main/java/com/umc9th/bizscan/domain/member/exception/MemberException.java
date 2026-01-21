package com.umc9th.bizscan.domain.member.exception;


import com.umc9th.bizscan.global.apiPayload.code.BaseErrorCode;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;

public class MemberException extends GeneralException {
    public MemberException(BaseErrorCode code) {
        super(code);
    }
}
