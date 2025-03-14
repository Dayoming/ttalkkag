package com.apitester.ttalkkag.kafka;

import com.apitester.ttalkkag.config.JwtTokenUtil;
import com.apitester.ttalkkag.dto.JwtValidationRequest;
import com.apitester.ttalkkag.dto.JwtValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtValidationConsumer {
    private final KafkaTemplate<String, JwtValidationResponse> kafkaTemplate;
    private final JwtTokenUtil jwtTokenUtil;

    @KafkaListener(topics = "jwt-validation-request", groupId = "jwt-validation-group")
    public void validateJwt(JwtValidationRequest request) {
        System.out.println("JwtValidationRequest: " + request);
        String jwtToken = request.getJwtToken();
        boolean isValid = jwtTokenUtil.validateToken(jwtToken);

        JwtValidationResponse response = new JwtValidationResponse(jwtToken, isValid);
        kafkaTemplate.send("jwt-validation-response", response);
    }
}
