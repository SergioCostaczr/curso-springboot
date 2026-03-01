package com.github.sergiocostaczr.libraryapi.controller;

import com.github.sergiocostaczr.libraryapi.controller.dto.CadastroLivroDTO;
import com.github.sergiocostaczr.libraryapi.controller.dto.ErroResposta;
import com.github.sergiocostaczr.libraryapi.controller.dto.ResultadoPesquisaLivroDTO;
import com.github.sergiocostaczr.libraryapi.controller.mappers.LivroMapper;
import com.github.sergiocostaczr.libraryapi.exceptions.RegistroDuplicadoException;
import com.github.sergiocostaczr.libraryapi.model.GeneroLivro;
import com.github.sergiocostaczr.libraryapi.model.Livro;
import com.github.sergiocostaczr.libraryapi.service.LivroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("livros")
@RequiredArgsConstructor
public class LivroController implements GenericController {

    private final LivroService livroService;
    private final LivroMapper livroMapper;

    //ROLE_OPERADOR
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    public ResponseEntity<Object> salvar(@RequestBody @Valid CadastroLivroDTO dto) {
        // mapear dto para entity.
        // enviar a entity para o service validar e salver na base.
        // criar url para acesso dos dados do livros.
        // retornrar codigo created com header location.
        Livro livro = livroMapper.toEntity(dto);
        livroService.salvar(livro);
        URI location = gerarHeaderLocation(livro.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    public ResponseEntity<ResultadoPesquisaLivroDTO> obterDetalhes(@PathVariable(name = "id") String id){
        return livroService.obterPorId(UUID.fromString(id))
                .map(livro -> {
                    var dto = livroMapper.toDTO(livro);
                    return ResponseEntity.ok(dto);
                }).orElseGet(()-> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    public ResponseEntity<?> deletar(@PathVariable String id){
        return  livroService.obterPorId(UUID.fromString(id))
                .map(livro -> {
                    livroService.deletar(livro);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    public ResponseEntity<Page<ResultadoPesquisaLivroDTO>> pesquisa(
            @RequestParam(value = "isbn", required = false) String isbn,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "nome-autor", required = false) String nomeAutor,
            @RequestParam(value = "genero", required = false) GeneroLivro generoLivro,
            @RequestParam(value = "ano-publicacao", required = false) Integer anoPublicacao,
            @RequestParam(value = "pagina",defaultValue = "0") Integer pagina,
            @RequestParam(value = "tamanho-pagina",defaultValue = "10") Integer tamanhoPagina
    ){

        Page<Livro> paginaResultado = livroService.pesquisa(isbn,titulo,nomeAutor,generoLivro,anoPublicacao,pagina,tamanhoPagina);

        Page<ResultadoPesquisaLivroDTO> resultado = paginaResultado.map(livroMapper::toDTO);

//      List<ResultadoPesquisaLivroDTO> list = result.stream()
//                                                .map(livroMapper::toDTO)
//                                                .collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    public ResponseEntity<?> atualizar(
            @PathVariable("id") String id, @RequestBody @Valid CadastroLivroDTO dto){

        return livroService.obterPorId(UUID.fromString(id))
                .map(livro -> {
                    Livro entityAux = livroMapper.toEntity(dto);

                    livro.setDataPublicacao(entityAux.getDataPublicacao());
                    livro.setIsbn(entityAux.getIsbn());
                    livro.setAutor(entityAux.getAutor());
                    livro.setTitulo(entityAux.getTitulo());
                    livro.setPreco(entityAux.getPreco());
                    livro.setGenero(entityAux.getGenero());

                    livroService.atualizar(livro);
                    return ResponseEntity.noContent().build();
                }).orElseGet(()-> ResponseEntity.notFound().build());
    }


}
