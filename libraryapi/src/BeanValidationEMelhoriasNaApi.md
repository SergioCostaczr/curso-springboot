# Spring - Bean Validation e Melhorias na API

## 1. Introdução ao Bean Validation

### 1.1 O que é Bean Validation?

**Bean Validation** é uma especificação Java (JSR 380) que permite validar objetos usando annotations. O Spring Boot usa a implementação **Hibernate Validator** por padrão.

**Vantagens:**
- Validações declarativas (usando annotations)
- Reutilizável em diferentes camadas
- Mensagens de erro customizáveis
- Integração automática com Spring MVC

---

### 1.2 Dependência

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Nota:** Já incluída no `spring-boot-starter-web`.

---

## 2. Principais Annotations de Validação

### 2.1 Validações de Nulidade

| Annotation | Descrição | Exemplo |
|------------|-----------|---------|
| `@NotNull` | Não pode ser null | `@NotNull Integer idade` |
| `@NotEmpty` | Não pode ser null nem vazio (String, Collection) | `@NotEmpty String nome` |
| `@NotBlank` | Não pode ser null, vazio ou apenas espaços (String) | `@NotBlank String email` |

**Diferenças:**
```java
String texto = "   ";

@NotNull    // ✅ Válido (não é null)
@NotEmpty   // ✅ Válido (não está vazio)
@NotBlank   // ❌ Inválido (só tem espaços)
```

---

### 2.2 Validações de Tamanho

| Annotation | Descrição | Exemplo |
|------------|-----------|---------|
| `@Size` | Tamanho mín/máx (String, Collection, Array) | `@Size(min=2, max=100)` |
| `@Length` | Similar ao @Size (apenas Hibernate) | `@Length(min=2, max=50)` |
| `@Min` | Valor numérico mínimo | `@Min(18) Integer idade` |
| `@Max` | Valor numérico máximo | `@Max(120) Integer idade` |

---

### 2.3 Validações de Data/Tempo

| Annotation | Descrição | Exemplo |
|------------|-----------|---------|
| `@Past` | Data no passado | `@Past LocalDate nascimento` |
| `@PastOrPresent` | Data no passado ou hoje | `@PastOrPresent LocalDate` |
| `@Future` | Data no futuro | `@Future LocalDate vencimento` |
| `@FutureOrPresent` | Data no futuro ou hoje | `@FutureOrPresent LocalDate` |

---

### 2.4 Validações de Valor

| Annotation | Descrição | Exemplo |
|------------|-----------|---------|
| `@Positive` | Número positivo (> 0) | `@Positive BigDecimal preco` |
| `@PositiveOrZero` | Número positivo ou zero (>= 0) | `@PositiveOrZero Integer` |
| `@Negative` | Número negativo (< 0) | `@Negative Integer` |
| `@NegativeOrZero` | Número negativo ou zero (<= 0) | `@NegativeOrZero Integer` |
| `@DecimalMin` | Valor decimal mínimo | `@DecimalMin("0.01")` |
| `@DecimalMax` | Valor decimal máximo | `@DecimalMax("999.99")` |

---

### 2.5 Validações de Formato

| Annotation | Descrição | Exemplo |
|------------|-----------|---------|
| `@Email` | Formato de email válido | `@Email String email` |
| `@Pattern` | Expressão regular (regex) | `@Pattern(regexp="\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")` |
| `@Digits` | Número de dígitos inteiros/fracionários | `@Digits(integer=5, fraction=2)` |

---

### 2.6 Validações Booleanas

| Annotation | Descrição | Exemplo |
|------------|-----------|---------|
| `@AssertTrue` | Deve ser true | `@AssertTrue Boolean ativo` |
| `@AssertFalse` | Deve ser false | `@AssertFalse Boolean` |

---

## 3. Implementação no DTO

### 3.1 AutorDTO com Validações

