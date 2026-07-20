package com.landlens.property.mapper;

import com.landlens.property.dto.*;
import com.landlens.property.model.*;
import com.landlens.user.mapper.UserMapper;

public class PropertyMapper {

    private PropertyMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static PropertyResponseDto toResponseDto(Property property) {
        if (property == null) {
            return null;
        }
        PropertyResponseDto dto = new PropertyResponseDto();
        dto.setId(property.getId());
        dto.setPropertyCode(property.getPropertyCode());
        dto.setTitle(property.getTitle());
        dto.setCategory(property.getCategory());
        dto.setArea(property.getArea());
        dto.setPrice(property.getPrice());
        dto.setDescription(property.getDescription());
        dto.setSurveyNumber(property.getSurveyNumber());
        dto.setAddress(property.getAddress());
        dto.setLatitude(property.getLatitude());
        dto.setLongitude(property.getLongitude());
        dto.setDistrict(property.getDistrict());
        dto.setVillage(property.getVillage());
        dto.setState(property.getState());
        dto.setPincode(property.getPincode());
        dto.setThreeSixtyImageUrl(property.getThreeSixtyImageUrl());
        dto.setStatus(property.getStatus());
        dto.setProvider(UserMapper.toResponseDto(property.getProvider()));
        dto.setIsActive(property.getIsActive());
        dto.setCreatedAt(property.getCreatedAt());
        dto.setUpdatedAt(property.getUpdatedAt());
        return dto;
    }

    public static Property toEntity(PropertyRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Property property = new Property();
        property.setTitle(dto.getTitle());
        property.setCategory(dto.getCategory());
        property.setArea(dto.getArea());
        property.setPrice(dto.getPrice());
        property.setDescription(dto.getDescription());
        property.setSurveyNumber(dto.getSurveyNumber());
        property.setAddress(dto.getAddress());
        property.setLatitude(dto.getLatitude());
        property.setLongitude(dto.getLongitude());
        property.setDistrict(dto.getDistrict());
        property.setVillage(dto.getVillage());
        property.setState(dto.getState());
        property.setPincode(dto.getPincode());
        property.setThreeSixtyImageUrl(dto.getThreeSixtyImageUrl());
        return property;
    }

    public static PropertyImageDto toResponseDto(PropertyImage image) {
        if (image == null) {
            return null;
        }
        PropertyImageDto dto = new PropertyImageDto();
        dto.setId(image.getId());
        if (image.getProperty() != null) {
            dto.setPropertyId(image.getProperty().getId());
        }
        dto.setImageUrl(image.getImageUrl());
        dto.setThumbnailUrl(image.getThumbnailUrl());
        dto.setDisplayOrder(image.getDisplayOrder());
        return dto;
    }

    public static PropertyVideoDto toResponseDto(PropertyVideo video) {
        if (video == null) {
            return null;
        }
        PropertyVideoDto dto = new PropertyVideoDto();
        dto.setId(video.getId());
        if (video.getProperty() != null) {
            dto.setPropertyId(video.getProperty().getId());
        }
        dto.setVideoUrl(video.getVideoUrl());
        dto.setDuration(video.getDuration());
        dto.setThumbnailUrl(video.getThumbnailUrl());
        return dto;
    }

    public static PropertyVisitResponseDto toResponseDto(PropertyVisit visit) {
        if (visit == null) {
            return null;
        }
        PropertyVisitResponseDto dto = new PropertyVisitResponseDto();
        dto.setId(visit.getId());
        dto.setBuyer(UserMapper.toResponseDto(visit.getBuyer()));
        dto.setProperty(toResponseDto(visit.getProperty()));
        dto.setVisitDate(visit.getVisitDate());
        dto.setVisitTime(visit.getVisitTime());
        dto.setStatus(visit.getStatus());
        return dto;
    }

    public static SavedPropertyResponseDto toResponseDto(SavedProperty saved) {
        if (saved == null) {
            return null;
        }
        SavedPropertyResponseDto dto = new SavedPropertyResponseDto();
        dto.setId(saved.getId());
        dto.setBuyer(UserMapper.toResponseDto(saved.getBuyer()));
        dto.setProperty(toResponseDto(saved.getProperty()));
        return dto;
    }
}
