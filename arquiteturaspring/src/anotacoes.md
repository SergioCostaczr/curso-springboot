# Spring - Arquitetura e Injeção de Dependências

## 1. Contexto Spring e Beans

### 1.1 O que é o Contexto Spring?

O **Spring Context** (Container IoC) é responsável por:
- Criar e gerenciar os beans (objetos)
- Realizar injeção de dependências
- Controlar o ciclo de vida dos objetos

**Bean**: Objeto gerenciado pelo Spring Container.

---

### 1.2 Classe Principal - @SpringBootApplication

```java
package com.github.sergiocostaczr.libraryapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// Combina 3 annotations:
// @Configuration - Classe de configuração
// @EnableAutoConfiguration - Configuração automática
// @ComponentScan - Escaneia @Component, @Service, @Repository, @Controller
public class LibraryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApiApplication.class, args);
    }
}
```

---

## 2. Injeção de Dependências

### 2.1 O que é Injeção de Dependências?

É um padrão onde o Spring **fornece automaticamente** as dependências necessárias para uma classe, ao invés da classe criar suas próprias dependências.

### 2.2 Tipos de Injeção

#### **Constructor Injection (Recomendado)**

```java
@Service
public class LivroService {

    private final LivroRepository repository;
    private final EmailService emailService;

    // Spring injeta automaticamente as dependências
    public LivroService(LivroRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public Livro salvar(Livro livro) {
        Livro salvo = repository.save(livro);
        emailService.enviarNotificacao("Livro cadastrado: " + livro.getTitulo());
        return salvo;
    }
}
```

**Vantagens:**
- Imutabilidade (final)
- Dependências obrigatórias
- Melhor para testes

---

#### **Field Injection**

```java
@Service
public class AutorService {

    @Autowired
    private AutorRepository repository;

    @Autowired
    private EmailService emailService;

    public Autor salvar(Autor autor) {
        return repository.save(autor);
    }
}
```

**Desvantagens:**
- Não permite final
- Dificulta testes
- Acoplamento com o Spring

---

#### **Setter Injection**

```java
@Service
public class NotificacaoService {

    private EmailService emailService;

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
}
```

---

## 3. Stereotypes - Marcando Beans

### 3.1 Principais Annotations

```java
// Componente genérico
@Component
public class CalculadoraMulta {
    public BigDecimal calcular(int diasAtraso) {
        return BigDecimal.valueOf(diasAtraso * 2.5);
    }
}

// Camada de controle (REST)
@RestController
@RequestMapping("/api/livros")
public class LivroController {
    // endpoints REST
}

// Camada de serviço (regras de negócio)
@Service
public class LivroService {
    // lógica de negócio
}

// Camada de dados (acesso ao banco)
@Repository
public interface LivroRepository extends JpaRepository<Livro, UUID> {
}
```

**Hierarquia:**
- `@Component` - Genérico
    - `@Service` - Lógica de negócio
    - `@Repository` - Acesso a dados
    - `@Controller` / `@RestController` - Camada web

---

## 4. @Configuration e @Bean

### 4.1 Classe de Configuração

```java
package com.github.sergiocostaczr.libraryapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.format.DateTimeFormatter;

@Configuration
public class AppConfiguration {

    // Cria um bean gerenciado pelo Spring
    @Bean
    public DateTimeFormatter dateFormatter() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }

    @Bean
    public CalculadoraDesconto calculadoraDesconto() {
        return new CalculadoraDesconto(0.10); // 10% desconto
    }

    @Bean(name = "emailPrincipal")
    public String emailContato() {
        return "contato@library.com";
    }
}
```

**Quando usar @Bean:**
- Classes de terceiros (bibliotecas externas)
- Objetos que precisam de configuração específica
- Múltiplas instâncias da mesma classe com configurações diferentes

---

### 4.2 Usando os Beans

