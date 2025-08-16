package org.egov.fhirtransformer.fhirBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.egov.fhirtransformer.common.Constants;
import org.egov.fhirtransformer.utils.MapUtils;
import org.hl7.fhir.r5.model.*;


/**
 * Utility to map boundary master data rows to FHIR Location resources.
 */
public class DIGITHCMBoundaryMapper {
    /**
     * Maps a database row to a FHIR Location resource.
     *
     * @param row The database row containing boundary information.
     * @return A FHIR Location resource populated with the data from the row.
     */
    public static Location buildLocation(Map<String, Object> row) {

        Location location = new Location();
        final String code = MapUtils.getString(row, Constants.COL_CODE);
        Long lastModifiedMillis = MapUtils.getLong(row, Constants.COL_LAST_MODIFIED);
        Date lastModified = (lastModifiedMillis != null) ? new Date(lastModifiedMillis) : null;

        location.setId(MapUtils.getString(row, Constants.COL_ID));
        location.setName(code);
        location.setStatus(Location.LocationStatus.ACTIVE);

        location.setMeta(new Meta()
                .setLastUpdated(lastModified)
                .addProfile(Constants.PROFILE_DIGIT_HCM_BOUNDARY));

        Identifier identifier = new Identifier()
                .setSystem(Constants.IDENTIFIER_SYSTEM_BOUNDARY)
                .setValue(code);

        location.addType(new CodeableConcept().addCoding(
                new Coding()
                        .setSystem(Constants.LOCATION_TYPE_SYSTEM)
                        .setCode(Constants.LOCATION_TYPE_JURISDICTION)
                        .setDisplay(Constants.LOCATION_TYPE_JURISDICTION)));

        location.addIdentifier(identifier);
        location.setAlias(
                Collections.singletonList(
                        new org.hl7.fhir.r5.model.StringType(MapUtils.getString(row, Constants.COL_BOUNDARY_TYPE))));
        return location;
    }
}
