package org.egov.fhirtransformer.utils;


import org.egov.common.models.core.URLParams;
import org.egov.fhirtransformer.common.Constants;
import org.hl7.fhir.r5.model.*;

import java.util.*;

public class BundleBuilder {

    public static <T extends Resource> Bundle buildBundle(
            List<T> resources, URLParams urlParams, int totalCount, String apiPath) {

        Bundle bundle = buildBaseBundle(totalCount);
        for (T resource : resources) {
            bundle.addEntry()
                    .setResource(resource)
                    .setFullUrl("urn:uuid:" + UUID.randomUUID());
        }
        addBundleLink(bundle, urlParams, totalCount, apiPath);
        return bundle;
    }

    public static Bundle buildBaseBundle(int totalCount){
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
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTimestamp(new Date());
        bundle.setId(UUID.randomUUID().toString());

        for (Location loc : locations) {
            bundle.addEntry()
                    .setResource(loc)
                    .setFullUrl("urn:uuid:" + loc.getId());
        }
        return bundle;
    }

    public static HashMap<String, Integer> fetchMetrics(HashMap<String, Integer> results, HashMap<String, List<String>> newAndExistingMap) {
        results.put(Constants.NEW_IDS,
                newAndExistingMap.getOrDefault(Constants.NEW_IDS,
                        Collections.emptyList()).size());
        results.put(Constants.EXISTING_IDS,
                newAndExistingMap.getOrDefault(Constants.EXISTING_IDS,
                        Collections.emptyList()).size());
        System.out.println(results);
        return results;
    }
}
