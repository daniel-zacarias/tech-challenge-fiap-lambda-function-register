package fiap.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import fiap.dto.FeedbackRequest;
import fiap.dto.FeedbackResponse;
import fiap.sns.Message;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.Instant;
import java.util.UUID;

public class FeedbackHandler implements RequestHandler<APIGatewayV2HTTPEvent, FeedbackResponse> {
    SnsClient snsClient = SnsClient.builder().build();

    @Override
    public FeedbackResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        if (event == null) {
            context.getLogger().log("Received null request");
            FeedbackResponse r = new FeedbackResponse("ERROR", null, Instant.now().toString());
            return r;
        }
        String body = event.getBody();
        ObjectMapper mapper = new ObjectMapper();
        FeedbackRequest request = mapper.convertValue(body, FeedbackRequest.class);

        String id = UUID.randomUUID().toString();
        context.getLogger().log("Received feedback with ID: " + id);
        try {
            Message message = new Message(id, request.descricao(), request.nota());
            String json = new ObjectMapper().writeValueAsString(message);

            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(System.getenv("SNS_TOPIC_ARN"))
                    .message(json)
                    .build();
            snsClient.publish(publishRequest);
        } catch (Exception e) {
            context.getLogger().log("Error publishing message to SNS: " + e.getMessage());
            FeedbackResponse r = new FeedbackResponse("ERROR", null, Instant.now().toString());
            return r;
        }

        FeedbackResponse response = new FeedbackResponse("RECEIVED", id, Instant.now().toString());

        return response;
    }
}
