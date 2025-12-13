
package org.egov.fhirtransformer.mapping.requestBuilder;

import org.egov.common.models.core.URLParams;
import org.egov.common.models.product.*;
import org.egov.fhirtransformer.common.Constants;
import org.egov.fhirtransformer.service.ApiIntegrationService;
import org.egov.fhirtransformer.utils.BundleBuilder;
import org.egov.fhirtransformer.utils.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class InventoryItemToProductVariant {

    @Autowired
    private ApiIntegrationService apiIntegrationService;

    @Value("${product.variant.create.url}")
    private String productVariantCreateUrl;

    @Value("${product.variant.update.url}")
    private String productVariantUpdateUrl;


    public HashMap<String, Integer> transformInventoryItemToProductVariant(HashMap<String, ProductVariant> productVariantMap) throws Exception {

        HashMap<String, Integer> results = new HashMap<>();
        if (productVariantMap == null || productVariantMap.isEmpty()) {
            return results;
        }

        try{
            List<String> productVariantIds = new ArrayList<>(productVariantMap.keySet());
            HashMap<String, List<String>> newAndExistingIdsMap = checkExistingProductvariant(productVariantIds);

            //call create or update based on existing or new productVariant
            callCreateOrUpdateProductVariant(newAndExistingIdsMap, productVariantMap);
            results.put(Constants.TOTAL_PROCESSED, productVariantMap.size());
            return BundleBuilder.fetchMetrics(results, newAndExistingIdsMap);
        }
        catch (Exception e){
            throw new Exception("Error in Transforming InventoryItem To ProductVariant: " + e.getMessage());
        }
    }

    public HashMap<String,List<String>> checkExistingProductvariant
            (List<String> productVariantIds) throws Exception {

        HashMap<String,List<String>> newandexistingids = new HashMap<>();
        try{
            URLParams urlParams = apiIntegrationService.formURLParams(productVariantIds);
            ProductVariantSearch productVariantSearch = new ProductVariantSearch();
            productVariantSearch.setId(productVariantIds);

            ProductVariantSearchRequest productVariantSearchRequest = new ProductVariantSearchRequest();
            productVariantSearchRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
            productVariantSearchRequest.setProductVariant(productVariantSearch);

            ProductVariantResponse productVariantResponse = apiIntegrationService.fetchAllProductVariants(urlParams, productVariantSearchRequest);

            if (productVariantResponse.getProductVariant() == null){
                newandexistingids.put(Constants.NEW_IDS, productVariantIds);
            }
            else {
                List<String> existingIds = new ArrayList<>();
                for (ProductVariant productVariant : productVariantResponse.getProductVariant()) {
                    existingIds.add(productVariant.getId());
                }
                
                List<String> newIds = new ArrayList<>(productVariantIds);
                newandexistingids = MapUtils.splitNewAndExistingIDS(newIds, existingIds);
            }
            System.out.println(newandexistingids);
        } catch (Exception e){
            throw new Exception("Error in checkExisting productVariant: " + e.getMessage());
        }
        return newandexistingids;
    }

    public void callCreateOrUpdateProductVariant(HashMap<String,List<String>> newandexistingProductVariant, HashMap<String, ProductVariant> productVariantMap) throws Exception {
        //Create ProductVariantRequest for new ProductVariant
        try{
            List<ProductVariant> newProductVariant = new ArrayList<>();
            if (newandexistingProductVariant.containsKey(Constants.NEW_IDS)) {
                for (String id : newandexistingProductVariant.get(Constants.NEW_IDS)) {
                    newProductVariant.add(productVariantMap.get(id));
                }
                if (!newProductVariant.isEmpty()) {
                    ProductVariantRequest productVariantRequest = new ProductVariantRequest();
                    productVariantRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
                    productVariantRequest.setProductVariant(newProductVariant);
                    //Call create API
                    apiIntegrationService.sendRequestToAPI(productVariantRequest, productVariantCreateUrl);
                }
            }

            //Create ProductVariantRequest for existing ProductVariant
            List<ProductVariant> existingProductVariant = new ArrayList<>();
            //handle key not found scenario
            if (newandexistingProductVariant.containsKey(Constants.EXISTING_IDS)) {
                for (String id : newandexistingProductVariant.get(Constants.EXISTING_IDS)) {
                    existingProductVariant.add(productVariantMap.get(id));
                }
                if (!existingProductVariant.isEmpty()) {
                    ProductVariantRequest  productVariantRequest= new ProductVariantRequest();
                    productVariantRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
                    productVariantRequest.setProductVariant(existingProductVariant);
                    //Call update API
                    apiIntegrationService.sendRequestToAPI(productVariantRequest, productVariantUpdateUrl);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error in call CreateOrUpdate Product Variant: " + e.getMessage());
        }
    }
}
