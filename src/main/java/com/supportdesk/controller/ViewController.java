package com.supportdesk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/", "/dashboard"})
    public String index() {
        return "forward:/index.html";
    }
}
