package com.zzschool.earlychildlearn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/numbers")
public class NumberController {

    @GetMapping("/learn")
    public String learn() {
        return "numbers/learn";
    }

    @GetMapping("/game")
    public String game() {
        return "numbers/game";
    }

    @GetMapping("/counting-game")
    public String countingGame() {
        return "numbers/counting_game";
    }

    @GetMapping("/stroke")
    public String stroke() {
        return "numbers/stroke";
    }
}
