package org.egov.fhirtransformer.common;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    public static final String PROFILE_DIGIT_HCM_BOUNDARY = "https://simplifier.net/DIGIT-HCM-Supply-Chain-Interoperability/DIGITHCMBoundary";
    public static final String IDENTIFIER_SYSTEM_BOUNDARY = "https://digit.org/fhir/boundarymasterdata";
    public static final String LOCATION_TYPE_SYSTEM = "https://digit.org/CodeSystem/DIGITHCM.Location.Types";
    public static final String LOCATION_TYPE_JURISDICTION = "jurisdiction";
    public static final String FACILITY_CACHE_KEY_PREFIX = "facility_";
    public static final long CACHE_TTL_MINUTES = 1;
    public static final String PARAM_BOUNDARY_LOCATION="DIGITHCMBoundary";
    public static final String PARAM_FACILITYBOUNDARY_LOCATION="DIGITHCMFacilityLocation";


    // --- Database Column Keys ---
    public static final String COL_ID = "id";
    public static final String COL_CODE = "code";
    public static final String COL_LAST_MODIFIED = "lastmodifiedtime";
    public static final String COL_BOUNDARY_TYPE = "boundarytype";


}
