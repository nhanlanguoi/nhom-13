package com.project.quanlybanhang.Role;


import com.project.quanlybanhang.Product.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.List;

@Controller
public class warranty_policy {
    @GetMapping("/warranty_policy")
    public String warranty_policy(Model model) throws IOException {
        return "html/warranty-policy.html";
    }
}
