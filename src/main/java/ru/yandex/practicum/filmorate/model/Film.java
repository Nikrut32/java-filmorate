package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private Set<User> usersWhoLiked;
}
