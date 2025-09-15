package com.example.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index.html";
    }

    @GetMapping("/users")
    public String users() {
        return "users.html";
    }

    @GetMapping("/orders")
    public String orders() {
        return "orders.html";
    }

    @GetMapping("/bookings")
    public String bookings() {
        return "bookings.html";
    }

    @GetMapping("/purchase")
    public String purchase() {
        return "purchase.html";
    }
}