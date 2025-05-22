package com.project.quanlybanhang.User;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.quanlybanhang.Product.Productservice;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {




    Productservice productService = new Productservice();
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) throws IOException {
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            model.addAttribute("showLoginPopup", true);
            model.addAttribute("products", productService.getAllProducts());
            return "html/home";
        }

        if (userObj instanceof User user) {
            model.addAttribute("user", user);
            return "html/user-info";
        }


        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    private static final String USER_FILE_PATH = "src/main/resources/static/data-user/users.json";

    @PostMapping("/update-profile")
    public  String updateinfo(@RequestParam String fullname,
                              @RequestParam String email ,
                              @RequestParam String password,
                              Model model,HttpSession session) throws IOException {

        User user = (User) session.getAttribute("user");

        ObjectMapper mapper = new ObjectMapper();
        File file = new File(USER_FILE_PATH);

        // Tạo thư mục nếu chưa tồn tại
        file.getParentFile().mkdirs();

        List<Map<String, String>> users = new ArrayList<>();
        users = mapper.readValue(file, new TypeReference<>() {});
        for (Map<String ,String> u : users){
            if(u.get("username").equals(user.getUsername())){

                String passwordconfig = u.get("password");
                if (passwordconfig.endsWith(" ")) {
                    passwordconfig = passwordconfig.substring(0, passwordconfig.length() - 1);
                }

                u.put("fullname" , fullname);
                u.put("email" , email);
                u.put("password" , passwordconfig);
                // save laij vào file
                mapper.writeValue(file, users);
                user.setFullname(fullname);
                user.setEmail(email);
                user.setPassword(passwordconfig);
                session.setAttribute("user", user);
                break;
            }
        }
        return "redirect:/profile";
    }
}
