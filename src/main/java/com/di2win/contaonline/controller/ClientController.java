package com.di2win.contaonline.controller;

import com.di2win.contaonline.entity.Client;
import com.di2win.contaonline.service.ClientService;
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
    public ResponseEntity<Client> createClient(@RequestBody Client client){
        Client createdClient = clientService.createClient(client);
        return new ResponseEntity<>(createdClient, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id){
        clientService.removeClientById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