```java
@Service
public class RelatorioService {

    private final DateTimeFormatter dateFormatter;
    private final CalculadoraDesconto calculadora;

    public RelatorioService(DateTimeFormatter dateFormatter, 
                           CalculadoraDesconto calculadora) {
        this.dateFormatter = dateFormatter;
        this.calculadora = calculadora;
    }

    public String gerarRelatorio(Livro livro) {
        String data = dateFormatter.format(livro.getDataPublicacao());
        BigDecimal desconto = calculadora.calcular(livro.getPreco());
        
        return String.format("Livro: %s | Data: %s | Desconto: R$ %.2f",
                livro.getTitulo(), data, desconto);
    }
}
```

---

## 5. @Qualifier - Resolvendo Ambiguidade

### 5.1 Problema: Múltiplos Beans do Mesmo Tipo

```java
@Configuration
public class NotificacaoConfig {

    @Bean
    public NotificacaoService emailNotificacao() {
        return new EmailNotificacaoService();
    }

    @Bean
    public NotificacaoService smsNotificacao() {
        return new SmsNotificacaoService();
    }
}
```

**Problema:** Spring não sabe qual bean injetar!

---

### 5.2 Solução com @Qualifier

```java
@Service
public class EmprestimoService {

    private final NotificacaoService emailService;
    private final NotificacaoService smsService;

    public EmprestimoService(
            @Qualifier("emailNotificacao") NotificacaoService emailService,
            @Qualifier("smsNotificacao") NotificacaoService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public void registrarEmprestimo(Emprestimo emprestimo) {
        // Usa email para confirmação
        emailService.enviar("Empréstimo confirmado!");
        
        // Usa SMS para lembrete
        smsService.enviar("Lembre-se de devolver em 7 dias");
    }
}
```

---

### 5.3 @Primary - Bean Padrão

```java
@Configuration
public class NotificacaoConfig {

    @Bean
    @Primary // Este será injetado por padrão
    public NotificacaoService emailNotificacao() {
        return new EmailNotificacaoService();
    }

    @Bean
    public NotificacaoService smsNotificacao() {
        return new SmsNotificacaoService();
    }
}
```

```java
@Service
public class AlertaService {

    // Injeta o @Primary automaticamente (email)
    private final NotificacaoService notificacao;

    public AlertaService(NotificacaoService notificacao) {
        this.notificacao = notificacao;
    }
}
```

---

## 6. @Value - Lendo Propriedades

### 6.1 application.properties

```properties
# Configurações da aplicação
app.nome=Library API
app.versao=1.0.0
app.email.contato=suporte@library.com
app.multa.valor-por-dia=2.50
app.emprestimo.dias-maximos=14
```

---

### 6.2 Usando @Value

```java
@Service
public class ConfiguracaoService {

    @Value("${app.nome}")
    private String nomeApp;

    @Value("${app.versao}")
    private String versao;

    @Value("${app.email.contato}")
    private String emailContato;

    @Value("${app.multa.valor-por-dia}")
    private BigDecimal valorMultaDia;

    @Value("${app.emprestimo.dias-maximos}")
    private int diasMaximos;

    public void exibirConfiguracoes() {
        System.out.println("App: " + nomeApp);
        System.out.println("Versão: " + versao);
        System.out.println("Email: " + emailContato);
        System.out.println("Multa/dia: R$ " + valorMultaDia);
        System.out.println("Dias máximos: " + diasMaximos);
    }
}
```

---

### 6.3 Valores Padrão com @Value

```java
@Component
public class EmailService {

    @Value("${app.email.smtp.host:smtp.gmail.com}")
    private String smtpHost; // Padrão: smtp.gmail.com

    @Value("${app.email.smtp.port:587}")
    private int smtpPort; // Padrão: 587

    @Value("${app.email.timeout:5000}")
    private int timeout; // Padrão: 5000ms
}
```

---

## 7. @ConfigurationProperties - Configurações Tipadas

### 7.1 application.yml

```yaml
biblioteca:
  nome: Biblioteca Municipal
  endereco: Rua das Flores, 123
  telefone: (85) 3333-4444
  
  emprestimo:
    dias-maximos: 14
    renovacoes-permitidas: 2
    
  multa:
    valor-por-dia: 2.50
    valor-maximo: 50.00
    
  notificacao:
    email-habilitado: true
    sms-habilitado: false
    dias-antes-vencimento: 3
```

