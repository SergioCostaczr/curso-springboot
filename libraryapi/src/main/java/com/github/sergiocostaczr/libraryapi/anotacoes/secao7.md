# Spring - MapStruct e Criteria API

## 1. MapStruct - Mapeamento Automático

### 1.1 O que é MapStruct?

**MapStruct** é uma biblioteca de geração de código que automatiza o mapeamento entre objetos Java (DTOs e Entidades). Diferente de bibliotecas como ModelMapper que usam reflexão em runtime, MapStruct gera código em **tempo de compilação**.

**Vantagens:**
- ⚡ **Performance**: Código gerado em compile-time (sem reflexão)
- 🛡️ **Type-safe**: Erros detectados na compilação
- 🔍 **Debugável**: Código gerado é legível
- 🎯 **Sem magia**: Você vê exatamente o que foi gerado
- 📝 **Menos boilerplate**: Não precisa escrever mappers manualmente

---

### 1.2 Dependência Maven

```xml
<properties>
    <org.mapstruct.version>1.5.5.Final</org.mapstruct.version>
    <lombok.version>1.18.30</lombok.version>
</properties>

<dependencies>
    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${org.mapstruct.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <!-- MapStruct Processor -->
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${org.mapstruct.version}</version>
                    </path>
                    <!-- Lombok (se estiver usando) -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <!-- Lombok + MapStruct -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>0.2.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

### 1.3 AutorMapper - Interface Simples

```java
package com.github.sergiocostaczr.libraryapi.mapper;

import com.github.sergiocostaczr.libraryapi.controller.dto.AutorDTO;
import com.github.sergiocostaczr.libraryapi.model.Autor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AutorMapper {

    // Converte DTO para Entidade
    // Por padrão, mapeia campos com o mesmo nome
    Autor toEntity(AutorDTO dto);

    // Converte Entidade para DTO
    AutorDTO toDTO(Autor autor);
}
```

**O que o MapStruct faz:**
```java
// MapStruct GERA automaticamente esta implementação:
@Component
public class AutorMapperImpl implements AutorMapper {

    @Override
    public Autor toEntity(AutorDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Autor autor = new Autor();
        autor.setId(dto.id());
        autor.setNome(dto.nome());
        autor.setDataNascimento(dto.dataNascimento());
        autor.setNacionalidade(dto.nacionalidade());
        
        return autor;
    }

    @Override
    public AutorDTO toDTO(Autor autor) {
        if (autor == null) {
            return null;
        }
        
        return new AutorDTO(
            autor.getId(),
            autor.getNome(),
            autor.getDataNascimento(),
            autor.getNacionalidade()
        );
    }
}
```

---

### 1.4 LivroMapper - Classe Abstrata com Lógica Customizada

```java
package com.github.sergiocostaczr.libraryapi.mapper;

import com.github.sergiocostaczr.libraryapi.controller.dto.CadastroLivroDTO;
import com.github.sergiocostaczr.libraryapi.controller.dto.ResultadoPesquisaLivroDTO;
import com.github.sergiocostaczr.libraryapi.model.Livro;
import com.github.sergiocostaczr.libraryapi.repository.AutorRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    componentModel = "spring",  // Gera como @Component do Spring
    uses = AutorMapper.class    // Usa AutorMapper para mapear autor
)
public abstract class LivroMapper {

    @Autowired
    protected AutorRepository autorRepository;

    @Mapping(
        target = "autor",
        expression = "java( autorRepository.findById(dto.idAutor()).orElse(null) )"
    )
    public abstract Livro toEntity(CadastroLivroDTO dto);

