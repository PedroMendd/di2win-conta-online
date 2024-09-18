package com.di2win.contaonline.controller;

import com.di2win.contaonline.entity.Client;
import com.di2win.contaonline.exception.client.ClientNotFoundException;
import com.di2win.contaonline.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@WebMvcTest(ClientController.class)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setCpf("06915290435");
        client.setNome("Pedro Mend");
        client.setDataNascimento(LocalDate.of(1988, 6, 20));
    }

    @Test
    void testCreateClientSuccess() throws Exception {
        when(clientService.createClient(Mockito.any(Client.class))).thenReturn(client);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"cpf\": \"06915290435\", \"nome\": \"Pedro Mend\", \"dataNascimento\": \"1988-06-20\" }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("06915290435"))
                .andExpect(jsonPath("$.nome").value("Pedro Mend"));

        verify(clientService).createClient(Mockito.any(Client.class));
    }

    @Test
    void testDeleteClientSuccess() throws Exception {
        mockMvc.perform(delete("/api/clients/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(clientService).removeClientById(1L);
    }

    @Test
    void testDeleteClientThrowsClientNotFoundException() throws Exception {
        Mockito.doThrow(new ClientNotFoundException("Cliente não encontrado"))
                .when(clientService).removeClientById(1L);

        mockMvc.perform(delete("/api/clients/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cliente não encontrado"));

        verify(clientService).removeClientById(1L);
    }

}
