package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedStorage feedStorage;

    @Transactional
    public Review addReview(NewReviewRequest request) {
        if (!userStorage.checkingId(request.getUserId())) {
            throw new ValidationNotObjectException("Пользователь с id: " + request.getUserId() + " не найден");
        }
        if (!filmStorage.checkingId(request.getFilmId())) {
            throw new ValidationNotObjectException("Фильм с id: " + request.getFilmId() + " не найден");
        }

        Review review = Review.builder()
                .content(request.getContent())
                .isPositive(request.getIsPositive())
                .userId(request.getUserId())
                .filmId(request.getFilmId())
                .useful(0)
                .build();

        Review created = reviewStorage.addReview(review);
        feedStorage.addEvent(created.getUserId(), created.getReviewId(),
                EventType.REVIEW.name(), Operation.ADD.name());
        log.info("Отзыв с id={} успешно создан", created.getReviewId());
        return created;
    }

    @Transactional
    public Review updateReview(UpdateReviewRequest request) {
        Review review = reviewStorage.getReviewById(request.getReviewId());

        if (request.hasContent()) {
            review.setContent(request.getContent());
        }
        if (request.hasIsPositive()) {
            review.setIsPositive(request.getIsPositive());
        }

        Review updated = reviewStorage.updateReview(review);
        feedStorage.addEvent(updated.getUserId(), updated.getReviewId(),
                EventType.REVIEW.name(), Operation.UPDATE.name());
        log.info("Отзыв с id={} успешно обновлен", updated.getReviewId());
        return updated;
    }

    @Transactional
    public void removeReview(long reviewId) {
        Review review = reviewStorage.getReviewById(reviewId);
        reviewStorage.removeReview(reviewId);
        feedStorage.addEvent(review.getUserId(), reviewId,
                EventType.REVIEW.name(), Operation.REMOVE.name());
        log.info("Отзыв с id={} успешно удален", reviewId);
    }

    public Review getReviewById(long reviewId) {
        return reviewStorage.getReviewById(reviewId);
    }

    public List<Review> getReviews(Long filmId, int count) {
        if (count <= 0) {
            throw new ValidationException("Количество отзывов должно быть положительным");
        }
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    @Transactional
    public void addLikeToReview(long reviewId, long userId, boolean isUseful) {
        if (!reviewStorage.checkingId(reviewId)) {
            throw new ValidationNotObjectException("Отзыв с id: " + reviewId + " не найден");
        }
        if (!userStorage.checkingId(userId)) {
            throw new ValidationNotObjectException("Пользователь с id: " + userId + " не найден");
        }
        reviewStorage.addLikeToReview(reviewId, userId, isUseful);
    }

    @Transactional
    public void deleteLikeFromReview(long reviewId, long userId) {
        if (!reviewStorage.checkingId(reviewId)) {
            throw new ValidationNotObjectException("Отзыв с id: " + reviewId + " не найден");
        }
        if (!userStorage.checkingId(userId)) {
            throw new ValidationNotObjectException("Пользователь с id: " + userId + " не найден");
        }
        reviewStorage.deleteLikeFromReview(reviewId, userId);
    }
}