---

### 7.2 Classe de Configuração

```java
package com.github.sergiocostaczr.libraryapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "biblioteca")
@Getter
@Setter
public class BibliotecaProperties {

    private String nome;
    private String endereco;
    private String telefone;
    
    private Emprestimo emprestimo = new Emprestimo();
    private Multa multa = new Multa();
    private Notificacao notificacao = new Notificacao();

    @Getter
    @Setter
    public static class Emprestimo {
        private int diasMaximos;
        private int renovacoesPermitidas;
    }

    @Getter
    @Setter
    public static class Multa {
        private BigDecimal valorPorDia;
        private BigDecimal valorMaximo;
    }

    @Getter
    @Setter
    public static class Notificacao {
        private boolean emailHabilitado;
        private boolean smsHabilitado;
        private int diasAntesVencimento;
    }
}
```

---

### 7.3 Usando ConfigurationProperties

```java
@Service
public class EmprestimoService {

    private final BibliotecaProperties properties;

    public EmprestimoService(BibliotecaProperties properties) {
        this.properties = properties;
    }

    public BigDecimal calcularMulta(int diasAtraso) {
        BigDecimal valorDia = properties.getMulta().getValorPorDia();
        BigDecimal valorMaximo = properties.getMulta().getValorMaximo();
        
        BigDecimal multa = valorDia.multiply(BigDecimal.valueOf(diasAtraso));
        
        // Não excede o valor máximo
        return multa.min(valorMaximo);
    }

    public boolean podeRenovar(Emprestimo emprestimo) {
        return emprestimo.getRenovacoes() < 
               properties.getEmprestimo().getRenovacoesPermitidas();
    }

    public LocalDate calcularDataDevolucao() {
        return LocalDate.now()
                .plusDays(properties.getEmprestimo().getDiasMaximos());
    }
}
```

---

## 8. Annotations Customizadas

### 8.1 Criando Annotation

```java
package com.github.sergiocostaczr.libraryapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // Pode ser usada em classes
@Retention(RetentionPolicy.RUNTIME) // Disponível em runtime
public @interface Auditavel {
    String value() default "";
}
```

---

### 8.2 Usando a Annotation

```java
@Service
@Auditavel("LivroService")
public class LivroService {

    public Livro salvar(Livro livro) {
        // Lógica de salvamento
        System.out.println("Operação auditada: salvar livro");
        return livro;
    }
}
```

---

