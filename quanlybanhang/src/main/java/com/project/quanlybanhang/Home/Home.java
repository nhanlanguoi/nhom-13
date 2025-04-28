package com.project.quanlybanhang.Home;

import com.project.quanlybanhang.Product.Product;
import com.project.quanlybanhang.Product.Productservice;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.project.quanlybanhang.User.User;
import com.project.quanlybanhang.Product.Productservice;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Controller
public class Home  {

    Productservice productService = new Productservice();

    @GetMapping("/")
    public String home(Model model) throws IOException {
        List<Product> listproduct = productService.getAllProducts();
        model.addAttribute("products",listproduct);
        return "html/home";


    }

    @GetMapping("/product")
    public String product(@RequestParam String id, Model model) throws IOException {
        Product product = productService.getProductById(id);

        if (product != null) {
            model.addAttribute("product", product); // gửi sản phẩm ra giao diện
            return "html/product"; // trả về giao diện chi tiết sản phẩm
        } else {
            return "redirect:/"; // không tìm thấy sản phẩm thì về trang chủ
        }
    }

}

