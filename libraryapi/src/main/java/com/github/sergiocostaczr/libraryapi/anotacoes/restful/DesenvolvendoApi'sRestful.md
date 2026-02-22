# Spring - Desenvolvimento de APIs RESTful

## 1. Fundamentos REST

### 1.1 O que é REST?

**REST** (Representational State Transfer) é um conjunto de princípios arquiteturais para desenvolvimento de APIs web.

**Princípios REST:**
- **Cliente-Servidor**: Separação de responsabilidades
- **Stateless**: Cada requisição é independente (sem sessão no servidor)
- **Cacheable**: Respostas podem ser cacheadas
- **Interface Uniforme**: Padrão consistente de comunicação
- **Sistema em Camadas**: Arquitetura modular

---

### 1.2 Recursos (Resources)

Recursos são entidades do sistema expostas pela API:
- `/autores` - Coleção de autores
- `/autores/{id}` - Autor específico
- `/livros` - Coleção de livros
- `/livros/{id}` - Livro específico

**Características:**
- Use substantivos no plural
- Use hierarquias quando apropriado: `/autores/{id}/livros`
- Evite verbos nas URIs

---

## 2. Protocolo HTTP

### 2.1 Métodos HTTP (Verbos)

| Método | Uso | Idempotente | Safe |
|--------|-----|-------------|------|
| **GET** | Buscar recursos | ✅ Sim | ✅ Sim |
| **POST** | Criar recurso | ❌ Não | ❌ Não |
| **PUT** | Atualizar recurso completo | ✅ Sim | ❌ Não |
| **PATCH** | Atualizar parcialmente | ❌ Não | ❌ Não |
| **DELETE** | Excluir recurso | ✅ Sim | ❌ Não |

**Idempotente**: Múltiplas requisições idênticas têm o mesmo efeito que uma única.  
**Safe**: Não modifica dados no servidor.

---

### 2.2 Estrutura de uma Requisição HTTP

```http
POST /autores HTTP/1.1
Host: api.library.com
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Accept: application/json

{
  "nome": "Machado de Assis",
  "dataNascimento": "1839-06-21",
  "nacionalidade": "Brasileiro"
}
```

**Componentes:**
1. **Método**: POST
2. **URI**: /autores
3. **Headers**: Metadados (Content-Type, Authorization)
4. **Body**: Dados enviados (JSON)

---

### 2.3 Estrutura de uma Resposta HTTP

```http
HTTP/1.1 201 Created
Content-Type: application/json
Location: /autores/a1b2c3d4-e5f6-7890-abcd-ef1234567890
Date: Mon, 27 Jan 2026 10:30:00 GMT

{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "nome": "Machado de Assis",
  "dataNascimento": "1839-06-21",
  "nacionalidade": "Brasileiro"
}
```

**Componentes:**
1. **Status Code**: 201 Created
2. **Headers**: Metadados da resposta
3. **Body**: Dados retornados (JSON)

---

### 2.4 Códigos de Status HTTP

#### **2xx - Sucesso**
- **200 OK**: Requisição bem-sucedida
- **201 Created**: Recurso criado com sucesso
- **204 No Content**: Sucesso sem retornar dados

#### **4xx - Erro do Cliente**
- **400 Bad Request**: Requisição mal formada
- **404 Not Found**: Recurso não encontrado
- **409 Conflict**: Conflito (ex: registro duplicado)
- **422 Unprocessable Entity**: Erro de validação

#### **5xx - Erro do Servidor**
- **500 Internal Server Error**: Erro interno
- **503 Service Unavailable**: Serviço indisponível

---

## 3. Modelagem do Contrato da API

### 3.1 Cadastrar Novo Autor

#### Requisição
```http
POST /autores
Content-Type: application/json

{
  "nome": "string",
  "dataNascimento": "date",
  "nacionalidade": "string"
}
```

#### Respostas

**1. Sucesso (201 Created)**
```http
HTTP/1.1 201 Created
Location: /autores/{id}
Content-Type: application/json

{
  "id": "uuid",
  "nome": "string",
  "dataNascimento": "date",
  "nacionalidade": "string"
}
```

