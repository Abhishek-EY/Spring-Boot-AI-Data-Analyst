package com.ai.data.analyst.service;

import lombok.Getter;

@Getter
public enum PromptConstants {
    ANALYSIS_PROMPT("""
            You are a data analyst working with a MongoDB collection named `superstore`. This collection contains the following fields:
            
            - rowId: Integer
            - orderId: String
            - orderDate: Date (Date, stored as BSON Date object, not string, e.g., ISODate("2016-08-10T18:30:00.000Z")) (formats: "yyyy-MM-dd")
            - shipDate: Date (Date, stored as BSON Date object, not string, e.g., ISODate("2016-08-10T18:30:00.000Z")) (formats: "yyyy-MM-dd")
            - shipMode: String (e.g., First Class, Second Class)
            - customerId: String
            - customerName: String
            - segment: String (e.g., Consumer, Corporate)
            - country: String
            - city: String
            - state: String
            - postalCode: String
            - region: String
            - productId: String
            - category: String (e.g., Furniture, Office Supplies)
            - subCategory: String
            - productName: String
            - sales: Double
            - quantity: Integer
            - discount: Double (e.g., 0.1 for 10%%)
            - profit: Double
            
            Return all **MongoDB aggregation pipeline** required for to below analysis, as an array of JSON objects, for example:
            [
              { "$match": { ... } },
              { "$group": { ... } },
              { "$sort": { ... } }
            ]
            1. Executive Summary (2–3 sentences)
                Clear, direct answer to the question without any disclaimers.
            
            2. Key Metrics (table or bullet points)
                Metric Value
            
            3. Insight & Interpretation
                Short paragraph explaining what the metrics mean in business terms.
            
            4. Recommendations
                Action 1
                Action 2
            Do not include any explanations.
            
            Assume the collection name is `superstore`.
            
            Question: %s
            """),
    DATA_ANALYSIS_PROMPT("""
            You are a data analyst, provide a explanation for the following analyst question and data:
            Question: %s
            Data: %s
            format of response should look like this:
            
            1. Executive Summary (2–3 sentences)
                Clear, direct answer to the question without any disclaimers.
            
            2. Key Metrics (table or bullet points)
                Metric Value
            
            3. Insight & Interpretation
                Short paragraph explaining what the metrics mean in business terms.
            
            4. Recommendations
                Action 1
                Action 2
            """),
    REPORT_PROMPT("""
            You previously generated a MongoDB aggregation: %s
            
            pipeline for the following question: %s
            
            The pipeline failed with the following error: %s
            
            Here are the details of the MongoDB collection:
            - Name: superstore
            - Fields:
            - rowId: Integer
            - orderId: String
            - orderDate: Date (Date, stored as BSON Date object, not string, e.g., ISODate("2016-08-10T18:30:00.000Z")) (formats: "yyyy-MM-dd")
            - shipDate: Date (Date, stored as BSON Date object, not string, e.g., ISODate("2016-08-10T18:30:00.000Z")) (formats: "yyyy-MM-dd")
            - shipMode: String (e.g., First Class, Second Class)
            - customerId: String
            - customerName: String
            - segment: String (e.g., Consumer, Corporate)
            - country: String
            - city: String
            - state: String
            - postalCode: String
            - region: String
            - productId: String
            - category: String (e.g., Furniture, Office Supplies)
            - subCategory: String
            - productName: String
            - sales: Double
            - quantity: Integer
            - discount: Double (e.g., 0.1 for 10%%)
            - profit: Double
            
              Return all **MongoDB aggregation pipeline** required for to below analysis, as an array of JSON objects, for example:
            [
              { "$match": { ... } },
              { "$group": { ... } },
              { "$sort": { ... } }
            ]
            
            Do not include any explanations.
            
            Assume the collection name is `superstore`.
            """);

    private final String prompt;

    PromptConstants(String prompt) {
        this.prompt = prompt;
    }
}
