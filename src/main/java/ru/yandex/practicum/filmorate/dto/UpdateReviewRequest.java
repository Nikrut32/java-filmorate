package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class UpdateReviewRequest {
    private Long id;
    private String content;
    private Boolean isPositive;
    private Long filmId;
    private Long userId;

    public boolean hasContent() { return !(content == null || content.isBlank()); }

    public boolean hasIsPositive() { return !(isPositive == null); }

    public boolean hasFilmId() { return !(filmId == null); }

    public boolean hasUserId() { return !(userId == null); }
}
