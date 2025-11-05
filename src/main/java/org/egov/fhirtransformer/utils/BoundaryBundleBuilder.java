package org.egov.fhirtransformer.utils;
import org.egov.fhirtransformer.common.Constants;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Location;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class BoundaryBundleBuilder {
    public static Bundle buildLocationBundle(List<Location> locations, String lastModifiedTime, int count, String afterId, int total) {

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTimestamp(new Date());
        bundle.setTotal(total);
        bundle.setId(UUID.randomUUID().toString());
        for (Location loc : locations) {
            bundle.addEntry()
                    .setResource(loc)
                    .setFullUrl("urn:uuid:" + loc.getId());
        }

        String baseUrl = "/getLocations?_profile="+ Constants.PARAM_BOUNDARY_LOCATION + "&_count=" + count;
        if (lastModifiedTime != null) {
            baseUrl += "&_lastmodifiedtime=gt" + lastModifiedTime;
        }

        bundle.addLink().setRelation(Bundle.LinkRelationTypes.valueOf("SELF")).setUrl(baseUrl + (afterId != null ? "&_afterId=" + afterId : ""));
        bundle.addLink().setRelation(Bundle.LinkRelationTypes.valueOf("FIRST")).setUrl(baseUrl);
        if (!locations.isEmpty() && locations.size() >= count) {
            String nextId = locations.get(locations.size() - 1).getId();
            bundle.addLink().setRelation(Bundle.LinkRelationTypes.valueOf("NEXT")).setUrl(baseUrl + "&_afterId=" + nextId);
        }

        return bundle;
    }
}
