package org.egov.fhirtransformer.web.controller;

import ca.uhn.fhir.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.BoundarySearchResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.facility.FacilityBulkResponse;
import org.egov.common.models.facility.FacilitySearchRequest;
import org.egov.common.models.product.ProductVariantResponse;
import org.egov.common.models.product.ProductVariantSearchRequest;
import org.egov.common.models.stock.*;
import org.egov.fhirtransformer.service.ApiIntegrationService;
import org.egov.fhirtransformer.service.FhirParseNLoadService;
import org.egov.fhirtransformer.service.FhirTransformerService;
import org.egov.fhirtransformer.repository.KafkaProducerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import digit.web.models.BoundaryRelationshipSearchCriteria;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;

@RestController
@RequestMapping("/fhir-api")
public class FhirApiController {

    @Autowired
    private FhirTransformerService ftService;

    @Autowired
    private ApiIntegrationService diService;

    @Autowired
    private KafkaProducerService kafkaService;

    @Autowired
    private FhirParseNLoadService fpService;

    private static final Logger logger = LoggerFactory.getLogger(FhirApiController.class);

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is healthy!");
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateFHIR(@RequestBody String fhirJson) throws JsonProcessingException {
        ValidationResult result = ftService.validateFHIRResource(fhirJson);
        boolean isValid = result.isSuccessful();
        return ResponseEntity.ok(
                isValid
                        ? "Valid FHIR resource"
                        : "Invalid FHIR resource. Errors: [" +
                        result.getMessages().stream()
                                .filter(msg -> msg.getSeverity() != null && msg.getSeverity().name().equalsIgnoreCase("error"))
                                .map(msg -> msg.getMessage())
                                .collect(Collectors.joining(", "))
                        + "]"
        );
    }


    @PostMapping("/fetchAllFacilities")
    public ResponseEntity<String> fetchAllFacilities(@Valid @ModelAttribute URLParams urlParams
            , @Valid @RequestBody FacilitySearchRequest request
    ) {
        FacilityBulkResponse response = diService.fetchAllFacilities(urlParams, request);
        if (response == null || response.getFacilities() == null)
            return ResponseEntity.ok("No facilities found");
        String facilities = ftService.convertFacilitiesToFHIR(response.getFacilities(), urlParams, response.getTotalCount().intValue());
        return ResponseEntity.ok(facilities);
    }

    @PostMapping("/fetchAllProductVariants")
    public ResponseEntity<String> fetchAllProductVariants(@Valid @ModelAttribute URLParams urlParams
            , @Valid @RequestBody ProductVariantSearchRequest request
    ) {
        ProductVariantResponse response = diService.fetchAllProductVariants(urlParams, request);
        if (response == null || response.getProductVariant() == null)
            return ResponseEntity.ok("No facilities found.");
        String productVariants = ftService.convertProductVariantsToFHIR(response.getProductVariant(), urlParams, 10);
        return ResponseEntity.ok(productVariants);
    }

    @PostMapping("/fetchAllStocks")
    public ResponseEntity<String> fetchAllStocks(@Valid @ModelAttribute URLParams urlParams
            , @Valid @RequestBody StockSearchRequest stockRequest) {

        StockBulkResponse response = diService.fetchAllStocks(urlParams, stockRequest);
        if (response.getStock() == null)
            return ResponseEntity.ok("No stock found");

        String stock = ftService.convertStocksToFHIR(response.getStock(),
                urlParams, response.getTotalCount().intValue());
        return ResponseEntity.ok(stock);
    }

    @PostMapping("/fetchAllStockReconciliation")
    public ResponseEntity<String> fetchAllStockReconciliation(@Valid @ModelAttribute URLParams urlParams,
                                                              @Valid @RequestBody StockReconciliationSearchRequest stockReconciliationSearchRequest) {

        StockReconciliationBulkResponse response = diService.fetchAllStockReconciliation(urlParams, stockReconciliationSearchRequest);
        if (response == null || response.getStockReconciliation() == null)
            return ResponseEntity.ok("No Stock Reconciliation found");
        String stockReconciliation = ftService.convertStocksReconciliationToFHIR(response.getStockReconciliation(),
                urlParams, response.getTotalCount().intValue());
        return ResponseEntity.ok(stockReconciliation);
    }

    @PostMapping("/fetchAllBoundaries")
    public ResponseEntity<String> fetchAllBoundaries(@Valid @ModelAttribute BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria
            , @Valid @RequestBody RequestInfo requestInfo
    ) {
        BoundarySearchResponse response = diService.fetchAllBoundaries(boundaryRelationshipSearchCriteria, requestInfo);
        String boundaries = ftService.convertBoundaryRelationshipToFHIR(response.getTenantBoundary());
        return ResponseEntity.ok(boundaries);
    }

    @PostMapping("/consumeFHIR")
    public ResponseEntity<String> consumeFHIR(@RequestBody String fhirJson) throws Exception {

        HashMap<String, HashMap<String, Integer>> response = new HashMap<>();
        try {
//           Parse incoming FHIR JSON
            JsonNode root = new ObjectMapper().readTree(fhirJson);
            String bundleId = root.path("id").asText();

            // Validate the FHIR resource
            ValidationResult result = ftService.validateFHIRResource(fhirJson);

            // If validation fails → publish to DLQ
            if (!result.isSuccessful()) {
                kafkaService.publishToDLQ(result, bundleId, root);
                return ResponseEntity
                        .badRequest()
                        .body("Invalid FHIR resource");
            }

            // If valid → parse and load FHIR resource
            response = fpService.parseAndLoadFHIRResource(fhirJson);
            return ResponseEntity.ok(response.toString());
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse FHIR JSON :", e);
            return ResponseEntity.badRequest().body("Invalid FHIR resource");
        } catch (Exception e) {
            logger.error("Unexpected error while processing FHIR resource", e);
            return ResponseEntity
                   .badRequest()
                   .body("Processing Failed");
        }
   }
}
