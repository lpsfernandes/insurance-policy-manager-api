package io.manager.policy.domain.exception;

import lombok.Getter;

@Getter
public class ProductNotFound extends RuntimeException {
    private final String message = "Produto informado na apolice nao existe";
}
