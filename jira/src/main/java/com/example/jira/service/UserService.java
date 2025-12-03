package com.example.jira.service;

import org.springframework.stereotype.Service;

import com.example.jira.dto.LoginRequest;
import com.example.jira.dto.RegisterRequest;
import com.example.jira.model.User;
import com.example.jira.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService
{
	private final UserRepository userRepo;

    public String register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            return "Username already exists!";
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword()); // store raw password since no Spring sec
        user.setRole(req.getRole());

        userRepo.save(user);
        return "User Registered Successfully!";
    }

    public String login(LoginRequest req) {
        User user = userRepo.findByUsername(req.getUsername())
                .orElse(null);

        if (user == null) {
            return "User not found!";
        }

        if (!user.getPassword().equals(req.getPassword())) {
            return "Invalid password!";
        }

        return "Login Successful! Role: " + user.getRole();
    }

}
