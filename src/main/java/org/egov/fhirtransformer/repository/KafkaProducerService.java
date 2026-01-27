package org.egov.fhirtransformer.repository;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hl7.fhir.r5.model.Bundle;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final FhirContext ctx = FhirContext.forR5();

    @Value("${kafka.dlq.topic}")
    private String dlqTopic;

    @Value("${kafka.failed.topic}")
    private String failedTopic;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishToDLQ(ValidationResult result, String bundleId, JsonNode fhirJson) throws JsonProcessingException {

        List<String> errorList = result.getMessages().stream()
                .filter(msg -> msg.getSeverity() == ResultSeverityEnum.ERROR)
                .map(SingleValidationMessage::getMessage)
                .collect(Collectors.toList());
        ObjectMapper mapper = new ObjectMapper();
        String jsonArray = mapper.writeValueAsString(errorList);

        ObjectNode dlqJson = mapper.createObjectNode();
        dlqJson.put("id", bundleId);
        dlqJson.put("timestamp", Instant.now().toString());
        dlqJson.put("fhirPayload", fhirJson);
        dlqJson.set("errors", mapper.readTree(jsonArray));

        String finalJson = mapper.writeValueAsString(dlqJson);
        System.out.println(finalJson);
        // Publish to Kafka
        kafkaTemplate.send(dlqTopic, bundleId, finalJson);
    }


    public void publishFhirResourceFailures(Bundle.BundleEntryComponent entry, String errorMessage) {

        String finalJson;
        String resourceId = entry.getResource().getIdElement().getIdPart();
        String resourceType = entry.getResource().fhirType();
        String resourceJson = ctx.newJsonParser().encodeResourceToString(entry.getResource());

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode failedResourceJson = objectMapper.createObjectNode();

        failedResourceJson.put("resourceId", resourceId);
        failedResourceJson.put("resourceType", resourceType);
        failedResourceJson.put("fhirResource", resourceJson);
        failedResourceJson.put("errorReason", errorMessage);

        try {
            finalJson = objectMapper.writeValueAsString(failedResourceJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to publish to Failed Topic", e);
        }
        // Publish to Kafka
        kafkaTemplate.send(failedTopic, resourceId, finalJson);
    }
}