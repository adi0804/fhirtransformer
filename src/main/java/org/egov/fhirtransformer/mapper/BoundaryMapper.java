package org.egov.fhirtransformer.mapper;

import java.util.Date;
import java.util.Map;
import org.hl7.fhir.r5.model.Location;
import org.hl7.fhir.r5.model.Meta;

public class BoundaryMapper {
    /**
     * Maps a database row to a FHIR Location resource.
     *
     * @param row The database row containing boundary information.
     * @return A FHIR Location resource populated with the data from the row.
     */
    public static Location mapToLocation(Map<String, Object> row) {
        Location location = new Location();

        location.setId(row.get("id").toString());
        location.setName((String) row.get("code"));
        location.setStatus(Location.LocationStatus.ACTIVE);

        Long lastModified = (Long) row.get("lastmodifiedtime");
        location.setMeta(new Meta()
                .setLastUpdated(new Date(lastModified))
                .addProfile("https://simplifier.net/DIGIT-HCM-Supply-Chain-Interoperability/DIGITHCMBoundary"));

        location.setDescription("Tenant: " + row.get("tenantid"));
        return location;
    }
}
