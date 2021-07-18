package com.venus.aoplog.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.venus.aoplog.exception.BusinessException;
import com.venus.aoplog.handler.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class LogAopScm {

    private final GlobalExceptionHandler exceptionHandler;  // Exception

    public LogAopScm(GlobalExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    private Map<String,Object> apiMap = null;
    private Map<String,Object> ExceptionMap = null;


    /**
     *   @AopPointCut 설정된 메소드 또는 클래스 설정
     *   AopPointCut 노테이션이 설정된 특정 클래스/메소드에만 AspectJ가 적용됨.
     */
    @Pointcut("execution(* com.venus.aoplog..*Controller.*(..))")
    public void AopPointCut(){}

    /**
     * @param joinPoint
     */
    @Before("AopPointCut()")
    public void before(JoinPoint joinPoint) {}

    /**
     * @param joinPoint
     * @param result
     */
    @AfterReturning(pointcut = "AopPointCut()", returning = "result")
    public void AfterReturning(JoinPoint joinPoint, Object result) {}

    /**
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("AopPointCut()")
    public Object Around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Object result = null;
        long start = System.currentTimeMillis();
        ExceptionMap = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        try{
            result = joinPoint.proceed(joinPoint.getArgs());
        }catch (BusinessException be){
            ExceptionMap.put("message",be.getMsg());
            ExceptionMap.put("printStackTrace",be);
            return exceptionHandler.handleBusinessException(be,request);
        }catch (Exception e){
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            ExceptionMap.put("message",e.getMessage());
            ExceptionMap.put("printStackTrace",e);
            return exceptionHandler.handleException(e,request);
        }finally{
            Map<Object, Object> paramMap = null;
            Map<String, Object> AopMap = new HashMap<>();
            AopMap.put("Request_Session", getSession(request));
            AopMap.put("Request_Header", getHeader(request));
            if (request.getHeader("content-type") == null || request.getHeader("content-type").indexOf("json") == -1) {
                paramMap = getBodyParam(request);
            } else if (request.getHeader("content-type").indexOf("json") > -1) {  //json 요청
                paramMap = getBody(request);
            }
            AopMap.put("Request_Param", paramMap);
            AopMap.put("Request_URI", request.getRequestURI());
            AopMap.put("Request_HttpMethod", request.getMethod());
            AopMap.put("Request_ServletPath", request.getServletPath());
            AopMap.put("API_PARAM", apiMap);
            AopMap.put("Response", result);
            AopMap.put("Exception", ExceptionMap);

            long end = System.currentTimeMillis();

            ObjectMapper mapper = new ObjectMapper();
            try {
                log.info("SCM AOP : {} {} ({}ms) ", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(AopMap), status, (end - start));
            } catch (JsonProcessingException je) {
                je.printStackTrace();
            }

        }
        return result;
    }

    @Pointcut("@annotation(com.venus.aoplog.aop.ApiValidator)")
    public void accountValidator(){}

    @Before("accountValidator()")
    public void validateAccount(JoinPoint joinPoint){
        Object[] parameterValues = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String parameterName;
        apiMap = new HashMap<>();
        for (int i = 0; i < method.getParameters().length; i++) {
            parameterName = method.getParameters()[i].getName();
            apiMap.put(parameterName,parameterValues[i]);
        }
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