```java
package com.github.sergiocostaczr.libraryapi.controller.dto;

import com.github.sergiocostaczr.libraryapi.model.Autor;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record AutorDTO(
    UUID id,

    @NotBlank(message = "Campo obrigatório")
    @Size(min = 2, max = 100, message = "Campo fora do tamanho padrão")
    String nome,

    @NotNull(message = "Campo obrigatório")
    @Past(message = "Não pode ser uma data futura")
    LocalDate dataNascimento,

    @NotBlank(message = "Campo obrigatório")
    @Size(min = 2, max = 50, message = "Campo fora do tamanho padrão")
    String nacionalidade
) {

    public Autor mapearParaAutor() {
        Autor autor = new Autor();
        autor.setNome(this.nome);
        autor.setDataNascimento(this.dataNascimento);
        autor.setNacionalidade(this.nacionalidade);
        return autor;
    }
}
```

**Explicação das Validações:**

1. **nome**:
   - `@NotBlank`: Não pode ser nulo, vazio ou só espaços
   - `@Size(min=2, max=100)`: Entre 2 e 100 caracteres

2. **dataNascimento**:
   - `@NotNull`: Não pode ser nulo
   - `@Past`: Deve ser uma data no passado

3. **nacionalidade**:
   - `@NotBlank`: Não pode ser nulo ou vazio
   - `@Size(min=2, max=50)`: Entre 2 e 50 caracteres

---

### 3.2 Outros Exemplos de DTOs

#### LivroDTO

```java
public record LivroDTO(
    UUID id,

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 1, max = 150, message = "Título deve ter entre 1 e 150 caracteres")
    String titulo,

    @NotBlank(message = "ISBN é obrigatório")
    @Pattern(regexp = "^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+$", 
             message = "ISBN inválido")
    String isbn,

    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser positivo")
    @DecimalMin(value = "0.01", message = "Preço mínimo é R$ 0,01")
    BigDecimal preco,

    @NotNull(message = "Data de publicação é obrigatória")
    @PastOrPresent(message = "Data de publicação não pode ser futura")
    LocalDate dataPublicacao,

    @NotBlank(message = "Gênero é obrigatório")
    String genero,

    @NotNull(message = "Autor é obrigatório")
    UUID idAutor
) {
    // ...
}
```

#### UsuarioDTO

```java
public record UsuarioDTO(
    UUID id,

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100)
    String nome,

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", 
             message = "CPF deve estar no formato XXX.XXX.XXX-XX")
    String cpf,

    @NotNull(message = "Idade é obrigatória")
    @Min(value = 18, message = "Idade mínima é 18 anos")
    @Max(value = 120, message = "Idade máxima é 120 anos")
    Integer idade,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
             message = "Senha deve conter maiúscula, minúscula, número e caractere especial")
    String senha
) {
    // ...
}
```

---

## 4. Ativando Validação no Controller

### 4.1 Usando @Valid

```java
@RestController
@RequestMapping("/autores")
@RequiredArgsConstructor
public class AutorController {

    private final AutorService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AutorDTO> criar(@RequestBody @Valid AutorDTO dto) {
        // @Valid ativa as validações do DTO
        Autor autor = dto.mapearParaAutor();
        Autor salvo = service.salvar(autor);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(salvo.getId())
            .toUri();
        
        AutorDTO response = new AutorDTO(
            salvo.getId(),
            salvo.getNome(),
            salvo.getDataNascimento(),
            salvo.getNacionalidade()
        );
        
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(
            @PathVariable UUID id, 
            @RequestBody @Valid AutorDTO dto) {
        // @Valid também valida em PUT
        Autor autor = dto.mapearParaAutor();
        autor.setId(id);
        service.atualizar(autor);
    }
}
```

**Pontos Importantes:**
- `@Valid`: Ativa a validação do objeto
- Se houver erro de validação, lança `MethodArgumentNotValidException`
- Sem `@Valid`, as validações do DTO são ignoradas

---

## 5. Tratamento Global de Erros de Validação

### 5.1 Classes de Resposta de Erro

```java
package com.github.sergiocostaczr.libraryapi.controller.dto;

import java.util.List;

public record ErroResposta(
    int status,
    String message,
    List<ErroCampo> errors
) {}
```

```java
package com.github.sergiocostaczr.libraryapi.controller.dto;

public record ErroCampo(
    String field,
    String error
) {}
```

---

### 5.2 GlobalExceptionHandler

