package io.insurance.policy.manager.application.controller;

import io.insurance.policy.manager.domain.exception.CreatePolicyException;
import io.insurance.policy.manager.domain.exception.PolicyNotFound;
import io.insurance.policy.manager.domain.exception.StatusNotAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class ErrorAdviceHandler {

    @ExceptionHandler(PolicyNotFound.class)
    public ResponseStatusException handlePolicyNotFoundException(PolicyNotFound e){
        return new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CreatePolicyException.class)
    public ResponseStatusException handleCreatePolicyException(CreatePolicyException e){
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(StatusNotAllowed.class)
    public ResponseStatusException handleStatusNotAllowed(StatusNotAllowed e){
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseStatusException handleException(Exception e){
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

}
