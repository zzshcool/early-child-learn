package com.zzschool.earlychildlearn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/maze")
public class MazeController {

    @GetMapping("/loading")
    public String loading() {
        return "maze/loading";
    }

    @GetMapping("/game")
    public String game() {
        return "maze/game";
    }
}
