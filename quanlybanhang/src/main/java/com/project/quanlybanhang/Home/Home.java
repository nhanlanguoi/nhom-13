package com.project.quanlybanhang.Home;

import com.project.quanlybanhang.Product.*;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.project.quanlybanhang.User.User;
import com.project.quanlybanhang.Utils.Numberutils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.project.quanlybanhang.Product.Productservice.processingprice;
import static com.project.quanlybanhang.Utils.Numberutils.parseSafeLongadddot;

@Controller
public class Home  {

    Productservice productService = new Productservice();

    @GetMapping("/")
    public String home(Model model) throws IOException {
        List<Product> listproduct = productService.getAllProducts();
        model.addAttribute("products",listproduct);
        System.out.println(listproduct);
        return "html/home";
    }





}

