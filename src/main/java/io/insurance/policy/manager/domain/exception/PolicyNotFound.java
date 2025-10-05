package io.insurance.policy.manager.domain.exception;

public class PolicyNotFound extends RuntimeException {
    public PolicyNotFound(String id){
        super(String.format("Apolice %s nao encontrada", id));
    }
}
