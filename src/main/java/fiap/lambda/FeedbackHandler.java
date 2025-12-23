package fiap.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import fiap.dto.FeedbackRequest;
import fiap.dto.FeedbackResponse;
import software.amazon.awssdk.services.sns.SnsClient;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

public class FeedbackHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    ObjectMapper mapper;
    SnsClient snsClient;

    public FeedbackHandler() {
        this(new ObjectMapper(), SnsClient.builder().build());
    }

    public FeedbackHandler(ObjectMapper mapper, SnsClient snsClient) {
        this.mapper = mapper;
        this.snsClient = snsClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            FeedbackRequest req = mapper.readValue(event.getBody(), FeedbackRequest.class);

            String id = UUID.randomUUID().toString();
            String receivedAt = OffsetDateTime
                    .now(ZoneId.of("America/Sao_Paulo"))
                    .toString();
            FeedbackResponse response = new FeedbackResponse("RECEIVED", id, req.descricao(), req.nota(), receivedAt);
            String bodyResponse = mapper.writeValueAsString(response);
            snsClient.publish(builder -> builder
                    .topicArn(System.getenv("SNS_TOPIC_ARN"))
                    .message(bodyResponse));

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(bodyResponse)
                    .withHeaders(Map.of("Content-Type", "application/json"));

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("{\"error\":\"" + e.getMessage() + "\"}")
                    .withHeaders(Map.of("Content-Type", "application/json"));
        }
    }
}
