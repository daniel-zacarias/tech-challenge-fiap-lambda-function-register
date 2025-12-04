package fiap.dto;

public record FeedbackResponse(String status, String id, String descricao, int nota, String receivedAt) {
}