    public abstract ResultadoPesquisaLivroDTO toDTO(Livro livro);
}
```

**Explicação:**

1. **componentModel = "spring"**
    - Gera a implementação como `@Component`
    - Permite injeção de dependências

2. **uses = AutorMapper.class**
    - Reutiliza AutorMapper para mapear o autor do livro
    - Evita duplicação de código

3. **Classe abstrata** (não interface)
    - Permite injetar dependências (@Autowired)
    - Permite adicionar métodos customizados

4. **@Mapping com expression**
    - Busca o autor no banco pelo ID
    - Associa ao livro durante o mapeamento

---

### 1.5 DTOs

```java
// DTO de entrada (criação)
public record CadastroLivroDTO(
    @NotBlank String titulo,
    @NotBlank String isbn,
    @NotNull BigDecimal preco,
    @NotNull LocalDate dataPublicacao,
    @NotBlank String genero,
    @NotNull UUID idAutor  // Apenas o ID do autor
) {}

// DTO de saída (resultado de pesquisa)
public record ResultadoPesquisaLivroDTO(
    UUID id,
    String titulo,
    String isbn,
    BigDecimal preco,
    LocalDate dataPublicacao,
    String genero,
    AutorDTO autor  // Objeto autor completo
) {}
```

---

### 1.6 Usando os Mappers no Service

```java
@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository repository;
    private final LivroMapper mapper;

    public ResultadoPesquisaLivroDTO criar(CadastroLivroDTO dto) {
        // Converte DTO para Entidade
        Livro livro = mapper.toEntity(dto);
        
        // Salva no banco
        Livro salvo = repository.save(livro);
        
        // Converte Entidade para DTO de resposta
        return mapper.toDTO(salvo);
    }

    public ResultadoPesquisaLivroDTO buscarPorId(UUID id) {
        Livro livro = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Livro não encontrado"));
        
        return mapper.toDTO(livro);
    }

    public List<ResultadoPesquisaLivroDTO> listarTodos() {
        return repository.findAll().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
}
```

---

### 1.7 Annotations Importantes do MapStruct

#### @Mapping - Mapeamento Customizado

```java
@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    // Mapeia campo com nome diferente
    @Mapping(source = "nomeCompleto", target = "nome")
    @Mapping(source = "valorUnitario", target = "preco")
    Produto toEntity(ProdutoDTO dto);

    // Ignora campo
    @Mapping(target = "senha", ignore = true)
    UsuarioDTO toDTO(Usuario usuario);

    // Valor constante
    @Mapping(target = "ativo", constant = "true")
    Produto toEntity(CadastroProdutoDTO dto);

    // Expressão Java
    @Mapping(target = "nomeCompleto", 
             expression = "java( usuario.getNome() + \" \" + usuario.getSobrenome() )")
    UsuarioDTO toDTO(Usuario usuario);

    // Valor default quando null
    @Mapping(target = "status", defaultValue = "ATIVO")
    Produto toEntity(ProdutoDTO dto);
}
```

#### @MappingTarget - Atualizar Entidade Existente

```java
@Mapper(componentModel = "spring")
public interface AutorMapper {

    // Atualiza entidade existente
    @Mapping(target = "id", ignore = true)  // Não atualiza ID
    void updateEntity(AutorDTO dto, @MappingTarget Autor autor);
}

// Uso no Service
public void atualizar(UUID id, AutorDTO dto) {
    Autor autor = repository.findById(id)
        .orElseThrow(() -> new NotFoundException("Autor não encontrado"));
    
    mapper.updateEntity(dto, autor);  // Atualiza o autor existente
    repository.save(autor);
}
```

#### @AfterMapping - Lógica Pós-Mapeamento

```java
@Mapper(componentModel = "spring")
public abstract class LivroMapper {

    @AfterMapping
    protected void afterMapping(@MappingTarget Livro livro, CadastroLivroDTO dto) {
        // Lógica executada após o mapeamento
        if (livro.getPreco().compareTo(BigDecimal.ZERO) < 0) {
            livro.setPreco(BigDecimal.ZERO);
        }
    }
}
```

#### @BeforeMapping - Lógica Pré-Mapeamento

```java
@Mapper(componentModel = "spring")
public abstract class UsuarioMapper {

