package br.com.sergioczr.gestao_vagas.modules.company.entities.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthCompanyDTO {
    private String password;
    private String username;

}
