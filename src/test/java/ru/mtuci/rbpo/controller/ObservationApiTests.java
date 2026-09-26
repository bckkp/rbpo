package ru.mtuci.rbpo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ObservationApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userCrud() throws Exception {
        long id = create("/api/users", """
                {"username": "galileo", "fullName": "Галилео Галилей", "email": "galileo@example.com"}""");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("galileo"));
        mockMvc.perform(patch("/api/users/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName": "Galileo Galilei"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Galileo Galilei"))
                .andExpect(jsonPath("$.email").value("galileo@example.com"));
        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidRequestsAreRejected() throws Exception {
        send(post("/api/users"), """
                {"username": "", "fullName": "X", "email": "x@example.com"}""", status().isBadRequest());
        send(post("/api/celestial-objects"), """
                {"name": "M31", "type": "UFO", "minAperture": 50}""", status().isBadRequest());
        send(post("/api/telescopes"), """
                {"name": "Tiny", "aperture": -1}""", status().isBadRequest());
        send(patch("/api/telescopes/999"), """
                {"name": "Nope"}""", status().isNotFound());
    }

    @Test
    void sessionRulesAndServerTime() throws Exception {
        long user = createUser();
        long saturn = create("/api/celestial-objects", """
                {"name": "Сатурн", "type": "PLANET", "minAperture": 60}""");
        long nebula = create("/api/celestial-objects", """
                {"name": "M57", "type": "NEBULA", "minAperture": 150}""");
        long telescope = create("/api/telescopes", """
                {"name": "Levenhuk 102", "aperture": 102}""");

        mockMvc.perform(get("/api/telescopes/{id}", telescope))
                .andExpect(jsonPath("$.available").value(true));

        // апертуры не хватает для туманности
        send(post("/api/observation-sessions"), session(nebula, telescope, user, "22:00", "23:00"),
                status().isBadRequest());
        // конец раньше начала
        send(post("/api/observation-sessions"), session(saturn, telescope, user, "23:00", "22:00"),
                status().isBadRequest());
        // несуществующий наблюдатель
        send(post("/api/observation-sessions"), session(saturn, telescope, 999, "22:00", "23:00"),
                status().isBadRequest());

        long sessionId = create("/api/observation-sessions", session(saturn, telescope, user, "22:00", "23:00"));
        mockMvc.perform(get("/api/observation-sessions/{id}", sessionId))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.createdAt").exists());

        // пересечение по времени на том же телескопе
        send(post("/api/observation-sessions"), session(saturn, telescope, user, "22:30", "23:30"),
                status().isConflict());

        // время записи задаёт сервер, а не клиент
        mockMvc.perform(post("/api/observation-records").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sessionId": %d, "description": "Хорошо видны кольца", "quality": 5,
                                 "recordedAt": "2000-01-01T00:00:00Z"}""".formatted(sessionId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.recordedAt").value(not("2000-01-01T00:00:00Z")));

        send(post("/api/observation-records"), """
                {"sessionId": %d, "description": "Слишком хорошо", "quality": 6}""".formatted(sessionId),
                status().isBadRequest());

        send(patch("/api/observation-sessions/" + sessionId), """
                {"status": "COMPLETED"}""", status().isOk());
    }

    @Test
    void deletingReferencedEntitiesLeavesNoDanglingReferences() throws Exception {
        long user = createUser();
        long saturn = create("/api/celestial-objects", """
                {"name": "Сатурн", "type": "PLANET", "minAperture": 60}""");
        long jupiter = create("/api/celestial-objects", """
                {"name": "Юпитер", "type": "PLANET", "minAperture": 50}""");
        long telescope = create("/api/telescopes", """
                {"name": "Levenhuk 102", "aperture": 102}""");
        long saturnSession = create("/api/observation-sessions", session(saturn, telescope, user, "20:00", "21:00"));
        long jupiterSession = create("/api/observation-sessions", session(jupiter, telescope, user, "21:00", "22:00"));
        long saturnRecord = create("/api/observation-records", """
                {"sessionId": %d, "description": "Кольца", "quality": 4}""".formatted(saturnSession));
        create("/api/observation-records", """
                {"sessionId": %d, "description": "Большое красное пятно", "quality": 5}""".formatted(jupiterSession));

        // удаление объекта удаляет его сеансы и их записи
        mockMvc.perform(delete("/api/celestial-objects/{id}", saturn)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/observation-sessions/{id}", saturnSession)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/observation-records/{id}", saturnRecord)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/observation-sessions")).andExpect(jsonPath("$", hasSize(1)));

        // удаление пользователя удаляет оставшиеся сеансы и записи
        mockMvc.perform(delete("/api/users/{id}", user)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/observation-sessions")).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/observation-records")).andExpect(jsonPath("$", hasSize(0)));
    }

    private long createUser() throws Exception {
        return create("/api/users", """
                {"username": "kepler", "fullName": "Иоганн Кеплер", "email": "kepler@example.com"}""");
    }

    private static String session(long objectId, long telescopeId, long observerId, String start, String end) {
        return """
                {"celestialObjectId": %d, "telescopeId": %d, "observerId": %d,
                 "startTime": "2026-10-01T%s:00Z", "endTime": "2026-10-01T%s:00Z"}"""
                .formatted(objectId, telescopeId, observerId, start, end);
    }

    private long create(String url, String json) throws Exception {
        String body = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private void send(MockHttpServletRequestBuilder request,
                      String json, ResultMatcher expected) throws Exception {
        mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(expected);
    }
}
