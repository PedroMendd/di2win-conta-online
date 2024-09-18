package com.di2win.contaonline.service;

import com.di2win.contaonline.entity.Client;
import com.di2win.contaonline.exception.client.ClientNotFoundException;
import com.di2win.contaonline.exception.client.InvalidBirthDateException;
import com.di2win.contaonline.exception.client.InvalidNameException;
import com.di2win.contaonline.exception.cpf.CpfAlreadyExistsException;
import com.di2win.contaonline.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public Client createClient(Client client) {
        CpfValidator.validate(client.getCpf());

        if (client.getNome() == null || client.getNome().trim().isEmpty()) {
            throw new InvalidNameException("O nome não pode ser nulo ou vazio.");
        }

        if (client.getDataNascimento() == null) {
            throw new InvalidBirthDateException("A data de nascimento não pode ser nula.");
        }

        Optional<Client> existingClient = clientRepository.findByCpf(client.getCpf());
        if (existingClient.isPresent()) {
            throw new CpfAlreadyExistsException("CPF já cadastrado: " + client.getCpf());
        }

        return clientRepository.save(client);
    }

    public void removeClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Cliente não encontrado com ID: " + id));
        clientRepository.delete(client);
    }
}
