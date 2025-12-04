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

    public String parseAndLoadFHIRResource(String fhirJson) {
        // Parse the FHIR resource
        IParser parser = new FhirContext().newJsonParser();
        Bundle bundle = new Bundle();
        try {
            bundle = parser.parseResource(Bundle.class, fhirJson);
        } catch (Exception e) {
            return "Failed to parse FHIR Bundle";
        }
        HashMap<String, Stock> supplyDeliveryMap = new HashMap<>();
        // Process each entry in the bundle
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof SupplyDelivery) {
                SupplyDelivery supplyDelivery = (SupplyDelivery) entry.getResource();
                Stock stock = DIGITHCMStockMapper.buildStockFromSupplyDelivery(supplyDelivery);
                supplyDeliveryMap.put(supplyDelivery.getId(), stock);
            }
        }
        List<String> stockIds = new ArrayList<>(supplyDeliveryMap.keySet());
        // Need to implement the logic to check existing stocks and save new stocks
        if (!stockIds.isEmpty()) {
            HashMap<String,List<String>> newandexistingstocks =  checkExistingStocks(stockIds);
            //call create or update based on existing or new stocks
            callCreateOrUpdateStocks(newandexistingstocks, supplyDeliveryMap);
        }

        return "Successfully parsed and loaded ";
    }

    public HashMap<String,List<String>> checkExistingStocks(List<String> stockIds) {

        HashMap<String,List<String>> newandexistingids = new HashMap<>();
        URLParams urlParams = new URLParams();
        urlParams.setLimit(stockIds.size());
        urlParams.setOffset(0);
        urlParams.setTenantId(Constants.TENANT_ID);

        //Form StockSearchRequest
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAuthToken("");

        StockSearch stockSearch = new StockSearch();
        stockSearch.setId(stockIds);

        StockSearchRequest stockSearchRequest = new StockSearchRequest();
        stockSearchRequest.setRequestInfo(requestInfo);
        stockSearchRequest.setStock(stockSearch);

        StockBulkResponse stockBulkResponse = diService.fetchAllStocks(urlParams, stockSearchRequest);

        if (stockBulkResponse.getStock() == null){
            newandexistingids.put("newIds", stockIds);
        }
        else {
            List<String> existingIds = new ArrayList<>();
            for (Stock stock : stockBulkResponse.getStock()) {
                existingIds.add(stock.getId());
            }
            List<String> newIds = new ArrayList<>(stockIds);
            newIds.removeAll(existingIds);
            newandexistingids.put("existingIds", existingIds);
            newandexistingids.put("newIds", newIds);
        }
        return newandexistingids;
    }

    public void callCreateOrUpdateStocks(HashMap<String,List<String>> newandexistingstocks, HashMap<String, Stock> supplyDeliveryMap) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAuthToken("");

        //Create StockBulkRequest for new stocks
        List<Stock> newStocks = new ArrayList<>();
        if (newandexistingstocks.containsKey("newIds")) {
            for (String id : newandexistingstocks.get("newIds")) {
                newStocks.add(supplyDeliveryMap.get(id));
            }
            if (!newStocks.isEmpty()) {
                StockBulkRequest stockBulkRequest = new StockBulkRequest();
                stockBulkRequest.setRequestInfo(requestInfo);
                stockBulkRequest.setStock(newStocks);
                //Call create API
                diService.createOrUpdateStocks(stockBulkRequest, true);
            }
        }

        //Create StockBulkRequest for existing stocks
        List<Stock> existingStocks = new ArrayList<>();
        //handle key not found scenario
        if (newandexistingstocks.containsKey("existingIds")) {
            for (String id : newandexistingstocks.get("existingIds")) {
                existingStocks.add(supplyDeliveryMap.get(id));
            }
            if (!existingStocks.isEmpty()) {
                StockBulkRequest stockBulkRequest = new StockBulkRequest();
                stockBulkRequest.setRequestInfo(requestInfo);
                stockBulkRequest.setStock(existingStocks);
                //Call update API
                diService.createOrUpdateStocks(stockBulkRequest, false);
            }
        }
    }
}
