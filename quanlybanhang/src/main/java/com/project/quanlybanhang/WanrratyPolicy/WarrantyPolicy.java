package com.project.quanlybanhang.WanrratyPolicy;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class WarrantyPolicy {
    @GetMapping("/warranty-policy")
    public String warrantyPolicy(Model model) {
        return "/html/warranty-policy"; 
    }

}
