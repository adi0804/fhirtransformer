package org.egov.fhirtransformer.service;

import org.egov.fhirtransformer.validator.CustomFHIRValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FhirValidatorService {

    @Autowired
    private CustomFHIRValidator validator;

    public boolean validateFHIRResource(String fhirJson) {
        return validator.validate(fhirJson);
    }

}
