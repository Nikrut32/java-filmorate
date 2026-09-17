package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {

    Director addDirector(Director director);

    Director updateDirector(Director director);

    void removeDirector(long directorId);

    Director getDirectorById(long directorId);

    List<Director> getDirectors();

    boolean checkingId(long directorId);
}