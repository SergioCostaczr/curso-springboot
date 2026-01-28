package com.github.sergiocostaczr.libraryapi.controller;

import com.github.sergiocostaczr.libraryapi.controller.dto.AutorDTO;
import com.github.sergiocostaczr.libraryapi.controller.dto.ErroReposta;
import com.github.sergiocostaczr.libraryapi.exceptions.OperacaoNaoPermitidaException;
import com.github.sergiocostaczr.libraryapi.exceptions.RegistroDuplicadoException;
import com.github.sergiocostaczr.libraryapi.model.Autor;
import com.github.sergiocostaczr.libraryapi.service.AutorService;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.reflect.IReflectionWorld;
import org.hibernate.cache.spi.support.RegionNameQualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/autores")
@RequiredArgsConstructor
//http://localhost:8080/autores
public class AutorController {

    private final AutorService autorService;


    @PostMapping

    // ResponseEntity é uma classe que represanta uma resposta.
    // <> -> tipo do body do ResponseEntity.
    public ResponseEntity<?> salvar(@RequestBody AutorDTO autor){
        try {
            Autor autorEntidade = autor.mapearParaAutor();
            autorService.salvar(autorEntidade);

            //Ex: http://localhost:8080/autores/123das123dsa-123dq12e121e2-5g5r424
            URI location =  ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(autorEntidade.getId())
                    .toUri();


            //Retora com o status created com o URI
            return  ResponseEntity.created(location).build();
        } catch (RegistroDuplicadoException e) {
            var erroDto = ErroReposta.conflito(e.getMessage());
            return ResponseEntity.status(erroDto.status()).body(erroDto);

        }
    }

    @GetMapping("{id}")
    public ResponseEntity<AutorDTO> obertDetalhes(@PathVariable("id") String id){
        UUID idAutor = UUID.fromString(id);
        Optional<Autor> autorOptional = autorService.obterPorId(idAutor);

        if (autorOptional.isPresent()){
            Autor autor = autorOptional.get();
            AutorDTO autorDTO = new AutorDTO(autor.getId(),autor.getNome(),autor.getDataNascimento(),autor.getNacionalidade());
            return ResponseEntity.ok(autorDTO);
        }

        return ResponseEntity.notFound().build();

    }
    @DeleteMapping("{id}")
    public ResponseEntity<?> deletarById(@PathVariable("id") String id){
        try {
            UUID idAutor = UUID.fromString(id);
            Optional<Autor> autorOptional = autorService.obterPorId(idAutor);

            if (autorOptional.isEmpty()){
                return ResponseEntity.notFound().build();
            }

            autorService.deletar(autorOptional.get());
            return ResponseEntity.noContent().build();
        } catch (OperacaoNaoPermitidaException e) {
            var erroReposta = ErroReposta.reppostaPadrao(e.getMessage());
            return ResponseEntity.status(erroReposta.status()).body(erroReposta);
        }
    }


    @GetMapping
    // Paremetro /= URL
    public ResponseEntity<List<AutorDTO>> pesquisar(@RequestParam(value = "nome", required = false) String nome,
                                                    @RequestParam(value = "nacionalidade", required = false) String nacionalidade){
        List<Autor> resultado  = autorService.pesquisa(nome,nacionalidade);
        List<AutorDTO> list = resultado
                .stream()
                .map(autor -> new AutorDTO(autor.getId(),autor.getNome(),autor.getDataNascimento(),autor.getNacionalidade()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("{id}")
    public ResponseEntity<?> atualizar(@PathVariable String id,@RequestBody AutorDTO autorDTO){
        try {
            UUID idAutor = UUID.fromString(id);
            Optional<Autor> autorOptional = autorService.obterPorId(idAutor);

            if (autorOptional.isEmpty()){

                return ResponseEntity.notFound().build();
            }

            Autor autor = autorOptional.get();

            autor.setNome(autorDTO.nome());
            autor.setNacionalidade(autorDTO.nacionalidade());
            autor.setDataNascimento(autorDTO.dataNascimento());

            autorService.atualizar(autor);

            return ResponseEntity.noContent().build();
        } catch (RegistroDuplicadoException e) {
            var erroDto = ErroReposta.conflito(e.getMessage());
            return ResponseEntity.status(erroDto.status()).body(erroDto);
        }


    }






}
