package com.zzschool.earlychildlearn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bopomofo")
public class BopomofoController {

    @GetMapping("/learn")
    public String learn() {
        return "bopomofo/learn";
    }

    @GetMapping("/game")
    public String game() {
        return "bopomofo/game";
    }

    @GetMapping("/stroke")
    public String stroke() {
        return "bopomofo/stroke";
    }
}
