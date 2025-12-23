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

    @GetMapping("/santa-rescue")
    public String santaRescue() {
        return "games/santa-rescue";
    }

    @GetMapping("/cat-fishing")
    public String catFishing() {
        return "games/cat-fishing";
    }

    @GetMapping("/dino-fruit")
    public String dinoFruit() {
        return "games/dino-fruit";
    }

    @GetMapping("/shape-helper")
    public String shapeHelper() {
        return "games/shape-helper";
    }

    @GetMapping("/farm-harvest")
    public String farmHarvest() {
        return "games/farm-harvest";
    }

    @GetMapping("/balloon-pop")
    public String balloonPop() {
        return "games/balloon-pop";
    }

    @GetMapping("/car-home")
    public String carHome() {
        return "games/car-home";
    }

    @GetMapping("/drawing")
    public String drawing() {
        return "games/drawing";
    }

    @GetMapping("/puzzle")
    public String puzzle() {
        return "games/puzzle";
    }
}
