package com.venus.aoplog.service;

import com.venus.aoplog.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MainService {

    public String MainGetService(final String param1,final String param2){

        if(1==1){
            log.info("11111111111111111111111111111");
            //throw new BusinessException("COM002","error message");
        }

        return "Get";
    }

}
