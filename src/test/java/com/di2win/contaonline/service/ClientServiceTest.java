package com.di2win.contaonline.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.di2win.contaonline.dto.ClientCreationDTO;
import com.di2win.contaonline.entity.Account;
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

        ClientCreationDTO clientDTO = new ClientCreationDTO();
        clientDTO.setCpf("06915290435");
        clientDTO.setNome("Pedro Mend");
        clientDTO.setDataNascimento(LocalDate.of(1988, 6, 20));

        when(clientRepository.findByCpf(clientDTO.getCpf())).thenReturn(Optional.empty());

        Client client = new Client();
        client.setCpf(clientDTO.getCpf());
        client.setNome(clientDTO.getNome());
        client.setDataNascimento(clientDTO.getDataNascimento());

        when(clientRepository.save(client)).thenReturn(client);

        Client savedClient = clientService.createClient(clientDTO);

        assertNotNull(savedClient);
        verify(clientRepository).save(client);
    }

    @Test
    void testCreateClientThrowsCpfAlreadyExistsException() {
        ClientCreationDTO clientDTO = new ClientCreationDTO();
        clientDTO.setCpf("06915290435");
        clientDTO.setNome("Pedro Mend");
        clientDTO.setDataNascimento(LocalDate.of(1988, 6, 20));

        Client existingClient = new Client();
        existingClient.setCpf(clientDTO.getCpf());

        when(clientRepository.findByCpf(clientDTO.getCpf())).thenReturn(Optional.of(existingClient));

        assertThrows(CpfAlreadyExistsException.class, () -> {
            clientService.createClient(clientDTO);
        });
    }

    @Test
    void testCreateClientThrowsInvalidCpfFormatException() {
        ClientCreationDTO clientDTO = new ClientCreationDTO();
        clientDTO.setCpf("123456789");
        clientDTO.setNome("Pedro Mend");
        clientDTO.setDataNascimento(LocalDate.of(1988, 6, 20));

        assertThrows(InvalidCpfFormatException.class, () -> {
            clientService.createClient(clientDTO);
        });
    }

    @Test
    void testCreateClientWithEmptyCpf() {
        ClientCreationDTO clientDTO = new ClientCreationDTO();
        clientDTO.setCpf("");
        clientDTO.setNome("Pedro Mend");
        clientDTO.setDataNascimento(LocalDate.of(1988, 6, 20));

        InvalidCpfFormatException exception = assertThrows(InvalidCpfFormatException.class,
                () -> clientService.createClient(clientDTO));

        assertEquals("O CPF não pode ser nulo ou vazio.", exception.getMessage());
    }

    @Test
    void testCreateClientWithShortCpf() {
        ClientCreationDTO clientDTO = new ClientCreationDTO();
        clientDTO.setCpf("12345678");
        clientDTO.setNome("Pedro Mend");
        clientDTO.setDataNascimento(LocalDate.of(1988, 6, 20));

        InvalidCpfFormatException exception = assertThrows(InvalidCpfFormatException.class,
                () -> clientService.createClient(clientDTO));

        assertEquals("O CPF deve conter 11 dígitos.", exception.getMessage());
    }

    @Test
    void testCreateClientWithLongCpf() {
        ClientCreationDTO clientDTO = new ClientCreationDTO();
        clientDTO.setCpf("123456789012");
        clientDTO.setNome("Pedro Mend");
        clientDTO.setDataNascimento(LocalDate.of(1988, 6, 20));

        InvalidCpfFormatException exception = assertThrows(InvalidCpfFormatException.class,
                () -> clientService.createClient(clientDTO));

        assertEquals("O CPF deve conter 11 dígitos.", exception.getMessage());
    }

    @Test
    void testCreateClientWithWrongCpfFirstDigit() {
        ClientCreationDTO clientDTO = new ClientCreationDTO();
        clientDTO.setCpf("06915290445");
        clientDTO.setNome("Pedro Mend");
        clientDTO.setDataNascimento(LocalDate.of(1988, 6, 20));

        InvalidCpfFormatException exception = assertThrows(InvalidCpfFormatException.class,
                () -> clientService.createClient(clientDTO));

        assertEquals("O primeiro dígito verificador do CPF é inválido.", exception.getMessage());
    }

    @Test
    void testCreateClientWithWrongCpfSecondDigit() {
        ClientCreationDTO clientDTO = new ClientCreationDTO();
        clientDTO.setCpf("06915290436");
        clientDTO.setNome("Pedro Mend");
        clientDTO.setDataNascimento(LocalDate.of(1988, 6, 20));

        InvalidCpfFormatException exception = assertThrows(InvalidCpfFormatException.class,
                () -> clientService.createClient(clientDTO));

        assertEquals("O segundo dígito verificador do CPF é inválido.", exception.getMessage());
    }

    @Test
    void testCreateClientWithNullName() {
        ClientCreationDTO clientDTO = new ClientCreationDTO();
        clientDTO.setCpf("06915290435");
        clientDTO.setNome(null); // Nome nulo
        clientDTO.setDataNascimento(LocalDate.of(1988, 6, 20));

        assertThrows(InvalidNameException.class, () -> {
            clientService.createClient(clientDTO);
        });
    }

    @Test
    void testCreateClientWithNullBirthDate() {
        ClientCreationDTO clientDTO = new ClientCreationDTO();
        clientDTO.setCpf("06915290435");
        clientDTO.setNome("Pedro Mend");
        clientDTO.setDataNascimento(null);

        assertThrows(InvalidBirthDateException.class, () -> {
            clientService.createClient(clientDTO);
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

    @Test
    void testRemoveClientWithAccountsThrowsException() {
        Long clientId = 1L;
        Client client = new Client();
        client.setId(clientId);
        client.setCpf("06915290435");
        client.setNome("Pedro Mend");
        client.setDataNascimento(LocalDate.of(1988, 6, 20));

        client.getContas().add(new Account());

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            clientService.removeClientById(clientId);
        });

        assertEquals("Cliente não pode ser removido porque possui contas associadas.", exception.getMessage());
    }

    @Test
    void testCpfValidatorIsCalledDuringClientCreation() {
        ClientCreationDTO clientDTO = new ClientCreationDTO();
        clientDTO.setCpf("06915290435");
        clientDTO.setNome("Pedro Mend");
        clientDTO.setDataNascimento(LocalDate.of(1988, 6, 20));

        when(clientRepository.findByCpf(clientDTO.getCpf())).thenReturn(Optional.empty());

        clientService.createClient(clientDTO);

        verify(clientRepository).findByCpf(clientDTO.getCpf());
    }


}
