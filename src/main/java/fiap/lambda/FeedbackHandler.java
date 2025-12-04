package fiap.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import fiap.dto.FeedbackRequest;
import fiap.dto.FeedbackResponse;
import fiap.sns.Message;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.Instant;
import java.util.UUID;

public class FeedbackHandler implements RequestHandler<FeedbackRequest, FeedbackResponse> {
    SnsClient snsClient = SnsClient.builder().build();

    @Override
    public FeedbackResponse handleRequest(FeedbackRequest request, Context context) {
        if (request == null) {
            context.getLogger().log("Received null request");
            FeedbackResponse r = new FeedbackResponse(
                    "ERROR",
                    null,
                    "Request is null",
                    0,
                    Instant.now().toString());
            return r;
        }

        String id = UUID.randomUUID().toString();
        context.getLogger().log("Received feedback with ID: " + id);

        FeedbackResponse response = new FeedbackResponse(
                "RECEIVED",
                id,
                request.descricao(),
                request.nota(),
                Instant.now().toString());

        return response;
    }
}
