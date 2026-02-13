package com.dietiestates25.controller;

import com.dietiestates25.dto.UserDTO;
import com.dietiestates25.dto.UserUpdateRequest;
import com.dietiestates25.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile() {
        String email = ((org.springframework.security.core.userdetails.UserDetails) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal()).getUsername();
        return ResponseEntity.ok(userService.getUser(email));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateMyProfile(@RequestBody UserUpdateRequest request) {
        String email = ((org.springframework.security.core.userdetails.UserDetails) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal()).getUsername();
        userService.updateUser(email, request);
        return ResponseEntity.ok().build();
    }
}