### 8.3 Annotation com Validação

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IsbnValidator.class)
public @interface ISBN {
    String message() default "ISBN inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

```java
public class LivroDTO {
    
    @ISBN(message = "O ISBN deve ter formato válido")
    private String isbn;
    
    // getters e setters
}
```

---

## 9. Arquitetura em Camadas - CRUD Completo

### 9.1 Model (Entidade)

```java
@Entity
@Table(name = "livro")
@Getter
@Setter
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String isbn;

    @Column(precision = 10, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    private GeneroLivro genero;
}
```

---

### 9.2 DTO (Data Transfer Object)

```java
package com.github.sergiocostaczr.libraryapi.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LivroDTO {

    private UUID id;
    private String titulo;
    private String isbn;
    private BigDecimal preco;
    private String genero;
}

@Data
public class CriarLivroDTO {

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @NotBlank(message = "ISBN é obrigatório")
    @ISBN
    private String isbn;

    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser positivo")
    private BigDecimal preco;

    @NotNull(message = "Gênero é obrigatório")
    private String genero;
}
```

---

### 9.3 Repository

```java
@Repository
public interface LivroRepository extends JpaRepository<Livro, UUID> {
    
    Optional<Livro> findByIsbn(String isbn);
    
    List<Livro> findByGenero(GeneroLivro genero);
    
    @Query("SELECT l FROM Livro l WHERE l.preco BETWEEN :min AND :max")
    List<Livro> findByPrecoRange(@Param("min") BigDecimal min, 
                                 @Param("max") BigDecimal max);
}
```

---

### 9.4 Service (Regras de Negócio)

```java
@Service
public class LivroService {

    private final LivroRepository repository;
    private final BibliotecaProperties properties;

    public LivroService(LivroRepository repository, 
                       BibliotecaProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public LivroDTO criar(CriarLivroDTO dto) {
        // Validar ISBN único
        repository.findByIsbn(dto.getIsbn())
                .ifPresent(l -> {
                    throw new BusinessException("ISBN já cadastrado");
                });

        // Converter DTO para Entity
        Livro livro = new Livro();
        livro.setTitulo(dto.getTitulo());
        livro.setIsbn(dto.getIsbn());
        livro.setPreco(dto.getPreco());
        livro.setGenero(GeneroLivro.valueOf(dto.getGenero()));

        // Salvar
        Livro salvo = repository.save(livro);

        // Converter Entity para DTO
        return converterParaDTO(salvo);
    }

    public LivroDTO buscarPorId(UUID id) {
        Livro livro = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Livro não encontrado"));
        return converterParaDTO(livro);
    }

    public List<LivroDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public LivroDTO atualizar(UUID id, CriarLivroDTO dto) {
        Livro livro = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Livro não encontrado"));

        livro.setTitulo(dto.getTitulo());
        livro.setPreco(dto.getPreco());
        livro.setGenero(GeneroLivro.valueOf(dto.getGenero()));

        Livro atualizado = repository.save(livro);
        return converterParaDTO(atualizado);
    }

    public void deletar(UUID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Livro não encontrado");
        }
        repository.deleteById(id);
    }

    private LivroDTO converterParaDTO(Livro livro) {
        LivroDTO dto = new LivroDTO();
        dto.setId(livro.getId());
        dto.setTitulo(livro.getTitulo());
        dto.setIsbn(livro.getIsbn());
        dto.setPreco(livro.getPreco());
        dto.setGenero(livro.getGenero().name());
        return dto;
    }
}
```

---

### 9.5 Controller (API REST)

```java
@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService service;

    public LivroController(LivroService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivroDTO criar(@RequestBody @Valid CriarLivroDTO dto) {
        return service.criar(dto);
    }

    @GetMapping("/{id}")
    public LivroDTO buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @GetMapping
    public List<LivroDTO> listarTodos() {
        return service.listarTodos();
    }

    @PutMapping("/{id}")
    public LivroDTO atualizar(@PathVariable UUID id, 
                             @RequestBody @Valid CriarLivroDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        service.deletar(id);
    }
}
```

---

## 10. Exception Handling

### 10.1 Exceptions Customizadas

```java
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

---

### 10.2 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException ex) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBusiness(BusinessException ex) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errors,
                LocalDateTime.now()
        );
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;
    }
}
```

---

## 11. Testando com Postman

### 11.1 Criar Livro (POST)

**Request:**
```http
POST http://localhost:8080/api/livros
Content-Type: application/json

{
  "titulo": "Clean Code",
  "isbn": "978-0132350884",
  "preco": 89.90,
  "genero": "CIENCIA"
}
```

**Response (201 Created):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "titulo": "Clean Code",
  "isbn": "978-0132350884",
  "preco": 89.90,
  "genero": "CIENCIA"
}
```

---

### 11.2 Buscar Todos (GET)

**Request:**
```http
GET http://localhost:8080/api/livros
```

**Response (200 OK):**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "titulo": "Clean Code",
    "isbn": "978-0132350884",
    "preco": 89.90,
    "genero": "CIENCIA"
  }
]
```

---

### 11.3 Buscar por ID (GET)

**Request:**
```http
GET http://localhost:8080/api/livros/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

---

### 11.4 Atualizar (PUT)

**Request:**
```http
PUT http://localhost:8080/api/livros/a1b2c3d4-e5f6-7890-abcd-ef1234567890
Content-Type: application/json

{
  "titulo": "Clean Code - 2ª Edição",
  "isbn": "978-0132350884",
  "preco": 99.90,
  "genero": "CIENCIA"
}
```

---

### 11.5 Deletar (DELETE)

**Request:**
```http
DELETE http://localhost:8080/api/livros/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response (204 No Content)**

