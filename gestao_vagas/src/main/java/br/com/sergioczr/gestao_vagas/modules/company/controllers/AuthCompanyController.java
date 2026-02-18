package br.com.sergioczr.gestao_vagas.modules.company.controllers;

import br.com.sergioczr.gestao_vagas.modules.company.entities.DTO.AuthCompanyDTO;
import br.com.sergioczr.gestao_vagas.modules.company.useCases.AuthCompanyUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/auth")
public class AuthCompanyController {

    @Autowired
    private AuthCompanyUseCase authCompanyUseCase;

    @PostMapping("/company")
    public ResponseEntity<?> create(@RequestBody AuthCompanyDTO authCompanyDTO) {
        try {
            String execute = this.authCompanyUseCase.execute(authCompanyDTO);
            return ResponseEntity.ok(execute);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

}
