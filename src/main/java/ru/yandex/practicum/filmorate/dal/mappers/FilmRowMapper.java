package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
@AllArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final MpaRowMapper mpaRowMapper;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getLong("duration"))
                .mpa(mpaRowMapper.mapRow(rs, rowNum))
                .genres(new ArrayList<>())
                .build();
    }
}
