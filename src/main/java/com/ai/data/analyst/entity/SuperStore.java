package com.ai.data.analyst.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Document(collation = "SuperStoreDB")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperStore {
    @Id
    private Integer rowId;

    private String orderId;

    private LocalDate orderDate;

    private LocalDate shipDate;

    private String shipMode;

    private String customerId;

    private String customerName;

    private String segment;

    private String country;

    private String city;

    private String state;

    private String postalCode;

    private String region;

    private String productId;

    private String category;

    private String subCategory;

    private String productName;

    private Double sales;

    private Integer quantity;

    private Double discount;

    private Double profit;
}
