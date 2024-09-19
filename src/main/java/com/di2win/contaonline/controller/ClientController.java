package com.di2win.contaonline.controller;

import com.di2win.contaonline.dto.ClientCreationDTO;
import com.di2win.contaonline.dto.ClientResponseDTO;
import com.di2win.contaonline.entity.Client;
import com.di2win.contaonline.service.ClientService;
import com.di2win.contaonline.util.ClientMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientCreationDTO clientCreationDTO) {
        Client createdClient = clientService.createClient(clientCreationDTO);
        ClientResponseDTO responseDTO = ClientMapper.mapToClientResponseDTO(createdClient);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.removeClientById(id);
        return ResponseEntity.noContent().build();
    }

}
