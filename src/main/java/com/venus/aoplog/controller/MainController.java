package com.venus.aoplog.controller;

import com.venus.aoplog.response.ResponseBase;
import com.venus.aoplog.service.MainService;
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
    public String TestPost(){
        return "post";
    }

}