**2. Erro de Validação (422 Unprocessable Entity)**
```json
{
  "status": 422,
  "message": "Erro de Validação",
  "errors": [
    { "field": "nome", "error": "Nome é obrigatório" }
  ]
}
```

**3. Autor Duplicado (409 Conflict)**
```json
{
  "status": 409,
  "message": "Registro Duplicado",
  "errors": []
}
```

---

### 3.2 Visualizar Detalhes do Autor

#### Requisição
```http
GET /autores/{id}
```

#### Respostas

**1. Sucesso (200 OK)**
```json
{
  "id": "uuid",
  "nome": "string",
  "dataNascimento": "date",
  "nacionalidade": "string"
}
```

**2. Não Encontrado (404 Not Found)**
```json
{
  "status": 404,
  "message": "Autor não encontrado",
  "errors": []
}
```

---

### 3.3 Atualizar Autor

#### Requisição
```http
PUT /autores/{id}
Content-Type: application/json

{
  "nome": "string",
  "dataNascimento": "date",
  "nacionalidade": "string"
}
```

#### Respostas

**1. Sucesso (204 No Content)**
```http
HTTP/1.1 204 No Content
```

**2. Erro de Validação (422)**
```json
{
  "status": 422,
  "message": "Erro de Validação",
  "errors": [
    { "field": "nome", "error": "Nome é obrigatório" }
  ]
}
```

**3. Duplicado (409 Conflict)**
```json
{
  "status": 409,
  "message": "Registro Duplicado",
  "errors": []
}
```

---

### 3.4 Excluir Autor

#### Requisição
```http
DELETE /autores/{id}
```

#### Respostas

**1. Sucesso (204 No Content)**
```http
HTTP/1.1 204 No Content
```

**2. Erro - Autor com Livros (400 Bad Request)**
```json
{
  "status": 400,
  "message": "Erro na exclusão: registro está sendo utilizado.",
  "errors": []
}
```

---

### 3.5 Pesquisar Autores

#### Requisição
```http
GET /autores?nome=Machado&nacionalidade=Brasileiro
```

**Query Parameters:**
- `nome` (opcional): Filtrar por nome
- `nacionalidade` (opcional): Filtrar por nacionalidade

#### Resposta (200 OK)
```json
[
  {
    "id": "uuid",
    "nome": "Machado de Assis",
    "dataNascimento": "1839-06-21",
    "nacionalidade": "Brasileiro"
  },
  {
    "id": "uuid",
    "nome": "Machado Santos",
    "dataNascimento": "1875-03-10",
    "nacionalidade": "Brasileiro"
  }
]
```

---

## 4. Implementação da API

### 4.1 Model - Entidade Autor

```java
@Entity
@Table(name = "autor", schema = "public")
@Getter
@Setter
@ToString(exclude = "livros")
@EntityListeners(AuditingEntityListener.class)
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "nacionalidade", length = 50, nullable = false)
    private String nacionalidade;

    @OneToMany(mappedBy = "autor", fetch = FetchType.LAZY)
    private List<Livro> livros;

    // Auditoria automática
    @CreatedDate
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "id_usuario")
    private UUID idUsuario;
}
```

**Pontos importantes:**
- `@EntityListeners(AuditingEntityListener.class)`: Habilita auditoria
- `@CreatedDate`: Preenche automaticamente na criação
- `@LastModifiedDate`: Atualiza automaticamente em modificações

---

### 4.2 DTO - Data Transfer Object

```java
package com.github.sergiocostaczr.libraryapi.controller.dto;

import com.github.sergiocostaczr.libraryapi.model.Autor;
import java.time.LocalDate;
import java.util.UUID;

public record AutorDTO(
        UUID id,
        String nome,
        LocalDate dataNascimento,
        String nacionalidade
) {

    // Método auxiliar para converter DTO em Entidade
    public Autor mapearParaAutor() {
        Autor autor = new Autor();
        autor.setNome(this.nome);
        autor.setDataNascimento(this.dataNascimento);
        autor.setNacionalidade(this.nacionalidade);
        return autor;
    }
}
```

