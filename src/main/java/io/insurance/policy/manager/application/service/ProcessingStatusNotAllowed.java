package io.insurance.policy.manager.application.service;

public class ProcessingStatusNotAllowed extends RuntimeException {
    public ProcessingStatusNotAllowed(ProcessingStatus status, ProcessingStatus newStatus) {
        super(String.format("Status de processamento atual %s nao pode ser atualizado para o status %s", status, newStatus));
    }
}
