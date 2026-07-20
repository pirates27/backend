package com.landlens.api.controller;

import com.landlens.ai.dto.AiVerificationResponseDto;
import com.landlens.ai.mapper.AiMapper;
import com.landlens.ai.model.AiVerification;
import com.landlens.ai.service.AiVerificationService;
import com.landlens.property.model.Property;
import com.landlens.property.service.PropertyService;
import com.landlens.verification.dto.VerificationTimelineResponseDto;
import com.landlens.verification.mapper.VerificationMapper;
import com.landlens.verification.model.VerificationTimeline;
import com.landlens.verification.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/external")
@Transactional
public class ExternalApiController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private AiVerificationService aiVerificationService;

    @Autowired
    private VerificationService verificationService;

    @GetMapping("/properties/{code}/verify")
    public ResponseEntity<Object> getExternalVerificationDetails(@PathVariable String code) {
        Property property = propertyService.getPropertyByCode(code);
        
        Map<String, Object> response = new HashMap<>();
        response.put("propertyCode", property.getPropertyCode());
        response.put("title", property.getTitle());
        response.put("category", property.getCategory());
        response.put("price", property.getPrice());
        response.put("district", property.getDistrict());
        response.put("village", property.getVillage());
        response.put("state", property.getState());
        response.put("status", property.getStatus());

        try {
            AiVerification aiReport = aiVerificationService.getReportByPropertyId(property.getId());
            AiVerificationResponseDto aiDto = AiMapper.toResponseDto(aiReport);
            response.put("aiVerification", aiDto);
        } catch (Exception e) {
            response.put("aiVerification", null);
        }

        List<VerificationTimeline> timeline = verificationService.getPropertyTimeline(property.getId());
        List<VerificationTimelineResponseDto> timelineDto = timeline.stream()
                .map(VerificationMapper::toResponseDto)
                .toList();
        response.put("timeline", timelineDto);

        return ResponseEntity.ok(response);
    }
}
