package com.github.sergiocostaczr.arquiteturaspring.montadora.configuration;

import com.github.sergiocostaczr.arquiteturaspring.montadora.Motor;
import com.github.sergiocostaczr.arquiteturaspring.montadora.TipoMotor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MontadoraConfiguration {

    @Bean(name = "motorEletrico")
    @Primary //Define como bean "default" quando não especificamos o qualifier.
    public Motor motorEletrico(){
        var motor = new Motor();
        motor.setCavalos(110);
        motor.setCilindros(4);
        motor.setModelo("XPTO-0");
        motor.setLitragem(2.0);
        motor.setTipo(TipoMotor.ELETRICO);
        return motor;
    }

    @Bean(name = "motorTurbo")
    public Motor motorTurbo(){
        var motor = new Motor();
        motor.setCavalos(130);
        motor.setCilindros(4);
        motor.setModelo("XPTO-0");
        motor.setLitragem(1.5);
        motor.setTipo(TipoMotor.TURBO);
        return motor;
    }

    @Bean(name = "motorAspirado") //Nome do bean é o mesmo do metodo, mas podemos personalizar usando (name = "").
    public Motor motorAspirado(){
        var motor = new Motor();
        motor.setCavalos(120);
        motor.setCilindros(4);
        motor.setModelo("XPTO-0");
        motor.setLitragem(2.0);
        motor.setTipo(TipoMotor.ASPIRADO);
        return motor;
    }
}
