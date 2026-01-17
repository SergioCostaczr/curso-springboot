package com.github.sergiocostaczr.arquiteturaspring.todos;

import org.springframework.stereotype.Component;

import javax.sound.midi.Soundbank;

@Component
public class MailSender {

    public void enviar(String msg){
        System.out.println("enviado email: " + msg);
    }
}
