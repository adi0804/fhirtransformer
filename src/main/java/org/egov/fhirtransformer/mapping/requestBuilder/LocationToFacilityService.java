package org.egov.fhirtransformer.mapping.requestBuilder;

import org.egov.common.models.core.URLParams;
import org.egov.common.models.facility.*;
import org.egov.fhirtransformer.common.Constants;
import org.egov.fhirtransformer.service.ApiIntegrationService;
import org.egov.fhirtransformer.utils.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Autowired
    private GenericCreateOrUpdateService genericCreateOrUpdateService;

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

        return genericCreateOrUpdateService.process(facilityMap,
                this::fetchExistingFacilityIds,
                this::createFacilities,
                this::updateFacilities,
                facilityCreateUrl,
                facilityUpdateUrl,
                "Error in transformLocationToFacility");
    }

    // Adapter: fetch existing facility ids
    public List<String> fetchExistingFacilityIds(List<String> idList) throws Exception {
        try{
            URLParams urlParams = apiIntegrationService.formURLParams(idList);

            FacilitySearch facilitySearch = new FacilitySearch();
            facilitySearch.setId(idList);

            FacilitySearchRequest facilitySearchRequest = new FacilitySearchRequest();
            facilitySearchRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
            facilitySearchRequest.setFacility(facilitySearch);

            FacilityBulkResponse facilityBulkResponse = apiIntegrationService.fetchAllFacilities(urlParams, facilitySearchRequest);

            if (facilityBulkResponse.getFacilities() == null){
                return new ArrayList<>();
            }
            List<String> existingIds = new ArrayList<>();
            for (Facility facility : facilityBulkResponse.getFacilities()) {
                existingIds.add(facility.getId());
            }
            return existingIds;
        } catch (Exception e){
            throw new Exception("Error in fetchExistingFacilities: " + e.getMessage());
        }
    }

    // Adapter: create facilities
    public void createFacilities(List<Facility> toCreate, String createUrl) throws Exception {
        try{
            if (toCreate == null || toCreate.isEmpty()) return;
            FacilityBulkRequest facilityBulkRequest = new FacilityBulkRequest();
            facilityBulkRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
            facilityBulkRequest.setFacilities(toCreate);
            apiIntegrationService.sendRequestToAPI(facilityBulkRequest, createUrl);
        } catch (Exception e) {
            throw new Exception("Error in createFacilities: " + e.getMessage());
        }
    }

    // Adapter: update facilities
    public void updateFacilities(List<Facility> toUpdate, String updateUrl) throws Exception {
        try{
            if (toUpdate == null || toUpdate.isEmpty()) return;
            FacilityBulkRequest facilityBulkRequest = new FacilityBulkRequest();
            facilityBulkRequest.setRequestInfo(apiIntegrationService.formRequestInfo());
            facilityBulkRequest.setFacilities(toUpdate);
            apiIntegrationService.sendRequestToAPI(facilityBulkRequest, updateUrl);
        } catch (Exception e) {
            throw new Exception("Error in updateFacilities: " + e.getMessage());
        }
    }
}