**Records (Java 14+):**
- Imutável por padrão
- Gera automaticamente: construtor, getters, equals, hashCode, toString
- Ideal para DTOs

---

### 4.3 Repository

```java
@Repository
public interface AutorRepository extends JpaRepository<Autor, UUID> {

    // Query Methods
    List<Autor> findByNome(String nome);

    List<Autor> findByNacionalidade(String nacionalidade);

    List<Autor> findByNomeAndNacionalidade(String nome, String nacionalidade);

    Optional<Autor> findByNomeAndDataNascimentoAndNacionalidade(
            String nome,
            LocalDate dataNascimento,
            String nacionalidade
    );
}
```

---

### 4.4 Validator - Validação de Negócio

```java
@Component
public class AutorValidator {

    private final AutorRepository autorRepository;

    public AutorValidator(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    public void validar(Autor autor) {
        if (existeAutorCadastrado(autor)) {
            throw new RegistroDuplicadoException("Autor já cadastrado!");
        }
    }

    private boolean existeAutorCadastrado(Autor autor) {
        Optional<Autor> autorOptional = autorRepository
                .findByNomeAndDataNascimentoAndNacionalidade(
                        autor.getNome(),
                        autor.getDataNascimento(),
                        autor.getNacionalidade()
                );

        // Novo autor
        if (autor.getId() == null) {
            return autorOptional.isPresent();
        }

        // Atualização: verifica se não é o próprio autor
        return autorOptional.isPresent() &&
                !autor.getId().equals(autorOptional.get().getId());
    }
}
```

**Lógica de Validação:**
- Se é um **novo autor** (id == null): verifica se já existe
- Se é **atualização**: verifica se o autor duplicado não é ele mesmo

---

### 4.5 Service - Lógica de Negócio

```java
@Service
@RequiredArgsConstructor // Gera construtor com variáveis final
public class AutorService {

    private final AutorRepository autorRepository;
    private final LivroRepository livroRepository;
    private final AutorValidator validator;

    public Autor salvar(Autor autor) {
        validator.validar(autor);
        return autorRepository.save(autor);
    }

    public void atualizar(Autor autor) {
        if (autor.getId() == null) {
            throw new IllegalArgumentException(
                    "Para atualizar é necessário que autor já esteja salvo na base"
            );
        }
        validator.validar(autor);
        autorRepository.save(autor);
    }

    public Optional<Autor> obterPorId(UUID id) {
        return autorRepository.findById(id);
    }

    public void deletar(Autor autor) {
        if (possuiLivro(autor)) {
            throw new OperacaoNaoPermitidaException(
                    "Não é permitido excluir autor que possui livros cadastrados"
            );
        }
        autorRepository.delete(autor);
    }

    public List<Autor> pesquisa(String nome, String nacionalidade) {
        // Ambos os filtros
        if (nome != null && nacionalidade != null) {
            return autorRepository.findByNomeAndNacionalidade(nome, nacionalidade);
        }

        // Apenas nome
        if (nome != null) {
            return autorRepository.findByNome(nome);
        }

        // Apenas nacionalidade
        if (nacionalidade != null) {
            return autorRepository.findByNacionalidade(nacionalidade);
        }

        // Sem filtros - retorna todos
        return autorRepository.findAll();
    }

    private boolean possuiLivro(Autor autor) {
        return livroRepository.existsByAutor(autor);
    }
}
```

**Responsabilidades do Service:**
- Validações de negócio
- Orquestração de operações
- Coordenação entre repositories
- Regras de integridade

---

### 4.6 Controller - Endpoints REST

```java
@RestController
@RequestMapping("/autores")
@RequiredArgsConstructor
public class AutorController {

    private final AutorService service;
    private final AutorMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AutorDTO> criar(@RequestBody @Valid AutorDTO dto) {
        Autor autor = dto.mapearParaAutor();
        Autor salvo = service.salvar(autor);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();

        AutorDTO response = mapper.toDTO(salvo);
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AutorDTO> obterPorId(@PathVariable UUID id) {
        return service.obterPorId(id)
                .map(mapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AutorDTO>> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String nacionalidade) {

        List<Autor> autores = service.pesquisa(nome, nacionalidade);
        List<AutorDTO> response = autores.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable UUID id, @RequestBody @Valid AutorDTO dto) {
        Autor autor = dto.mapearParaAutor();
        autor.setId(id);
        service.atualizar(autor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        Autor autor = service.obterPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Autor não encontrado"
                ));
        service.deletar(autor);
    }
}
```

