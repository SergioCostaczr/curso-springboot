package br.com.sergioczr.gestao_vagas.modules.company.useCases;

import br.com.sergioczr.gestao_vagas.modules.company.entities.CompanyEntity;
import br.com.sergioczr.gestao_vagas.modules.company.entities.DTO.AuthCompanyDTO;
import br.com.sergioczr.gestao_vagas.modules.company.repository.CompanyRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class AuthCompanyUseCase {

    @Value("${security.token.secret}")
    private String secretKey;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String execute(AuthCompanyDTO authCompanyDTO) throws AuthenticationException {
        System.out.println(authCompanyDTO.getUsername());
        CompanyEntity company = companyRepository.findByUsername(authCompanyDTO.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("Username/Password incorrect"));
        boolean matches = this.passwordEncoder.matches(authCompanyDTO.getPassword(), company.getPassword());

        if(!matches){
            throw new AuthenticationException();
        }

        // se for igual gera o token

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String token = JWT.create().withIssuer("javagas")
                .withExpiresAt(Instant.now().plus(Duration.ofHours(2)))
                .withSubject(company.getId().toString())
                .sign(algorithm);

        return token;

    }
}
