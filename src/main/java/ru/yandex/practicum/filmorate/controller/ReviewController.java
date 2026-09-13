package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review createReview(@Valid @RequestBody NewReviewRequest request) {
        log.info("Получен запрос POST /reviews на создание отзыва: {}", request);
        return reviewService.addReview(request);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody UpdateReviewRequest request) {
        log.info("Получен запрос PUT /reviews на обновление отзыва: {}", request);
        return reviewService.updateReview(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteReview(@PathVariable long id) {
        log.info("Получен запрос DELETE /reviews/{}", id);
        reviewService.removeReview(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable long id) {
        log.info("Получен запрос GET /reviews/{}", id);
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) Long filmId,
                                   @RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос GET /reviews с filmId={}, count={}", filmId, count);
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос PUT /reviews/{}/like/{}", id, userId);
        reviewService.addLikeToReview(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addDislike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос PUT /reviews/{}/dislike/{}", id, userId);
        reviewService.addLikeToReview(id, userId, false);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос DELETE /reviews/{}/like/{}", id, userId);
        reviewService.deleteLikeFromReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteDislike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос DELETE /reviews/{}/dislike/{}", id, userId);
        reviewService.deleteLikeFromReview(id, userId);
    }
}