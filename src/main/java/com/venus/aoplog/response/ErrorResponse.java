package com.venus.aoplog.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
    private String timestamp;
    private String code;
    private int status;
    private String message;

    private ErrorResponse(final String code,final int status,final String message,final String time){
        this.timestamp = time;
        this.code = code;
        this.status = status;
        this.message = message;
    }


    public static ErrorResponse of(final String code,final int status,final String message,final String time){
        return new ErrorResponse(code,status,message,time);
    }


}
