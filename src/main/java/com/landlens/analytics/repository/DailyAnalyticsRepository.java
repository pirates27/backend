package com.landlens.analytics.repository;

import com.landlens.analytics.model.DailyAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyAnalyticsRepository extends JpaRepository<DailyAnalytics, UUID> {
    Optional<DailyAnalytics> findByAnalyticsDate(LocalDate date);
}
