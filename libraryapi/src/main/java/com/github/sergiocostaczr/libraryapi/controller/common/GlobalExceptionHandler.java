package com.github.sergiocostaczr.libraryapi.controller.common;

import com.github.sergiocostaczr.libraryapi.controller.dto.ErroCampo;
import com.github.sergiocostaczr.libraryapi.controller.dto.ErroResposta;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice // Captura exception e retorna uma Response
public class GlobalExceptionHandler{

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT) // Mapeia o retorno do metodo
    public ErroResposta handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        List<FieldError> fieldErrors = e.getFieldErrors();
        List<ErroCampo> listaErros = fieldErrors.stream()
                .map(fe -> new ErroCampo(fe.getField(), fe.getDefaultMessage())).toList();
        return new ErroResposta(HttpStatus.UNPROCESSABLE_CONTENT.value(), "Erro validação", listaErros);
    }
}
