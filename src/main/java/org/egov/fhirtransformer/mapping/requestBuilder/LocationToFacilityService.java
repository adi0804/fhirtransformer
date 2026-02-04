package org.egov.fhirtransformer.mapping.requestBuilder;

import org.egov.common.models.core.URLParams;
import org.egov.common.models.facility.*;
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

/**
 * Service responsible for transforming FHIR Location–derived
 * {@link Facility} data into DIGIT Facility service requests.
 */
@Service
public class LocationToFacilityService {

    @Autowired
    private ApiIntegrationService apiIntegrationService;

    @Value("${facility.create.url}")
    private String facilityCreateUrl;

    @Value("${facility.update.url}")
    private String facilityUpdateUrl;

    /**
     * Transforms and persists Facility records derived from Locations.
     * @param facilityMap map of Facility ID to Facility data;
     *                    may be empty but not {@code null}
     * @return map containing processing metrics
     * @throws Exception if transformation or API invocation fails
     */
    public HashMap<String, Integer> transformLocationToFacility(HashMap<String, Facility> facilityMap) throws Exception {
        HashMap<String, Integer> results = new HashMap<>();
        try{
            List<String> idList = new ArrayList<>(facilityMap.keySet());
            if (!idList.isEmpty()) {
                HashMap<String, List<String>> newAndExistingIdsMap = checkExistingFacilities(idList);
                //call create or update based on existing or new stocks
                callCreateOrUpdateFacilities(newAndExistingIdsMap, facilityMap);

                results.put(Constants.TOTAL_PROCESSED, facilityMap.size());
                results = BundleBuilder.fetchMetrics(results, newAndExistingIdsMap);

            }
        }
        catch (Exception e){
            throw new Exception("Error in transformLocationToFacility: " + e.getMessage());
        }
        return results;
    }

    /**
     * Identifies existing and new Facility IDs by querying the Facility service.
     * @param idList list of Facility IDs to check; must not be {@code null}
     * @return map of new and existing Facility IDs
     * @throws Exception if the search API invocation fails
     */
    public HashMap<String,List<String>> checkExistingFacilities(List<String> idList) throws Exception {

        HashMap<String,List<String>> newandexistingids = new HashMap<>();
        try{
            URLParams urlParams = apiIntegrationService.formURLParams(idList);

            FacilitySearch facilitySearch = new FacilitySearch();
            facilitySearch.setId(idList);

            FacilitySearchRequest facilitySearchRequest = new FacilitySearchRequest();
            facilitySearchRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
            facilitySearchRequest.setFacility(facilitySearch);

            FacilityBulkResponse facilityBulkResponse = apiIntegrationService.fetchAllFacilities(urlParams, facilitySearchRequest);

            if (facilityBulkResponse.getFacilities() == null){
                newandexistingids.put(Constants.NEW_IDS, idList);
            }
            else {
                List<String> existingIds = new ArrayList<>();
                for (Facility facility : facilityBulkResponse.getFacilities()) {
                    existingIds.add(facility.getId());
                }
                List<String> newIds = new ArrayList<>(idList);
                newandexistingids = MapUtils.splitNewAndExistingIDS(newIds, existingIds);
            }
            System.out.println(newandexistingids);
        } catch (Exception e){
            throw new Exception("Error in checkExistingFacilities: " + e.getMessage());
        }
        return newandexistingids;
    }

    /**
     * Creates or updates Facility records based on their existence.
     * @param newandexistingskeys map containing new and existing Facility IDs
     * @param facilityMap map of Facility ID to Facility data
     * @throws Exception if API invocation for create or update fails
     */
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
                    facilityBulkRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
                    facilityBulkRequest.setFacilities(newIds);
                    //Call create API
                    apiIntegrationService.sendRequestToAPI(facilityBulkRequest, facilityCreateUrl);
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
                    facilityBulkRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
                    facilityBulkRequest.setFacilities(existingIds);
                    //Call create API
                    apiIntegrationService.sendRequestToAPI(facilityBulkRequest, facilityUpdateUrl);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error in callCreateOrUpdateFacilities: " + e.getMessage());
        }
    }
}
