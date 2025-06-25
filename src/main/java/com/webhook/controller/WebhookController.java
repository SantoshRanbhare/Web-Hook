package com.webhook.controller;

import com.webhook.entity.WebhookMapping;
import com.webhook.repository.WebhookMappingRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final WebhookMappingRepository repository;

    private final RestTemplate restTemplate = new RestTemplate();

    public WebhookController(WebhookMappingRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/{source}")
    public ResponseEntity<String> receiveWebhook(@PathVariable String source,
                                                 @RequestBody String payload) {
        System.out.println("Webhook received from: " + source);
        System.out.println(payload);

        return repository.findBySourceIgnoreCase(source)
                .map(mapping -> {
                    String destinationUrl = mapping.getDestinationUrl();
                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<String> request = new HttpEntity<>(payload, headers);

                        ResponseEntity<String> response =
                                restTemplate.postForEntity(destinationUrl, request, String.class);

                        System.out.println("➡️ Forwarded to: " + destinationUrl);
                        return ResponseEntity.ok("Forwarded successfully: " + response.getStatusCode());
                    } catch (Exception e) {
                        System.err.println("Error forwarding: " + e.getMessage());
                        return ResponseEntity.status(500).body("Error forwarding: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No destination URL found for source: " + source));
    }
    @PostMapping("/add")
    public ResponseEntity<?> addMapping(@RequestBody WebhookMapping mapping) {
        // Check if source already exists
        if (repository.findBySourceIgnoreCase(mapping.getSource()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("❌ Mapping for source '" + mapping.getSource() + "' already exists");
        }
        WebhookMapping saved = repository.save(mapping);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

}

