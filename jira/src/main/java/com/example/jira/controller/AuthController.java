package com.example.jira.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.jira.dto.LoginRequest;
import com.example.jira.dto.RegisterRequest;
import com.example.jira.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jira/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	 private final UserService userService;

	    @PostMapping("/register")
	    public String register(@RequestBody RegisterRequest request) {
	        return userService.register(request);
	    }

	    @PostMapping("/login")
	    public String login(@RequestBody LoginRequest request) {
	        return userService.login(request);
	    }
	    
}
