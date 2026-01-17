package com.github.sergiocostaczr.arquiteturaspring.montadora.api;

import com.github.sergiocostaczr.arquiteturaspring.montadora.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carros")
public class TesteFabricaController {

    //@Autowired//injeção de dependência
    //@Qualifier("motorEletrico") //Nome do bean. Especificamos o bean quando temos mais de um do mesmo tipo no container.
    //private Motor motor;

    @Autowired
    @Aspirado
    private Motor motor;

    @PostMapping
    public CarroStatus ligarCarro(@RequestBody Chave chave){
        Carro carro = new HondaHRV(motor);
        return carro.darIngincao(chave);
    }


}
