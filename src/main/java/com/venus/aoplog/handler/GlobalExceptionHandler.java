package com.venus.aoplog.handler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.venus.aoplog.exception.BusinessException;
import com.venus.aoplog.response.ErrorResponse;
import com.venus.aoplog.response.ResponseBase;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNotFoundError(NoHandlerFoundException e,HttpServletRequest request){
        //Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String time = simpleDateFormat.format(new Date());



        final ErrorResponse response = ErrorResponse.of("COM001",HttpStatus.NOT_FOUND.value(),e.toString(), time);
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<Object> handleBusinessException(final BusinessException e,HttpServletRequest request){
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String timestamp = simpleDateFormat.format(new Date());
        String message = e.getMsg();

        return new ResponseEntity<>(ResponseBase.business(e.getErrCd(),message,timestamp),HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request){
        long start = System.currentTimeMillis();
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String time = simpleDateFormat.format(new Date());

        // 세션 출력
        HashMap<String,Object> sessionMap = new HashMap<>();
        Enumeration<String> sessionEnum = request.getSession().getAttributeNames();
        while(sessionEnum.hasMoreElements()){
            String sessionAttribute = sessionEnum.nextElement();
            //log.info("Request Session: {} {}",sessionAttribute,request.getSession().getAttribute(sessionAttribute));
            sessionMap.put(sessionAttribute,request.getSession().getAttribute(sessionAttribute));
        }


        // 헤더 출력
        HashMap<String, String> headerMap = new HashMap<>();
        Enumeration headerEnum = request.getHeaderNames();
        while(headerEnum.hasMoreElements()){
            String headerName = (String)headerEnum.nextElement();
            String headerValue = request.getHeader(headerName);
            //log.info("Request Header: {} {}",headerName,headerValue);
            headerMap.put(headerName,headerValue);
        }

        // 파라메터 출력
        HashMap<String, String> paramMap = new HashMap<>();
        Enumeration params = request.getParameterNames();
        while(params.hasMoreElements()){
            String paramName = (String)params.nextElement();
            //log.info("Request Param: {} {}",paramName,request.getParameter(paramName));
            paramMap.put(paramName,request.getParameter(paramName));
        }


        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("ERR_CD","COM001");
        errorMap.put("ERR_MSG",e.toString());
        errorMap.put("Request_Session",sessionMap);
        errorMap.put("Request_Header",headerMap);
        errorMap.put("Request_Param",paramMap);
        errorMap.put("Request_URI",request.getRequestURI());
        errorMap.put("Request_HttpMethod",request.getMethod());
        errorMap.put("Request_ServletPath",request.getServletPath());

        ObjectMapper mapper = new ObjectMapper();
        try{
            //String json = mapper.writeValueAsString(errorMap);
            log.error("SCM ERR : {} / {}",mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorMap),e);
        }catch(JsonProcessingException je){
            je.printStackTrace();
        }
        log.error("=====================================================");
        final ErrorResponse response = ErrorResponse.of("COM001",HttpStatus.INTERNAL_SERVER_ERROR.value(),e.toString(), time);
        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
