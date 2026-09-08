package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review updateReview);

    void removeReview(long reviewId);

    List<Review> getReviewStorage(long film_id, long count);

    Review getReviewById(long reviewId);

    void addLikeOrDislikeReview(long userId, long reviewId, boolean grade);

    void removeLikeOrDislikeReview(long userId, long reviewId);

    void updateUsefulReview(long reviewId);

    boolean checkingId(long reviewId);
}
