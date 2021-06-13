package main.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Пользователь не авторизован или не является модератором")
public class UnauthorizedException extends RuntimeException{
}
