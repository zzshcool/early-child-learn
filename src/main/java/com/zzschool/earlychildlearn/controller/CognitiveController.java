package com.zzschool.earlychildlearn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cognitive")
public class CognitiveController {

    @GetMapping("/menu")
    public String menu() {
        return "cognitive/menu";
    }

    @GetMapping("/shape")
    public String shape() {
        return "cognitive/shape";
    }

    @GetMapping("/color")
    public String color() {
        return "cognitive/color";
    }

    @GetMapping("/size")
    public String size() {
        return "cognitive/size";
    }
}
