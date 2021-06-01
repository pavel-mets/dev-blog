package main.exceptions;

import main.api.responses.GenericResponseObject;
import main.api.responses.RegisterDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(RegisterException.class)
    protected ResponseEntity<RegisterDTO> handleRegisterException (RegisterException ex) {
        return ResponseEntity.ok(ex.getBody());
    }

    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<GenericResponseObject> handleBadRequestException (BadRequestException ex) {
        return new ResponseEntity<>(ex.getBody(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<RegisterDTO> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.ok(new RegisterDTO());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<RegisterDTO> handleAllExceptions(Exception ex, WebRequest request) {
        return ResponseEntity.ok(new RegisterDTO());
    }

}