package com.example.carsharing1.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({"/", "/cars", "/bookings", "/users", "/locations", "/features"})
    public String forward() {
        return "forward:/index.html";
    }
}
