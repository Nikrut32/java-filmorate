package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
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
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties("friends")
    private Set<User> friends;
}
