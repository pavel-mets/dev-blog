package main.exceptions;

import main.api.responses.GenericResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class BadRequestException extends RuntimeException {

    private GenericResponseObject response;

    public BadRequestException(GenericResponseObject response){
        this.response = response;
        }

    public GenericResponseObject getBody(){
        return response;
    }
}