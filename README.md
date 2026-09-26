# rbpo
РБПО БАС2402 Панкова К. П.

Spring Boot 4.1.1, Java 21, Maven. Вариант 23 — «Планирование астрономических наблюдений».

## Запуск

```bash
./mvnw spring-boot:run   # Windows: mvnw.cmd spring-boot:run
./mvnw test
```

Данные хранятся в памяти приложения и сбрасываются при перезапуске.

## Задание 1

| Метод | URL | Что возвращает |
|---|---|---|
| GET | `/api/greeting?name=Галилей` | текст: `Ясного неба, Галилей!` |
| GET | `/api/messier?from=1&to=10` | числа: номера объектов каталога Мессье (1..110) |

## Задание 2

Для каждого ресурса: `GET /api/{resources}`, `GET /api/{resources}/{id}`, `POST /api/{resources}` (201),
`PATCH /api/{resources}/{id}` (200), `DELETE /api/{resources}/{id}` (204).

| Ресурс | Поля |
|---|---|
| `/api/users` | `username`, `fullName`, `email` |
| `/api/celestial-objects` | `name`, `type` (`STAR`, `PLANET`, `MOON`, `GALAXY`, `NEBULA`, `STAR_CLUSTER`, `COMET`, `ASTEROID`, `OTHER`), `minAperture` (мм) |
| `/api/telescopes` | `name`, `aperture` (мм), `available` (по умолчанию `true`) |
| `/api/observation-sessions` | `celestialObjectId`, `telescopeId`, `observerId`, `startTime`, `endTime`, `status` (`PLANNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`); `createdAt` задаёт сервер |
| `/api/observation-records` | `sessionId`, `description`, `quality` (1..5); `recordedAt` задаёт сервер |

Правила:

- апертура телескопа должна быть не меньше `minAperture` объекта;
- `endTime` позже `startTime`; сеансы на одном телескопе не пересекаются по времени (иначе 409);
- новый сеанс нельзя поставить на недоступный телескоп (409);
- удаление пользователя, объекта или телескопа удаляет связанные сеансы, удаление сеанса — его записи;
- ошибки возвращаются в формате `application/problem+json`: 400 — неверные данные, 404 — нет ресурса, 409 — конфликт.

Пример:

```bash
curl -X POST localhost:8080/api/telescopes -H 'Content-Type: application/json' \
     -d '{"name": "Levenhuk 102", "aperture": 102}'
```

## Структура

- `controller` — обработка запросов и маппинг DTO ↔ сущности;
- `service` — бизнес-логика и проверки;
- `repository` — хранилище в памяти;
- `model` — сущности, `dto` — тела запросов и ответов, `exception` — ошибки и их обработчик.