---

### 4.7 Mapper - Conversão entre Entidade e DTO

```java
@Component
public class AutorMapper {

    public AutorDTO toDTO(Autor autor) {
        return new AutorDTO(
                autor.getId(),
                autor.getNome(),
                autor.getDataNascimento(),
                autor.getNacionalidade()
        );
    }

    public Autor toEntity(AutorDTO dto) {
        Autor autor = new Autor();
        autor.setId(dto.id());
        autor.setNome(dto.nome());
        autor.setDataNascimento(dto.dataNascimento());
        autor.setNacionalidade(dto.nacionalidade());
        return autor;
    }
}
```

---

## 5. Exceptions Customizadas

### 5.1 RegistroDuplicadoException

```java
public class RegistroDuplicadoException extends RuntimeException {
    public RegistroDuplicadoException(String message) {
        super(message);
    }
}
```

---

### 5.2 OperacaoNaoPermitidaException

```java
public class OperacaoNaoPermitidaException extends RuntimeException {
    public OperacaoNaoPermitidaException(String message) {
        super(message);
    }
}
```

---

### 5.3 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegistroDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleRegistroDuplicado(RegistroDuplicadoException ex) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                List.of()
        );
    }

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleOperacaoNaoPermitida(OperacaoNaoPermitidaException ex) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                List.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        return new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Erro de Validação",
                fieldErrors
        );
    }

    public record ErrorResponse(
            int status,
            String message,
            List<FieldError> errors
    ) {}

    public record FieldError(
            String field,
            String error
    ) {}
}
```

---

## 6. Validação com Bean Validation

### 6.1 DTO com Validações

```java
public record AutorDTO(
        UUID id,

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
        String nome,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve estar no passado")
        LocalDate dataNascimento,

        @NotBlank(message = "Nacionalidade é obrigatória")
        @Size(max = 50, message = "Nacionalidade deve ter no máximo 50 caracteres")
        String nacionalidade
) {
    // ...
}
```

**Principais Annotations:**
- `@NotNull`: Não pode ser null
- `@NotBlank`: Não pode ser null, vazio ou apenas espaços
- `@Size`: Define tamanho mínimo/máximo
- `@Past`: Data no passado
- `@Future`: Data no futuro
- `@Email`: Valida formato de email
- `@Pattern`: Valida regex

---

## 7. Auditoria com Spring Data JPA

### 7.1 Habilitando Auditoria

```java
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}
```

---

### 7.2 Entidade com Auditoria

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Autor {

    @CreatedDate
    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @CreatedBy
    @Column(name = "criado_por", updatable = false)
    private String criadoPor;

    @LastModifiedBy
    @Column(name = "modificado_por")
    private String modificadoPor;
}
```

**Annotations de Auditoria:**
- `@CreatedDate`: Data de criação (preenchida automaticamente)
- `@LastModifiedDate`: Data da última modificação
- `@CreatedBy`: Usuário que criou
- `@LastModifiedBy`: Usuário que modificou

---

## 8. Testando a API

### 8.1 Criar Autor (POST)

**Request:**
```http
POST http://localhost:8080/autores
Content-Type: application/json

{
  "nome": "Machado de Assis",
  "dataNascimento": "1839-06-21",
  "nacionalidade": "Brasileiro"
}
```

**Response (201 Created):**
```http
HTTP/1.1 201 Created
Location: /autores/a1b2c3d4-e5f6-7890-abcd-ef1234567890
Content-Type: application/json

{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "nome": "Machado de Assis",
  "dataNascimento": "1839-06-21",
  "nacionalidade": "Brasileiro"
}
```

---

### 8.2 Buscar por ID (GET)

