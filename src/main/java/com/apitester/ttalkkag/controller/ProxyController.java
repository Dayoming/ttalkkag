package com.apitester.ttalkkag.controller;

import com.apitester.ttalkkag.dto.ProxyRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/proxy")
public class ProxyController {

    private final RestTemplate restTemplate;

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<?> proxyRequest(@RequestPart ProxyRequest request,
                                          @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(headers::set);
            }

            System.out.println("Request: " + request);

            HttpEntity<?> entity = new HttpEntity<>(request.getBody(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    request.getUrl(),
                    HttpMethod.valueOf(request.getMethod()),
                    entity,
                    String.class
            );

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Proxy request failed: " + e.getMessage());
        }
    }
}

