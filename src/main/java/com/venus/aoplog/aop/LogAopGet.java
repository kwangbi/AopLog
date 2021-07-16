package com.venus.aoplog.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class LogAopGet {
    /**
     *   @GetMapping 설정된 메소드 또는 클래스 설정
     *   GetMapping 노테이션이 설정된 특정 클래스/메소드에만 AspectJ가 적용됨.
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void GetMapping(){ }

    /**
     * @param joinPoint
     */
    @Before("GetMapping()")
    public void before(JoinPoint joinPoint) {
    }

    /**
     * @param joinPoint
     * @param result
     */
    @AfterReturning(pointcut = "GetMapping()", returning = "result")
    public void AfterReturning(JoinPoint joinPoint, Object result) {
    }

    /**
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("GetMapping()")
    public Object Around(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("=====================AspectJ GET  : Around Logging Start=====================");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.info("=====================AspectJ GET  : Around Logging END=====================");
            HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getResponse();


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
                //log.info("Request Param: {}",request.getParameter(paramName));
                paramMap.put(paramName,request.getParameter(paramName));
            }

            Map<String, Object> AopMap = new HashMap<>();
            AopMap.put("Request_Session",sessionMap);
            AopMap.put("Request_Header",headerMap);
            AopMap.put("Request_Param",paramMap);
            AopMap.put("Request_URI",request.getRequestURI());
            AopMap.put("Request_HttpMethod",request.getMethod());
            AopMap.put("Request_ServletPath",request.getServletPath());

            long end = System.currentTimeMillis();

            ObjectMapper mapper = new ObjectMapper();
            try{
                //String json = mapper.writeValueAsString(errorMap);
                log.info("SCM AOP : {} ({}ms)",mapper.writerWithDefaultPrettyPrinter().writeValueAsString(AopMap),HttpStatus.OK, (end - start));
            }catch(JsonProcessingException je){
                je.printStackTrace();
            }

            return result;
        }catch (Exception e) {
            log.error("=====================AspectJ Around GET Exception=====================");
            log.error(e.toString());
            throw e;
        }finally {

        }
    }

}
