package com.jackops.rotavital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class InterfaceWebTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveServirAPaginaInicial() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("<title>Rota Vital</title>")));
    }

    @Test
    void deveServirEstiloEScriptDaInterface() throws Exception {
        mockMvc.perform(get("/css/rotavital.css")).andExpect(status().isOk());
        mockMvc.perform(get("/js/rotavital.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("/api/bolsas")));
    }
}
