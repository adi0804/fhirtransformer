package org.egov.fhirtransformer.fhirBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import digit.web.models.EnrichedBoundary;
import org.egov.fhirtransformer.common.Constants;
import org.egov.fhirtransformer.utils.MapUtils;
import org.hl7.fhir.r5.model.*;


/**
 * Utility to map boundary master data rows to FHIR Location resources.
 */
public class DIGITHCMBoundaryMapper {

    public static Location buildLocationFromHierarchyRelation(EnrichedBoundary enrichedBoundary, Location parentLocation){
        Location location = new Location();
        String code = enrichedBoundary.getCode();
        location.setId(enrichedBoundary.getId());

        location.setMeta(new Meta()
                .addProfile(Constants.PROFILE_DIGIT_HCM_BOUNDARY));

        Identifier identifier = new Identifier()
                .setSystem(Constants.IDENTIFIER_SYSTEM_BOUNDARY)
                .setValue(code);
        location.addIdentifier(identifier);
        location.setName(code);
        location.setStatus(Location.LocationStatus.ACTIVE);
        location.addType(new CodeableConcept().addCoding(
                new Coding()
                        .setSystem(Constants.LOCATION_TYPE_SYSTEM)
                        .setCode(Constants.LOCATION_TYPE_JURISDICTION)
                        .setDisplay(Constants.LOCATION_TYPE_JURISDICTION)));
        location.setAlias(
                Collections.singletonList(
                        new org.hl7.fhir.r5.model.StringType(enrichedBoundary.getBoundaryType())));

        if(parentLocation != null){
            location.setPartOf(new Reference().setReference("Location/" + parentLocation.getId()));
        }

        return location;
    }

}
