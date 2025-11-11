package org.egov.fhirtransformer.service;

import digit.web.models.BoundaryRelationshipSearchCriteria;
import digit.web.models.BoundarySearchResponse;
import jakarta.validation.Valid;
import org.egov.common.contract.request.RequestInfo;
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
import org.springframework.web.bind.annotation.RequestBody;
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

    @Value("${boundary.relationship.search.url}")
    private String boundaryRelationshipUrl;

    public URI formUri(URLParams urlParams, String url){

        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("limit", urlParams.getLimit())
                .queryParam("offset", urlParams.getOffset())
                .queryParam("tenantId", urlParams.getTenantId())
                .build().toUri();

        return uri;

    }

    public URI formBoundaryUri(BoundaryRelationshipSearchCriteria criteria, String url) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        if (criteria.getTenantId() != null) {
            builder.queryParam("tenantId", criteria.getTenantId());
        }
        if (criteria.getBoundaryType() != null) {
            builder.queryParam("boundaryType", criteria.getBoundaryType());
        }
        if (criteria.getHierarchyType() != null) {
            builder.queryParam("hierarchyType", criteria.getHierarchyType());
        }
        if (criteria.getIncludeChildren() != null) {
            builder.queryParam("includeChildren", criteria.getIncludeChildren());
        }
        if (criteria.getIncludeParents() != null) {
            builder.queryParam("includeParents", criteria.getIncludeParents());
        }
        if (criteria.getCodes() != null && !criteria.getCodes().isEmpty()) {
            builder.queryParam("codes", criteria.getCodes());
        }

        return builder.build().toUri();
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

    public BoundarySearchResponse fetchAllBoundaries( BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria,RequestInfo requestInfo) {
        URI uri = formBoundaryUri(boundaryRelationshipSearchCriteria, boundaryRelationshipUrl);
        System.out.println(uri);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestInfo> entity = new HttpEntity<>(requestInfo, headers);
        ResponseEntity<BoundarySearchResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                entity,
                BoundarySearchResponse.class
        );
        return response.getBody();
    }
}
