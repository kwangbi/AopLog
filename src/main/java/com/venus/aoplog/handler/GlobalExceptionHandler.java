package com.venus.aoplog.handler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.venus.aoplog.exception.BusinessException;
import com.venus.aoplog.response.ResponseBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 오류 처리
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseBase<Object> handleNotFoundError(NoHandlerFoundException e,HttpServletRequest request){
        String errCd = "COM002";
        String timestamp = ExceptionLogPrint(e,request,errCd);
        return ResponseBase.error(HttpStatus.NOT_FOUND.value(),errCd,e.getMessage(),timestamp);
    }

    /**
     * 업무 오류 처리
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseBase<Object> handleBusinessException(final BusinessException e,HttpServletRequest request){
        String errCd = "BE0001";
        String message = e.getMsg();
        //String timestamp = ExceptionLogPrint(e,request,errCd);
        String timestamp = getTimestamp();
        return ResponseBase.error(HttpStatus.OK.value(),errCd,message,timestamp);
    }


    /**
     * 공통 오류 처리
     * @param e
     * @param request
     * @return
     */

    @ExceptionHandler(Exception.class)
    public ResponseBase<Object> handleException(Exception e, HttpServletRequest request){
        String errCd = "COM001";
        String timestamp = getTimestamp();
        return ResponseBase.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),errCd,e.getMessage(),timestamp);
    }


    /**
     * 공통 로그 출력
     * @param e
     * @param request
     * @return
     */
    private static String ExceptionLogPrint(Exception e,HttpServletRequest request,String errCd){
        String timestamp = getTimestamp();

        Map<Object, Object> paramMap = null;

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("ERR_CD",errCd);
        errorMap.put("ERR_MSG",e.toString());
        errorMap.put("Request_Session",getSession(request));
        errorMap.put("Request_Header",getHeader(request));

        if(request.getHeader("content-type") == null || request.getHeader("content-type").indexOf("json") == -1 ){
            paramMap = getBodyParam(request);
        } else if(request.getHeader("content-type").indexOf("json") > -1){  //json 요청
            paramMap = getBody(request);
        }

        errorMap.put("Request_Param",paramMap);
        errorMap.put("Request_URI",request.getRequestURI());
        errorMap.put("Request_HttpMethod",request.getMethod());
        errorMap.put("Request_ServletPath",request.getServletPath());
        errorMap.put("timestamp",timestamp);

        ObjectMapper mapper = new ObjectMapper();
        try{
            //String json = mapper.writeValueAsString(errorMap);
            log.error("SCM ERR : {} / {}",mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorMap),e);
        }catch(JsonProcessingException je){
            je.printStackTrace();
        }finally {
            return timestamp;
        }

    }

    /**
     * timestamp 가져오기
     * @param
     * @param
     * @return
     */
    private static String getTimestamp(){
        final String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String timestamp = simpleDateFormat.format(new Date());

        return timestamp;
    }

    /**
     * Get Session param
     * @param request
     * @return
     */
    private static Map<Object, Object> getSession(HttpServletRequest request){
        Map<Object, Object> sessionMap = new HashMap<>();
        Enumeration<String> sessionEnum = request.getSession().getAttributeNames();
        while(sessionEnum.hasMoreElements()){
            String sessionAttribute = sessionEnum.nextElement();
            //log.info("Request Session: {} {}",sessionAttribute,request.getSession().getAttribute(sessionAttribute));
            sessionMap.put(sessionAttribute,request.getSession().getAttribute(sessionAttribute));
        }

        return sessionMap;
    }

    /**
     * Get Header param
     * @param request
     * @return
     */
    private static Map<Object, Object> getHeader(HttpServletRequest request){
        Map<Object, Object> headerMap = new HashMap<>();
        Enumeration headerEnum = request.getHeaderNames();
        while(headerEnum.hasMoreElements()){
            String headerName = (String)headerEnum.nextElement();
            String headerValue = request.getHeader(headerName);
            //log.info("Request Header: {} {}",headerName,headerValue);
            headerMap.put(headerName,headerValue);
        }

        return headerMap;
    }


    /**
     * request param type
     * @param request
     * @return
     */
    private static Map<Object, Object> getBodyParam(HttpServletRequest request){
        Map<Object, Object> _param = new HashMap<>();
        request.getParameterMap().forEach((key, value)->{
            if(value != null && value.length > 1) {
                _param.put(key, value);
            } else {
                _param.put(key, value[0]);
            }
        });
        return _param;
    }

    /**
     * request body(json) type
     * @param request
     * @return
     */
    private static Map<Object, Object> getBody(HttpServletRequest request) {
        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();  //스트림에서 데이터를 전부 읽습니다.
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (bufferedReader != null) { try { bufferedReader.close(); } catch (IOException ex) { ex.printStackTrace(); } }
        }
        body = stringBuilder.toString();
        try {
            org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
            org.json.simple.JSONObject jsonObj = (org.json.simple.JSONObject) parser.parse(body);
            return jsonObj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }




}
