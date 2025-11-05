package org.egov.fhirtransformer.service;

import ca.uhn.fhir.context.FhirContext;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.facility.Facility;
import org.egov.common.models.facility.FacilityBulkResponse;
import org.egov.common.models.facility.FacilitySearchRequest;
import org.egov.fhirtransformer.fhirBuilder.DIGITHCMBoundaryMapper;
import org.egov.fhirtransformer.fhirBuilder.DIGITHCMFacilityMapper;
import org.egov.fhirtransformer.repository.FhirTransformerRepository;
import org.egov.fhirtransformer.utils.BoundaryBundleBuilder;
import org.egov.fhirtransformer.utils.FacilityBundleBuilder;
import org.egov.fhirtransformer.validator.CustomFHIRValidator;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FhirTransformerService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CustomFHIRValidator validator;

    @Autowired
    private FhirTransformerRepository repository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${facility.search.url}")
    private String facilityUrl;

    private final FhirContext ctx = FhirContext.forR5();

    public boolean validateFHIRResource(String fhirJson) {
        return validator.validate(fhirJson);
    }

    public List<Map<String, Object>> getFacilities(String facilityId) {
        return repository.getFacilities(facilityId);
    }


    public String convertFacilitiesToFHIR(List<Facility> facilities, URLParams urlParams, Integer totalCount) {
        List<Location> locations = facilities.stream()
                .map(DIGITHCMFacilityMapper::buildLocationFromFacility)
                .collect(Collectors.toList());

        Bundle bundle = FacilityBundleBuilder.buildFacilityLocationBundle(locations, urlParams, totalCount);

        String json = ctx.newJsonParser().encodeResourceToString(bundle);
        return json;
    }

    public FacilityBulkResponse fetchAllFacilities(URLParams urlParams, FacilitySearchRequest facilitySearchRequest) {
        URI uri = UriComponentsBuilder.fromHttpUrl(facilityUrl)
                .queryParam("limit", urlParams.getLimit())
                .queryParam("offset", urlParams.getOffset())
                .queryParam("tenantId", urlParams.getTenantId())
                .build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FacilitySearchRequest> entity = new HttpEntity<>(facilitySearchRequest, headers);

        ResponseEntity<FacilityBulkResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                entity,
                FacilityBulkResponse.class
        );
        return response.getBody();
    }

    public String getBoundaries(String afterId, String lastModifiedDate, int count) {
        List<Map<String, Object>> rows = repository.getLocation(afterId, lastModifiedDate, count);
        List<Location> locations = rows.stream()
                .map(DIGITHCMBoundaryMapper::buildLocation)
                .collect(Collectors.toList());

        int total = repository.totalMatchingRecords(afterId, lastModifiedDate);
        Bundle bundle = BoundaryBundleBuilder.buildLocationBundle(locations, lastModifiedDate, count, afterId, total);

        String json = ctx.newJsonParser().encodeResourceToString(bundle);
        return json;
    }

}
