package br.com.sergioczr.gestao_vagas.modules.company.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;
import org.springframework.jdbc.core.SqlReturnType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
@Component
@Entity(name = "company")
@Data
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Pattern(regexp = "\\S+", message = "O campo [username] não deve conter espaco")
    private String username;

    @Email(message = "O campo deve conter [email] valido")
    private String email;

    @Length(min = 10, max = 100)
    private String password;
    private String name;

    private String description;

    private String website;

    @CreationTimestamp
    private LocalDateTime createdAt;


}
