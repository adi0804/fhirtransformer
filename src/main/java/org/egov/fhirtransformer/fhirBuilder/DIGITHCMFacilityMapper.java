package org.egov.fhirtransformer.fhirBuilder;

import org.egov.common.models.facility.Facility;
import org.egov.fhirtransformer.common.Constants;
import org.egov.fhirtransformer.utils.MapUtils;
import org.hl7.fhir.r5.model.*;
import java.util.UUID;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class DIGITHCMFacilityMapper {
    /**
     * @return A FHIR Location resource populated with the data from the row.
     */
    public static Location buildLocationFromFacility(Facility facility) {

        Location location = new Location();
        Long lastModifiedMillis = facility.getAuditDetails().getLastModifiedTime();
        Date lastModified = (lastModifiedMillis != null) ? new Date(lastModifiedMillis) : null;

//        location.setId(facility.getId());
        location.setId(UUID.randomUUID().toString());
        location.setName(facility.getName());
        location.setStatus(Location.LocationStatus.ACTIVE);

        // Setting meta information for the Location resource DIGIT HCM Facility profile
        location.setMeta(new Meta()
                .setLastUpdated(lastModified)
                .addProfile(Constants.PROFILE_DIGIT_HCM_FACILITY));

        // Adding identifier for facility ID
        Identifier identifier = new Identifier()
                .setSystem(Constants.IDENTIFIER_SYSTEM_FACILITY)
                .setValue(facility.getId());
        location.addIdentifier(identifier);

        // Adding facility type and usage
        location.addType(new CodeableConcept().addCoding(
                new Coding()
                        .setSystem(Constants.LOCATION_TYPE_SYSTEM)
                        .setCode(Constants.FACILITY_LOCATION_TYPE)));
        location.addType(new CodeableConcept().addCoding(
                new Coding()
                        .setSystem(Constants.FACILITY_USAGE_SYSTEM)
                        .setCode(facility.getUsage())));

        // Setting address details
        Address address = new Address();
        if (facility.getAddress() != null) {
            if (facility.getAddress().getBuildingName() != null) {
                address.addLine(facility.getAddress().getBuildingName());
            }
            if (facility.getAddress().getAddressLine1() != null) {
                address.addLine(facility.getAddress().getAddressLine1());
            }
            if (facility.getAddress().getAddressLine2() != null) {
                address.addLine(facility.getAddress().getAddressLine2());
            }
            address.setCity(facility.getAddress().getCity());
            address.setPostalCode(facility.getAddress().getPincode());
        }
        location.setAddress(address);

        // Setting position details (latitude and longitude)
        Location.LocationPositionComponent position = new Location.LocationPositionComponent()
                .setLatitude(facility.getAddress().getLatitude())
                .setLongitude(facility.getAddress().getLongitude());

        location.setPosition(position);

        return location;
    }
}
