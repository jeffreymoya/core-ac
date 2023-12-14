package com.synpulse8.pulse8.core.accesscontrolsvc.repository;

import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyMetaData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface PolicyDefinitionRepository extends MongoRepository<PolicyMetaData, String> {
    @Query("{ 'name' : ?0 }")
    Optional<PolicyMetaData> findByName(String policyName);
}