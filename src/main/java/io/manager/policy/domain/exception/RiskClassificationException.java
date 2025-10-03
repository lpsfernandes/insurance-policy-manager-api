package io.manager.policy.domain.exception;

import lombok.Getter;

@Getter
public class RiskClassificationException extends RuntimeException {
    private final String message = "Não é possível aplicar regras antes da classificação de risco";
}
