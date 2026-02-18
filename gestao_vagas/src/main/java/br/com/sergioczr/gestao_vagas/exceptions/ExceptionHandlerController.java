package br.com.sergioczr.gestao_vagas.exceptions;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@ControllerAdvice
public class ExceptionHandlerController {

    private  MessageSource messageSource;

    public ExceptionHandlerController(MessageSource message){
        this.messageSource = message;

    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorMessageDTO>> handleMMethodArgumentNotValidException(MethodArgumentNotValidException e){
        List<ErrorMessageDTO> dtos = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String message = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
            dtos.add(new ErrorMessageDTO(message, fieldError.getField()));
        });

        return new ResponseEntity<>(dtos, HttpStatus.BAD_REQUEST);
    }
}
