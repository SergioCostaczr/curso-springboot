package br.com.sergioczr.gestao_vagas.modules.company.repository;

import br.com.sergioczr.gestao_vagas.modules.candidate.entities.CandidateEntity;
import br.com.sergioczr.gestao_vagas.modules.company.entities.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID> {

    Optional<CompanyEntity> findByUsernameOrEmail(String username, String email);

    Optional<CompanyEntity> findByUsername(String username);

}
