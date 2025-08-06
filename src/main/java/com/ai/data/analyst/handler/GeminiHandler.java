package com.ai.data.analyst.handler;

import com.ai.data.analyst.response.GeminiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class GeminiHandler {

    @Value("${google.gemini.api.key}")
    private String API_KEY;

    @Value("${google.gemini.api.url}")
    private String GEMINI_URL;

    public String generateContent(String prompt) {
        try {
            String url = String.format(GEMINI_URL, API_KEY);
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(url, request, GeminiResponse.class);
            if (Objects.nonNull(response.getBody())) {
                return response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();
            } else {
                log.error("No response body received from Gemini API");
                return "No content generated";
            }
        } catch (Exception e) {
            log.error("Error generating content with Gemini: {}", e.getMessage());
            return "Error generating content";
        }
    }
}


/**
 * HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
 * String requestJson = String.format("""
 * {
 * "contents": [
 * {
 * "parts": [
 * {
 * "text": "%s"
 * }
 * ]
 * }
 * ],
 * "generationConfig": {
 * "maxOutputTokens": 200,
 * "temperature": 0.7,
 * "topP": 0.9
 * }
 * }
 * """, prompt);
 */
