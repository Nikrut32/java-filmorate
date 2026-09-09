package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Review {
    private Long reviewId;
    private String content;
    private Boolean isPositive;
    private Long filmId;
    private Long userId;
    private Long useful;
}
