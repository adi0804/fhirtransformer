package org.egov.fhirtransformer.repository;

import org.egov.fhirtransformer.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
public class FhirTransformerRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @Value("${get.facilities}")
    private String getFacilitiesQuery;

    @Value("${get.boundaries}")
    private String getBoundariesQuery;

    @Value("${get.totalMatchingRecords}")
    private String getTotalMatchingRecordsQuery;

    public List<Map<String, Object>> getFacilities(String facilityId) {
        String cacheKey = Constants.FACILITY_CACHE_KEY_PREFIX + facilityId;

        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            System.out.println("Fetching from Redis Cache...");
            return cached;
        }

        List<Map<String, Object>> facility =  jdbcTemplate.queryForList(getFacilitiesQuery, facilityId);
        facility = convertPgObjectToString(facility);
        System.out.println("Facilities fetched: " + facility);
        redisTemplate.opsForValue().set(cacheKey, facility, Constants.CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        System.out.println("Data stored in Redis Cache with key: " + cacheKey);
        return facility;
    }

    public List<Map<String, Object>> getLocation(String afterId, String lastModifiedDate, int count) {
        StringBuilder sql = new StringBuilder(getBoundariesQuery);
        Map<String, Object> params = new HashMap<>();

        if (afterId != null && !afterId.isEmpty()) {
            sql.append(" AND id > :afterId");
            params.put("afterId", afterId);
        }
        if (lastModifiedDate != null && !lastModifiedDate.isEmpty()) {
            sql.append(" AND cast(TO_TIMESTAMP(lastmodifiedtime/1000) as date) >= cast(:lastModifiedDate as date)");
            params.put("lastModifiedDate", lastModifiedDate);
        }
        sql.append(" ORDER BY id ASC LIMIT :count");
        params.put("count", count);
        List<Map<String, Object>> boundaries = namedParameterJdbcTemplate.queryForList(sql.toString(), params);
        boundaries = convertPgObjectToString(boundaries);
//        System.out.println("Boundaries fetched: " + boundaries);
        return boundaries;
    }

    public int totalMatchingRecords(String afterId, String lastModifiedDate) {
        StringBuilder sql = new StringBuilder(getTotalMatchingRecordsQuery);
        Map<String, Object> params = new HashMap<>();

        if (afterId != null && !afterId.isEmpty()) {
            sql.append(" AND id > :afterId");
            params.put("afterId", afterId);
        }
        if (lastModifiedDate != null && !lastModifiedDate.isEmpty()) {
            sql.append(" AND cast(TO_TIMESTAMP(lastmodifiedtime/1000) as date) >= cast(:lastModifiedDate as date)");
            params.put("lastModifiedDate", lastModifiedDate);
        }
        return namedParameterJdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
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
