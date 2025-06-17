package com.project.quanlybanhang.Role;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class callbuy {
    @GetMapping("/callbuy")
    public String callbuy(Model model) throws IOException {
        return "html/callbuy.html";
    }
}
