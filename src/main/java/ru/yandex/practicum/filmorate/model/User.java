package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class User {

    private Long id;
    @Email
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    @JsonIgnore
    private Set<Long> friends;
}
