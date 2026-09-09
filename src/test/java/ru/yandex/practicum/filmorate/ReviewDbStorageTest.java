package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({ReviewRowMapper.class, ReviewDbStorage.class, UserDbStorage.class, FilmDbStorage.class, FilmRowMapper.class,
        UserRowMapper.class, MpaRowMapper.class, MpaDbStorage.class, GenreDbStorage.class, GenreRowMapper.class,
        DirectorDbStorage.class, DirectorRowMapper.class, LongRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewDbStorageTest {
    private final ReviewDbStorage reviewDbStorage;

    @Test
    void addReviewValidSavesTest() {
        Review savedReview = reviewDbStorage.addReview(createTestPreview());

        assertThat(savedReview).isNotNull();
        assertThat(savedReview.getReviewId()).isPositive();
        assertThat(savedReview.getUseful()).isEqualTo(0L);
    }

    @Test
    void addReviewValidationFailsThrowsTest() {
        Review testReview = createTestPreview();
        testReview.setContent(null);
        assertThatThrownBy(() -> reviewDbStorage.addReview(testReview))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addReviewUserNotFoundThrowsTest() {
        Review testReview = createTestPreview();
        testReview.setUserId(999L);
        assertThatThrownBy(() -> reviewDbStorage.addReview(testReview))
                .isInstanceOf(ValidationNotObjectException.class)
                .hasMessageContaining("Пользователь с таким ID: 999 не найден");
    }

    @Test
    void updateReviewValidDataTest() {
        Review savedReview = reviewDbStorage.addReview(createTestPreview());
        savedReview.setContent("Обновленный отзыв");
        savedReview.setIsPositive(false);

        Review updatedReview = reviewDbStorage.updateReview(savedReview);

        assertThat(updatedReview.getContent()).isEqualTo("Обновленный отзыв");
        assertThat(updatedReview.getIsPositive()).isFalse();
    }

    @Test
    void removeReviewExistsDeletesTest() {
        Review savedReview = reviewDbStorage.addReview(createTestPreview());
        reviewDbStorage.removeReview(savedReview.getReviewId());
        assertThat(reviewDbStorage.checkingId(savedReview.getReviewId())).isFalse();
    }

    @Test
    void getReviewByIdExistsReturnsTest() {
        Review savedReview = reviewDbStorage.addReview(createTestPreview());
        Review foundReview = reviewDbStorage.getReviewById(savedReview.getReviewId());
        assertThat(foundReview).isNotNull();
    }

    @Test
    void getReviewByIdMissingThrowsTest() {
        assertThatThrownBy(() -> reviewDbStorage.getReviewById(999L))
                .isInstanceOf(ValidationNotObjectException.class)
                .hasMessageContaining("Отзыв с id: 999 не найден");
    }

    @Test
    void getReviewsByFilmIdTest() {
        reviewDbStorage.addReview(createTestPreview());
        Review anotherReview = Review.builder()
                .filmId(2L)
                .userId(1L)
                .content("Отзыв на другой фильм")
                .isPositive(true)
                .build();
        reviewDbStorage.addReview(anotherReview);

        List<Review> reviews = reviewDbStorage.getReviewStorage(1L, 10L);

        assertThat(reviews).isNotEmpty();
        assertThat(reviews).allMatch(review -> review.getFilmId() == 1L);
    }

    @Test
    void updateReviewUsefulTest() {
        Review savedReview = reviewDbStorage.addReview(createTestPreview());

        reviewDbStorage.addLikeOrDislikeReview(savedReview.getReviewId(), 1L, true);
        Review updatedReview = reviewDbStorage.getReviewById(savedReview.getReviewId());
        assertThat(updatedReview.getUseful()).isEqualTo(1L);

        reviewDbStorage.addLikeOrDislikeReview(savedReview.getReviewId(), 1L, false);
        updatedReview = reviewDbStorage.getReviewById(savedReview.getReviewId());
        assertThat(updatedReview.getUseful()).isEqualTo(-1L);
    }

    @Test
    void removeReviewGradeTest() {
        Review savedReview = reviewDbStorage.addReview(createTestPreview());
        reviewDbStorage.addLikeOrDislikeReview(savedReview.getReviewId(), 1L, true);
        reviewDbStorage.removeLikeOrDislikeReview(savedReview.getReviewId(), 1L);

        Review updatedReview = reviewDbStorage.getReviewById(savedReview.getReviewId());
        assertThat(updatedReview.getUseful()).isEqualTo(0L);
    }

    private Review createTestPreview() {
        return Review.builder()
                .filmId(1L)
                .userId(1L)
                .content("Отличный фильм!")
                .isPositive(true)
                .build();
    }
}
