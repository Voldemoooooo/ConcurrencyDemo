package com.ljl.xue.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author ljl
 */

@Controller
public class XPageController {
    @GetMapping("/index")
    private String index(){
        return "index";
    }
}
