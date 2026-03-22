package com.github.sergiocostaczr.libraryapi.controller;

import com.github.sergiocostaczr.libraryapi.model.Client;
import com.github.sergiocostaczr.libraryapi.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void salvar(@RequestBody Client client){
      clientService.salvar(client);
    }
}

