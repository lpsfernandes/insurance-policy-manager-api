package io.insurance.policy.manager.domain.exception;

import lombok.Getter;

@Getter
public class CreatePolicyException extends RuntimeException {
    private final String message = "Ocorreu um erro durante a tentativa de cadastro da apolice";
}