    @BeforeMapping
    protected void beforeMapping(UsuarioDTO dto) {
        // Lógica executada antes do mapeamento
        if (dto.email() != null) {
            dto = new UsuarioDTO(
                dto.id(),
                dto.nome(),
                dto.email().toLowerCase()  // Normaliza email
            );
        }
    }
}
```

---

### 1.8 Mapeamento de Listas

```java
@Mapper(componentModel = "spring")
public interface AutorMapper {

    Autor toEntity(AutorDTO dto);
    AutorDTO toDTO(Autor autor);

    // MapStruct gera automaticamente o mapeamento de listas
    List<Autor> toEntityList(List<AutorDTO> dtos);
    List<AutorDTO> toDTOList(List<Autor> autores);
}
```

---

### 1.9 Mapeamento com Enums

```java
@Mapper(componentModel = "spring")
public interface LivroMapper {

    // MapStruct converte automaticamente String para Enum
    @Mapping(target = "genero", source = "generoString")
    Livro toEntity(CadastroLivroDTO dto);

    // E Enum para String
    @Mapping(target = "generoString", source = "genero")
    LivroDTO toDTO(Livro livro);
}
```

---

## 2. Criteria API - Consultas Dinâmicas Tipadas

### 2.1 O que é Criteria API?

**Criteria API** é uma API Java para construir consultas JPA de forma programática e **type-safe**. Ao invés de escrever JPQL em strings, você constrói a query usando métodos Java.

**Vantagens:**
- ✅ **Type-safe**: Erros detectados em compile-time
- ✅ **Dinâmica**: Constrói queries baseado em condições
- ✅ **Refatoração segura**: IDEs refatoram junto
- ✅ **Sem strings**: Menos propenso a erros de digitação

**Desvantagens:**
- ❌ **Verbosa**: Mais código que JPQL
- ❌ **Menos legível**: Sintaxe complexa

---

### 2.2 JpaSpecificationExecutor

```java
@Repository
public interface LivroRepository extends 
        JpaRepository<Livro, UUID>,
        JpaSpecificationExecutor<Livro> {  // Adiciona suporte a Specifications
    
    // Métodos tradicionais...
}
```

**JpaSpecificationExecutor** adiciona métodos:
- `findAll(Specification<T> spec)`
- `findOne(Specification<T> spec)`
- `count(Specification<T> spec)`
- `findAll(Specification<T> spec, Pageable pageable)`

---

### 2.3 LivroSpecs - Specifications Reutilizáveis

```java
package com.github.sergiocostaczr.libraryapi.repository.specs;

import com.github.sergiocostaczr.libraryapi.model.GeneroLivro;
import com.github.sergiocostaczr.libraryapi.model.Livro;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para consultas dinâmicas de Livro usando Criteria API
 * 
 * @see <a href="https://jakarta.ee/learn/docs/jakartaee-tutorial/current/persist/persistence-criteria/persistence-criteria.html">Criteria API</a>
 */
public class LivroSpecs {

