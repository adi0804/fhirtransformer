package org.egov.fhirtransformer.service;

import org.egov.fhirtransformer.repository.FhirTransformerRepository;
import org.egov.fhirtransformer.validator.CustomFHIRValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FhirTransformerService {

    @Autowired
    private CustomFHIRValidator validator;

    @Autowired
    private FhirTransformerRepository repository;

    public boolean validateFHIRResource(String fhirJson) {
        return validator.validate(fhirJson);
    }

    public List<Map<String, Object>> getFacilities(String facilityId) {
        return repository.getFacilities(facilityId);
    }
}
