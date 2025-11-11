package org.egov.fhirtransformer.web.controller;


import digit.web.models.BoundarySearchResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.facility.FacilityBulkResponse;
import org.egov.common.models.facility.FacilitySearchRequest;
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
import digit.web.models.BoundaryRelationshipSearchCriteria;


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

    @PostMapping("/fetchAllBoundaries")
    public ResponseEntity<String> fetchAllBoundaries(@Valid @ModelAttribute BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria
                                                     ,@Valid @RequestBody RequestInfo requestInfo
                                                    ) {
        BoundarySearchResponse response = diService.fetchAllBoundaries(boundaryRelationshipSearchCriteria, requestInfo);
        System.out.println(response);
        String boundaries = ftService.convertBoundaryRelationshipToFHIR(response.getTenantBoundary());
        return ResponseEntity.ok(boundaries);
    }

}
