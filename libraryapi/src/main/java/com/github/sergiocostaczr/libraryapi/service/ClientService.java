package com.github.sergiocostaczr.libraryapi.service;

import com.github.sergiocostaczr.libraryapi.model.Client;
import com.github.sergiocostaczr.libraryapi.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public Client salvar(Client client){
        String encode = passwordEncoder.encode(client.getClientSecret());
        client.setClientSecret(encode);
        return clientRepository.save(client);
    }

    public Client obterPorClientId(String clientId){
        return clientRepository.findByClientId(clientId);
    }

}
