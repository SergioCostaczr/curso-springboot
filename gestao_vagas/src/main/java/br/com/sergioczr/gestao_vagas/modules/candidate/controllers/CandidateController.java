package br.com.sergioczr.gestao_vagas.modules.candidate.controllers;

import br.com.sergioczr.gestao_vagas.modules.candidate.entities.CandidateEntity;
import br.com.sergioczr.gestao_vagas.modules.candidate.useCases.CreateCandidateUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("candidate")
public class CandidateController {

    @Autowired
    private CreateCandidateUseCase createCandidateUseCase;

    @PostMapping("/")
    public ResponseEntity<?> create(@Valid @RequestBody CandidateEntity candidateEntity){
        try {
            CandidateEntity candidateEntity1 = createCandidateUseCase.create(candidateEntity);
            return ResponseEntity.ok(candidateEntity1);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
}
