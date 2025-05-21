package com.project.quanlybanhang.Register;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.quanlybanhang.User.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.util.*;

@Controller
public class RegisterController {

    private static final String USER_FILE_PATH = "src/main/resources/static/data-user/users.json";

    @GetMapping("/register")
    public String showRegisterForm() {
        return "html/register"; // Giao diện form
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam  String fullname,
                           @RequestParam String email,
                           Model model) {
        try {
            // Kiểm tra mật khẩu xác nhận
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu và xác nhận mật khẩu không khớp");
                return "html/register";
            }

            ObjectMapper mapper = new ObjectMapper();
            File file = new File(USER_FILE_PATH);

            // Tạo thư mục nếu chưa tồn tại
            file.getParentFile().mkdirs();

            List<Map<String, String>> users = new ArrayList<>();

            // Nếu file không tồn tại, tạo file và list rỗng
            if (!file.exists()) {
                file.getParentFile().mkdirs(); // tạo thư mục nếu chưa có
                file.createNewFile();          // tạo file
            } else if (file.length() > 0) {
                // Nếu file có nội dung, thì đọc vào list
                users = mapper.readValue(file, new TypeReference<>() {});
            }

            // Kiểm tra username trùng
            for (Map<String, String> u : users) {
                if (u.get("username").equals(username)) {
                    model.addAttribute("error", "Tên đăng nhập đã tồn tại");
                    return "html/register";
                }
            }

            // Tạo user mới
            Map<String, String> newUser = new LinkedHashMap<>();
            newUser.put("username", username);
            newUser.put("password", password);
            newUser.put("fullname", fullname );
            newUser.put("email", email );
            newUser.put("ban" , "true");
            newUser.put("role" , "EMPLOYEE");

            // Thêm vào danh sách
            users.add(newUser);

            // Ghi lại danh sách ra file với định dạng đẹp
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, users);

            return "redirect:/login";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi hệ thống! Vui lòng thử lại.");
            return "html/register";
        }
    }

}
