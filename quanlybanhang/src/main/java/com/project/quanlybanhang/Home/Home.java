package com.project.quanlybanhang.Home;

import com.project.quanlybanhang.Product.Product;
import com.project.quanlybanhang.Product.Productservice;
import com.project.quanlybanhang.Product.Variant;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.project.quanlybanhang.User.User;
import com.project.quanlybanhang.Utils.Numberutils;
import org.springframework.web.bind.annotation.RequestParam;
import  com.project.quanlybanhang.Product.managerdataproduct;

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

    @GetMapping("/product")
    public String product(@RequestParam String id, Model model) throws IOException {
        Product product = productService.getProductById(id);

        if (product != null) {
            List<Variant> listvariant = productService.getProductVariantsById(id);
            model.addAttribute("variant" , listvariant);


            model.addAttribute("product", product); // gửi sản phẩm ra giao diện
            Variant selectedVariant = product.getVariants().get(0);


            String pricediscount = Long.toString(processingprice(Long.parseLong(selectedVariant.getPrice()), selectedVariant.getDiscount()));
            pricediscount = parseSafeLongadddot(pricediscount);


            String price = selectedVariant.getPrice();
            price = parseSafeLongadddot(price);

            model.addAttribute("price" , price);
            model.addAttribute("discount" , pricediscount);




            return "html/product"; // trả về giao diện chi tiết sản phẩm
        } else {
            return "redirect:/"; // không tìm thấy sản phẩm thì về trang chủ
        }
    }



}

