package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.LongRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage {
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private LongRowMapper rowMapperLong;

    private static final String INSERT_QUERY = "INSERT INTO reviews (film_id, user_id, content, is_positive) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE reviews SET film_id = ?, user_id = ?, content = ?" +
            ", is_positive = ? WHERE review_id = ?";
    private static final String GET_BY_ID_QUERY = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM reviews WHERE review_id = ?";
    private static final String GET_ALL_FILM_ID_QUERY = "SELECT * FROM reviews WHERE film_id = ? " +
            "ORDER BY useful DESC LIMIT ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO grade_reviews (review_id, user_id, is_helpful) " +
            "VALUES (?, ?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM grade_reviews WHERE review_id = ? AND user_id = ?";
    private static final String GET_COUNT_LIKE_QUERY = "SELECT COUNT(*) FROM grade_reviews " +
            "WHERE is_helpful AND review_id = ?";
    private static final String GET_COUNT_DISLIKE_QUERY = "SELECT COUNT(*) FROM grade_reviews " +
            "WHERE NOT is_helpful AND review_id = ?";
    private static final String UPDATE_USEFUL_QUERY = "UPDATE reviews SET useful = ? WHERE review_id = ?";

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> rowMapper) {
        super(jdbc, rowMapper);
    }

    @Override
    public Review addReview(Review review) {
        exceptionReview(review);

        if (!userStorage.checkingId(review.getUserId())) {
            throw new ValidationNotObjectException("Пользователь с таким ID: " + review.getUserId() + " не найден");
        }
        if (!filmStorage.checkingId(review.getFilmId())) {
            throw new ValidationNotObjectException("Фильм с таким ID: " + review.getFilmId() + " не найден");
        }

        long id = insert(INSERT_QUERY,
                review.getFilmId(),
                review.getUserId(),
                review.getContent(),
                review.getIsPositive());

        review.setReviewId(id);
        review.setUseful(0L);

        return review;
    }

    @Override
    public Review updateReview(Review updateReview) {
        if (!userStorage.checkingId(updateReview.getUserId())) {
            throw new ValidationNotObjectException("Пользователь с таким ID: " + updateReview.getUserId() + " не найден");
        }

        update(UPDATE_QUERY,
                updateReview.getFilmId(),
                updateReview.getUserId(),
                updateReview.getContent(),
                updateReview.getIsPositive(),
                updateReview.getReviewId());

        return getReviewById(updateReview.getReviewId());
    }

    @Override
    public void removeReview(long reviewId) {
        delete(checkingId(reviewId), DELETE_BY_ID_QUERY, reviewId);
    }

    @Override
    public List<Review> getReviewStorage(long filmId, long count) {
        if (count <= 0) {
            log.warn("Некорректное значение count={}", count);
            throw new ValidationException("Количество отзывов не может быть ноль или меньше ноля");
        }
        if (filmId == 0) {
            return findAll(GET_ALL_QUERY, count);
        }
        return findAll(GET_ALL_FILM_ID_QUERY, filmId, count);
    }

    @Override
    public Review getReviewById(long reviewId) {
        return findOne(GET_BY_ID_QUERY, reviewId)
                .orElseThrow(() -> new ValidationNotObjectException("Отзыв с id: " + reviewId + " не найден"));
    }

    @Override
    public void addLikeOrDislikeReview(long reviewId, long userId, boolean grade) {
        try {
            insertNotId(ADD_LIKE_QUERY, reviewId, userId, grade);
            updateUsefulReview(reviewId);
        } catch (DuplicateKeyException ignored) {
            removeLikeOrDislikeReview(reviewId, userId);
            addLikeOrDislikeReview(reviewId, userId, grade);
        }
    }

    @Override
    public void removeLikeOrDislikeReview(long reviewId, long userId) {
        delete((checkingId(reviewId) && userStorage.checkingId(userId)), DELETE_LIKE_QUERY, reviewId, userId);
        updateUsefulReview(reviewId);
    }

    @Override
    public void updateUsefulReview(long reviewId) {
        long countLike = getCountLike(GET_COUNT_LIKE_QUERY, reviewId).orElse(0L);
        long countDislike = getCountLike(GET_COUNT_DISLIKE_QUERY, reviewId).orElse(0L);
        long useful = countLike - countDislike;
        update(UPDATE_USEFUL_QUERY, useful, reviewId);
    }

    @Override
    public boolean checkingId(long reviewId) {
        return findOne(GET_BY_ID_QUERY, reviewId).isPresent();
    }

    private void exceptionReview(Review review) {
        if (review.getUserId() == null) {
            log.warn("Валидация не пройдена: id пользователя не было указано");
            throw new ValidationException("id пользователя не было указано");
        }

        if (review.getContent() == null) {
            log.warn("Валидация не пройдена: текст отзыва не указан");
            throw new ValidationException("Тест отзыва не указан");
        }

        if (review.getContent().isBlank()) {
            log.warn("Валидация не пройдена: текст отзыва пустой");
            throw new ValidationException("Тест отзыва пустой");
        }

        if (review.getIsPositive() == null) {
            log.warn("Валидация не пройдена: тип отзыва не указан");
            throw new ValidationException("Тип отзыва не указан");
        }
    }

    private Optional<Long> getCountLike(String query, long reviewId) {
        try {
            Long count = jdbc.queryForObject(query, rowMapperLong, reviewId);
            return Optional.ofNullable(count);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
