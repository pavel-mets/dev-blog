package main.exceptions;

import main.api.responses.RegisterDTO;

public class RegisterException extends RuntimeException {

    private RegisterDTO registerDTO = new RegisterDTO();

    public RegisterException(RegisterDTO.Errors errors){
        registerDTO.setErrors(errors);
        }

    public RegisterDTO getBody(){
        return registerDTO;
    }

}

