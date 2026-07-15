-- LandLens Production Database Schema Script (MySQL Compatibility)
-- 3NF Relational Database Schema with UUID VARCHAR(36) Primary Keys and Constraints

CREATE DATABASE IF NOT EXISTS u833088220_land_lens;
USE u833088220_land_lens;

-- 1. roles
CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. users
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NULL,
    role_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. refresh_tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. login_histories
CREATE TABLE IF NOT EXISTS login_histories (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    login_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(512) NULL,
    status VARCHAR(20) NOT NULL, -- 'SUCCESS', 'FAILED'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_login_histories_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. properties
CREATE TABLE IF NOT EXISTS properties (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    property_code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL, -- 'RESIDENTIAL', 'COMMERCIAL', 'AGRICULTURAL', 'INDUSTRIAL'
    area DECIMAL(12, 2) NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    description TEXT NULL,
    survey_number VARCHAR(50) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,
    district VARCHAR(100) NOT NULL,
    village VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    three_sixty_image_url VARCHAR(512) NULL,
    status VARCHAR(30) NOT NULL, -- 'PENDING_AI', 'PENDING_GOVT', 'APPROVED', 'REJECTED', 'DISPUTED'
    provider_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_properties_provider FOREIGN KEY (provider_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. property_images
CREATE TABLE IF NOT EXISTS property_images (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    property_id VARCHAR(36) NOT NULL,
    image_url VARCHAR(512) NOT NULL,
    thumbnail_url VARCHAR(512) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_property_images_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. property_videos
CREATE TABLE IF NOT EXISTS property_videos (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    property_id VARCHAR(36) NOT NULL,
    video_url VARCHAR(512) NOT NULL,
    duration INT NULL,
    thumbnail_url VARCHAR(512) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_property_videos_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. property_documents
CREATE TABLE IF NOT EXISTS property_documents (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    property_id VARCHAR(36) NOT NULL,
    document_type VARCHAR(50) NOT NULL, -- 'SALE_DEED', 'PATTA', 'SURVEY_MAP', 'TAX_RECEIPT', 'IDENTITY_PROOF', 'OWNERSHIP_PROOF'
    file_url VARCHAR(512) NOT NULL,
    ocr_status VARCHAR(30) NOT NULL, -- 'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'
    verification_status VARCHAR(30) NOT NULL, -- 'UNVERIFIED', 'VERIFIED', 'REJECTED'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_property_docs_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. ai_verifications
CREATE TABLE IF NOT EXISTS ai_verifications (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    property_id VARCHAR(36) NOT NULL UNIQUE,
    ai_trust_score DECIMAL(5, 2) NOT NULL,
    forgery_score DECIMAL(5, 2) NOT NULL,
    duplicate_score DECIMAL(5, 2) NOT NULL,
    ownership_match TINYINT(1) NOT NULL,
    risk_score DECIMAL(5, 2) NOT NULL,
    summary TEXT NULL,
    confidence DECIMAL(5, 2) NOT NULL,
    generated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_ai_verifications_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. government_verifications
CREATE TABLE IF NOT EXISTS government_verifications (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    property_id VARCHAR(36) NOT NULL UNIQUE,
    officer_id VARCHAR(36) NOT NULL,
    remarks TEXT NULL,
    status VARCHAR(30) NOT NULL, -- 'APPROVED', 'REJECTED', 'DISPUTED'
    verified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_govt_verifications_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE,
    CONSTRAINT fk_govt_verifications_officer FOREIGN KEY (officer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. verification_timelines
CREATE TABLE IF NOT EXISTS verification_timelines (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    property_id VARCHAR(36) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action VARCHAR(50) NOT NULL, -- 'UPLOADED', 'AI_STARTED', 'AI_COMPLETED', 'GOVT_REVIEW_STARTED', 'APPROVED', 'REJECTED', 'DISPUTED'
    remarks TEXT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_ver_timelines_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE,
    CONSTRAINT fk_ver_timelines_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. duplicate_claims
CREATE TABLE IF NOT EXISTS duplicate_claims (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    property_a_id VARCHAR(36) NOT NULL,
    property_b_id VARCHAR(36) NOT NULL,
    similarity DECIMAL(5, 2) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL, -- 'FLAGGED', 'INVESTIGATING', 'RESOLVED', 'FALSE_POSITIVE'
    decision VARCHAR(50) NULL, -- 'MERGED', 'CANCELLED_A', 'CANCELLED_B', 'NO_ACTION'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_duplicate_claims_prop_a FOREIGN KEY (property_a_id) REFERENCES properties (id) ON DELETE CASCADE,
    CONSTRAINT fk_duplicate_claims_prop_b FOREIGN KEY (property_b_id) REFERENCES properties (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. fraud_reports
CREATE TABLE IF NOT EXISTS fraud_reports (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    reporter_id VARCHAR(36) NOT NULL,
    property_id VARCHAR(36) NOT NULL,
    reason VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL, -- 'SUBMITTED', 'UNDER_INVESTIGATION', 'RESOLVED_FRAUD', 'RESOLVED_DISMISSED'
    officer_id VARCHAR(36) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_fraud_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
    CONSTRAINT fk_fraud_reports_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE,
    CONSTRAINT fk_fraud_reports_officer FOREIGN KEY (officer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. property_visits
CREATE TABLE IF NOT EXISTS property_visits (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    buyer_id VARCHAR(36) NOT NULL,
    property_id VARCHAR(36) NOT NULL,
    visit_date DATE NOT NULL,
    visit_time TIME NOT NULL,
    status VARCHAR(30) NOT NULL, -- 'SCHEDULED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_property_visits_buyer FOREIGN KEY (buyer_id) REFERENCES users (id),
    CONSTRAINT fk_property_visits_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. saved_properties
CREATE TABLE IF NOT EXISTS saved_properties (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    buyer_id VARCHAR(36) NOT NULL,
    property_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_saved_properties_buyer FOREIGN KEY (buyer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_properties_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE,
    CONSTRAINT uq_saved_properties UNIQUE (buyer_id, property_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. notifications
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'SYSTEM', 'PROPERTY_VERIFIED', 'VISIT_SCHEDULED', 'FRAUD_ALERT', 'API_LIMIT_REACHED'
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    receiver_id VARCHAR(36) NOT NULL,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_notifications_receiver FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. ai_conversations
CREATE TABLE IF NOT EXISTS ai_conversations (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(150) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_ai_conversations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. ai_messages
CREATE TABLE IF NOT EXISTS ai_messages (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    sender_role VARCHAR(20) NOT NULL, -- 'USER', 'AI'
    content TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_ai_messages_conv FOREIGN KEY (conversation_id) REFERENCES ai_conversations (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. api_keys
CREATE TABLE IF NOT EXISTS api_keys (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    key_hash VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    prefix VARCHAR(8) NOT NULL,
    status VARCHAR(20) NOT NULL, -- 'ACTIVE', 'REVOKED', 'EXPIRED'
    expiry_date TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_api_keys_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. api_usages
CREATE TABLE IF NOT EXISTS api_usages (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    api_key_id VARCHAR(36) NOT NULL,
    usage_date DATE NOT NULL,
    call_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_api_usages_key FOREIGN KEY (api_key_id) REFERENCES api_keys (id) ON DELETE CASCADE,
    CONSTRAINT uq_api_usages UNIQUE (api_key_id, usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. api_logs
CREATE TABLE IF NOT EXISTS api_logs (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    api_key_id VARCHAR(36) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    status_code INT NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    request_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response_time_ms INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_api_logs_key FOREIGN KEY (api_key_id) REFERENCES api_keys (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 22. api_rate_limits
CREATE TABLE IF NOT EXISTS api_rate_limits (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    api_key_id VARCHAR(36) NOT NULL UNIQUE,
    limit_type VARCHAR(20) NOT NULL, -- 'HOURLY', 'DAILY'
    max_requests INT NOT NULL,
    current_window_start TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_api_rate_limits_key FOREIGN KEY (api_key_id) REFERENCES api_keys (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. daily_analytics
CREATE TABLE IF NOT EXISTS daily_analytics (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    analytics_date DATE NOT NULL UNIQUE,
    property_views INT NOT NULL DEFAULT 0,
    search_count INT NOT NULL DEFAULT 0,
    verification_count INT NOT NULL DEFAULT 0,
    fraud_count INT NOT NULL DEFAULT 0,
    api_calls INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NULL,
    updated_by VARCHAR(36) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for performance optimizations
CREATE INDEX idx_users_role ON users(role_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_login_histories_user_time ON login_histories(user_id, login_timestamp);
CREATE INDEX idx_properties_location ON properties(district, village, state);
CREATE INDEX idx_properties_coordinates ON properties(latitude, longitude);
CREATE INDEX idx_properties_status ON properties(status);
CREATE INDEX idx_properties_provider ON properties(provider_id);
CREATE INDEX idx_property_images_display ON property_images(property_id, display_order);
CREATE INDEX idx_property_docs_type ON property_documents(property_id, document_type);
CREATE INDEX idx_verification_timelines_time ON verification_timelines(property_id, timestamp);
CREATE INDEX idx_duplicate_claims_pair ON duplicate_claims(property_a_id, property_b_id);
CREATE INDEX idx_fraud_reports_prop ON fraud_reports(property_id);
CREATE INDEX idx_property_visits_buyer ON property_visits(buyer_id);
CREATE INDEX idx_saved_properties_buyer ON saved_properties(buyer_id);
CREATE INDEX idx_notifications_receiver_read ON notifications(receiver_id, is_read);
CREATE INDEX idx_ai_messages_conv ON ai_messages(conversation_id);
CREATE INDEX idx_api_keys_user ON api_keys(user_id);
CREATE INDEX idx_api_usages_date ON api_usages(usage_date);
CREATE INDEX idx_api_logs_key_time ON api_logs(api_key_id, request_timestamp);