**Request:**
```http
GET http://localhost:8080/autores/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response (200 OK):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "nome": "Machado de Assis",
  "dataNascimento": "1839-06-21",
  "nacionalidade": "Brasileiro"
}
```

---

### 8.3 Pesquisar com Filtros (GET)

**Request:**
```http
GET http://localhost:8080/autores?nacionalidade=Brasileiro
```

**Response (200 OK):**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "nome": "Machado de Assis",
    "dataNascimento": "1839-06-21",
    "nacionalidade": "Brasileiro"
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "nome": "Clarice Lispector",
    "dataNascimento": "1920-12-10",
    "nacionalidade": "Brasileiro"
  }
]
```

---

### 8.4 Atualizar (PUT)

**Request:**
```http
PUT http://localhost:8080/autores/a1b2c3d4-e5f6-7890-abcd-ef1234567890
Content-Type: application/json

{
  "nome": "Joaquim Maria Machado de Assis",
  "dataNascimento": "1839-06-21",
  "nacionalidade": "Brasileiro"
}
```

**Response (204 No Content)**

---

### 8.5 Deletar (DELETE)

**Request:**
```http
DELETE http://localhost:8080/autores/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response (204 No Content)**

---

### 8.6 Erro - Autor Duplicado (409)

**Request:**
```http
POST http://localhost:8080/autores
Content-Type: application/json

{
  "nome": "Machado de Assis",
  "dataNascimento": "1839-06-21",
  "nacionalidade": "Brasileiro"
}
```

**Response (409 Conflict):**
```json
{
  "status": 409,
  "message": "Autor já cadastrado!",
  "errors": []
}
```

---

### 8.7 Erro - Validação (422)

**Request:**
```http
POST http://localhost:8080/autores
Content-Type: application/json

{
  "nome": "",
  "dataNascimento": null,
  "nacionalidade": "Brasileiro"
}
```

**Response (422 Unprocessable Entity):**
```json
{
  "status": 422,
  "message": "Erro de Validação",
  "errors": [
    { "field": "nome", "error": "Nome é obrigatório" },
    { "field": "dataNascimento", "error": "Data de nascimento é obrigatória" }
  ]
}
```

---

## 9. Boas Práticas REST

### 9.1 URIs

✅ **Correto:**
- `/autores` - Coleção
- `/autores/{id}` - Recurso específico
- `/autores?nome=Machado` - Filtros via query params

❌ **Evite:**
- `/getAutores` - Verbos na URI
- `/autor` - Singular
- `/autores/buscarPorNome?nome=Machado` - Ações na URI

---

### 9.2 Métodos HTTP

✅ **Use corretamente:**
- **GET** para leitura
- **POST** para criação
- **PUT** para atualização completa
- **PATCH** para atualização parcial
- **DELETE** para exclusão

❌ **Evite:**
- POST para tudo
- GET para operações que modificam dados

---

### 9.3 Status Codes

✅ **Use apropriadamente:**
- **200** para GET bem-sucedido
- **201** para POST bem-sucedido (com Location header)
- **204** para PUT/DELETE bem-sucedido
- **400** para erro do cliente
- **404** para recurso não encontrado
- **422** para erro de validação
- **409** para conflito (duplicado)
- **500** para erro do servidor

---

### 9.4 DTOs

✅ **Sempre use DTOs:**
- Não exponha entidades JPA diretamente
- DTOs controlam o que é exposto
- Facilitam versionamento da API
- Evitam exposição de dados sensíveis

---

### 9.5 Validação

✅ **Valide em múltiplas camadas:**
- **Controller**: Validações básicas (Bean Validation)
- **Service**: Validações de negócio (duplicidade, integridade)
- **Repository**: Constraints de banco

---

### 9.6 Paginação

Para listas grandes, use paginação:

```java
@GetMapping
public ResponseEntity<Page<AutorDTO>> pesquisar(
        @RequestParam(required = false) String nome,
        @PageableDefault(size = 20, sort = "nome") Pageable pageable) {

    Page<Autor> autores = service.pesquisar(nome, pageable);
    Page<AutorDTO> response = autores.map(mapper::toDTO);

    return ResponseEntity.ok(response);
}
```

