package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage {

    private static final String INSERT_QUERY = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE review_id = ?";
    private static final String GET_BY_ID_QUERY = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String GET_BY_FILM_QUERY = "SELECT * FROM reviews WHERE film_id = ? " +
            "ORDER BY useful DESC LIMIT ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
    private static final String MERGE_LIKE_QUERY = "MERGE INTO review_likes (review_id, user_id, is_useful) " +
            "KEY (review_id, user_id) VALUES (?, ?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
    private static final String RECALCULATE_USEFUL_QUERY = "UPDATE reviews SET useful = " +
            "(SELECT COALESCE(SUM(CASE WHEN is_useful = TRUE THEN 1 ELSE -1 END), 0) " +
            "FROM review_likes WHERE review_id = ?) WHERE review_id = ?";

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> rowMapper) {
        super(jdbc, rowMapper);
    }

    @Override
    public Review addReview(Review review) {
        int useful = review.getUseful() == null ? 0 : review.getUseful();
        long id = insert(INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                useful);
        review.setReviewId(id);
        review.setUseful(useful);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        update(UPDATE_QUERY, review.getContent(), review.getIsPositive(), review.getReviewId());
        return getReviewById(review.getReviewId());
    }

    @Override
    public void removeReview(long reviewId) {
        delete(checkingId(reviewId), DELETE_QUERY, reviewId);
    }

    @Override
    public Review getReviewById(long reviewId) {
        return findOne(GET_BY_ID_QUERY, reviewId)
                .orElseThrow(() -> new ValidationNotObjectException("Отзыв с id: " + reviewId + " не найден"));
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        if (filmId == null) {
            return findAll(GET_ALL_QUERY, count);
        }
        return findAll(GET_BY_FILM_QUERY, filmId, count);
    }

    @Override
    public boolean checkingId(long reviewId) {
        Optional<Review> review = findOne(GET_BY_ID_QUERY, reviewId);
        return review.isPresent();
    }

    @Override
    public void addLikeToReview(long reviewId, long userId, boolean isUseful) {
        insertNotId(MERGE_LIKE_QUERY, reviewId, userId, isUseful);
        recalculateUseful(reviewId);
    }

    @Override
    public void deleteLikeFromReview(long reviewId, long userId) {
        delete(true, DELETE_LIKE_QUERY, reviewId, userId);
        recalculateUseful(reviewId);
    }

    @Override
    public void recalculateUseful(long reviewId) {
        jdbc.update(RECALCULATE_USEFUL_QUERY, reviewId, reviewId);
    }
}