```java
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

@RestControllerAdvice // Captura exceptions e retorna uma Response
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY) // 422
    public ErroResposta handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        
        // Extrai os erros de validação
        List<FieldError> fieldErrors = e.getFieldErrors();
        
        // Converte para o formato da nossa resposta
        List<ErroCampo> listaErros = fieldErrors.stream()
            .map(fe -> new ErroCampo(fe.getField(), fe.getDefaultMessage()))
            .toList();
        
        return new ErroResposta(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Erro de validação",
            listaErros
        );
    }
}
```

**Explicação:**
1. `@RestControllerAdvice`: Intercepta exceptions globalmente
2. `@ExceptionHandler`: Define qual exception será tratada
3. `@ResponseStatus`: Define o status HTTP da resposta (422)
4. `e.getFieldErrors()`: Obtém lista de campos com erro
5. Converte `FieldError` para `ErroCampo` (nosso formato)
6. Retorna `ErroResposta` padronizada

---

### 5.3 Exemplo de Resposta de Erro

**Request Inválido:**
```http
POST /autores
Content-Type: application/json

{
  "nome": "M",
  "dataNascimento": "2030-12-31",
  "nacionalidade": ""
}
```

**Response (422 Unprocessable Entity):**
```json
{
  "status": 422,
  "message": "Erro de validação",
  "errors": [
    {
      "field": "nome",
      "error": "Campo fora do tamanho padrão"
    },
    {
      "field": "dataNascimento",
      "error": "Não pode ser uma data futura"
    },
    {
      "field": "nacionalidade",
      "error": "Campo obrigatório"
    }
  ]
}
```

---

## 6. Query By Example - Pesquisa Dinâmica

### 6.1 O que é Query By Example?

**Query By Example (QBE)** é uma técnica que permite fazer consultas dinâmicas usando um objeto exemplo. O Spring Data JPA gera a query automaticamente baseada nos campos preenchidos.

**Vantagens:**
- Não precisa criar múltiplos métodos no Repository
- Consultas dinâmicas sem JPQL/SQL
- Código mais limpo e flexível
- Ideal para filtros opcionais

---

### 6.2 Repository com QueryByExampleExecutor

```java
@Repository
public interface AutorRepository extends 
        JpaRepository<Autor, UUID>, 
        QueryByExampleExecutor<Autor> {  // Adiciona suporte a QBE
    
    // Métodos query methods tradicionais
    List<Autor> findByNome(String nome);
    List<Autor> findByNacionalidade(String nacionalidade);
    List<Autor> findByNomeAndNacionalidade(String nome, String nacionalidade);
    
    Optional<Autor> findByNomeAndDataNascimentoAndNacionalidade(
        String nome, LocalDate dataNascimento, String nacionalidade);
}
```

**QueryByExampleExecutor** adiciona os métodos:
- `findAll(Example<T> example)`
- `findOne(Example<T> example)`
- `count(Example<T> example)`
- `exists(Example<T> example)`

---

### 6.3 Implementação no Service

```java
@Service
@RequiredArgsConstructor
public class AutorService {

    private final AutorRepository autorRepository;
    private final LivroRepository livroRepository;
    private final AutorValidator validator;

    // Método antigo (com múltiplos ifs)
    public List<Autor> pesquisaAntiga(String nome, String nacionalidade) {
        if (nome != null && nacionalidade != null) {
            return autorRepository.findByNomeAndNacionalidade(nome, nacionalidade);
        }
        
        if (nome != null) {
            return autorRepository.findByNome(nome);
        }
        
        if (nacionalidade != null) {
            return autorRepository.findByNacionalidade(nacionalidade);
        }
        
        return autorRepository.findAll();
    }

    // Método novo (usando Query By Example)
    public List<Autor> pesquisaByExample(String nome, String nacionalidade) {
        // 1. Criar objeto exemplo
        Autor autor = new Autor();
        autor.setNome(nome);
        autor.setNacionalidade(nacionalidade);

        // 2. Configurar o ExampleMatcher
        ExampleMatcher matcher = ExampleMatcher
            .matching()
            .withIgnoreNullValues()           // Ignora campos null
            .withIgnoreCase()                 // Case-insensitive
            .withStringMatcher(
                ExampleMatcher.StringMatcher.CONTAINING  // LIKE %valor%
            );

        // 3. Criar o Example
        Example<Autor> autorExample = Example.of(autor, matcher);

        // 4. Executar a consulta
        return autorRepository.findAll(autorExample);
    }
}
```

