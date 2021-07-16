package com.venus.aoplog.exception;

import lombok.Getter;


@Getter
public class BusinessException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private String errCd;
    private String msg;
    private String[] msgArr;
    private String timestamp;

    public BusinessException(String errCd) {
        this.errCd = errCd;
    }
    public BusinessException(String errCd,String msg) {
        this.errCd = errCd;
        this.msg = msg;
    }

    public BusinessException(String errCd,String msg,String timestamp) {
        this.errCd = errCd;
        this.msg = msg;
        this.timestamp = timestamp;
    }

    public BusinessException(String errCd,final String... messageArgs) {
        this.errCd = errCd;
        this.msgArr = messageArgs;
    }

}