    // WHERE isbn = :isbn
    public static Specification<Livro> isbnEqual(String isbn) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("isbn"), isbn);
    }

    // WHERE UPPER(titulo) LIKE UPPER('%:titulo%')
    public static Specification<Livro> tituloLike(String titulo) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(
                criteriaBuilder.upper(root.get("titulo")),
                "%" + titulo.toUpperCase() + "%"
            );
    }

    // WHERE genero = :genero
    public static Specification<Livro> generoEqual(GeneroLivro generoLivro) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("genero"), generoLivro);
    }

    // WHERE TO_CHAR(data_publicacao, 'YYYY') = :anoPublicacao
    public static Specification<Livro> anoPublicacaoEqual(Integer anoPublicacao) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(
                criteriaBuilder.function(
                    "to_char",
                    String.class,
                    root.get("dataPublicacao"),
                    criteriaBuilder.literal("YYYY")
                ),
                anoPublicacao.toString()
            );
    }

    /*
     * SELECT l.*
     * FROM livro l
     * LEFT JOIN autor a ON a.id = l.id_autor
     * WHERE UPPER(a.nome) LIKE UPPER('%:nome%')
     */
    public static Specification<Livro> nomeAutorLike(String nome) {
        return (root, query, criteriaBuilder) -> {
            // Join com a tabela autor
            Join<Object, Object> joinAutor = root.join("autor", JoinType.LEFT);
            
            return criteriaBuilder.like(
                criteriaBuilder.upper(joinAutor.get("nome")),
                "%" + nome.toUpperCase() + "%"
            );
            
            // Alternativa sem variável Join:
            // return criteriaBuilder.like(
            //     criteriaBuilder.upper(root.get("autor").get("nome")),
            //     "%" + nome.toUpperCase() + "%"
            // );
        };
    }
}
```

---

### 2.4 Anatomia de uma Specification

```java
public static Specification<Livro> tituloLike(String titulo) {
    return (root, query, criteriaBuilder) -> {
        // root: Representa a entidade Livro (FROM livro)
        // query: Representa a query sendo construída
        // criteriaBuilder: Construtor de condições (WHERE, ORDER BY, etc)
        
        return criteriaBuilder.like(
            criteriaBuilder.upper(root.get("titulo")),  // Campo
            "%" + titulo.toUpperCase() + "%"            // Valor
        );
    };
}
```

**Componentes:**
- **Root**: Representa a entidade raiz (FROM)
- **CriteriaQuery**: A query sendo construída
- **CriteriaBuilder**: Cria predicados (condições WHERE)

---

### 2.5 Usando Specifications no Service

```java
@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository repository;

    public List<Livro> pesquisar(
            String titulo,
            String isbn,
            GeneroLivro genero,
            Integer anoPublicacao,
            String nomeAutor) {
        
        // Inicia com uma Specification vazia (SELECT * FROM livro)
        Specification<Livro> specs = Specification.where(null);

        // Adiciona condições dinamicamente
        if (titulo != null) {
            specs = specs.and(LivroSpecs.tituloLike(titulo));
        }

        if (isbn != null) {
            specs = specs.and(LivroSpecs.isbnEqual(isbn));
        }

        if (genero != null) {
            specs = specs.and(LivroSpecs.generoEqual(genero));
        }

        if (anoPublicacao != null) {
            specs = specs.and(LivroSpecs.anoPublicacaoEqual(anoPublicacao));
        }

        if (nomeAutor != null) {
            specs = specs.and(LivroSpecs.nomeAutorLike(nomeAutor));
        }

        // Executa a query com todas as condições
        return repository.findAll(specs);
    }
}
```

---

### 2.6 Operadores Lógicos

```java
// AND - Todas as condições devem ser verdadeiras
Specification<Livro> spec = Specification
    .where(LivroSpecs.tituloLike("Java"))
    .and(LivroSpecs.generoEqual(GeneroLivro.CIENCIA))
    .and(LivroSpecs.anoPublicacaoEqual(2020));

// OR - Pelo menos uma condição deve ser verdadeira
Specification<Livro> spec = Specification
    .where(LivroSpecs.generoEqual(GeneroLivro.FICCAO))
    .or(LivroSpecs.generoEqual(GeneroLivro.FANTASIA));

// NOT - Nega a condição
Specification<Livro> spec = Specification
    .not(LivroSpecs.generoEqual(GeneroLivro.ROMANCE));

// Combinação complexa
Specification<Livro> spec = Specification
    .where(LivroSpecs.tituloLike("Spring"))
    .and(
        Specification.where(LivroSpecs.generoEqual(GeneroLivro.CIENCIA))
            .or(LivroSpecs.generoEqual(GeneroLivro.BIOGRAFIA))
    );
