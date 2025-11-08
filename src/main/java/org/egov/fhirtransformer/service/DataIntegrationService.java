package org.egov.fhirtransformer.service;

import org.egov.common.models.core.URLParams;
import org.egov.common.models.facility.FacilityBulkResponse;
import org.egov.common.models.facility.FacilitySearchRequest;
import org.egov.common.models.product.ProductVariantResponse;
import org.egov.common.models.product.ProductVariantSearchRequest;
import org.egov.common.models.stock.StockBulkResponse;
import org.egov.common.models.stock.StockReconciliationBulkResponse;
import org.egov.common.models.stock.StockReconciliationSearchRequest;
import org.egov.common.models.stock.StockSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class DataIntegrationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${facility.search.url}")
    private String facilityUrl;

    @Value("${productVariant.search.url}")
    private String productVariantUrl;

    @Value("${stock.search.url}")
    private String stockSearchUrl;

    @Value("${stock.reconciliation.search.url}")
    private String stockReconciliationUrl;

    public URI formUri(URLParams urlParams, String url){

        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("limit", urlParams.getLimit())
                .queryParam("offset", urlParams.getOffset())
                .queryParam("tenantId", urlParams.getTenantId())
                .build().toUri();

        return uri;

    }
    public FacilityBulkResponse fetchAllFacilities(URLParams urlParams, FacilitySearchRequest facilitySearchRequest) {

        URI uri = formUri(urlParams,facilityUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FacilitySearchRequest> entity = new HttpEntity<>(facilitySearchRequest, headers);

        ResponseEntity<FacilityBulkResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                entity,
                FacilityBulkResponse.class
        );
        return response.getBody();
    }

    public ProductVariantResponse fetchAllProductVariants(URLParams urlParams, ProductVariantSearchRequest productVariantSearchRequest) {

        URI uri = formUri(urlParams,productVariantUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductVariantSearchRequest> entity = new HttpEntity<>(productVariantSearchRequest, headers);

        ResponseEntity<ProductVariantResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                entity,
                ProductVariantResponse.class
        );
        System.out.println("Response: " + response.getBody());
        return response.getBody();
    }

    public StockBulkResponse fetchAllStocks(URLParams urlParams, StockSearchRequest stockRequest) {

        URI uri = formUri(urlParams,stockSearchUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StockSearchRequest> entity = new HttpEntity<>(stockRequest, headers);

        ResponseEntity<StockBulkResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                entity,
                StockBulkResponse.class
        );
        return response.getBody();
    }

    public StockReconciliationBulkResponse fetchAllStockReconciliation(URLParams urlParams, StockReconciliationSearchRequest stockReconciliationSearchRequest) {

        URI uri = formUri(urlParams,stockReconciliationUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StockReconciliationSearchRequest> entity = new HttpEntity<>(stockReconciliationSearchRequest, headers);

        ResponseEntity<StockReconciliationBulkResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                entity,
                StockReconciliationBulkResponse.class
        );
        return response.getBody();
    }
}
