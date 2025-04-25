package com.project.quanlybanhang.Home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class Home {

    @GetMapping("/")
    public String home(Model model) {

        return "html/home";
    }
}