// WHERE titulo LIKE '%Spring%' AND (genero = 'CIENCIA' OR genero = 'BIOGRAFIA')
```

---

### 2.7 Paginação com Specifications

```java
@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository repository;

    public Page<Livro> pesquisarPaginado(
            String titulo,
            String nomeAutor,
            Pageable pageable) {
        
        Specification<Livro> specs = Specification.where(null);

        if (titulo != null) {
            specs = specs.and(LivroSpecs.tituloLike(titulo));
        }

        if (nomeAutor != null) {
            specs = specs.and(LivroSpecs.nomeAutorLike(nomeAutor));
        }

        // Busca paginada com Specification
        return repository.findAll(specs, pageable);
    }
}
```

**No Controller:**
```java
@GetMapping
public ResponseEntity<Page<LivroDTO>> pesquisar(
        @RequestParam(required = false) String titulo,
        @RequestParam(required = false) String nomeAutor,
        @PageableDefault(size = 20, sort = "titulo") Pageable pageable) {
    
    Page<Livro> livros = service.pesquisarPaginado(titulo, nomeAutor, pageable);
    Page<LivroDTO> response = livros.map(mapper::toDTO);
    
    return ResponseEntity.ok(response);
}
```

---

### 2.8 Specifications Avançadas

#### Comparações Numéricas

```java
public class LivroSpecs {

    // preco > :valor
    public static Specification<Livro> precoMaiorQue(BigDecimal valor) {
        return (root, query, cb) ->
            cb.greaterThan(root.get("preco"), valor);
    }

    // preco >= :valor
    public static Specification<Livro> precoMaiorOuIgual(BigDecimal valor) {
        return (root, query, cb) ->
            cb.greaterThanOrEqualTo(root.get("preco"), valor);
    }

    // preco < :valor
    public static Specification<Livro> precoMenorQue(BigDecimal valor) {
        return (root, query, cb) ->
            cb.lessThan(root.get("preco"), valor);
    }

    // preco BETWEEN :min AND :max
    public static Specification<Livro> precoEntre(BigDecimal min, BigDecimal max) {
        return (root, query, cb) ->
            cb.between(root.get("preco"), min, max);
    }
}
```

#### Datas

```java
public class LivroSpecs {

    // data_publicacao > :data
    public static Specification<Livro> publicadoDepoisDe(LocalDate data) {
        return (root, query, cb) ->
            cb.greaterThan(root.get("dataPublicacao"), data);
    }

    // data_publicacao BETWEEN :inicio AND :fim
    public static Specification<Livro> publicadoEntre(LocalDate inicio, LocalDate fim) {
        return (root, query, cb) ->
            cb.between(root.get("dataPublicacao"), inicio, fim);
    }

    // YEAR(data_publicacao) = :ano
    public static Specification<Livro> publicadoNoAno(int ano) {
        return (root, query, cb) -> {
            Expression<Integer> year = cb.function(
                "YEAR",
                Integer.class,
                root.get("dataPublicacao")
            );
            return cb.equal(year, ano);
        };
    }
}
```

#### Collections (IN)

```java
public class LivroSpecs {

    // genero IN (:generos)
    public static Specification<Livro> generoIn(List<GeneroLivro> generos) {
        return (root, query, cb) ->
            root.get("genero").in(generos);
    }

    // autor.id IN (:ids)
    public static Specification<Livro> autorIdIn(List<UUID> idsAutores) {
        return (root, query, cb) ->
            root.get("autor").get("id").in(idsAutores);
    }
}
```

#### Null e Not Null

```java
public class LivroSpecs {

    // data_publicacao IS NULL
    public static Specification<Livro> semDataPublicacao() {
        return (root, query, cb) ->
            cb.isNull(root.get("dataPublicacao"));
    }

    // data_publicacao IS NOT NULL
    public static Specification<Livro> comDataPublicacao() {
        return (root, query, cb) ->
            cb.isNotNull(root.get("dataPublicacao"));
    }
}
```

#### Joins Complexos

```java
public class LivroSpecs {

