package org.platform.ticket.ticket_platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String index() {
       
        return "pages/home";
    }
   

    @GetMapping ("/login")
    public String login(){
        return "/pages/login";
    }
    
}