---

## 12. Estrutura do Projeto

```
src/main/java/com/github/sergiocostaczr/libraryapi/
│
├── LibraryApiApplication.java          # Classe principal
│
├── config/                             # Configurações
│   ├── AppConfiguration.java
│   ├── BibliotecaProperties.java
│   └── DatabaseConfiguration.java
│
├── controller/                         # Controllers REST
│   ├── LivroController.java
│   └── AutorController.java
│
├── service/                           # Lógica de negócio
│   ├── LivroService.java
│   └── AutorService.java
│
├── repository/                        # Acesso a dados
│   ├── LivroRepository.java
│   └── AutorRepository.java
│
├── model/                             # Entidades JPA
│   ├── Livro.java
│   └── Autor.java
│
├── dto/                               # Data Transfer Objects
│   ├── LivroDTO.java
│   └── CriarLivroDTO.java
│
├── exception/                         # Exceptions
│   ├── NotFoundException.java
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
│
└── annotation/                        # Annotations customizadas
    ├── Auditavel.java
    └── ISBN.java
```

---

## 13. Boas Práticas

### 13.1 Injeção de Dependências
✅ **Prefira Constructor Injection**  
✅ Use `final` para imutabilidade  
✅ Evite Field Injection em produção

### 13.2 Configurações
✅ Use `@ConfigurationProperties` para configs complexas  
✅ Use `@Value` para valores simples  
✅ Organize configs em classes separadas

### 13.3 Beans
✅ Use stereotypes (`@Service`, `@Repository`) quando possível  
✅ Use `@Bean` para classes de terceiros  
✅ Use `@Qualifier` para resolver ambiguidade

### 13.4 DTOs
✅ Nunca exponha entidades JPA diretamente na API  
✅ Use DTOs para entrada e saída  
✅ Valide DTOs com Bean Validation

### 13.5 Exceptions
✅ Crie exceptions de negócio específicas  
✅ Use `@RestControllerAdvice` para tratamento global  
✅ Retorne mensagens claras e úteis

---

## 14. Comparação @Value vs @ConfigurationProperties

| Aspecto | @Value | @ConfigurationProperties |
|---------|--------|--------------------------|
| **Uso** | Valores simples | Configurações complexas |
| **Type-safe** | ❌ Não | ✅ Sim |
| **Validação** | ❌ Limitada | ✅ Bean Validation |
| **IDE Support** | ❌ Fraco | ✅ Autocomplete |
| **Organização** | ❌ Espalhado | ✅ Centralizado |
| **Relaxed Binding** | ❌ Não | ✅ Sim (kebab-case) |

**Quando usar cada um:**
- `@Value`: emails, URLs simples, flags booleanos
- `@ConfigurationProperties`: múltiplas propriedades relacionadas, configs hierárquicas

---

## 15. Ciclo de Vida dos Beans

```java
@Component
public class MinhaBean {

    @PostConstruct
    public void init() {
        System.out.println("Bean inicializado!");
        // Executado após injeção de dependências
    }

    @PreDestroy
    public void destroy() {
        System.out.println("Bean sendo destruído!");
        // Executado antes do container ser fechado
    }
}
```

---

## 16. Resumo de Annotations

| Annotation | Uso |
|------------|-----|
| `@SpringBootApplication` | Classe principal |
| `@Component` | Bean genérico |
| `@Service` | Camada de negócio |
| `@Repository` | Camada de dados |
| `@RestController` | Controller REST |
| `@Configuration` | Classe de config |
| `@Bean` | Método que cria bean |
| `@Autowired` | Injeção de dependência |
| `@Qualifier` | Resolver ambiguidade |
| `@Primary` | Bean padrão |
| `@Value` | Ler propriedade |
| `@ConfigurationProperties` | Configs tipadas |
| `@PostConstruct` | Após inicialização |
| `@PreDestroy` | Antes destruição |

---

## 17. Documentação Oficial

- [Spring Framework Documentation](https://spring.io/projects/spring-framework)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Dependency Injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)
- [Configuration Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties)