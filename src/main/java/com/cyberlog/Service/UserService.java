package com.cyberlog.Service;

import com.cyberlog.Models.User;
import com.cyberlog.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    AuthenticationManager autMan;

    @Autowired
    private JWTService jwtService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);

    }

    public String verify(User user) {
        System.out.println("Login con: " + user.getEmail() + " / " + user.getPassword());

        Authentication auth = autMan.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

        if (auth.isAuthenticated()) {
            return jwtService.generateToken(user.getEmail());
        }
        return "fail";
    }
}
