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

import java.io.IOException;
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
}

