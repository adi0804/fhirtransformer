package org.egov.fhirtransformer.fhirBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import digit.web.models.BoundaryRelation;
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
        location.setId(code);

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
            location.setPartOf(new Reference().setReference("Location/" + parentLocation.getName()));
        }

        return location;
    }

    public static BoundaryRelation convertFhirLocationToBoundaryRelation(Location location){
        BoundaryRelation boundaryRelation = new BoundaryRelation();
        //Set mandatory fields
        boundaryRelation.setTenantId(Constants.TENANT_ID);
        boundaryRelation.setHierarchyType("ADMIN");

        // Set code from identifier
        if(location.hasIdentifier()){
            for(Identifier identifier : location.getIdentifier()){
                if(identifier.getSystem().equals(Constants.IDENTIFIER_SYSTEM_BOUNDARY)){
                    boundaryRelation.setCode(identifier.getValue());
                    break;
                }
            }
        }

        // Set boundaryType from alias
        if(location.hasAlias() && !location.getAlias().isEmpty()){
            boundaryRelation.setBoundaryType(location.getAlias().get(0).getValueAsString());
        }

        // Set parent code from partOf reference
        if(location.hasPartOf()){
            Reference partOfRef = location.getPartOf();
            if(partOfRef.getReference() != null && partOfRef.getReference().startsWith("Location/")){
                boundaryRelation.setParent(partOfRef.getReference().substring("Location/".length()));
            }
        }

        return boundaryRelation;
    }

}
