package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaStorage {
    public Mpa getRatingById(long ratingId);

    public List<Mpa> getRatingStorage();

    public boolean checkRatingId(long ratingId);
}
