package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SystemErrorException extends RuntimeException {
    public SystemErrorException(String message) {
        super(message);
    }
}
