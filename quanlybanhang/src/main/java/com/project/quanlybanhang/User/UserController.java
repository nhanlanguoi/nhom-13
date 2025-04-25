package com.project.quanlybanhang.User;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User user) {
            model.addAttribute("user", user);
            return "html/user-info";
        }
        return "redirect:/login";
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
