package org.egov.fhirtransformer.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ValidationResult;
import digit.web.models.EnrichedBoundary;
import digit.web.models.HierarchyRelation;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.facility.Facility;
import org.egov.common.models.product.ProductVariant;
import org.egov.common.models.stock.*;
import org.egov.fhirtransformer.common.Constants;
import org.egov.fhirtransformer.mapping.fhirBuilder.DIGITHCMFacilityMapper;
import org.egov.fhirtransformer.mapping.fhirBuilder.DIGITHCMProductVariantMapper;
import org.egov.fhirtransformer.mapping.fhirBuilder.DIGITHCMStockMapper;
import org.egov.fhirtransformer.repository.FhirTransformerRepository;
import org.egov.fhirtransformer.utils.BundleBuilder;
import org.egov.fhirtransformer.validator.CustomFHIRValidator;
import org.hl7.fhir.r5.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.fhirtransformer.mapping.fhirBuilder.DIGITHCMBoundaryMapper.buildLocationFromHierarchyRelation;

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

    private final FhirContext ctx = FhirContext.forR5();
    public ValidationResult validateFHIRResource(String fhirJson) {
        return validator.validate(fhirJson);
    }
    public List<Map<String, Object>> getFacilities(String facilityId) {
        return repository.getFacilities(facilityId);
    }


    public String convertFacilitiesToFHIR(List<Facility> facilities, URLParams urlParams, Integer totalCount) {
        List<Location> locations = facilities.stream()
                .map(DIGITHCMFacilityMapper::buildLocationFromFacility)
                .collect(Collectors.toList());

        Bundle bundle = BundleBuilder.buildBundle(locations, urlParams, totalCount, Constants.FACILITIES_API_PATH);
        return ctx.newJsonParser().encodeResourceToString(bundle);
    }

    public String convertProductVariantsToFHIR(List<ProductVariant> productVariants, URLParams urlParams, Integer totalCount) {
        List<InventoryItem> inventoryItems = productVariants.stream()
                .map(DIGITHCMProductVariantMapper::buildInventoryFromProductVariant)
                .collect(Collectors.toList());

        Bundle bundle = BundleBuilder.buildBundle(inventoryItems, urlParams, totalCount, Constants.PRODUCT_VARIANT_API_PATH);
        return ctx.newJsonParser().encodeResourceToString(bundle);
    }

    public String convertStocksToFHIR(List<Stock> stock, URLParams urlParams, Integer totalCount) {
        List<SupplyDelivery> supplyDeliveries = stock.stream()
                .map(DIGITHCMStockMapper::buildSupplyDeliveryFromStock)
                .toList();

        Bundle bundle = BundleBuilder.buildBundle(supplyDeliveries, urlParams, totalCount, Constants.STOCKS_API_PATH);
        return ctx.newJsonParser().encodeResourceToString(bundle);
    }

    public String convertStocksReconciliationToFHIR(List<StockReconciliation> stockReconciliation,
                                                    URLParams urlParams, Integer totalCount) {
        List<InventoryReport> inventoryReport = stockReconciliation.stream()
                .map(DIGITHCMStockMapper::buildInventoryReportFromStockReconciliation)
                .toList();

        Bundle bundle = BundleBuilder.buildBundle(inventoryReport, urlParams, totalCount, Constants.STOCK_RECONCILIATION_API_PATH);
        return ctx.newJsonParser().encodeResourceToString(bundle);
    }

    public String convertBoundaryRelationshipToFHIR(List<HierarchyRelation> hierarchyRelations){
        List<Location> locations = new ArrayList<>();

        for (HierarchyRelation relation : hierarchyRelations) {
            for (EnrichedBoundary boundary : relation.getBoundary()) {
                traverseBoundary(boundary, null, locations);
            }
        }

        Bundle bundle = BundleBuilder.buildBoundaryLocationBundle(locations);
        return ctx.newJsonParser().encodeResourceToString(bundle);
    }

    private void traverseBoundary(EnrichedBoundary current, Location parentLocation, List<Location> locations) {
        Location location = buildLocationFromHierarchyRelation(current, parentLocation);
        locations.add(location);

        if (current.getChildren() != null) {
            for (EnrichedBoundary child : current.getChildren()) {
                traverseBoundary(child, location, locations);
            }
        }
    }
}
