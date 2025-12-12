package org.egov.fhirtransformer.service;

import digit.web.models.BoundaryRelation;
import digit.web.models.BoundaryRelationshipRequest;
import digit.web.models.BoundaryRelationshipSearchCriteria;
import digit.web.models.BoundarySearchResponse;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.facility.*;
import org.egov.fhirtransformer.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class LocationToBoundaryService {

    @Autowired
    private DataIntegrationService diService;

    public HashMap<String, Integer> transformLocationToBoundary(HashMap<String, BoundaryRelation> boundaryRelationMap) throws Exception {
        HashMap<String, Integer> results = new HashMap<>();
        try{
            boundaryRelationMap = updateBoundaryRelationParent(boundaryRelationMap);
            System.out.println("updated parent boundary" + boundaryRelationMap);
            List<String> idList = new ArrayList<>(boundaryRelationMap.keySet());
            if (!idList.isEmpty()) {
                HashMap<String, List<String>> newandexistingstocks = checkExistingBoundaries(idList);
                //call create or update based on existing or new stocks
                callCreateOrUpdateBoundaries(newandexistingstocks, boundaryRelationMap);
                results.put(Constants.TOTAL_PROCESSED, boundaryRelationMap.size());
                results.put(Constants.NEW_IDS,
                        newandexistingstocks.getOrDefault(Constants.NEW_IDS, Collections.emptyList()).size());
                results.put(Constants.EXISTING_IDS,
                        newandexistingstocks.getOrDefault(Constants.EXISTING_IDS, Collections.emptyList()).size());
                System.out.println(results);
            }
        } catch (Exception e){
            throw new Exception("Error in transformLocationToBoundary: " + e.getMessage());
        }
        return new HashMap<String, Integer>();
    }

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

    public HashMap<String,List<String>> checkExistingBoundaries(List<String> idList) throws Exception {

        HashMap<String,List<String>> newandexistingids = new HashMap<>();
        try{
            for(String id : idList){
                BoundaryRelationshipSearchCriteria criteria = new BoundaryRelationshipSearchCriteria();
                criteria.setCodes(List.of(id));
                criteria.setTenantId(Constants.TENANT_ID);
                BoundarySearchResponse boundarySearchResponse = diService.fetchAllBoundaries(criteria,diService.formRequestInfo());
                if(!boundarySearchResponse.getTenantBoundary().isEmpty()){
                    List<String> existingIds = new ArrayList<>();
                    existingIds.add(id);
                    newandexistingids.put(Constants.EXISTING_IDS, existingIds);
                }
                else{
                    List<String> newIds = new ArrayList<>();
                    newIds.add(id);
                    newandexistingids.put(Constants.NEW_IDS, newIds);
                }
            }
            System.out.println(newandexistingids);
        } catch (Exception e){
            throw new Exception("Error in checkExistingFacilities: " + e.getMessage());
        }
        return newandexistingids;
    }

    public void callCreateOrUpdateBoundaries(HashMap<String,List<String>> newandexistingskeys, HashMap<String, BoundaryRelation> boundaryRelationMap) throws Exception {
        //Create StockBulkRequest for new stocks
        try{
            List<BoundaryRelation> newIds = new ArrayList<>();
            List<BoundaryRelation> existingIds = new ArrayList<>();

            if (newandexistingskeys.containsKey(Constants.NEW_IDS)) {
                for (String id : newandexistingskeys.get(Constants.NEW_IDS)) {
                    newIds.add(boundaryRelationMap.get(id));
                    BoundaryRelationshipRequest boundaryRelationshipRequest = new BoundaryRelationshipRequest();
                    boundaryRelationshipRequest.setRequestInfo(diService.formRequestInfo());
                    boundaryRelationshipRequest.setBoundaryRelationship(boundaryRelationMap.get(id));
                    //Call create API
                    diService.createOrUpdateBoundaries(boundaryRelationshipRequest, true);
                }
            }
            if (newandexistingskeys.containsKey(Constants.EXISTING_IDS)) {
                for (String id : newandexistingskeys.get(Constants.EXISTING_IDS)) {
                    existingIds.add(boundaryRelationMap.get(id));
                    BoundaryRelationshipRequest boundaryRelationshipRequest = new BoundaryRelationshipRequest();
                    boundaryRelationshipRequest.setRequestInfo(diService.formRequestInfo());
                    boundaryRelationshipRequest.setBoundaryRelationship(boundaryRelationMap.get(id));
                    //Call create API
                    diService.createOrUpdateBoundaries(boundaryRelationshipRequest, false);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error in callCreateOrUpdateFacilities: " + e.getMessage());
        }
    }
}
