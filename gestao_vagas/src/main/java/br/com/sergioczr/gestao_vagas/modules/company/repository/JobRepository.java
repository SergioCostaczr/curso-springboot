package br.com.sergioczr.gestao_vagas.modules.company.repository;


import br.com.sergioczr.gestao_vagas.modules.company.entities.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, UUID> {






}
