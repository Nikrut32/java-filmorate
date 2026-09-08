package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review updateReview);

    void removeReview(long reviewId);

    List<Review> getReviewStorage(long film_id, long count);

    Review getReviewById(long reviewId);

    void addLikeOrDislikeReview(long reviewId, long userId, boolean grade);

    void removeLikeOrDislikeReview(long reviewId, long userId);

    void updateUsefulReview(long reviewId);

    boolean checkingId(long reviewId);
}
