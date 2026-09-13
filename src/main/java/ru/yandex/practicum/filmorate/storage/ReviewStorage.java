package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review review);

    void removeReview(long reviewId);

    Review getReviewById(long reviewId);

    List<Review> getReviewsByFilmId(Long filmId, int count);

    boolean checkingId(long reviewId);

    void addLikeToReview(long reviewId, long userId, boolean isUseful);

    void deleteLikeFromReview(long reviewId, long userId);

    void recalculateUseful(long reviewId);
}