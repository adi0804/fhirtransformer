package org.egov.fhirtransformer.web.controller;


import org.egov.common.models.core.URLParams;
import org.egov.common.models.facility.FacilityBulkResponse;
import org.egov.common.models.facility.FacilitySearchRequest;
import org.egov.common.models.product.ProductVariant;
import org.egov.common.models.product.ProductVariantResponse;
import org.egov.common.models.product.ProductVariantSearchRequest;
import org.egov.common.models.stock.*;
import org.egov.fhirtransformer.service.DataIntegrationService;
import org.egov.fhirtransformer.service.FhirTransformerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.egov.fhirtransformer.common.Constants;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fhir-api")
public class FhirApiController {

    @Autowired
    private FhirTransformerService ftService;

    @Autowired
    private DataIntegrationService diService;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Piku is Awesome and Adi is Stupid");
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateFHIR(@RequestBody String fhirJson) {
        boolean isValid = ftService.validateFHIRResource(fhirJson);
        return ResponseEntity.ok(isValid ? "Valid FHIR resource" : "Invalid FHIR resource");
    }

//    @GetMapping("/getFacilities")
//    public List<Map<String, Object>> getFacilities(@RequestParam String facilityId) {
//        return ftService.getFacilities(facilityId);
//    }

    @PostMapping("/fetchAllFacilities")
    public ResponseEntity<String> fetchAllFacilities(@Valid @ModelAttribute URLParams urlParams
                                                    ,@Valid @RequestBody FacilitySearchRequest request
                                                    ) {
        FacilityBulkResponse response = diService.fetchAllFacilities(urlParams, request);
        if (response == null || response.getFacilities() == null) return ResponseEntity.ok("No facilities found.");
        String facilities = ftService.convertFacilitiesToFHIR(response.getFacilities(), urlParams, response.getTotalCount().intValue());
        return ResponseEntity.ok(facilities);
    }

    @PostMapping("/fetchAllProductVariants")
    public  ResponseEntity<String> fetchAllProductVariants(@Valid @ModelAttribute URLParams urlParams
                                                           ,@Valid @RequestBody ProductVariantSearchRequest request
                                                          ) {
        ProductVariantResponse response = diService.fetchAllProductVariants(urlParams, request);
        if (response == null || response.getProductVariant() == null) return ResponseEntity.ok("No facilities found.");
//        String productVariants = service.convertFacilitiesToFHIR(response.getProductVariant(), urlParams, response.getTotalCount().intValue());
        String productVariants = ftService.convertProductVariantsToFHIR(response.getProductVariant(), urlParams, 10);
        return ResponseEntity.ok(productVariants);
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
        String boundaries = ftService.getBoundaries(afterId, lastModifiedDate, count);
        return ResponseEntity.ok(boundaries);
    }

    @PostMapping("/fetchAllStocks")
    public ResponseEntity<String> fetchAllStocks(@Valid @ModelAttribute URLParams urlParams
            ,@Valid @RequestBody StockSearchRequest stockRequest) {

        StockBulkResponse response = diService.fetchAllStocks(urlParams, stockRequest);
        System.out.println(response.getStock());
        if (response == null || response.getStock() == null)
            return ResponseEntity.ok("No Stock found..!");
        String stock = ftService.convertStocksToFHIR(response.getStock(),
                urlParams, response.getTotalCount().intValue());
        return ResponseEntity.ok(stock);
    }


    @PostMapping("/fetchAllStockReconciliation")
    public ResponseEntity<String> fetchAllStockReconciliation(@Valid @ModelAttribute URLParams urlParams,
                                                 @Valid @RequestBody StockReconciliationSearchRequest stockReconciliationSearchRequest) {

        StockReconciliationBulkResponse response = diService.fetchAllStockReconciliation(urlParams, stockReconciliationSearchRequest);
        System.out.println(response.getStockReconciliation());
        if (response == null || response.getStockReconciliation() == null)
            return ResponseEntity.ok("No Stock Reconciliation found..!");
        String stockReconciliation = ftService.convertStocksReconciliationToFHIR(response.getStockReconciliation(),
                urlParams, response.getTotalCount().intValue());
        return ResponseEntity.ok(stockReconciliation);
    }

}
