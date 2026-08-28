package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaRowMapper.class, MpaDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTest {
    private final MpaDbStorage mpaDbStorage;

    @Test
    void checkRatingId_ShouldReturnTrueIfExists() {
        assertTrue(mpaDbStorage.checkRatingId(1L));
        assertTrue(mpaDbStorage.checkRatingId(5L));
        assertFalse(mpaDbStorage.checkRatingId(999L));
    }

    @Test
    void getRatingById_ShouldReturnMpa() {
        Mpa mpa = mpaDbStorage.getRatingById(1L);

        assertNotNull(mpa);
        assertEquals(1L, mpa.getId());
        assertEquals("G", mpa.getName());
    }

    @Test
    void getRatingById_WithDifferentId_ShouldReturnCorrectMpa() {
        Mpa mpa = mpaDbStorage.getRatingById(4L);

        assertNotNull(mpa);
        assertEquals(4L, mpa.getId());
        assertEquals("R", mpa.getName());
    }

    @Test
    void getRatingById_NotFound_ShouldThrowException() {
        assertThrows(ValidationNotObjectException.class,
                () -> mpaDbStorage.getRatingById(999L));
    }

    @Test
    void getRatingStorage_ShouldReturnAllRatings() {
        List<Mpa> mpaList = mpaDbStorage.getRatingStorage();

        assertNotNull(mpaList);
        assertEquals(5, mpaList.size());
        assertEquals("G", mpaList.get(0).getName());
        assertEquals("PG", mpaList.get(1).getName());
        assertEquals("PG-13", mpaList.get(2).getName());
        assertEquals("R", mpaList.get(3).getName());
        assertEquals("NC-17", mpaList.get(4).getName());
    }
}
