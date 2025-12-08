package org.egov.fhirtransformer.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.stock.*;
import org.egov.fhirtransformer.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class SupplyDeliveryToStockService {

    @Autowired
    private DataIntegrationService diService;


    public HashMap<String, Integer> transformSupplyDeliveryToStock(HashMap<String, Stock> supplyDeliveryMap) throws Exception {

        HashMap<String, Integer> results = new HashMap<>();
        try{
            List<String> stockIds = new ArrayList<>(supplyDeliveryMap.keySet());
            if (!stockIds.isEmpty()) {
                HashMap<String, List<String>> newandexistingstocks = checkExistingStocks(stockIds);
                //call create or update based on existing or new stocks
                callCreateOrUpdateStocks(newandexistingstocks, supplyDeliveryMap);
                results.put(Constants.TOTAL_PROCESSED, supplyDeliveryMap.size());
                results.put(Constants.NEW_IDS,
                        newandexistingstocks.getOrDefault(Constants.NEW_IDS, Collections.emptyList()).size());
                results.put(Constants.EXISTING_IDS,
                        newandexistingstocks.getOrDefault(Constants.EXISTING_IDS, Collections.emptyList()).size());
                System.out.println(results);
            }
        }
        catch (Exception e){
            throw new Exception("Error in transformSupplyDeliveryToStock: " + e.getMessage());
        }
        return results;
    }

    public HashMap<String,List<String>> checkExistingStocks(List<String> stockIds) throws Exception {

        HashMap<String,List<String>> newandexistingids = new HashMap<>();
        try{
            URLParams urlParams = diService.formURLParams(stockIds);

            StockSearch stockSearch = new StockSearch();
            stockSearch.setId(stockIds);

            StockSearchRequest stockSearchRequest = new StockSearchRequest();
            stockSearchRequest.setRequestInfo(diService.formRequestInfo());
            stockSearchRequest.setStock(stockSearch);

            StockBulkResponse stockBulkResponse = diService.fetchAllStocks(urlParams, stockSearchRequest);

            if (stockBulkResponse.getStock() == null){
                newandexistingids.put(Constants.NEW_IDS, stockIds);
            }
            else {
                List<String> existingIds = new ArrayList<>();
                for (Stock stock : stockBulkResponse.getStock()) {
                    existingIds.add(stock.getId());
                }
                List<String> newIds = new ArrayList<>(stockIds);
                newIds.removeAll(existingIds);
                newandexistingids.put(Constants.EXISTING_IDS, existingIds);
                newandexistingids.put(Constants.NEW_IDS, newIds);
            }
            System.out.println(newandexistingids);
        } catch (Exception e){
            throw new Exception("Error in checkExistingStocks: " + e.getMessage());
        }
        return newandexistingids;
    }

    public void callCreateOrUpdateStocks(HashMap<String,List<String>> newandexistingstocks, HashMap<String, Stock> supplyDeliveryMap) throws Exception {
        //Create StockBulkRequest for new stocks
        try{
            List<Stock> newStocks = new ArrayList<>();
            if (newandexistingstocks.containsKey(Constants.NEW_IDS)) {
                for (String id : newandexistingstocks.get(Constants.NEW_IDS)) {
                    newStocks.add(supplyDeliveryMap.get(id));
                }
                if (!newStocks.isEmpty()) {
                    StockBulkRequest stockBulkRequest = new StockBulkRequest();
                    stockBulkRequest.setRequestInfo(diService.formRequestInfo());
                    stockBulkRequest.setStock(newStocks);
                    //Call create API
                    diService.createOrUpdateStocks(stockBulkRequest, true);
                }
            }

            //Create StockBulkRequest for existing stocks
            List<Stock> existingStocks = new ArrayList<>();
            //handle key not found scenario
            if (newandexistingstocks.containsKey(Constants.EXISTING_IDS)) {
                for (String id : newandexistingstocks.get(Constants.EXISTING_IDS)) {
                    existingStocks.add(supplyDeliveryMap.get(id));
                }
                if (!existingStocks.isEmpty()) {
                    StockBulkRequest stockBulkRequest = new StockBulkRequest();
                    stockBulkRequest.setRequestInfo(diService.formRequestInfo());
                    stockBulkRequest.setStock(existingStocks);
                    //Call update API
                    diService.createOrUpdateStocks(stockBulkRequest, false);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error in callCreateOrUpdateStocks: " + e.getMessage());
        }
    }
}
