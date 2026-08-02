package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    @EqualsAndHashCode.Exclude
    private Set<User> friends;
}
