package main.exception;

import main.api.responses.GenericResponseObject;

public class BadRequestException extends RuntimeException {

    private GenericResponseObject response;

    public BadRequestException(GenericResponseObject response){
        this.response = response;
        }

    public GenericResponseObject getBody(){
        return response;
    }
}