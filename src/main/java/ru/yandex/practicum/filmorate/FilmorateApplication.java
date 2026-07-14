package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class FilmorateApplication {
	public static void main(String[] args) {
		log.info("Запуск приложения Filmorate");
		SpringApplication.run(FilmorateApplication.class, args);
		log.info("Приложение Filmorate успешно запущено и готово к работе!");
	}
}
