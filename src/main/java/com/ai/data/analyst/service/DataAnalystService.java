package com.ai.data.analyst.service;

import com.ai.data.analyst.entity.SuperStore;
import com.ai.data.analyst.handler.GeminiHandler;
import com.ai.data.analyst.repository.DataAnalystRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DataAnalystService {

    private final DataAnalystRepository dataAnalystRepository;
    private final GeminiHandler geminiHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MongoTemplate mongoTemplate;

    public static final String SUPER_STORE = "superStore";
    private static final int MAX_RETRIES = 2;

    @Autowired
    public DataAnalystService(DataAnalystRepository dataAnalystRepository,
                              GeminiHandler geminiHandler,
                              MongoTemplate mongoTemplate) {
        this.dataAnalystRepository = dataAnalystRepository;
        this.geminiHandler = geminiHandler;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Processes the uploaded CSV file and saves the parsed data into the database.
     *
     * @param file the uploaded CSV file
     * @throws IOException if an error occurs while reading the file
     */
    public void processData(MultipartFile file) throws IOException {
        List<SuperStore> uploadedData = parseCsvFile(file.getInputStream());
        log.info("Parsed {} records from the uploaded file", uploadedData.size());
        dataAnalystRepository.saveAll(uploadedData);

    }

    /**
     * Parses the CSV file and converts it into a list of SuperStore entities.
     *
     * @param inputStream the input stream of the CSV file
     * @return a list of SuperStore entities
     */
    private List<SuperStore> parseCsvFile(InputStream inputStream) {
        List<SuperStore> superStores = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {   //if we use UTF_8 CSV file, need to do like this try (InputStreamReader reader = new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8))
            CSVParser parser = new CSVParser(reader,
                    CSVFormat.Builder.create(CSVFormat.DEFAULT)
                            .setHeader()
                            .setSkipHeaderRecord(false)
                            .setIgnoreHeaderCase(true)
                            .setTrim(true)
                            .build());
            log.info("Parsed headers: {}", parser.getHeaderMap().keySet());
            for (CSVRecord csvRecord : parser) {
                SuperStore superStore = SuperStore.builder()
                        .rowId(Integer.valueOf(csvRecord.get("Row ID")))
                        .orderId(csvRecord.get("Order ID"))
                        .orderDate(parseDate(csvRecord.get("Order Date")))
                        .shipDate(parseDate(csvRecord.get("Ship Date")))
                        .shipMode(csvRecord.get("Ship Mode"))
                        .customerId(csvRecord.get("Customer ID"))
                        .customerName(csvRecord.get("Customer Name"))
                        .segment(csvRecord.get("Segment"))
                        .country(csvRecord.get("Country"))
                        .city(csvRecord.get("City"))
                        .state(csvRecord.get("State"))
                        .postalCode(csvRecord.get("Postal Code"))
                        .region(csvRecord.get("Region"))
                        .productId(csvRecord.get("Product ID"))
                        .category(csvRecord.get("Category"))
                        .subCategory(csvRecord.get("Sub-Category"))
                        .productName(csvRecord.get("Product Name"))
                        .sales(Double.valueOf(csvRecord.get("Sales")))
                        .quantity(Integer.valueOf(csvRecord.get("Quantity")))
                        .discount(Double.valueOf(csvRecord.get("Discount")))
                        .profit(Double.valueOf(csvRecord.get("Profit")))
                        .build();
                superStores.add(superStore);
            }
            return superStores;
        } catch (Exception e) {
            log.error("Error parsing CSV file: {}", e.getMessage());
            throw new RuntimeException("Failed to parse CSV file", e);
        }
    }

    /**
     * Parses a date string into a LocalDate object using multiple formats.
     *
     * @param dateStr the date string to parse
     * @return the parsed LocalDate object
     * @throws IllegalArgumentException if the date format is unsupported
     */
    private LocalDate parseDate(String dateStr) {
        String[] formats = {"dd/MM/yyyy", "dd-MM-yyyy", "MM/dd/yyyy", "M/dd/yyyy"};
        for (String format : formats) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
            } catch (Exception ignored) {
                log.debug("Ignoring date parsing error for format {}: {}", format, dateStr);
            }
        }
        throw new IllegalArgumentException("Unsupported date format: " + dateStr);
    }

    /**
     * Generates an analysis based on the provided prompt using the Gemini AI handler.
     *
     * @param prompt the prompt for analysis
     * @return the generated analysis as a String
     */
    public String generateAnalysis(String prompt) throws JsonProcessingException {
        String formattedPrompt = String.format(PromptConstants.ANALYSIS_PROMPT.getPrompt(), prompt);
        log.debug("Generated prompt for analysis: {}", formattedPrompt);

        String jsonPipeline = geminiHandler.generateContent(formattedPrompt);
        log.debug("Gemini Response: {}", jsonPipeline);

        String sanitizedJsonPipeline = jsonPipeline.replace("```json", "").replace("```", "").trim();
        log.info("Generated JSON pipeline: {}", sanitizedJsonPipeline);

        int retryCount = 0;
        List<Document> result = executeAggregation(prompt, sanitizedJsonPipeline, retryCount);

        log.info("processing a result for natural language analysis: {}", result);
        return generateAnalysisPrompt(prompt, result);
    }

    /**
     * Executes the aggregation pipeline on the MongoDB collection.
     *
     * @param prompt       the original prompt for context
     * @param jsonPipeline the JSON representation of the aggregation pipeline
     * @param retryCount   the current retry count
     * @return a list of Document results from the aggregation
     * @throws JsonProcessingException if there is an error processing the JSON
     */
    private List<Document> executeAggregation(String prompt, String jsonPipeline, int retryCount) throws JsonProcessingException {

        // Parse JSON string to List<Document>
        List<Document> pipeline = objectMapper.readValue(jsonPipeline, objectMapper.getTypeFactory().constructCollectionType(List.class, Document.class));

        // Convert to AggregationOperation list
        List<AggregationOperation> operations = pipeline.stream()
                .map(document -> (AggregationOperation) context -> document)
                .toList();

        // Build aggregation
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> results = null;

        if (retryCount == MAX_RETRIES) {
            log.error("Max retries reached for aggregation execution. Returning empty results.");
            return new ArrayList<>();
        } else {
            try {
                // Execute aggregation
                log.info("Attempt:{}, Executing aggregation with pipeline: {}", retryCount, aggregation);
                results = mongoTemplate.aggregate(aggregation, SUPER_STORE, Document.class);
                log.info("Aggregation executed successfully, found {} results", results.getMappedResults());
            } catch (Exception e) {
                log.error("Error executing aggregation: {}", e.getMessage());
                validatePipeline(prompt, jsonPipeline, e.getMessage(), retryCount);
            }
            return results.getMappedResults();
        }
    }

    /**
     * Generates an analysis prompt based on the provided prompt and related data.
     *
     * @param prompt      the original prompt
     * @param relatedData the related data to include in the analysis
     * @return the generated analysis prompt
     */
    private String generateAnalysisPrompt(String prompt, List<Document> relatedData) {
        String analysisPrompt = String.format(PromptConstants.DATA_ANALYSIS_PROMPT.getPrompt(), prompt, relatedData);
        log.debug("Generated analysis prompt: {}", analysisPrompt);
        return geminiHandler.generateContent(analysisPrompt);
    }

    /**
     * Validates the generated pipeline by re-querying the Gemini AI handler.
     *
     * @param prompt       the original prompt for context
     * @param jsonPipeline the JSON representation of the aggregation pipeline
     * @param errorMessage the error message encountered during aggregation
     * @param retryCount   the current retry count
     * @throws JsonProcessingException if there is an error processing the JSON
     */
    private void validatePipeline(String prompt, String jsonPipeline, String errorMessage, int retryCount) throws JsonProcessingException {
        retryCount++;
        String validationPrompt = String.format(PromptConstants.REPORT_PROMPT.getPrompt(), jsonPipeline, prompt, errorMessage);
        log.info("Validation prompt: {}", validationPrompt);

        String updatedPipeline = geminiHandler.generateContent(validationPrompt);

        String sanitizedJsonPipeline = updatedPipeline.replace("```json", "").replace("```", "").trim();
        log.info("Updated JSON pipeline: {}", sanitizedJsonPipeline);
        executeAggregation(prompt, sanitizedJsonPipeline, retryCount);
    }

}
