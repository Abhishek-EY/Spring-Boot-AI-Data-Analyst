package com.ai.data.analyst.controller;

import com.ai.data.analyst.service.DataAnalystService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/dataAnalyst")
@Slf4j
public class DataAnalystController {

    private final DataAnalystService dataAnalystService;

    @Autowired
    public DataAnalystController(DataAnalystService dataAnalystService) {
        this.dataAnalystService = dataAnalystService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File is empty");
        }
        if (!file.getContentType().equals("text/csv")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Unsupported file type. Please upload a CSV file.");
        }
        log.info("Received file: {}", file.getOriginalFilename());
        try {
            dataAnalystService.processData(file);
        } catch (Exception e) {
            log.error("Error processing file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
        }
        return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
    }

    @PostMapping("/analyse")
    public ResponseEntity<String> analyseData (@RequestBody Map<String, String> request) throws JsonProcessingException {
        String prompt = request.get("prompt");
        log.info("Received prompt: {}", prompt);
        String response = dataAnalystService.generateAnalysis(prompt);
        log.info("Generated response: {}", response);
        return ResponseEntity.ok(response);
    }

}
