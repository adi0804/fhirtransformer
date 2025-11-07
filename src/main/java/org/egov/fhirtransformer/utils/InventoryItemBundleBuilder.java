package org.egov.fhirtransformer.utils;

import org.egov.common.models.core.URLParams;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.InventoryItem;
import org.hl7.fhir.r5.model.Location;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class InventoryItemBundleBuilder {
    public static Bundle buildInventoryItemBundle(List<InventoryItem> inventoryItems, URLParams urlParams, int totalCount) {

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTimestamp(new Date());
        bundle.setTotal(totalCount);
        bundle.setId(UUID.randomUUID().toString());
        for (InventoryItem inventoryItem : inventoryItems) {
            bundle.addEntry()
                    .setResource(inventoryItem)
                    .setFullUrl("urn:uuid:" + inventoryItem.getId());
        }

        String baseUrl = "/fetchAllProductVariants?limit=" + urlParams.getLimit() + "&tenantId=" + urlParams.getTenantId();

        bundle.addLink().setRelation(Bundle.LinkRelationTypes.valueOf("SELF")).setUrl(baseUrl + (urlParams.getOffset() != null ? "&_offset=" + urlParams.getOffset() : ""));
        bundle.addLink().setRelation(Bundle.LinkRelationTypes.valueOf("FIRST")).setUrl(baseUrl + "&_offset=0" );
        int nextOffset = (urlParams.getOffset() != null ? urlParams.getOffset() : 0) + urlParams.getLimit();
        if (nextOffset < totalCount) {
            bundle.addLink().setRelation(Bundle.LinkRelationTypes.valueOf("NEXT")).setUrl(baseUrl + "&offset=" + nextOffset);
        }

        return bundle;
    }
}
