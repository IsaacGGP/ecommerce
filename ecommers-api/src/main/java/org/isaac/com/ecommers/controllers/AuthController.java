package org.isaac.com.ecommers.controllers;

import jakarta.validation.Valid;
import org.isaac.com.ecommers.models.dto.CreateUserRequest;
import org.isaac.com.ecommers.models.dto.LoginRequest;
import org.isaac.com.ecommers.models.dto.LoginResponse;
import org.isaac.com.ecommers.models.dto.UserResponse;
import org.isaac.com.ecommers.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

     private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    //Registro user
    @PostMapping("/register")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request){
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    //Controller Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        LoginResponse response = userService.loginUser(loginRequest);
        return ResponseEntity.ok(response);
    }
}
