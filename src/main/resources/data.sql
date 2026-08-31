MERGE INTO genres KEY (genre_id, name) VALUES (1, 'Комедия');
MERGE INTO genres KEY (genre_id, name) VALUES (2, 'Драма');
MERGE INTO genres KEY (genre_id, name) VALUES (3, 'Мультфильм');
MERGE INTO genres KEY (genre_id, name) VALUES (4, 'Триллер');
MERGE INTO genres KEY (genre_id, name) VALUES (5, 'Документальный');
MERGE INTO genres KEY (genre_id, name) VALUES (6, 'Боевик');

MERGE INTO rating KEY (rating_id, name_rating) VALUES (1, 'G');
MERGE INTO rating KEY (rating_id, name_rating) VALUES (2, 'PG');
MERGE INTO rating KEY (rating_id, name_rating) VALUES (3, 'PG-13');
MERGE INTO rating KEY (rating_id, name_rating) VALUES (4, 'R');
MERGE INTO rating KEY (rating_id, name_rating) VALUES (5, 'NC-17');