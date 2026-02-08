package org.egov.fhirtransformer.mapping.requestBuilder;

import org.egov.common.models.core.URLParams;
import org.egov.common.models.stock.*;
import org.egov.fhirtransformer.common.Constants;
import org.egov.fhirtransformer.service.ApiIntegrationService;
import org.egov.fhirtransformer.utils.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Service responsible for transforming FHIR SupplyDelivery–derived
 * {@link Stock} data into DIGIT Stock service requests.
 */
@Service
public class SupplyDeliveryToStockService {

    @Autowired
    private ApiIntegrationService apiIntegrationService;

    @Autowired
    private GenericCreateOrUpdateService genericCreateOrUpdateService;

    @Value("${stock.create.url}")
    private String stockCreateUrl;

    @Value("${stock.update.url}")
    private String stockUpdateUrl;

    /**
     * Transforms and persists Stock records derived from SupplyDelivery resources.
     * @param supplyDeliveryMap map of Stock ID to Stock data;
     *                          may be empty but not {@code null}
     * @return map containing processing metrics
     * @throws Exception if transformation or API invocation fails
     */
    public HashMap<String, Integer> transformSupplyDeliveryToStock(HashMap<String, Stock> supplyDeliveryMap) throws Exception {

        return genericCreateOrUpdateService.process(supplyDeliveryMap,
                this::fetchExistingStockIds,
                this::createStocks,
                this::updateStocks,
                stockCreateUrl,
                stockUpdateUrl,
                "Error in transformSupplyDeliveryToStock");
    }

    // Adapter: fetch existing stock ids
    public List<String> fetchExistingStockIds(List<String> stockIds) throws Exception {
        try{
            URLParams urlParams = apiIntegrationService.formURLParams(stockIds);

            StockSearch stockSearch = new StockSearch();
            stockSearch.setId(stockIds);

            StockSearchRequest stockSearchRequest = new StockSearchRequest();
            stockSearchRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
            stockSearchRequest.setStock(stockSearch);

            StockBulkResponse stockBulkResponse = apiIntegrationService.fetchAllStocks(urlParams, stockSearchRequest);

            if (stockBulkResponse.getStock() == null){
                return new ArrayList<>();
            }
            List<String> existingIds = new ArrayList<>();
            for (Stock stock : stockBulkResponse.getStock()) {
                existingIds.add(stock.getId());
            }
            return existingIds;
        } catch (Exception e){
            throw new Exception("Error in fetchExistingStocks: " + e.getMessage());
        }
    }

    // Adapter: create stocks
    public void createStocks(List<Stock> toCreate, String createUrl) throws Exception {
        try{
            if (toCreate == null || toCreate.isEmpty()) return;
            StockBulkRequest stockBulkRequest = new StockBulkRequest();
            stockBulkRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
            stockBulkRequest.setStock(toCreate);
            apiIntegrationService.sendRequestToAPI(stockBulkRequest, createUrl);
        } catch (Exception e) {
            throw new Exception("Error in createStocks: " + e.getMessage());
        }
    }

    // Adapter: update stocks
    public void updateStocks(List<Stock> toUpdate, String updateUrl) throws Exception {
        try{
            if (toUpdate == null || toUpdate.isEmpty()) return;
            StockBulkRequest stockBulkRequest = new StockBulkRequest();
            stockBulkRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
            stockBulkRequest.setStock(toUpdate);
            apiIntegrationService.sendRequestToAPI(stockBulkRequest, updateUrl);
        } catch (Exception e) {
            throw new Exception("Error in updateStocks: " + e.getMessage());
        }
    }
}
