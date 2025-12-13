package org.egov.fhirtransformer.repository;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.dlq.topic}")
    private String dlqTopic;

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
}