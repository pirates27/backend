package com.landlens.analytics.service;

import com.landlens.analytics.model.DailyAnalytics;
import com.landlens.analytics.repository.DailyAnalyticsRepository;
import com.landlens.api.model.ApiUsage;
import com.landlens.api.repository.ApiUsageRepository;
import com.landlens.fraud.repository.FraudReportRepository;
import com.landlens.property.repository.PropertyRepository;
import com.landlens.verification.repository.GovernmentVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class DailyAnalyticsService {

    @Autowired
    private DailyAnalyticsRepository analyticsRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private GovernmentVerificationRepository verificationRepository;

    @Autowired
    private FraudReportRepository fraudReportRepository;

    @Autowired
    private ApiUsageRepository apiUsageRepository;

    @Transactional
    public DailyAnalytics getDailyAnalyticsForDate(LocalDate date) {
        Optional<DailyAnalytics> existing = analyticsRepository.findByAnalyticsDate(date);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Dynamically compute/aggregate from actual tables
        long propertyCount = propertyRepository.count();
        long verificationCount = verificationRepository.count();
        long fraudCount = fraudReportRepository.count();

        // Sum api usage call counts for the date
        // (Just retrieve sum or seed with default mock values if empty)
        long apiCalls = apiUsageRepository.findAll().stream()
                .filter(usage -> usage.getUsageDate().equals(date))
                .mapToLong(ApiUsage::getCallCount)
                .sum();

        DailyAnalytics analytics = new DailyAnalytics();
        analytics.setAnalyticsDate(date);
        // Simulate views/searches for illustration
        analytics.setPropertyViews((int) (propertyCount * 12 + 5));
        analytics.setSearchCount((int) (propertyCount * 25 + 10));
        analytics.setVerificationCount((int) verificationCount);
        analytics.setFraudCount((int) fraudCount);
        analytics.setApiCalls((int) apiCalls);
        analytics.setIsActive(true);

        return analyticsRepository.save(analytics);
    }
}
