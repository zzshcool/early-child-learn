package com.zzschool.earlychildlearn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/english")
public class EnglishController {

    @GetMapping("/menu")
    public String menu() {
        return "english/menu";
    }

    @GetMapping("/learn")
    public String learn() {
        return "english/learn";
    }

    @GetMapping("/game")
    public String game() {
        return "english/game";
    }

    @GetMapping("/animal-game")
    public String animalGame() {
        return "english/animal_game";
    }

    @GetMapping("/stroke")
    public String stroke() {
        return "english/stroke";
    }

    @GetMapping("/memory-match")
    public String memoryMatch() {
        return "english/memory_match";
    }
}
