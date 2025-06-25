package com.webhook.repository;

import com.webhook.entity.WebhookMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookMappingRepository extends JpaRepository<WebhookMapping, Long> {
    Optional<WebhookMapping> findBySourceIgnoreCase(String source);
    
}

