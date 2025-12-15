package com.example.jira.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.jira.constant.AuthConstants;
import com.example.jira.dto.AuthResponse;
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
	    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
	    	AuthResponse response = userService.register(request);
	    	 if (response.getMessage().equals(AuthConstants.USER_ALREADY_EXISTS)) {
	             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	         }

	         return ResponseEntity.status(HttpStatus.CREATED).body(response);
	    }

	    @PostMapping("/login")
	    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
	    	AuthResponse response =userService.login(request);
	    	switch (response.getMessage()) {
            case AuthConstants.USER_NOT_FOUND:
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

            case AuthConstants.INVALID_PASSWORD:
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

            default:
                return ResponseEntity.ok(response);
        }
	    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@RequestBody LoginRequest request) {

        AuthResponse response = userService.forgotPassword(request);

        if (response.getMessage().equals(AuthConstants.USER_NOT_FOUND)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }


}
