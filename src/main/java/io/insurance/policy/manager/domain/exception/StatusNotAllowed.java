package io.insurance.policy.manager.domain.exception;

import io.insurance.policy.manager.domain.model.enums.Status;

public class StatusNotAllowed extends RuntimeException {
    public StatusNotAllowed(Status status, Status newStatus){
        super(String.format("Status atual %s nao pode ser atualizado para o status %s", status, newStatus));
    }
}
