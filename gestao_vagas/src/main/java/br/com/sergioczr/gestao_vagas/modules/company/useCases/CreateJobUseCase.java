package br.com.sergioczr.gestao_vagas.modules.company.useCases;

import br.com.sergioczr.gestao_vagas.modules.company.entities.JobEntity;
import br.com.sergioczr.gestao_vagas.modules.company.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateJobUseCase {

    @Autowired
    private JobRepository jobRepository;


    public JobEntity execute(JobEntity jobEntity){
        return jobRepository.save(jobEntity);
    }
}
