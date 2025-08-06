package com.ai.data.analyst.repository;

import com.ai.data.analyst.entity.SuperStore;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataAnalystRepository  extends MongoRepository<SuperStore , Integer> {
}
