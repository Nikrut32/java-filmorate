package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class User {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @Email
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
