package org.egov.fhirtransformer.utils;


import org.egov.common.models.core.URLParams;
import org.egov.fhirtransformer.common.Constants;
import org.hl7.fhir.r5.model.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BundleBuilder {

    public static Bundle buildSupplyDeliveryBundle(List<SupplyDelivery> supplyDeliveries, URLParams urlParams, int totalCount) {

        Bundle bundle = formBundle(totalCount);
        for (SupplyDelivery supplyDelivery : supplyDeliveries) {
            bundle.addEntry()
                    .setResource(supplyDelivery)
                    .setFullUrl("urn:uuid:" + UUID.randomUUID());
        }

        addBundleLink(bundle, urlParams, totalCount, Constants.STOCKS_API_PATH);
        return bundle;
    }

    public static Bundle buildInventoryReportBundle(List<InventoryReport> inventoryReport, URLParams urlParams, int totalCount) {

        Bundle bundle = formBundle(totalCount);
        for (InventoryReport report : inventoryReport) {
            bundle.addEntry()
                    .setResource(report)
                    .setFullUrl("urn:uuid:" + UUID.randomUUID());
        }

        addBundleLink(bundle, urlParams, totalCount, Constants.STOCK_RECONCILIATION_API_PATH);
        return bundle;
    }

    public static Bundle buildFacilityLocationBundle(List<Location> location, URLParams urlParams, int totalCount) {

        Bundle bundle = formBundle(totalCount);
        for (Location loc : location) {
            bundle.addEntry()
                    .setResource(loc)
                    .setFullUrl("urn:uuid:" + UUID.randomUUID());
        }

        addBundleLink(bundle, urlParams, totalCount, Constants.FACILITIES_API_PATH);
        return bundle;
    }

    public static Bundle buildInventoryItemBundle(List<InventoryItem> inventoryItem, URLParams urlParams, int totalCount) {

        Bundle bundle = formBundle(totalCount);
        for (InventoryItem item : inventoryItem) {
            bundle.addEntry()
                    .setResource(item)
                    .setFullUrl("urn:uuid:" + UUID.randomUUID());
        }

        addBundleLink(bundle, urlParams, totalCount, Constants.PRODUCT_VARIANT_API_PATH);
        return bundle;
    }


    public static Bundle formBundle(int totalCount){
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTimestamp(new Date());
        bundle.setTotal(totalCount);
        bundle.setId(UUID.randomUUID().toString());
        return bundle;
    }

    public static Bundle addBundleLink(Bundle bundle, URLParams urlParams, int totalCount, String apiPath){

        String baseUrl = apiPath + Constants.SET_LIMIT
                + urlParams.getLimit()
                + Constants.SET_TENANT_ID
                + urlParams.getTenantId();

        bundle.addLink().setRelation(Bundle.LinkRelationTypes.valueOf(Constants.SELF))
                .setUrl(baseUrl + (urlParams.getOffset() != null ? Constants.SET_OFFSET + urlParams.getOffset() : ""));
        bundle.addLink().setRelation(Bundle.LinkRelationTypes.valueOf(Constants.FIRST))

                .setUrl(baseUrl + Constants.FIRST_OFFSET );
        int nextOffset = (urlParams.getOffset() != null ? urlParams.getOffset() : 0)
                + urlParams.getLimit();
        if (nextOffset < totalCount) {
            bundle.addLink().setRelation(Bundle.LinkRelationTypes.valueOf(Constants.NEXT))
                    .setUrl(baseUrl + Constants.SET_OFFSET + nextOffset);
        }
        return bundle;
    }

    public static Bundle buildBoundaryLocationBundle(List<Location> locations){
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);
        bundle.setTimestamp(new Date());
        bundle.setId(UUID.randomUUID().toString());

        for (Location loc : locations) {
            bundle.addEntry()
                    .setResource(loc)
                    .setFullUrl("urn:uuid:" + UUID.randomUUID());
        }
        return bundle;
    }

}
