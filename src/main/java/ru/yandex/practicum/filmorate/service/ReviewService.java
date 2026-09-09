package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    private final UserStorage userStorage;
    private final ReviewStorage reviewStorage;

    public void addLike(long reviewId, long userId, boolean grade) {
        log.trace("Вход в метод addLike с параметрами reviewId={}, userId={}", reviewId, userId);

        if (!reviewStorage.checkingId(reviewId)) {
            log.warn("Попытка добавить лайк к несуществующему отзыву с id={}", reviewId);
            throw new ValidationNotObjectException("Отзыв с таким ID: " + reviewId + " не найден");
        }
        if (!userStorage.checkingId(userId)) {
            log.warn("Попытка добавить лайк от несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        reviewStorage.addLikeOrDislikeReview(reviewId, userId, grade);

    }

    public void deleteLike(long reviewId, long userId) {
        log.trace("Вход в метод deleteLike с параметрами reviewId={}, userId={}", reviewId, userId);
        if (!reviewStorage.checkingId(reviewId)) {
            log.warn("Попытка удалить лайк у несуществующего отзыва с id={}", reviewId);
            throw new ValidationNotObjectException("Отзыв с таким ID:" + reviewId + " не найден");
        }
        if (!userStorage.checkingId(userId)) {
            log.warn("Попытка удалить лайк от несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        reviewStorage.removeLikeOrDislikeReview(reviewId, userId);
    }

    public Review updateReview(UpdateReviewRequest updateReview) {
        Review review = reviewStorage.getReviewById(updateReview.getId());
        if (updateReview.hasContent()) {
            review.setContent(updateReview.getContent());
        }
        if (updateReview.hasIsPositive()) {
            review.setIsPositive(updateReview.getIsPositive());
        }
        if (updateReview.hasFilmId()) {
            review.setFilmId(updateReview.getFilmId());
        }
        if (updateReview.hasUserId()) {
            review.setUserId(updateReview.getUserId());
        }

        return reviewStorage.updateReview(review);
    }
}
