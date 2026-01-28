package com.github.sergiocostaczr.libraryapi.controller.dto;

import org.springframework.http.HttpStatus;

import java.util.List;

public record ErroReposta(int status, String mensagem, List<ErroCampo> erros){

    public static ErroReposta reppostaPadrao(String mensagem){
        return new ErroReposta(HttpStatus.BAD_REQUEST.value(), mensagem, List.of());
    }
    public static ErroReposta conflito(String mensagem){
        return new ErroReposta(HttpStatus.CONFLICT.value(), mensagem, List.of());
    }
}
