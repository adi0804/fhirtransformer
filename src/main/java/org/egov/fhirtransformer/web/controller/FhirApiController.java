package org.egov.fhirtransformer.web.controller;


import org.egov.fhirtransformer.service.FhirValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fhir-api")
public class FhirApiController {

    @Autowired
    private FhirValidatorService service;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Piku is Awesome and Adi is Stupid");
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateFHIR(@RequestBody String fhirJson) {
        boolean isValid = service.validateFHIRResource(fhirJson);
        return ResponseEntity.ok(isValid ? "Valid FHIR resource" : "Invalid FHIR resource");
    }
}
