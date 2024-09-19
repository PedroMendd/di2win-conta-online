package com.di2win.contaonline.controller;

import com.di2win.contaonline.dto.ClientCreationDTO;
import com.di2win.contaonline.dto.ClientResponseDTO;
import com.di2win.contaonline.entity.Client;
import com.di2win.contaonline.exception.client.ClientNotFoundException;
import com.di2win.contaonline.exception.cpf.CpfAlreadyExistsException;
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

    @BeforeEach
    void setUp() {
        ClientResponseDTO clientResponseDTO = new ClientResponseDTO();
        clientResponseDTO.setId(1L);
        clientResponseDTO.setCpf("06915290435");
        clientResponseDTO.setNome("Pedro Mend");
        clientResponseDTO.setDataNascimento(LocalDate.of(1988, 6, 20));

        ClientCreationDTO clientCreationDTO = new ClientCreationDTO();
        clientCreationDTO.setCpf("06915290435");
        clientCreationDTO.setNome("Pedro Mend");
        clientCreationDTO.setDataNascimento(LocalDate.of(1988, 6, 20));
    }

    @Test
    void testCreateClientSuccess() throws Exception {
        Client client = new Client();
        client.setCpf("06915290435");
        client.setNome("Pedro Mend");
        client.setDataNascimento(LocalDate.of(1988, 6, 20));

        when(clientService.createClient(Mockito.any(ClientCreationDTO.class))).thenReturn(client);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"cpf\": \"06915290435\", \"nome\": \"Pedro Mend\", \"dataNascimento\": \"1988-06-20\" }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("06915290435"))
                .andExpect(jsonPath("$.nome").value("Pedro Mend"));

        verify(clientService).createClient(Mockito.any(ClientCreationDTO.class));
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

    @Test
    void testCreateClientThrowsCpfAlreadyExistsException() throws Exception {
        Mockito.doThrow(new CpfAlreadyExistsException("CPF já cadastrado"))
                .when(clientService).createClient(Mockito.any(ClientCreationDTO.class));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"cpf\": \"06915290435\", \"nome\": \"Pedro Mend\", \"dataNascimento\": \"1988-06-20\" }"))
                .andExpect(status().isConflict())
                .andExpect(content().string("CPF já cadastrado"));
    }

    @Test
    void testCreateClientThrowsInvalidCpfFormatException() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"cpf\": \"12345678\", \"nome\": \"Pedro Mend\", \"dataNascimento\": \"1988-06-20\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cpf").value("O CPF deve conter 11 dígitos"));
    }


    @Test
    void testCreateClientWithEmptyName() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"cpf\": \"06915290435\", \"nome\": \"\", \"dataNascimento\": \"1988-06-20\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nome").value("O nome é obrigatório"));
    }


    @Test
    void testCreateClientWithNullBirthDate() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"cpf\": \"06915290435\", \"nome\": \"Pedro Mend\", \"dataNascimento\": null }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.dataNascimento").value("A data de nascimento é obrigatória"));
    }

}
