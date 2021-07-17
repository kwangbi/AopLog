package com.venus.aoplog.service;

import com.venus.aoplog.dto.testDTO;
import com.venus.aoplog.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MainService {

    public String MainGetService(final String param1,final String param2){

        if(1==1){
            //throw new BusinessException("BE001","error message");
        }

        return "Get";
    }


    public String MainPostService(final testDTO dto){

        if(1==1){
            //throw new BusinessException("BE002","POST BusinessException");
        }

        log.info("dto : {}",dto.toString());

        return "post";
    }

}
