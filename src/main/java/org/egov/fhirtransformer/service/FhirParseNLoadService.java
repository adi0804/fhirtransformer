package org.egov.fhirtransformer.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import io.netty.util.Constant;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.stock.*;
import org.egov.fhirtransformer.common.Constants;
import org.egov.fhirtransformer.fhirBuilder.DIGITHCMStockMapper;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.SupplyDelivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class FhirParseNLoadService {

    private final FhirContext ctx = FhirContext.forR5();

    @Autowired
    private DataIntegrationService diService;

    @Autowired
    private SupplyDeliveryToStockService sdToStockService;

    public HashMap<String, HashMap<String, Integer>> parseAndLoadFHIRResource(String fhirJson) {
        HashMap<String, HashMap<String, Integer>> entityResults = new HashMap<>();
        // Parse the FHIR resource

        IParser parser = ctx.newJsonParser();
        Bundle bundle = new Bundle();
        try {
            bundle = parser.parseResource(Bundle.class, fhirJson);
        } catch (Exception e) {
            return entityResults;
        }
        HashMap<String, Stock> supplyDeliveryMap = new HashMap<>();
        // Process each entry in the bundle
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof SupplyDelivery) {
                SupplyDelivery supplyDelivery = (SupplyDelivery) entry.getResource();
                String logicalId = supplyDelivery.getIdElement().getIdPart();
                Stock stock = DIGITHCMStockMapper.buildStockFromSupplyDelivery(supplyDelivery);
                supplyDeliveryMap.put(logicalId, stock);
            }
        }
        System.out.println("supply delivery map size: " + supplyDeliveryMap);
        //call Create supply delivery to stock service
        HashMap<String, Integer> results = sdToStockService.transformSupplyDeliveryToStock(supplyDeliveryMap);
        entityResults.put("Stock", results);

        return entityResults;
    }


}
