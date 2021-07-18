package com.venus.aoplog.response;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Data
public class ResponseBase<T> {
    protected String code = "00";
    protected String message = "SUCCESS";
    protected String timestamp;
    private int status = 200;
    protected T result;

    protected ResponseBase() {
        this.result = null;
    }

    protected ResponseBase(final T value) {
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String time = simpleDateFormat.format(new Date());

        this.timestamp = time;
        this.result = Objects.requireNonNull(value);
    }

    public static <T> ResponseBase<T> success() {
        return new ResponseBase<>();
    }

    public static <T> ResponseBase<T> of(final T result) {
        return new ResponseBase<>(result);
    }

    public static <T> ResponseBase<T> error(final int status,final String code,final String message,final String timestamp){
        ResponseBase<T> res = new ResponseBase<>();
        res.setCode(code);
        res.setTimestamp(timestamp);
        res.setMessage(message);
        res.setStatus(status);
        return res;
    }

}