    /*
     * SELECT l.*
     * FROM livro l
     * INNER JOIN autor a ON a.id = l.id_autor
     * WHERE a.nacionalidade = :nacionalidade
     */
    public static Specification<Livro> autorNacionalidadeEqual(String nacionalidade) {
        return (root, query, cb) -> {
            Join<Livro, Autor> joinAutor = root.join("autor", JoinType.INNER);
            return cb.equal(joinAutor.get("nacionalidade"), nacionalidade);
        };
    }

    /*
     * SELECT DISTINCT l.*
     * FROM livro l
     * JOIN autor a ON a.id = l.id_autor
     * WHERE a.data_nascimento BETWEEN :inicio AND :fim
     */
    public static Specification<Livro> autorNascidoEntre(LocalDate inicio, LocalDate fim) {
        return (root, query, cb) -> {
            query.distinct(true);  // DISTINCT
            Join<Livro, Autor> joinAutor = root.join("autor");
            return cb.between(joinAutor.get("dataNascimento"), inicio, fim);
        };
    }
}
```

---

### 2.9 Ordenação com Specifications

```java
public List<Livro> pesquisarOrdenado(String titulo, String ordem) {
    Specification<Livro> spec = Specification.where(null);

    if (titulo != null) {
        spec = spec.and(LivroSpecs.tituloLike(titulo));
    }

    // Adiciona ordenação
    Sort sort = "desc".equalsIgnoreCase(ordem)
        ? Sort.by("titulo").descending()
        : Sort.by("titulo").ascending();

    return repository.findAll(spec, sort);
}
```

---

## 3. Comparação: Query Methods vs Query By Example vs Criteria API

| Aspecto | Query Methods | Query By Example | Criteria API |
|---------|---------------|------------------|--------------|
| **Complexidade** | ✅ Simples | ✅ Simples | ❌ Complexa |
| **Dinâmica** | ❌ Fixa | ✅ Dinâmica | ✅ Dinâmica |
| **Type-safe** | ✅ Sim | ⚠️ Parcial | ✅ Sim |
| **Joins** | ✅ Sim | ⚠️ Limitado | ✅ Sim |
| **Performance** | ✅ Ótima | ✅ Ótima | ✅ Ótima |
| **Legibilidade** | ✅ Alta | ✅ Alta | ⚠️ Média |
| **Manutenibilidade** | ⚠️ Muitos métodos | ✅ Boa | ✅ Boa |

**Quando usar cada um:**
- **Query Methods**: Consultas simples e fixas
- **Query By Example**: Filtros opcionais simples (sem joins)
- **Criteria API**: Consultas dinâmicas complexas com joins

---

## 4. Boas Práticas

### 4.1 MapStruct
✅ Use interface para mappers simples  
✅ Use classe abstrata quando precisar injetar dependências  
✅ Configure `componentModel = "spring"` para integração  
✅ Use `uses` para reutilizar outros mappers  
✅ Prefira expressões Java para lógica simples  
✅ Use `@AfterMapping` para validações pós-mapeamento

### 4.2 Criteria API
✅ Crie classes utilitárias com Specifications reutilizáveis  
✅ Use nomes descritivos para os métodos (tituloLike, precoMaiorQue)  
✅ Combine Specifications com `.and()` e `.or()`  
✅ Use `JoinType.LEFT` para joins opcionais  
✅ Adicione `query.distinct(true)` quando necessário  
✅ Prefira Criteria API para consultas dinâmicas complexas

---

## 5. Documentação Oficial

- [MapStruct Reference](https://mapstruct.org/documentation/stable/reference/html/)
- [Criteria API Tutorial](https://jakarta.ee/learn/docs/jakartaee-tutorial/current/persist/persistence-criteria/persistence-criteria.html)
- [Spring Data JPA Specifications](https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html)