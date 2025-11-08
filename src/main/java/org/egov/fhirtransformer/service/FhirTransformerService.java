package org.egov.fhirtransformer.service;

import ca.uhn.fhir.context.FhirContext;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.facility.Facility;
import org.egov.common.models.product.ProductVariant;
import org.egov.common.models.stock.*;
import org.egov.fhirtransformer.fhirBuilder.DIGITHCMBoundaryMapper;
import org.egov.fhirtransformer.fhirBuilder.DIGITHCMFacilityMapper;
import org.egov.fhirtransformer.fhirBuilder.DIGITHCMProductVariantMapper;
import org.egov.fhirtransformer.fhirBuilder.DIGITHCMStockMapper;
import org.egov.fhirtransformer.repository.FhirTransformerRepository;
import org.egov.fhirtransformer.utils.BoundaryBundleBuilder;
import org.egov.fhirtransformer.utils.BundleBuilder;
import org.egov.fhirtransformer.validator.CustomFHIRValidator;
import org.hl7.fhir.r5.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

        Bundle bundle = BundleBuilder.buildFacilityLocationBundle(locations, urlParams, totalCount);
        return ctx.newJsonParser().encodeResourceToString(bundle);
    }



    public String convertProductVariantsToFHIR(List<ProductVariant> productVariants, URLParams urlParams, Integer totalCount) {
        List<InventoryItem> inventoryItems = productVariants.stream()
                .map(DIGITHCMProductVariantMapper::buildInventoryFromProductVariant)
                .collect(Collectors.toList());

        Bundle bundle = BundleBuilder.buildInventoryItemBundle(inventoryItems, urlParams, totalCount);

        return ctx.newJsonParser().encodeResourceToString(bundle);
    }

    public String getBoundaries(String afterId, String lastModifiedDate, int count) {
        List<Map<String, Object>> rows = repository.getLocation(afterId, lastModifiedDate, count);
        List<Location> locations = rows.stream()
                .map(DIGITHCMBoundaryMapper::buildLocation)
                .collect(Collectors.toList());

        int total = repository.totalMatchingRecords(afterId, lastModifiedDate);
        Bundle bundle = BoundaryBundleBuilder.buildLocationBundle(locations, lastModifiedDate, count, afterId, total);

        return ctx.newJsonParser().encodeResourceToString(bundle);
    }

    public String convertStocksToFHIR(List<Stock> stock, URLParams urlParams, Integer totalCount) {
        List<SupplyDelivery> supplyDeliveries = stock.stream()
                .map(DIGITHCMStockMapper::buildSupplyDeliveryFromStock)
                .toList();

        Bundle bundle = BundleBuilder.buildSupplyDeliveryBundle(supplyDeliveries, urlParams, totalCount);

        return ctx.newJsonParser().encodeResourceToString(bundle);
    }

    public String convertStocksReconciliationToFHIR(List<StockReconciliation> stockReconciliation,
                                                    URLParams urlParams, Integer totalCount) {
        List<InventoryReport> inventoryReport = stockReconciliation.stream()
                .map(DIGITHCMStockMapper::buildInventoryReportFromStockReconciliation)
                .toList();

        Bundle bundle = BundleBuilder.buildInventoryReportBundle(inventoryReport, urlParams, totalCount);

        return ctx.newJsonParser().encodeResourceToString(bundle);
    }



}