**Request:**
```http
GET /autores?page=0&size=10&sort=nome,asc
```

---

## 10. Versionamento de API

### 10.1 Via URI (Recomendado)

```java
@RequestMapping("/api/v1/autores")
public class AutorControllerV1 { }

@RequestMapping("/api/v2/autores")
public class AutorControllerV2 { }
```

---

### 10.2 Via Header

```java
@GetMapping(headers = "X-API-VERSION=1")
public ResponseEntity<AutorDTO> obterV1(@PathVariable UUID id) { }

@GetMapping(headers = "X-API-VERSION=2")
public ResponseEntity<AutorDTOV2> obterV2(@PathVariable UUID id) { }
```

---

## 11. HATEOAS (Hypermedia)

Adiciona links para navegação na API:

```java
@GetMapping("/{id}")
public ResponseEntity<EntityModel<AutorDTO>> obterPorId(@PathVariable UUID id) {
    Autor autor = service.obterPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    AutorDTO dto = mapper.toDTO(autor);

    EntityModel<AutorDTO> resource = EntityModel.of(dto);
    resource.add(linkTo(methodOn(AutorController.class).obterPorId(id)).withSelfRel());
    resource.add(linkTo(methodOn(AutorController.class).pesquisar(null, null)).withRel("autores"));
    resource.add(linkTo(methodOn(LivroController.class).listarPorAutor(id)).withRel("livros"));

    return ResponseEntity.ok(resource);
}
```

**Response:**
```json
{
  "id": "a1b2c3d4",
  "nome": "Machado de Assis",
  "dataNascimento": "1839-06-21",
  "nacionalidade": "Brasileiro",
  "_links": {
    "self": { "href": "/autores/a1b2c3d4" },
    "autores": { "href": "/autores" },
    "livros": { "href": "/autores/a1b2c3d4/livros" }
  }
}
```

---

## 12. Resumo de Annotations REST

| Annotation | Uso |
|------------|-----|
| `@RestController` | Marca classe como controller REST |
| `@RequestMapping` | Define rota base |
| `@GetMapping` | Endpoint GET |
| `@PostMapping` | Endpoint POST |
| `@PutMapping` | Endpoint PUT |
| `@PatchMapping` | Endpoint PATCH |
| `@DeleteMapping` | Endpoint DELETE |
| `@PathVariable` | Variável na URI (`/autores/{id}`) |
| `@RequestParam` | Query parameter (`?nome=valor`) |
| `@RequestBody` | Corpo da requisição (JSON) |
| `@ResponseStatus` | Define status code padrão |
| `@Valid` | Ativa validação Bean Validation |
| `@RestControllerAdvice` | Tratamento global de exceptions |
| `@ExceptionHandler` | Trata exception específica |

---

## 13. Fluxo Completo de uma Requisição

```
1. Request HTTP chega no Controller
   ↓
2. Controller valida dados (@Valid)
   ↓
3. Controller chama Service
   ↓
4. Service executa validações de negócio
   ↓
5. Service chama Repository
   ↓
6. Repository acessa banco de dados
   ↓
7. Dados retornam para Service
   ↓
8. Service retorna para Controller
   ↓
9. Controller converte para DTO
   ↓
10. Response HTTP é enviado ao cliente
```

---

## 14. Checklist de Implementação de API

### Criar novo endpoint REST:

✅ Definir contrato da API (request/response)  
✅ Criar/atualizar entidade JPA  
✅ Criar DTO (request e response)  
✅ Criar/atualizar Repository  
✅ Criar Validator (se necessário)  
✅ Criar/atualizar Service  
✅ Criar Controller com endpoints  
✅ Criar Mapper (Entity ↔ DTO)  
✅ Criar exceptions customizadas  
✅ Implementar tratamento de erros  
✅ Adicionar validações Bean Validation  
✅ Testar com Postman/Insomnia  
✅ Documentar com Swagger (OpenAPI)

---

## 15. Documentação Oficial

- [Spring REST Docs](https://spring.io/guides/gs/rest-service/)
- [Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Bean Validation](https://beanvalidation.org/)
- [HTTP Status Codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
- [REST API Design](https://restfulapi.net/)