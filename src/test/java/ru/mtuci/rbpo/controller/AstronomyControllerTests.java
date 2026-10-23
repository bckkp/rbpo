package ru.mtuci.rbpo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AstronomyController.class)
class AstronomyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void greetingReturnsText() throws Exception {
        mockMvc.perform(get("/api/greeting").param("name", "Галилей"))
                .andExpect(status().isOk())
                .andExpect(content().string("Ясного неба, Галилей!"));
    }

    @Test
    void messierReturnsNumbers() throws Exception {
        mockMvc.perform(get("/api/messier").param("from", "108").param("to", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value(108))
                .andExpect(jsonPath("$[2]").value(110));
    }
}