---

### 6.4 Entendendo o ExampleMatcher

#### **withIgnoreNullValues()**
```java
Autor exemplo = new Autor();
exemplo.setNome("Machado");  // Preenchido
exemplo.setNacionalidade(null);  // Null

// Query gerada: SELECT * FROM autor WHERE nome = 'Machado'
// nacionalidade é IGNORADA por ser null
```

#### **withIgnoreCase()**
```java
exemplo.setNome("machado");

// Query gerada: SELECT * FROM autor WHERE LOWER(nome) = LOWER('machado')
// Ignora maiúsculas/minúsculas
```

#### **StringMatcher.CONTAINING**
```java
exemplo.setNome("Machado");

// StringMatcher.CONTAINING
// Query: WHERE nome LIKE '%Machado%'

// StringMatcher.STARTING
// Query: WHERE nome LIKE 'Machado%'

// StringMatcher.ENDING
// Query: WHERE nome LIKE '%Machado'

// StringMatcher.EXACT (padrão)
// Query: WHERE nome = 'Machado'
```

---

### 6.5 Exemplos Práticos

#### Exemplo 1: Buscar por nome
```java
List<Autor> autores = service.pesquisaByExample("Machado", null);
// SQL: SELECT * FROM autor WHERE LOWER(nome) LIKE LOWER('%Machado%')
```

#### Exemplo 2: Buscar por nacionalidade
```java
List<Autor> autores = service.pesquisaByExample(null, "Brasileiro");
// SQL: SELECT * FROM autor WHERE LOWER(nacionalidade) LIKE LOWER('%Brasileiro%')
```

#### Exemplo 3: Buscar por ambos
```java
List<Autor> autores = service.pesquisaByExample("Machado", "Brasileiro");
// SQL: SELECT * FROM autor 
//      WHERE LOWER(nome) LIKE LOWER('%Machado%')
//      AND LOWER(nacionalidade) LIKE LOWER('%Brasileiro%')
```

#### Exemplo 4: Listar todos
```java
List<Autor> autores = service.pesquisaByExample(null, null);
// SQL: SELECT * FROM autor
// Todos os campos são null, retorna tudo
```

---

### 6.6 Configurações Avançadas do ExampleMatcher

```java
ExampleMatcher matcher = ExampleMatcher
    .matching()
    
    // Ignora campos null
    .withIgnoreNullValues()
    
    // Case-insensitive
    .withIgnoreCase()
    
    // Modo de matching para strings
    .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
    
    // Ignora campos específicos
    .withIgnorePaths("id", "dataCadastro", "dataAtualizacao")
    
    // Configuração por campo
    .withMatcher("nome", match -> match.startsWith())
    .withMatcher("nacionalidade", match -> match.exact())
    
    // Transforma valores antes de comparar
    .withTransformer("nome", o -> o.map(value -> value.toString().toUpperCase()));
```

---

### 6.7 Query By Example vs Query Methods

| Aspecto | Query Methods | Query By Example |
|---------|---------------|------------------|
| **Flexibilidade** | ❌ Fixa | ✅ Dinâmica |
| **Número de métodos** | ❌ Muitos | ✅ Um só |
| **Legibilidade** | ✅ Clara | ⚠️ Menos clara |
| **Performance** | ✅ Ótima | ✅ Ótima |
| **Complexidade** | ❌ Cresce rápido | ✅ Constante |
| **Joins** | ✅ Suportado | ⚠️ Limitado |

**Quando usar cada um:**
- **Query Methods**: Consultas fixas e simples
- **Query By Example**: Consultas dinâmicas com filtros opcionais
- **@Query (JPQL)**: Consultas complexas com joins

---

## 7. Validação Customizada

### 7.1 Criando Annotation de Validação

