package ru.mtuci.rbpo.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;
import ru.mtuci.rbpo.model.User;
import ru.mtuci.rbpo.model.UserRole;
import ru.mtuci.rbpo.model.UserStatus;
import ru.mtuci.rbpo.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SecurityApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void createAdmin() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setFullName("Администратор");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
    }

    @Test
    void registrationCsrfAndBasicAuthenticationWorkTogether() throws Exception {
        CsrfData csrf = getCsrf();

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registration("newton", "Newton123!")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Отсутствует или неверен CSRF-токен"));

        mockMvc.perform(withCsrf(post("/api/auth/register"), csrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration("weak", "123")))
                .andExpect(status().isBadRequest());

        register("newton", "Newton123!", csrf);

        mockMvc.perform(get("/api/telescopes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Требуется аутентификация"));

        mockMvc.perform(get("/api/telescopes").with(httpBasic("newton", "Newton123!")))
                .andExpect(status().isOk());

        User blocked = new User();
        blocked.setUsername("blocked");
        blocked.setFullName("Заблокированный пользователь");
        blocked.setEmail("blocked@example.com");
        blocked.setPasswordHash(passwordEncoder.encode("Blocked123!"));
        blocked.setRole(UserRole.OBSERVER);
        blocked.setStatus(UserStatus.BLOCKED);
        userRepository.save(blocked);

        mockMvc.perform(get("/api/telescopes").with(httpBasic("blocked", "Blocked123!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Требуется аутентификация"));
    }

    @Test
    void rolesAndSessionOwnerLimitOperations() throws Exception {
        CsrfData csrf = getCsrf();
        long newtonId = register("newton", "Newton123!", csrf);
        register("kepler2", "Kepler123!", csrf);

        mockMvc.perform(withCsrf(post("/api/celestial-objects"), csrf)
                        .with(httpBasic("newton", "Newton123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Марс","type":"PLANET","minAperture":50}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Недостаточно прав для выполнения операции"));

        long objectId = createAsAdmin("/api/celestial-objects", """
                {"name":"Марс","type":"PLANET","minAperture":50}
                """, csrf);
        long telescopeId = createAsAdmin("/api/telescopes", """
                {"name":"Рефрактор 90","aperture":90}
                """, csrf);

        String sessionBody = """
                {"celestialObjectId":%d,"telescopeId":%d,"observerId":%d,
                 "startTime":"2026-12-01T20:00:00Z","endTime":"2026-12-01T21:00:00Z"}
                """.formatted(objectId, telescopeId, newtonId);
        String body = mockMvc.perform(withCsrf(post("/api/observation-sessions"), csrf)
                        .with(httpBasic("newton", "Newton123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sessionBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long sessionId = ((Number) JsonPath.read(body, "$.id")).longValue();

        mockMvc.perform(withCsrf(post("/api/observation-sessions/{id}/start", sessionId), csrf)
                        .with(httpBasic("kepler2", "Kepler123!")))
                .andExpect(status().isForbidden());

        mockMvc.perform(withCsrf(post("/api/observation-sessions/{id}/start", sessionId), csrf)
                        .with(httpBasic("newton", "Newton123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    private CsrfData getCsrf() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andReturn();
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        return new CsrfData(token, result.getResponse().getCookie("XSRF-TOKEN"));
    }

    private long register(String username, String password, CsrfData csrf) throws Exception {
        String body = mockMvc.perform(withCsrf(post("/api/auth/register"), csrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration(username, password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("OBSERVER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long createAsAdmin(String url, String json, CsrfData csrf) throws Exception {
        String body = mockMvc.perform(withCsrf(post(url), csrf)
                        .with(httpBasic("admin", "Admin123!"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request, CsrfData csrf) {
        return request.cookie(csrf.cookie()).header("X-XSRF-TOKEN", csrf.token());
    }

    private String registration(String username, String password) {
        return """
                {"username":"%s","fullName":"Test User","email":"%s@example.com","password":"%s"}
                """.formatted(username, username, password);
    }

    private record CsrfData(String token, Cookie cookie) {
    }
}
