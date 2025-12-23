package fiap.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import fiap.dto.FeedbackResponse;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@ExtendWith(MockitoExtension.class)
public class FeedbackHandlerTest {

    @Mock
    private SnsClient snsClient;

    private ObjectMapper mapper;
    private FeedbackHandler handler;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        handler = new FeedbackHandler(mapper, snsClient);

        // evita NPE do env var
        System.setProperty("SNS_TOPIC_ARN", "arn:aws:sns:us-east-1:123456789:feedback");
    }

    @Test
    void shouldReturn200AndPublishMessage() throws Exception {
        // given
        String requestJson = """
                {
                  "descricao": "Muito bom",
                  "nota": 5
                }
                """;
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setBody(requestJson);

        when(snsClient.publish(
                ArgumentMatchers.<Consumer<PublishRequest.Builder>>any()))
                .thenReturn(PublishResponse.builder().messageId("123").build());

        // when
        APIGatewayProxyResponseEvent response = handler.handleRequest(event, mock(Context.class));

        // then
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));

        FeedbackResponse body = mapper.readValue(response.getBody(), FeedbackResponse.class);

        assertEquals("RECEIVED", body.status());
        assertEquals("Muito bom", body.descricao());
        assertEquals(5, body.nota());
        assertNotNull(body.id());
        assertNotNull(body.receivedAt());

        verify(snsClient, times(1)).publish(
                ArgumentMatchers.<Consumer<PublishRequest.Builder>>any());
    }

    @Test
    void shouldReturn400WhenInvalidJson() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setBody("{ invalid json ");

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, mock(Context.class));

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
    }

}
