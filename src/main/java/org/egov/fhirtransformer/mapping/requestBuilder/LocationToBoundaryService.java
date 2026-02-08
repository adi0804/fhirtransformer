package org.egov.fhirtransformer.mapping.requestBuilder;

import digit.web.models.*;
import org.egov.fhirtransformer.common.Constants;
import org.egov.fhirtransformer.service.ApiIntegrationService;
import org.egov.fhirtransformer.utils.BundleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Service responsible for transforming FHIR Location–derived
 * {@link BoundaryRelation} data into DIGIT Boundary service requests.
 */
@Service
public class LocationToBoundaryService {

    @Autowired
    private ApiIntegrationService apiIntegrationService;

    @Value("${boundary.create.url}")
    private String boundaryCreateUrl;

    @Value("${boundary.update.url}")
    private String boundaryUpdateUrl;

    /**
     * Transforms and persists BoundaryRelation records derived from Locations.
     * @param boundaryRelationMap map of boundary ID to BoundaryRelation data;
     *                            may be empty but not {@code null}
     * @return map containing processing metrics
     * @throws Exception if transformation or API invocation fails
     */
    public HashMap<String, Integer> transformLocationToBoundary(HashMap<String, BoundaryRelation> boundaryRelationMap) throws Exception {
        HashMap<String, Integer> results = new HashMap<>();
        try{
            List<String> idList = new ArrayList<>(boundaryRelationMap.keySet());
            if (!idList.isEmpty()) {
                HashMap<String, List<String>> newAndExistingIdsMap = checkExistingBoundaries(idList);
                callCreateOrUpdateBoundaries(newAndExistingIdsMap, boundaryRelationMap);
                results.put(Constants.TOTAL_PROCESSED, boundaryRelationMap.size());
                results = BundleBuilder.fetchMetrics(results, newAndExistingIdsMap);
            }
        } catch (Exception e){
            throw new Exception("Error in transformLocationToBoundary: " + e.getMessage());
        }
        return results;
    }

    /**
     * Updates parent boundary references using in-memory BoundaryRelation data.
     * @param boundaryRelationMap map of boundary ID to BoundaryRelation data;
     *                            must not be {@code null}
     * @return updated map with resolved parent boundary codes
     * @throws Exception if parent resolution fails
     */
    public HashMap<String, BoundaryRelation> updateBoundaryRelationParent(HashMap<String, BoundaryRelation> boundaryRelationMap) throws Exception {
        try{
            for (String key : boundaryRelationMap.keySet()) {
                BoundaryRelation boundaryRelation = boundaryRelationMap.get(key);
                String parentId = boundaryRelation.getParent();
                if (boundaryRelationMap.containsKey(parentId)) {
                    BoundaryRelation parentBoundaryRelation = boundaryRelationMap.get(parentId);
                    boundaryRelation.setParent(parentBoundaryRelation.getCode());
                } else {
                    boundaryRelation.setParent(null);
                }
            }
        } catch (Exception e){
            throw new Exception("Error in updateBoundaryRelationParent: " + e.getMessage());
        }
        return boundaryRelationMap;
    }

    /**
     * Identifies existing and new boundary IDs by querying the Boundary service.
     * @param idList list of boundary IDs to check; must not be {@code null}
     * @return map of new and existing boundary IDs
     * @throws Exception if the search API invocation fails
     */
    public HashMap<String,List<String>> checkExistingBoundaries(List<String> idList) throws Exception {

        HashMap<String,List<String>> newandexistingids = new HashMap<>();
        try{
            BoundaryRelationshipSearchCriteria criteria = new BoundaryRelationshipSearchCriteria();
            criteria.setCodes(idList);
            criteria.setTenantId(Constants.TENANT_ID);
            criteria.setHierarchyType(Constants.HIERARCHY_TYPE);
            criteria.setIncludeChildren(Constants.INCLUDE_CHILDREN);
            BoundarySearchResponse boundarySearchResponse = apiIntegrationService.fetchAllBoundaries(criteria, apiIntegrationService.formRequestInfo());
            List<String> existingIds = new ArrayList<>();
            if (!boundarySearchResponse.getTenantBoundary().isEmpty()) {
                for (EnrichedBoundary boundary : boundarySearchResponse.getTenantBoundary().get(0).getBoundary()) {
                    extractBoundaryCodes(boundary, existingIds);
                }
                System.out.println(existingIds);
                newandexistingids.put(Constants.EXISTING_IDS, existingIds);
            }
            List<String> newIds = new ArrayList<>();
            for (String id : idList) {
                if (!existingIds.contains(id)) {
                    newIds.add(id);
                }
            }
            newandexistingids.put(Constants.NEW_IDS, newIds);
            System.out.println(newandexistingids);
        } catch (Exception e){
            throw new Exception("Error in checkExistingFacilities: " + e.getMessage());
        }
        return newandexistingids;
    }

    /**
     * Creates or updates BoundaryRelation records based on their existence.
     * @param newAndExistingIds map containing new and existing boundary IDs
     * @param boundaryRelationMap map of boundary ID to BoundaryRelation data
     * @throws Exception if API invocation for create or update fails
     */
    public void callCreateOrUpdateBoundaries(HashMap<String,List<String>> newAndExistingIds, HashMap<String, BoundaryRelation> boundaryRelationMap) throws Exception {
        try{
            List<BoundaryRelation> newIds = new ArrayList<>();
            List<BoundaryRelation> existingIds = new ArrayList<>();

            if (newAndExistingIds.containsKey(Constants.NEW_IDS)) {
                for (String id : newAndExistingIds.get(Constants.NEW_IDS)) {
                    newIds.add(boundaryRelationMap.get(id));
                    BoundaryRelationshipRequest boundaryRelationshipRequest = new BoundaryRelationshipRequest();
                    boundaryRelationshipRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
                    boundaryRelationshipRequest.setBoundaryRelationship(boundaryRelationMap.get(id));
                    //Call create API
                    apiIntegrationService.sendRequestToAPI(boundaryRelationshipRequest, boundaryCreateUrl);
                }
            }
            if (newAndExistingIds.containsKey(Constants.EXISTING_IDS)) {
                for (String id : newAndExistingIds.get(Constants.EXISTING_IDS)) {
                    existingIds.add(boundaryRelationMap.get(id));
                    BoundaryRelationshipRequest boundaryRelationshipRequest = new BoundaryRelationshipRequest();
                    boundaryRelationshipRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
                    boundaryRelationshipRequest.setBoundaryRelationship(boundaryRelationMap.get(id));
                    //Call create API
                    apiIntegrationService.sendRequestToAPI(boundaryRelationshipRequest, boundaryUpdateUrl);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error in callCreateOrUpdate Boundary API: " + e.getMessage());
        }
    }

    private void extractBoundaryCodes(EnrichedBoundary boundary, List<String> codes) {
        codes.add(boundary.getCode());
        if (boundary.getChildren() != null && !boundary.getChildren().isEmpty()) {
            for (EnrichedBoundary child : boundary.getChildren()) {
                extractBoundaryCodes(child, codes);
            }
        }
    }

}
