package org.egov.fhirtransformer.web.controller;


import org.egov.fhirtransformer.service.FhirTransformerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.egov.fhirtransformer.common.Constants;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fhir-api")
public class FhirApiController {

    @Autowired
    private FhirTransformerService service;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Piku is Awesome and Adi is Stupid");
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateFHIR(@RequestBody String fhirJson) {
        boolean isValid = service.validateFHIRResource(fhirJson);
        return ResponseEntity.ok(isValid ? "Valid FHIR resource" : "Invalid FHIR resource");
    }

    @GetMapping("/getFacilities")
    public List<Map<String, Object>> getFacilities(@RequestParam String facilityId) {
        return service.getFacilities(facilityId);
    }

    @GetMapping("/getLocations")
    public ResponseEntity<String> getBoundaries(@RequestParam(name = "_profile", required = true) String profile,
                                                @RequestParam(name = "_afterId", required = false) String afterId,
                                                @RequestParam(name = "_lastmodifiedtime", required = false) String lastModifiedStr,
                                                @RequestParam(name = "_count", defaultValue = "10") int count) {

        if (profile == null || profile.isEmpty() || (!profile.equalsIgnoreCase(Constants.PARAM_BOUNDARY_LOCATION) && !profile.equalsIgnoreCase(Constants.PARAM_FACILITYBOUNDARY_LOCATION))) {
            return ResponseEntity.badRequest().body("Invalid or missing _profile parameter");
        }

        String lastModifiedDate = null;
        if (lastModifiedStr != null && !lastModifiedStr.isEmpty()) {
            if (lastModifiedStr.startsWith("gt")) {
                lastModifiedStr = lastModifiedStr.substring(2);
            }
            lastModifiedDate = lastModifiedStr;
        }
        String boundaries = service.getBoundaries(afterId, lastModifiedDate, count);
        return ResponseEntity.ok(boundaries);
    }

}
