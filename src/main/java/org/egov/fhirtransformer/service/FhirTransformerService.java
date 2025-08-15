package org.egov.fhirtransformer.service;

import ca.uhn.fhir.context.FhirContext;
import org.egov.fhirtransformer.mapper.BoundaryMapper;
import org.egov.fhirtransformer.repository.FhirTransformerRepository;
import org.egov.fhirtransformer.utils.BoundaryBundleBuilder;
import org.egov.fhirtransformer.validator.CustomFHIRValidator;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FhirTransformerService {

    @Autowired
    private CustomFHIRValidator validator;

    @Autowired
    private FhirTransformerRepository repository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final FhirContext ctx = FhirContext.forR5();

    public boolean validateFHIRResource(String fhirJson) {
        return validator.validate(fhirJson);
    }

    public List<Map<String, Object>> getFacilities(String facilityId) {
        return repository.getFacilities(facilityId);
    }

    public String getBoundaries(String afterId, String lastModifiedDate, int count) {
//        String cacheKey = "location_" + (afterId != null ? afterId : "start") + "_" + (lastModifiedDate != null ? lastModifiedDate : "all") + "_" + count;
//        String cachedJson = (String) redisTemplate.opsForValue().get(cacheKey);
//        if (cachedJson != null) {
//            return cachedJson;
//        }
        List<Map<String, Object>> rows = repository.getBoundaries(afterId, lastModifiedDate, count);
        List<Location> locations = rows.stream()
                .map(BoundaryMapper::mapToLocation)
                .collect(Collectors.toList());

        int total = repository.totalMatchingRecords(lastModifiedDate);
        Bundle bundle = BoundaryBundleBuilder.buildLocationBundle(locations, lastModifiedDate, count, afterId, total);

        String json = ctx.newJsonParser().encodeResourceToString(bundle);
//        redisTemplate.opsForValue().set(cacheKey, json);
        return json;
    }
}
