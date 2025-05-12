package com.cyberlog.Controllers;

import com.cyberlog.Models.User;
import com.cyberlog.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/register")
    @ResponseBody
    public User register(@RequestBody User user) {
        return service.register(user);
    }

    @PostMapping("/login")
    @ResponseBody
    public String login(@RequestBody User user) {
        return service.verify(user);
    }
}
