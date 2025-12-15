package com.example.jira.service;

import org.springframework.stereotype.Service;

import com.example.jira.constant.AuthConstants;
import com.example.jira.dto.AuthResponse;
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

    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            return new AuthResponse(AuthConstants.USER_ALREADY_EXISTS, req.getUsername()) ;
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword()); // store raw password since no Spring sec
        user.setRole(req.getRole());

        userRepo.save(user);
        return new AuthResponse(AuthConstants.USER_REGISTERED, req.getUsername()) ;
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByUsername(req.getUsername())
                .orElse(null);

        if (user == null) {
             return new AuthResponse(AuthConstants.USER_NOT_FOUND,req.getUsername()) ;
        }

        if (!user.getPassword().equals(req.getPassword())) {
            return new AuthResponse(AuthConstants.INVALID_PASSWORD,req.getUsername()) ;
        }

        return new AuthResponse(AuthConstants.LOGIN_SUCCESS, req.getUsername());
    }

    public AuthResponse forgotPassword(LoginRequest loginRequest) {

        User user = userRepo.findByUsername(loginRequest.getUsername())
                .orElse(null);

        if (user == null) {
            return new AuthResponse(AuthConstants.USER_NOT_FOUND, loginRequest.getUsername());
        }

        user.setPassword(loginRequest.getPassword());
        userRepo.save(user);

        return new AuthResponse(AuthConstants.PASSWORD_UPDATED, loginRequest.getUsername());
    }


}
