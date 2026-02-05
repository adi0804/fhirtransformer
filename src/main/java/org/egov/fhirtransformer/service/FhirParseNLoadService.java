package org.egov.fhirtransformer.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import digit.web.models.BoundaryRelation;
import org.egov.common.models.facility.Facility;
import org.egov.common.models.product.ProductVariant;
import org.egov.common.models.stock.*;
import org.egov.fhirtransformer.common.Constants;
import org.egov.fhirtransformer.mapping.fhirBuilder.DIGITHCMBoundaryMapper;
import org.egov.fhirtransformer.mapping.fhirBuilder.DIGITHCMFacilityMapper;
import org.egov.fhirtransformer.mapping.fhirBuilder.DIGITHCMProductVariantMapper;
import org.egov.fhirtransformer.mapping.fhirBuilder.DIGITHCMStockMapper;
import org.egov.fhirtransformer.mapping.requestBuilder.*;
import org.egov.fhirtransformer.repository.KafkaProducerService;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.InventoryItem;
import org.hl7.fhir.r5.model.InventoryReport;
import org.hl7.fhir.r5.model.SupplyDelivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service responsible for parsing FHIR Bundles and loading
 * corresponding DIGIT domain entities.
 */
@Service
public class FhirParseNLoadService {

    private static final Logger logger = LoggerFactory.getLogger(FhirParseNLoadService.class);

    private final FhirContext ctx = FhirContext.forR5();

    @Autowired
    private ApiIntegrationService apiIntegrationService;

    @Autowired
    private SupplyDeliveryToStockService sdToStockService;

    @Autowired
    private LocationToFacilityService locToFacilityService;

    @Autowired
    private LocationToBoundaryService locToBoundaryService;

    @Autowired
    private InventoryReportToStockReconciliationService irToStkRecService;

    @Autowired
    private InventoryItemToProductVariant invToProductService;

    @Autowired
    private KafkaProducerService kafkaService;

    /**
     * Parses a FHIR Bundle JSON and loads supported resources into DIGIT services
     * @param fhirJson FHIR Bundle payload as JSON
     * @return map of entity name to processing metrics
     * @throws Exception if downstream service invocation fails
     */
    public HashMap<String, HashMap<String, Integer>> parseAndLoadFHIRResource(String fhirJson) throws Exception {
        HashMap<String, HashMap<String, Integer>> entityResults = new HashMap<>();
        // Parse the FHIR resource
        IParser parser = ctx.newJsonParser();
        Bundle bundle = new Bundle();
        try {
            bundle = parser.parseResource(Bundle.class, fhirJson);
        } catch (Exception e) {
            logger.error("Failed to parse FHIR resource: {}", e.getMessage(), e);
            return entityResults;
        }
        HashMap<String, Stock> supplyDeliveryMap = new HashMap<>();
        HashMap<String, Facility> facilityMap = new HashMap<>();
        HashMap<String, BoundaryRelation> boundaryRelationMap = new HashMap<>();
        HashMap<String, StockReconciliation> stockReconciliationMap = new HashMap<>();
        HashMap<String, ProductVariant> productVariantMap = new HashMap<>();

        // Process each entry in the bundle
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            try {
                if (entry.getResource() instanceof SupplyDelivery) {
                    SupplyDelivery supplyDelivery = (SupplyDelivery) entry.getResource();
                    String logicalId = supplyDelivery.getIdElement().getIdPart();
                    Stock stock = DIGITHCMStockMapper.buildStockFromSupplyDelivery(supplyDelivery);
                    supplyDeliveryMap.put(logicalId, stock);
                }
                if (entry.getResource() instanceof org.hl7.fhir.r5.model.Location) {
                    org.hl7.fhir.r5.model.Location location = (org.hl7.fhir.r5.model.Location) entry.getResource();
                    List<String> profiles = location.getMeta().getProfile().stream()
                            .map(p -> p.getValue())
                            .collect(Collectors.toList());
                    String logicalId = location.getIdElement().getIdPart();
                    if (profiles.contains(Constants.PROFILE_DIGIT_HCM_FACILITY)){
                        Facility facility = DIGITHCMFacilityMapper.convertFhirLocationToFacility(location);
                        facilityMap.put(logicalId, facility);
                    }
                    else if (profiles.contains(Constants.PROFILE_DIGIT_HCM_BOUNDARY)) {
                        logicalId = location.getName();
                        BoundaryRelation boundaryRelation = DIGITHCMBoundaryMapper.convertFhirLocationToBoundaryRelation(location);
                        boundaryRelationMap.put(logicalId, boundaryRelation);
                    }
                }
                if (entry.getResource() instanceof InventoryReport inventoryReport) {
                    String logicalId = inventoryReport.getIdElement().getIdPart();
                    StockReconciliation stockRecon= DIGITHCMStockMapper.buildStockReconFromInventoryReport(inventoryReport);
                    stockReconciliationMap.put(logicalId, stockRecon);
                }
                if (entry.getResource() instanceof InventoryItem inventoryItem) {
                    String logicalId = inventoryItem.getIdElement().getIdPart();
                    ProductVariant productVariant = DIGITHCMProductVariantMapper.buildProductVariantFromInventoryItem(inventoryItem);
                    productVariantMap.put(logicalId, productVariant);

                }
            } catch (Exception e) {
                logger.error("Error processing entry: {}", e.getMessage(), e);
                logger.info("Skipping entry with resource ID: {}", entry.getResource().getIdElement().getIdPart());
                kafkaService.publishFhirResourceFailures(entry, e.getMessage());
            }
        }
        //call Create supply delivery to stock service
        logger.info("supply delivery map: {}", supplyDeliveryMap);
        HashMap<String, Integer> stockResults = sdToStockService.transformSupplyDeliveryToStock(supplyDeliveryMap);
        entityResults.put("Stock", stockResults);

        //call Create Location to facility service
        logger.info("facility map: {}", facilityMap);
        HashMap<String, Integer> facilityResults = locToFacilityService.transformLocationToFacility(facilityMap);
        entityResults.put("facility", facilityResults);

        //call Create boundary relation service
        logger.info("boundary relation map: {}", boundaryRelationMap);
        HashMap<String, Integer> boundaryResults = locToBoundaryService.transformLocationToBoundary(boundaryRelationMap);
        entityResults.put("boundary", boundaryResults);

        //call Create Inventory Report to stockrecon service
        logger.info("Stock Reconciliation map: {}", stockReconciliationMap);
        HashMap<String, Integer> stockReconResults = irToStkRecService.transformInventoryReportToStockReconciliation(stockReconciliationMap);
        entityResults.put("Stock", stockResults);

        //call Create Inventory Item to Product Variant service
        logger.info("Product Variant map: {}", productVariantMap);
        HashMap<String, Integer> productVariantResults = invToProductService.transformInventoryItemToProductVariant(productVariantMap);
        entityResults.put("Product Variant", productVariantResults);

        return entityResults;
    }
}
