package com.synpulse8.pulse8.core.accesscontrolsvc.controller.kafka;

import com.synpulse8.pulse8.core.accesscontrolsvc.dto.RelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.ApiError;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CError;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.producer.CreateRelationshipProducer;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("development")
@OpenAPIDefinition(
        info = @Info(title = "Kafka Event Controller API", version = "v1"),
        security = @SecurityRequirement(name = "X-Authenticated-User"))
@ApiResponse(responseCode = "500", description = "An internal error has occurred", content = @Content(schema = @Schema(implementation = P8CError.class)))
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = P8CError.class)))
@RequestMapping(value = "/v1", produces = "application/json")
public class KafkaEventController {

    private final CreateRelationshipProducer createRelationshipProducer;

    public KafkaEventController(CreateRelationshipProducer createRelationshipProducer) {
        this.createRelationshipProducer = createRelationshipProducer;
    }

    @PostMapping("/relationships/create")
    @Operation(description = "Store Relationships", summary = "Endpoint to store relationships.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully wrote relationships", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to write relationships", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public void createRelationship(@Valid @RequestBody RelationshipRequestDto requestBody) {
        createRelationshipProducer.createRelationship(requestBody);
    }
}
