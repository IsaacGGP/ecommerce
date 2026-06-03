package org.isaac.com.ecommers.controllers;

import jakarta.validation.Valid;
import org.isaac.com.ecommers.models.dto.UpdateUserRequest;
import org.isaac.com.ecommers.models.dto.UserResponse;
import org.isaac.com.ecommers.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController{

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> findByEmail(@PathVariable String email){
        UserResponse user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id){
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> userUpdate(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest update){
        UserResponse response = userService.updateUser(id, update);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
         userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }
}