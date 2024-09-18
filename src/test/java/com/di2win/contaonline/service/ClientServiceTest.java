package com.di2win.contaonline.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import com.di2win.contaonline.entity.Client;
import com.di2win.contaonline.exception.client.ClientNotFoundException;
import com.di2win.contaonline.exception.client.InvalidBirthDateException;
import com.di2win.contaonline.exception.client.InvalidNameException;
import com.di2win.contaonline.exception.cpf.CpfAlreadyExistsException;
import com.di2win.contaonline.exception.cpf.InvalidCpfFormatException;
import com.di2win.contaonline.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateClientSuccess(){

        Client client = new Client();
        client.setCpf("06915290435");
        client.setNome("Pedro Mend");
        client.setDataNascimento(LocalDate.of(1988, 6, 20));

        when(clientRepository.findByCpf(client.getCpf())).thenReturn(Optional.empty());

        when(clientRepository.save(client)).thenReturn(client);

        Client savedClient = clientService.createClient(client);

        assertNotNull(savedClient);
        verify(clientRepository).save(client);

    }
    @Test
    void testCreateClientThrowsCpfAlreadyExistsException() {
        Client existingClient = new Client();
        existingClient.setCpf("06915290435");
        existingClient.setNome("Pedro Mend");
        existingClient.setDataNascimento(LocalDate.of(1988, 6, 20));

        when(clientRepository.findByCpf(existingClient.getCpf())).thenReturn(Optional.of(existingClient));

        assertThrows(CpfAlreadyExistsException.class, () -> {
            clientService.createClient(existingClient);
        });
    }

    @Test
    void testCreateClientThrowsInvalidCpfFormatException() {
        Client client = new Client();
        client.setCpf("123456789");
        client.setNome("Pedro Mend");
        client.setDataNascimento(LocalDate.of(1988, 6, 20));

        assertThrows(InvalidCpfFormatException.class, () -> {
            clientService.createClient(client);
        });
    }

    @Test
    void testCreateClientWithEmptyCpf() {
        Client client = new Client();
        client.setCpf("");
        client.setNome("Pedro Mend");
        client.setDataNascimento(LocalDate.of(1988, 6, 20));

        InvalidCpfFormatException exception = assertThrows(InvalidCpfFormatException.class,
                () -> clientService.createClient(client));

        assertEquals("O CPF não pode ser nulo ou vazio.", exception.getMessage());
    }

    @Test
    void testCreateClientWithShortCpf() {
        Client client = new Client();
        client.setCpf("12345678");
        client.setNome("Pedro Mend");
        client.setDataNascimento(LocalDate.of(1988, 6, 20));

        InvalidCpfFormatException exception = assertThrows(InvalidCpfFormatException.class,
                () -> clientService.createClient(client));

        assertEquals("O CPF deve conter 11 dígitos.", exception.getMessage());
    }

    @Test
    void testCreateClientWithLongCpf() {
        Client client = new Client();
        client.setCpf("123456789012");
        client.setNome("Pedro Mend");
        client.setDataNascimento(LocalDate.of(1988, 6, 20));

        InvalidCpfFormatException exception = assertThrows(InvalidCpfFormatException.class,
                () -> clientService.createClient(client));

        assertEquals("O CPF deve conter 11 dígitos.", exception.getMessage());
    }

    @Test
    void testCreateClientWithWrongCpfFirstDigit() {
        Client client = new Client();
        client.setCpf("06915290445");
        client.setNome("Pedro Mend");
        client.setDataNascimento(LocalDate.of(1988, 6, 20));

        InvalidCpfFormatException exception = assertThrows(InvalidCpfFormatException.class,
                () -> clientService.createClient(client));

        assertEquals("O primeiro dígito verificador do CPF é inválido.", exception.getMessage());
    }

    @Test
    void testCreateClientWithWrongCpfSecondDigit() {
        Client client = new Client();
        client.setCpf("06915290436");
        client.setNome("Pedro Mend");
        client.setDataNascimento(LocalDate.of(1988, 6, 20));

        InvalidCpfFormatException exception = assertThrows(InvalidCpfFormatException.class,
                () -> clientService.createClient(client));

        assertEquals("O segundo dígito verificador do CPF é inválido.", exception.getMessage());
    }

    @Test
    void testCreateClientWithNullName() {
        Client client = new Client();
        client.setCpf("06915290435");
        client.setNome(null); // Nome nulo
        client.setDataNascimento(LocalDate.of(1988, 6, 20));

        assertThrows(InvalidNameException.class, () -> {
            clientService.createClient(client);
        });
    }


    @Test
    void testCreateClientWithNullBirthDate() {
        Client client = new Client();
        client.setCpf("06915290435");
        client.setNome("Pedro Mend");
        client.setDataNascimento(null);

        assertThrows(InvalidBirthDateException.class, () -> {
            clientService.createClient(client);
        });
    }




    @Test
    void testRemoveClientByIdSuccess() {
        Long clientId = 1L;
        Client client = new Client();
        client.setId(clientId);
        client.setCpf("06915290435");
        client.setNome("Pedro Mend");
        client.setDataNascimento(LocalDate.of(1988, 6, 20));

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        clientService.removeClientById(clientId);

        verify(clientRepository).delete(client);
    }

    @Test
    void testRemoveClientByIdThrowsClientNotFoundException() {
        Long clientId = 1L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> {
            clientService.removeClientById(clientId);
        });
    }


}
