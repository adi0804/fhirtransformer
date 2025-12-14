# FHIR Transformer

FHIR Transformer is a Spring Boot based Java project scaffold for receiving, validating, transforming, and forwarding FHIR (Fast Healthcare Interoperability Resources) data. 

## Quick summary
- Language: Java (Spring Boot)
- Package base: org.egov.fhirtransformer
- Purpose: Provide a lightweight application to receive, validate, transform, and forward FHIR resources.


## Recommended architecture and components
- Controllers (org.egov.fhirtransformer.web)
  - Accept HTTP requests containing FHIR JSON or Request payloads.
  - Expose endpoints such as POST /transform and GET /health.
- Services (org.egov.fhirtransformer.service)
  - TransformerService: orchestrates parsing, validation, transformation, and persistence or forwarding.
- Mapping (org.egov.fhirtransformer.mapping) 
  - FhirMapper: 
    - Provides builder classes and helper factories that construct HAPI-FHIR resource objects from raw payloads, centralizing object creation and defaulting logic.
  - RequestBuilder
    - Builds outgoing request payloads (HTTP bodies, message envelopes, or integration DTOs) from internal models or transformed FHIR resources, handling serialization format and required headers/metadata.
- Validators (org.egov.fhirtransformer.validator)
  - FhirValidator: validates incoming payloads using a FHIR library (e.g., HAPI-FHIR) or JSON Schema/StructureDefinition.
  - BusinessRuleValidator: applies business rules and returns structured operation outcomes.
- Cross-cutting
  - config: application configuration, ObjectMapper beans, property bindings.
  - util: utilities, logging helpers, error mappers.
  - common : Shared utilities and constants
 

## Suggested dependencies
- Spring Boot Starter Web
- Spring Boot Starter Validation
- HAPI-FHIR (for parsing, validation, and operationOutcome construction)
- Jackson (JSON handling)
- Lombok (optional) for reducing boilerplate


