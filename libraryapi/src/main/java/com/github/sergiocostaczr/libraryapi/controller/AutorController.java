package com.github.sergiocostaczr.libraryapi.controller;

import com.github.sergiocostaczr.libraryapi.controller.dto.AutorDTO;
import com.github.sergiocostaczr.libraryapi.controller.dto.ErroResposta;
import com.github.sergiocostaczr.libraryapi.controller.mappers.AutorMapper;
import com.github.sergiocostaczr.libraryapi.exceptions.OperacaoNaoPermitidaException;
import com.github.sergiocostaczr.libraryapi.exceptions.RegistroDuplicadoException;
import com.github.sergiocostaczr.libraryapi.model.Autor;
import com.github.sergiocostaczr.libraryapi.model.Usuario;
import com.github.sergiocostaczr.libraryapi.security.SecurityService;
import com.github.sergiocostaczr.libraryapi.service.AutorService;
import com.github.sergiocostaczr.libraryapi.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@RestController
@RequestMapping("/autores")
@RequiredArgsConstructor
@Tag(name = "Autores")
@Slf4j
//http://localhost:8080/autores
public class AutorController implements GenericController {

    private final AutorService autorService;
    private final UsuarioService usuarioService;
    private final AutorMapper mapper;
    private final SecurityService securityService;

    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Salvar", description = "Cadastrar novo autor")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cadastrado com sucesso"),
            @ApiResponse(responseCode = "402", description = "Erro de validação"),
            @ApiResponse(responseCode = "409", description = "Autor já cadastrado")

    }
    )
    // ResponseEntity é uma classe que represanta uma resposta.
    // <> -> tipo do body do ResponseEntity.
    public ResponseEntity<?> salvar(@RequestBody @Valid AutorDTO dto
//                                    Authentication authentication
    ) {
        log.info("Cadastrando novo autor: {}", dto.nome()); // {} adciona parametro

//        //Authentication  getPrincipal retorna UserDetaisl
//        UserDetails usarioLogado = (UserDetails) authentication.getPrincipal();
//        Usuario usuario = usuarioService.obterPorLogin(usarioLogado.getUsername());

        Autor autorEntidade = mapper.toEntity(dto);
//        autorEntidade.setIdUsuario(usuario.getId());
        autorService.salvar(autorEntidade);

        //Ex: http://localhost:8080/autores/123das123dsa-123dq12e121e2-5g5r424
        URI location = gerarHeaderLocation(autorEntidade.getId());

        //Retora com o status created com o URI
        return ResponseEntity.created(location).build();
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    @Operation(summary = "Obter Detalhes", description = "Retorna os dados do autor pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Autor encontrado"),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado")

    }
    )
    public ResponseEntity<AutorDTO> obterDetalhes(@PathVariable("id") String id) {
        UUID idAutor = UUID.fromString(id);
        Optional<Autor> autorOptional = autorService.obterPorId(idAutor);

        return autorService.obterPorId(idAutor).map(optional -> {
            AutorDTO dto = mapper.toDTO(optional);
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());

//        if (autorOptional.isPresent()){
//            Autor autor = autorOptional.get();
//            AutorDTO autorDTO = new AutorDTO(autorOptional.getId(),autorOptional.getNome(),autorOptional.getDataNascimento(),autorOptional.getNacionalidade());
//            return ResponseEntity.ok(autorDTO);
//        }
//        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Deletar", description = "Deleta um autor existente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado"),
            @ApiResponse(responseCode = "400", description = "Autor possui livro cadastrado")

    }
    )
    public ResponseEntity<?> deletarById(@PathVariable("id") String id) {
        log.info("Deletando autor de ID {}", id);

        UUID idAutor = UUID.fromString(id);
        Optional<Autor> autorOptional = autorService.obterPorId(idAutor);
        if (autorOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        autorService.deletar(autorOptional.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    @Operation(summary = "Pesquisar", description = "Realiza pesquisa de autores por parametros")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso")
    }
    )
    // Paremetro /= URL
    public ResponseEntity<List<AutorDTO>> pesquisar(@RequestParam(value = "nome", required = false) String nome,
                                                    @RequestParam(value = "nacionalidade", required = false) String nacionalidade) {
        List<Autor> resultado = autorService.pesquisa(nome, nacionalidade);
        List<AutorDTO> list = resultado
                .stream()
                .map(autor -> new AutorDTO(autor.getId(), autor.getNome(), autor.getDataNascimento(), autor.getNacionalidade()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Atualizar", description = "Atualiza um autor existente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado"),
            @ApiResponse(responseCode = "409", description = "Autor ja cadastrado")

    }
    )
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody @Valid AutorDTO autorDTO) {

        UUID idAutor = UUID.fromString(id);
        Optional<Autor> autorOptional = autorService.obterPorId(idAutor);
        if (autorOptional.isEmpty()) {

            return ResponseEntity.notFound().build();
        }
        Autor autor = autorOptional.get();
        autor.setNome(autorDTO.nome());
        autor.setNacionalidade(autorDTO.nacionalidade());
        autor.setDataNascimento(autorDTO.dataNascimento());
        autorService.atualizar(autor);
        return ResponseEntity.noContent().build();
    }

}