```java
package com.github.sergiocostaczr.libraryapi.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ISBNValidator.class)
@Documented
public @interface ISBN {
    String message() default "ISBN inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

---

### 7.2 Implementando o Validador

```java
package com.github.sergiocostaczr.libraryapi.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ISBNValidator implements ConstraintValidator<ISBN, String> {

    @Override
    public void initialize(ISBN constraintAnnotation) {
        // Inicialização se necessário
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank cuida disso
        }
        
        // Lógica de validação do ISBN
        String isbn = value.replaceAll("[^0-9]", "");
        
        return isbn.length() == 10 || isbn.length() == 13;
    }
}
```

---

### 7.3 Usando a Validação Customizada

```java
public record LivroDTO(
    UUID id,
    
    @NotBlank(message = "ISBN é obrigatório")
    @ISBN(message = "ISBN deve ter 10 ou 13 dígitos")
    String isbn,
    
    // outros campos...
) {}
```

---

## 8. Validação em Grupo

### 8.1 Definindo Grupos

```java
public interface OnCreate {}
public interface OnUpdate {}
```

---

### 8.2 Usando Grupos

```java
public record AutorDTO(
    @Null(groups = OnCreate.class, message = "ID deve ser null ao criar")
    @NotNull(groups = OnUpdate.class, message = "ID é obrigatório ao atualizar")
    UUID id,

    @NotBlank(message = "Nome é obrigatório")
    String nome,

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve estar no passado")
    LocalDate dataNascimento,

    @NotBlank(message = "Nacionalidade é obrigatória")
    String nacionalidade
) {}
```

---

### 8.3 Ativando Grupo no Controller

```java
@PostMapping
public ResponseEntity<AutorDTO> criar(
        @RequestBody @Validated(OnCreate.class) AutorDTO dto) {
    // Valida apenas as annotations do grupo OnCreate
    // ...
}

@PutMapping("/{id}")
public void atualizar(
        @PathVariable UUID id,
        @RequestBody @Validated(OnUpdate.class) AutorDTO dto) {
    // Valida apenas as annotations do grupo OnUpdate
    // ...
}
```

---

## 9. Resumo de Annotations de Validação

### Nulidade
- `@NotNull` - Não pode ser null
- `@NotEmpty` - Não pode ser null nem vazio
- `@NotBlank` - Não pode ser null, vazio ou só espaços (String)

### Tamanho
- `@Size(min, max)` - Tamanho de String, Collection, Array
- `@Min(valor)` - Valor numérico mínimo
- `@Max(valor)` - Valor numérico máximo

### Data/Tempo
- `@Past` - Data no passado
- `@PastOrPresent` - Data no passado ou presente
- `@Future` - Data no futuro
- `@FutureOrPresent` - Data no futuro ou presente

### Valor
- `@Positive` - Número positivo (> 0)
- `@PositiveOrZero` - Número >= 0
- `@Negative` - Número negativo (< 0)
- `@NegativeOrZero` - Número <= 0

### Formato
- `@Email` - Email válido
- `@Pattern(regexp)` - Expressão regular
- `@Digits(integer, fraction)` - Dígitos inteiros/decimais

### Booleano
- `@AssertTrue` - Deve ser true
- `@AssertFalse` - Deve ser false

---

## 10. Boas Práticas

### 10.1 Validações
✅ Use `@NotBlank` para Strings obrigatórias (não `@NotNull` ou `@NotEmpty`)  
✅ Sempre adicione mensagens customizadas  
✅ Valide no DTO, não na entidade  
✅ Use `@Valid` no Controller para ativar validação  
✅ Trate `MethodArgumentNotValidException` globalmente  

### 10.2 Query By Example
✅ Use para filtros dinâmicos  
✅ Configure `ExampleMatcher` adequadamente  
✅ Sempre use `withIgnoreNullValues()`  
✅ Use `CONTAINING` para buscas parciais  
✅ Prefira Query Methods para consultas fixas  

### 10.3 Exception Handling
✅ Crie DTOs específicos para erros  
✅ Retorne lista de campos com erro  
✅ Use status HTTP apropriados (422 para validação)  
✅ Seja consistente no formato de resposta  

---

## 11. Documentação Oficial

- [Bean Validation Specification](https://beanvalidation.org/)
- [Hibernate Validator](https://hibernate.org/validator/)
- [Spring Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)
- [Query By Example](https://docs.spring.io/spring-data/jpa/reference/repositories/query-by-example.html)