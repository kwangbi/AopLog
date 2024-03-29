package com.venus.aoplog.api.controller;

import com.venus.aoplog.dto.testDTO;
import com.venus.aoplog.response.ResponseBase;
import com.venus.aoplog.api.service.MainService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/main")
@AllArgsConstructor
public class MainController {

    private final MainService service;

    @GetMapping("/test")
    public ResponseBase<Object> TestGet(@RequestParam String param1, @RequestParam String param2){
        return ResponseBase.of(service.MainGetService(param1,param2));
    }

    @PostMapping("/test")
    public ResponseBase<Object> TestPost(@RequestBody testDTO dto){

        return ResponseBase.of(service.MainPostService(dto));
    }


    @GetMapping("/test2")
    public ResponseBase<Object> TestGet2(){
        return ResponseBase.of(service.MainTest("111","fjdskfdjkfdskl"));
    }
}
