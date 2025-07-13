package org.egov.fhirtransformer.validator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;

import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class CustomFHIRValidator {
    private final FhirContext ctx = FhirContext.forR5();
    private final FhirValidator validator;
    private final PrePopulatedValidationSupport support = new PrePopulatedValidationSupport(ctx);


    public CustomFHIRValidator() {
        loadProfiles("profiles");

        ValidationSupportChain chain = new ValidationSupportChain(
                new DefaultProfileValidationSupport(ctx),
                new InMemoryTerminologyServerValidationSupport(ctx),
                new CommonCodeSystemsTerminologyService(ctx),
                support
        );
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(chain);
        validator = ctx.newValidator();
        validator.registerValidatorModule(instanceValidator);
    }

    private void loadProfiles(String folderName) {
        try {
            Path folderPath = Paths.get(Objects.requireNonNull(
                    getClass().getClassLoader().getResource(folderName)).toURI());

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, "*.json")) {
                for (Path path : stream) {
                    try (InputStream is = Files.newInputStream(path)) {
                        IBaseResource resource = ctx.newJsonParser().parseResource(is);
                        if (resource instanceof StructureDefinition) {
                            StructureDefinition sd = (StructureDefinition) resource;
                            support.addStructureDefinition(sd);
                            System.out.println("Loaded profile: " + sd.getUrl());
                        }
                    }
                }
            }

        } catch (URISyntaxException | NullPointerException | java.io.IOException e) {
            throw new RuntimeException("Failed to load FHIR profiles from /profiles directory", e);
        }
    }

    public boolean validate(String fhirJson) {
        IBaseResource resource = ctx.newJsonParser().parseResource(fhirJson);
        ValidationResult result = validator.validateWithResult(resource);
        result.getMessages().forEach(msg -> System.out.println(msg.getSeverity() + ": " + msg.getMessage()));
        return result.isSuccessful();
    }
}
