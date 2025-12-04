package fiap.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import fiap.dto.FeedbackRequest;
import fiap.dto.FeedbackResponse;

import java.util.Map;
import java.util.UUID;

public class FeedbackHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            FeedbackRequest req = mapper.readValue(event.getBody(), FeedbackRequest.class);

            String id = UUID.randomUUID().toString();
            String receivedAt = java.time.Instant.now().toString();
            FeedbackResponse response = new FeedbackResponse("RECEIVED", id, req.descricao(), req.nota(), receivedAt);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(mapper.writeValueAsString(response))
                    .withHeaders(Map.of("Content-Type", "application/json"));

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("{\"error\":\"" + e.getMessage() + "\"}")
                    .withHeaders(Map.of("Content-Type", "application/json"));
        }
    }
}
