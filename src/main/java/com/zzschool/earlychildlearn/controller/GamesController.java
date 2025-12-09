package com.zzschool.earlychildlearn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/games")
public class GamesController {

    @GetMapping("/menu")
    public String menu() {
        return "games/menu";
    }

    @GetMapping("/rock-paper-scissors")
    public String rockPaperScissors() {
        return "games/rock_paper_scissors";
    }

    @GetMapping("/maze/loading")
    public String mazeLoading() {
        return "games/maze/loading";
    }

    @GetMapping("/maze/game")
    public String mazeGame() {
        return "games/maze/game";
    }
}
