# RBPO

Spring Boot 4.1.1, Java 21, Maven. Тематика — вариант 23 «Планирование астрономических наблюдений».

## Эндпоинты

| Метод | URL | Что возвращает |
|---|---|---|
| GET | `/api/greeting?name=Галилей` | текст: `Ясного неба, Галилей!` |
| GET | `/api/messier?from=1&to=10` | числа: номера объектов каталога Мессье (1..110) — `[1,2,...,10]` |

## Запуск

```bash
./mvnw spring-boot:run   # Windows: mvnw.cmd spring-boot:run
./mvnw test
```
