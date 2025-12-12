package org.egov.fhirtransformer.service;

import org.egov.common.models.core.URLParams;
import org.egov.common.models.facility.*;
import org.egov.common.models.stock.*;
import org.egov.fhirtransformer.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class LocationToFacilityService {

    @Autowired
    private DataIntegrationService diService;

    public HashMap<String, Integer> transformLocationToFacility(HashMap<String, Facility> facilityMap) throws Exception {
        HashMap<String, Integer> results = new HashMap<>();
        try{
            List<String> idList = new ArrayList<>(facilityMap.keySet());
            if (!idList.isEmpty()) {
                HashMap<String, List<String>> newandexistingstocks = checkExistingFacilities(idList);
                //call create or update based on existing or new stocks
                callCreateOrUpdateFacilities(newandexistingstocks, facilityMap);
                results.put(Constants.TOTAL_PROCESSED, facilityMap.size());
                results.put(Constants.NEW_IDS,
                        newandexistingstocks.getOrDefault(Constants.NEW_IDS, Collections.emptyList()).size());
                results.put(Constants.EXISTING_IDS,
                        newandexistingstocks.getOrDefault(Constants.EXISTING_IDS, Collections.emptyList()).size());
                System.out.println(results);
            }
        }
        catch (Exception e){
            throw new Exception("Error in transformLocationToFacility: " + e.getMessage());
        }
        return results;
    }

    public HashMap<String,List<String>> checkExistingFacilities(List<String> idList) throws Exception {

        HashMap<String,List<String>> newandexistingids = new HashMap<>();
        try{
            URLParams urlParams = diService.formURLParams(idList);

            FacilitySearch facilitySearch = new FacilitySearch();
            facilitySearch.setId(idList);

            FacilitySearchRequest facilitySearchRequest = new FacilitySearchRequest();
            facilitySearchRequest.setRequestInfo(diService.formRequestInfo());
            facilitySearchRequest.setFacility(facilitySearch);

            FacilityBulkResponse facilityBulkResponse = diService.fetchAllFacilities(urlParams, facilitySearchRequest);

            if (facilityBulkResponse.getFacilities() == null){
                newandexistingids.put(Constants.NEW_IDS, idList);
            }
            else {
                List<String> existingIds = new ArrayList<>();
                for (Facility facility : facilityBulkResponse.getFacilities()) {
                    existingIds.add(facility.getId());
                }
                List<String> newIds = new ArrayList<>(idList);
                newIds.removeAll(existingIds);
                newandexistingids.put(Constants.EXISTING_IDS, existingIds);
                newandexistingids.put(Constants.NEW_IDS, newIds);
            }
            System.out.println(newandexistingids);
        } catch (Exception e){
            throw new Exception("Error in checkExistingFacilities: " + e.getMessage());
        }
        return newandexistingids;
    }

    public void callCreateOrUpdateFacilities(HashMap<String,List<String>> newandexistingskeys, HashMap<String, Facility> facilityMap) throws Exception {
        //Create StockBulkRequest for new stocks
        try{
            List<Facility> newIds = new ArrayList<>();
            if (newandexistingskeys.containsKey(Constants.NEW_IDS)) {
                for (String id : newandexistingskeys.get(Constants.NEW_IDS)) {
                    newIds.add(facilityMap.get(id));
                }
                if (!newIds.isEmpty()) {
                    FacilityBulkRequest facilityBulkRequest = new FacilityBulkRequest();
                    facilityBulkRequest.setRequestInfo(diService.formRequestInfo());
                    facilityBulkRequest.setFacilities(newIds);
                    //Call create API
                    diService.createOrUpdateFacilities(facilityBulkRequest, true);
                }
            }
            List<Facility> existingIds = new ArrayList<>();
            //handle key not found scenario
            if (newandexistingskeys.containsKey(Constants.EXISTING_IDS)) {
                for (String id : newandexistingskeys.get(Constants.EXISTING_IDS)) {
                    existingIds.add(facilityMap.get(id));
                }
                if (!existingIds.isEmpty()) {
                    FacilityBulkRequest facilityBulkRequest = new FacilityBulkRequest();
                    facilityBulkRequest.setRequestInfo(diService.formRequestInfo());
                    facilityBulkRequest.setFacilities(existingIds);
                    //Call create API
                    diService.createOrUpdateFacilities(facilityBulkRequest, false);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error in callCreateOrUpdateFacilities: " + e.getMessage());
        }
    }
}
