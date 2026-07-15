package com.landlens.user.controller;

import com.landlens.user.dto.UserResponseDto;
import com.landlens.user.dto.UserUpdateDto;
import com.landlens.user.mapper.UserMapper;
import com.landlens.user.model.User;
import com.landlens.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Transactional
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UserMapper.toResponseDto(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateProfile(
            @Valid @RequestBody UserUpdateDto userDetails, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        User detailsEntity = new User();
        detailsEntity.setFirstName(userDetails.getFirstName());
        detailsEntity.setLastName(userDetails.getLastName());
        detailsEntity.setPhoneNumber(userDetails.getPhoneNumber());

        User updatedUser = userService.updateProfile(userId, detailsEntity);
        return ResponseEntity.ok(UserMapper.toResponseDto(updatedUser));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponseDto> list = users.stream()
                .map(UserMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
