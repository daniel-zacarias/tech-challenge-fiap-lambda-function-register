package fiap.dto;

import io.smallrye.common.constraint.NotNull;

public record FeedbackRequest(
        @NotNull String descricao,
        @NotNull int nota) {

    public FeedbackRequest {
        if (nota < 1 || nota > 10) {
            throw new IllegalArgumentException("Nota must be between 1 and 5");
        }
    }
}
