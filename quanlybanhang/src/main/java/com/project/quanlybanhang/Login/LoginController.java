package com.project.quanlybanhang.Login;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.quanlybanhang.User.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm() {
        return "html/login";
    }
    // Đường dẫn file JSON trùng với RegisterController
    private static final String USER_FILE_PATH = "src/main/resources/static/data-user/users.json";

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(USER_FILE_PATH);

            if (!file.exists()) {
                model.addAttribute("error", "Không thể tải dữ liệu người dùng");
                return "html/login";
            }

            List<Map<String, String>> users = mapper.readValue(file, new TypeReference<>() {});

            // Tìm kiếm user
            for (Map<String, String> u : users) {
                if (u.get("username").equals(username)) {
                    if (u.get("password").equals(password)) {
                        // Tạo đối tượng User và đăng nhập
                        User userObj = new User(
                                u.get("username"),
                                u.get("password"),
                                u.get("fullname"),
                                u.get("email")
                        );
                        session.setAttribute("user", userObj);
                        return "redirect:/profile";
                    } else {
                        model.addAttribute("error", "Mật khẩu không đúng");
                        return "html/login";
                    }
                }
            }

            model.addAttribute("error", "Không có tài khoản");
            return "html/login";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi hệ thống, vui lòng thử lại");
            return "html/login";
        }

    }

    }
