package br.com.sergioczr.gestao_vagas.modules.candidate.useCases;

import br.com.sergioczr.gestao_vagas.exceptions.UserFoundException;
import br.com.sergioczr.gestao_vagas.modules.candidate.entities.CandidateEntity;
import br.com.sergioczr.gestao_vagas.modules.candidate.repository.CandidateRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class CreateCandidateUseCase {

    @Autowired
    private CandidateRepository candidateRepository;

    public CandidateEntity create(@Valid @RequestBody CandidateEntity candidateEntity) {
        System.out.println("Candidato: ");
        System.out.println(candidateEntity.getEmail());
        System.out.println(candidateEntity);

        candidateRepository.findByUsernameOrEmail(candidateEntity.getUsername(), candidateEntity.getEmail())
                .ifPresent(user -> {
                    throw new UserFoundException();
                });
        return candidateRepository.save(candidateEntity);
    }
}
