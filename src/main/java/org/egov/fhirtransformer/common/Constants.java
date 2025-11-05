package org.egov.fhirtransformer.common;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    public static final String PROFILE_DIGIT_HCM_BOUNDARY = "https://digit.org/fhir/StructureDefinition/DIGITHCMBoundary";
    public static final String IDENTIFIER_SYSTEM_BOUNDARY = "https://digit.org/fhir/boundarymasterdata";
    public static final String LOCATION_TYPE_SYSTEM = "https://digit.org/CodeSystem/DIGITHCM.Location.Types";
    public static final String LOCATION_TYPE_JURISDICTION = "jurisdiction";
    public static final String FACILITY_CACHE_KEY_PREFIX = "facility_";
    public static final long CACHE_TTL_MINUTES = 1;
    public static final String PARAM_BOUNDARY_LOCATION="DIGITHCMBoundary";
    public static final String PARAM_FACILITYBOUNDARY_LOCATION="DIGITHCMFacilityLocation";

    //  Facility Related Constants
    public static final String PROFILE_DIGIT_HCM_FACILITY = "https://digit.org/fhir/StructureDefinition/DIGITHCMFacilityLocation";
    public static final String IDENTIFIER_SYSTEM_FACILITY = "https://digit.org/fhir/facilityid";
    public static final String FACILITY_USAGE_SYSTEM = "http://digit.org/fhir/CodeSystem/facilityUsage";
    public static final String FACILITY_LOCATION_TYPE = "facility";

    // --- Database Column Keys ---
    public static final String COL_ID = "id";
    public static final String COL_CODE = "code";
    public static final String COL_LAST_MODIFIED = "lastmodifiedtime";
    public static final String COL_BOUNDARY_TYPE = "boundarytype";


}
