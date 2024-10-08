package com.di2win.contaonline.service;

import com.di2win.contaonline.dto.ClientCreationDTO;
import com.di2win.contaonline.entity.Client;
import com.di2win.contaonline.exception.client.ClientNotFoundException;
import com.di2win.contaonline.exception.client.InvalidBirthDateException;
import com.di2win.contaonline.exception.client.InvalidNameException;
import com.di2win.contaonline.exception.cpf.CpfAlreadyExistsException;
import com.di2win.contaonline.repository.ClientRepository;
import com.di2win.contaonline.util.CpfValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public Client createClient(ClientCreationDTO clientCreationDTO) {
        CpfValidator.validate(clientCreationDTO.getCpf());

        if (clientCreationDTO.getNome() == null || clientCreationDTO.getNome().trim().isEmpty()) {
            throw new InvalidNameException("O nome não pode ser nulo ou vazio.");
        }

        if (clientCreationDTO.getDataNascimento() == null) {
            throw new InvalidBirthDateException("A data de nascimento não pode ser nula.");
        }

        Optional<Client> existingClient = clientRepository.findByCpf(clientCreationDTO.getCpf());
        if (existingClient.isPresent()) {
            throw new CpfAlreadyExistsException("CPF já cadastrado: " + clientCreationDTO.getCpf());
        }

        Client client = new Client();
        client.setCpf(clientCreationDTO.getCpf());
        client.setNome(clientCreationDTO.getNome());
        client.setDataNascimento(clientCreationDTO.getDataNascimento());

        return clientRepository.save(client);
    }


    public void removeClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Cliente não encontrado com ID: " + id));

        if (!client.getContas().isEmpty()) {
            throw new IllegalStateException("Cliente não pode ser removido porque possui contas associadas.");
        }

        clientRepository.delete(client);
    }


}
