package com.venus.aoplog.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogAopPost {
    /**
     *   @PostMapping 설정된 메소드 또는 클래스 설정
     *   GetMapping 노테이션이 설정된 특정 클래스/메소드에만 AspectJ가 적용됨.
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void PostMapping(){ }

    /**
     * @param joinPoint
     */
    @Before("PostMapping()")
    public void before(JoinPoint joinPoint) {
        //log.info("=====================AspectJ POST  : Before Logging Start=====================");
        //log.info("=====================AspectJ POST  : Before Logging End=====================");
    }

    /**
     * @param joinPoint
     * @param result
     */
    @AfterReturning(pointcut = "PostMapping()", returning = "result")
    public void AfterReturning(JoinPoint joinPoint, Object result) {
        //log.info("=====================AspectJ POST  : AfterReturning Logging Start=====================");
        //log.info("=====================AspectJ POST  : AfterReturning Logging END=====================");
    }

    /**
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("PostMapping()")
    public Object Around(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("=====================AspectJ POST  : Around Logging Start=====================");
        try {
            Object result = joinPoint.proceed();
            log.info("=====================AspectJ POST  : Around Logging END=====================");
            return result;
        }catch (Exception e) {
            log.error("=====================AspectJ Around POST Exception=====================");
            log.error(e.toString());
            return null;
        }
    }
}
