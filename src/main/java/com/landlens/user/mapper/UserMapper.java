package com.landlens.user.mapper;

import com.landlens.user.dto.UserResponseDto;
import com.landlens.user.model.User;

public class UserMapper {

    public static UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        if (user.getRole() != null) {
            dto.setRole(user.getRole().getName());
        }
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
