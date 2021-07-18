package com.venus.aoplog.api.service;

import com.venus.aoplog.aop.ApiValidator;
import com.venus.aoplog.dto.testDTO;
import com.venus.aoplog.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MainService {

    public String MainGetService(final String param1,final String param2){

        if(1==1){
            throw new BusinessException("BE001","error message");
        }

        return "Get";
    }


    @ApiValidator
    public String MainPostService(final testDTO dto){
        //log.info("dto : {}",dto.toString());

        if(1==1){
            //throw new BusinessException("BE002","POST BusinessException");
            throw new IllegalArgumentException("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
        }


        return "post";
    }


    @ApiValidator
    public String MainTest(final String id,final String token){
        return "ok";
    }

}
