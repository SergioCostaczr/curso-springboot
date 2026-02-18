package br.com.sergioczr.gestao_vagas.modules.company.controllers;

import br.com.sergioczr.gestao_vagas.exceptions.UserFoundException;
import br.com.sergioczr.gestao_vagas.modules.candidate.useCases.CreateCandidateUseCase;
import br.com.sergioczr.gestao_vagas.modules.company.entities.CompanyEntity;
import br.com.sergioczr.gestao_vagas.modules.company.useCases.CreateCompanyUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    CreateCompanyUseCase createCompanyUseCase;

    @PostMapping("/")
    public ResponseEntity<?> create(@Valid @RequestBody CompanyEntity companyEntity){
        try {
          var result =   createCompanyUseCase.execute(companyEntity);
            return ResponseEntity.ok(result);
        } catch (UserFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
