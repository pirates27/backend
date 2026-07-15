package com.landlens.property.service;

import com.landlens.property.model.Property;
import com.landlens.property.model.PropertyImage;
import com.landlens.property.model.PropertyVideo;
import com.landlens.property.model.PropertyVisit;
import com.landlens.property.model.SavedProperty;
import com.landlens.property.repository.PropertyImageRepository;
import com.landlens.property.repository.PropertyRepository;
import com.landlens.property.repository.PropertyVideoRepository;
import com.landlens.property.repository.PropertyVisitRepository;
import com.landlens.property.repository.SavedPropertyRepository;
import com.landlens.user.model.User;
import com.landlens.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyImageRepository propertyImageRepository;

    @Autowired
    private PropertyVideoRepository propertyVideoRepository;

    @Autowired
    private PropertyVisitRepository propertyVisitRepository;

    @Autowired
    private SavedPropertyRepository savedPropertyRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Property createProperty(Property property, UUID providerId) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        property.setProvider(provider);
        property.setPropertyCode("LL-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000));
        property.setStatus("PENDING_AI");
        property.setIsActive(true);

        return propertyRepository.save(property);
    }

    public Property getPropertyById(UUID id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        if (!property.getIsActive()) {
            throw new RuntimeException("Property is inactive");
        }
        return property;
    }

    public Property getPropertyByCode(String code) {
        Property property = propertyRepository.findByPropertyCode(code)
                .orElseThrow(() -> new RuntimeException("Property not found with code: " + code));
        if (!property.getIsActive()) {
            throw new RuntimeException("Property is inactive");
        }
        return property;
    }

    public List<Property> searchProperties(
            String district, String village, String state, String status, String category,
            BigDecimal minPrice, BigDecimal maxPrice) {
        return propertyRepository.searchProperties(district, village, state, status, category, minPrice, maxPrice);
    }

    @Transactional
    public Property updateProperty(UUID id, Property details, UUID providerId) {
        Property property = getPropertyById(id);
        
        // Ensure user is the owner provider
        if (!property.getProvider().getId().equals(providerId)) {
            throw new RuntimeException("Unauthorized update: not the listing owner");
        }

        property.setTitle(details.getTitle());
        property.setCategory(details.getCategory());
        property.setArea(details.getArea());
        property.setPrice(details.getPrice());
        property.setDescription(details.getDescription());
        property.setSurveyNumber(details.getSurveyNumber());
        property.setAddress(details.getAddress());
        property.setLatitude(details.getLatitude());
        property.setLongitude(details.getLongitude());
        property.setDistrict(details.getDistrict());
        property.setVillage(details.getVillage());
        property.setState(details.getState());
        property.setPincode(details.getPincode());
        property.setThreeSixtyImageUrl(details.getThreeSixtyImageUrl());
        // Do not update status directly, this is governed by AI & Govt verification flow
        
        return propertyRepository.save(property);
    }

    @Transactional
    public void deleteProperty(UUID id, UUID providerId) {
        Property property = getPropertyById(id);
        if (!property.getProvider().getId().equals(providerId)) {
            throw new RuntimeException("Unauthorized delete: not the listing owner");
        }
        property.setIsActive(false);
        propertyRepository.save(property);
    }

    @Transactional
    public PropertyImage addImage(UUID propertyId, PropertyImage image) {
        Property property = getPropertyById(propertyId);
        image.setProperty(property);
        image.setIsActive(true);
        return propertyImageRepository.save(image);
    }

    public List<PropertyImage> getImages(UUID propertyId) {
        return propertyImageRepository.findByPropertyIdAndIsActiveTrueOrderByDisplayOrderAsc(propertyId);
    }

    @Transactional
    public PropertyVideo addVideo(UUID propertyId, PropertyVideo video) {
        Property property = getPropertyById(propertyId);
        video.setProperty(property);
        video.setIsActive(true);
        return propertyVideoRepository.save(video);
    }

    public List<PropertyVideo> getVideos(UUID propertyId) {
        return propertyVideoRepository.findByPropertyIdAndIsActiveTrue(propertyId);
    }

    @Transactional
    public SavedProperty saveProperty(UUID propertyId, UUID buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
        Property property = getPropertyById(propertyId);

        Optional<SavedProperty> existing = savedPropertyRepository
                .findByBuyerIdAndPropertyIdAndIsActiveTrue(buyerId, propertyId);
        if (existing.isPresent()) {
            return existing.get();
        }

        SavedProperty saved = new SavedProperty();
        saved.setBuyer(buyer);
        saved.setProperty(property);
        saved.setIsActive(true);
        return savedPropertyRepository.save(saved);
    }

    @Transactional
    public void unsaveProperty(UUID propertyId, UUID buyerId) {
        savedPropertyRepository.findByBuyerIdAndPropertyIdAndIsActiveTrue(buyerId, propertyId)
                .ifPresent(saved -> {
                    saved.setIsActive(false);
                    savedPropertyRepository.save(saved);
                });
    }

    public List<SavedProperty> getSavedProperties(UUID buyerId) {
        return savedPropertyRepository.findByBuyerIdAndIsActiveTrue(buyerId);
    }

    @Transactional
    public PropertyVisit scheduleVisit(UUID propertyId, UUID buyerId, PropertyVisit visit) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
        Property property = getPropertyById(propertyId);

        visit.setBuyer(buyer);
        visit.setProperty(property);
        visit.setStatus("SCHEDULED");
        visit.setIsActive(true);
        return propertyVisitRepository.save(visit);
    }

    @Transactional
    public PropertyVisit updateVisitStatus(UUID visitId, String status, UUID userId) {
        PropertyVisit visit = propertyVisitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit details not found"));
        
        // Allowed roles or users can update. For simplicity, just update status
        visit.setStatus(status.toUpperCase());
        return propertyVisitRepository.save(visit);
    }

    public List<PropertyVisit> getBuyerVisits(UUID buyerId) {
        return propertyVisitRepository.findByBuyerIdAndIsActiveTrue(buyerId);
    }

    public List<PropertyVisit> getProviderVisits(UUID providerId) {
        return propertyVisitRepository.findByPropertyProviderIdAndIsActiveTrue(providerId);
    }
}
