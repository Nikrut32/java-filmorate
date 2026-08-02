package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ValidationNotObjectException extends RuntimeException {
    public ValidationNotObjectException(String message) {
        super(message);
    }
}
