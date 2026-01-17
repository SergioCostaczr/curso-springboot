package com.github.sergiocostaczr.arquiteturaspring;

import com.github.sergiocostaczr.arquiteturaspring.todos.MailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigAcessoEmail {

    @Autowired
    private AppProperties appProperties;


//    @Bean
    public MailSender mailSender(){
        return null;
    }

}
