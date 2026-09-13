package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Director addDirector(Director director) {
        validateDirector(director);
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        validateDirector(director);

        if (!directorStorage.checkingId(director.getId())) {   // ← вот здесь был error
            throw new ValidationNotObjectException("Режиссёр с id: " + director.getId() + " не найден");
        }

        return directorStorage.updateDirector(director);
    }

    public void removeDirector(long directorId) {
        directorStorage.removeDirector(directorId);
    }

    public Director getDirectorById(long directorId) {
        return directorStorage.getDirectorById(directorId);
    }

    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    private void validateDirector(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }
    }
}