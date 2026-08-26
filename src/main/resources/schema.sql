DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS rating CASCADE;
DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS liked_film CASCADE;
DROP TABLE IF EXISTS user_friends CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS users CASCADE;


CREATE TABLE genres (
    genre_id INTEGER NOT NULL AUTO_INCREMENT,
    name CHARACTER VARYING,
    CONSTRAINT CONSTRAINT_5D PRIMARY KEY (genre_id)
);

CREATE TABLE rating (
    rating_id INTEGER NOT NULL AUTO_INCREMENT,
    name_rating CHARACTER VARYING,
    CONSTRAINT CONSTRAINT_C PRIMARY KEY (rating_id)
);

CREATE TABLE users (
    user_id INTEGER NOT NULL AUTO_INCREMENT,
    name CHARACTER VARYING,
    email CHARACTER VARYING NOT NULL,
    login CHARACTER VARYING NOT NULL,
    birthday DATE,
    CONSTRAINT CONSTRAINT_6 PRIMARY KEY (user_id)
);

CREATE TABLE films (
    film_id INTEGER NOT NULL AUTO_INCREMENT,
    name CHARACTER VARYING NOT NULL,
    description CHARACTER VARYING,
    release_date DATE,
    duration INTEGER,
    rating_id INTEGER NOT NULL,
    CONSTRAINT CONSTRAINT_5 PRIMARY KEY (film_id),
    CONSTRAINT CONSTRAINT_5C FOREIGN KEY (rating_id) REFERENCES rating(rating_id)
);

CREATE TABLE liked_film (
    film_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, user_id),
    CONSTRAINT CONSTRAINT_D FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE,
    CONSTRAINT CONSTRAINT_D9 FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE user_friends (
    user_id INTEGER NOT NULL,
    friend_id INTEGER NOT NULL,
    status BOOLEAN,
    CONSTRAINT CONSTRAINT_3 FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT CONSTRAINT_37 FOREIGN KEY (friend_id) REFERENCES users(user_id)
);

CREATE TABLE film_genres (
    film_id INTEGER NOT NULL,
    genre_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    CONSTRAINT CONSTRAINT_A FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE,
    CONSTRAINT CONSTRAINT_A3 FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
);

INSERT INTO genres (name) VALUES
('Комедия'), ('Драма'), ('Мультфильм'), ('Триллер'),
('Документальный'), ('Боевик');

INSERT INTO rating (name_rating) VALUES
('G'), ('PG'), ('PG-13'), ('R'), ('NC-17');

