
package org.egov.fhirtransformer.mapping.requestBuilder;

import org.egov.common.models.core.URLParams;
import org.egov.common.models.stock.*;
import org.egov.fhirtransformer.common.Constants;
import org.egov.fhirtransformer.service.ApiIntegrationService;
import org.egov.fhirtransformer.utils.BundleBuilder;
import org.egov.fhirtransformer.utils.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class InventoryReportToStockReconciliationService {

    @Autowired
    private ApiIntegrationService apiIntegrationService;

    @Value("${stock.recon.create.url}")
    private String stockReconCreateUrl;

    @Value("${stock.recon.update.url}")
    private String stockReconUpdateUrl;


    public HashMap<String, Integer> transformInventoryReportToStockReconciliation(HashMap<String, StockReconciliation> stockReconciliationMap) throws Exception {

        HashMap<String, Integer> results = new HashMap<>();
        try{
            List<String> stockReconIds = new ArrayList<>(stockReconciliationMap.keySet());
            if (!stockReconIds.isEmpty()) {
                HashMap<String, List<String>> newAndExistingIdsMap = checkExistingStockRecon(stockReconIds);
                //call create or update based on existing or new StockReconciliation
                callCreateOrUpdateStockReconciliation(newAndExistingIdsMap, stockReconciliationMap);
                results.put(Constants.TOTAL_PROCESSED, stockReconciliationMap.size());
                results = BundleBuilder.fetchMetrics(results, newAndExistingIdsMap);
            }
        }
        catch (Exception e){
            throw new Exception("Error in transformSupplyDeliveryToStock: " + e.getMessage());
        }
        return results;
    }

    public HashMap<String,List<String>> checkExistingStockRecon(List<String> stockReconIds) throws Exception {

        HashMap<String,List<String>> newandexistingids = new HashMap<>();
        try{
            URLParams urlParams = apiIntegrationService.formURLParams(stockReconIds);
            StockReconciliationSearch stockReconciliationSearch = new StockReconciliationSearch();
            stockReconciliationSearch.setId(stockReconIds);

            StockReconciliationSearchRequest stockReconciliationSearchRequest = new StockReconciliationSearchRequest();
            stockReconciliationSearchRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
            stockReconciliationSearchRequest.setStockReconciliation(stockReconciliationSearch);

            StockReconciliationBulkResponse stockBulkReconResponse = apiIntegrationService.fetchAllStockReconciliation(urlParams, stockReconciliationSearchRequest);
            System.out.println(stockBulkReconResponse);
            if (stockBulkReconResponse.getStockReconciliation() == null ||
                    stockBulkReconResponse.getStockReconciliation().isEmpty()){
                newandexistingids.put(Constants.NEW_IDS, stockReconIds);
            }
            else {
                List<String> existingIds = new ArrayList<>();
                for (StockReconciliation stockRecon : stockBulkReconResponse.getStockReconciliation()) {
                    existingIds.add(stockRecon.getId());
                }
                List<String> newIds = new ArrayList<>(stockReconIds);
                newandexistingids = MapUtils.splitNewAndExistingIDS(newIds, existingIds);

            }
            System.out.println(newandexistingids);
        } catch (Exception e){
            throw new Exception("Error in checkExisting StockRecon: " + e.getMessage());
        }
        return newandexistingids;
    }

    public void callCreateOrUpdateStockReconciliation(HashMap<String,List<String>> newandexistingStockReconciliation, HashMap<String, StockReconciliation> stockReconciliationHashMap) throws Exception {
        //Create StockReconciliationBulkRequest for new StockReconciliation
        try{
            List<StockReconciliation> newStockRecon = new ArrayList<>();
            if (newandexistingStockReconciliation.containsKey(Constants.NEW_IDS)) {
                for (String id : newandexistingStockReconciliation.get(Constants.NEW_IDS)) {
                    newStockRecon.add(stockReconciliationHashMap.get(id));
                }
                if (!newStockRecon.isEmpty()) {
                    StockReconciliationBulkRequest stockReconciliationBulkRequest = new StockReconciliationBulkRequest();
                    stockReconciliationBulkRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
                    stockReconciliationBulkRequest.setStockReconciliation(newStockRecon);
                    //Call create API
                    apiIntegrationService.sendRequestToAPI(stockReconciliationBulkRequest, stockReconCreateUrl);
                }
            }

            //Create StockBulkRequest for existing StockReconciliation
            List<StockReconciliation> existingStockReconciliation = new ArrayList<>();
            //handle key not found scenario
            if (newandexistingStockReconciliation.containsKey(Constants.EXISTING_IDS)) {
                for (String id : newandexistingStockReconciliation.get(Constants.EXISTING_IDS)) {
                    existingStockReconciliation.add(stockReconciliationHashMap.get(id));
                }
                if (!existingStockReconciliation.isEmpty()) {
                    StockReconciliationBulkRequest  stockReconciliationBulkRequest= new StockReconciliationBulkRequest();
                    stockReconciliationBulkRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
                    stockReconciliationBulkRequest.setStockReconciliation(existingStockReconciliation);
                    //Call update API
                    apiIntegrationService.sendRequestToAPI(stockReconciliationBulkRequest, stockReconUpdateUrl);

                }
            }
        } catch (Exception e) {
            throw new Exception("Error in call CreateOrUpdate Stock Reconcilation: " + e.getMessage());
        }
    }
}
