package org.egov.fhirtransformer.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.postgresql.util.PGobject;

@Repository
public class FhirTransformerRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${get.facilities}")
    private String getFacilitiesQuery;

    public List<Map<String, Object>> getFacilities(String facilityId) {
        String cacheKey = "facility_" + facilityId;

        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            System.out.println("Fetching from Redis Cache...");
            return cached;
        }

        List<Map<String, Object>> facility =  jdbcTemplate.queryForList(getFacilitiesQuery, facilityId);
        facility = convertPgObjectToString(facility);
        System.out.println("Facilities fetched: " + facility);
        redisTemplate.opsForValue().set(cacheKey, facility, 1, TimeUnit.MINUTES);
        System.out.println("Data stored in Redis Cache with key: " + cacheKey);
        return facility;
    }



    private List<Map<String, Object>> convertPgObjectToString(List<Map<String, Object>> data) {
        for (Map<String, Object> row : data) {
            row.replaceAll((key, value) -> {
                if (value instanceof org.postgresql.util.PGobject pgObject) {
                    return pgObject.getValue(); // Convert PGobject to its string value
                }
                return value;
            });
        }
        return data;
    }
}
