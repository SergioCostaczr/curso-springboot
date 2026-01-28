# Spring Data JPA - Configuração, Mapeamento e Transações

## 1. Configuração do Banco de Dados

### 1.1 Application.yml - Configuração Básica

```yaml
spring:
  application:
    name: libraryapi

  # Conexão com PostgreSQL
  datasource:
    url: jdbc:postgresql://localhost:5432/library
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  # Configurações JPA
  jpa:
    show-sql: true  # Exibe SQL no console
    hibernate:
      ddl-auto: update  # Opções: none, create, create-drop, update
    properties:
      hibernate.format_sql: true  # Formata SQL exibido
```

**Propriedades do ddl-auto:**
- `none`: Não faz nada
- `create`: Cria as tabelas, apagando dados existentes
- `create-drop`: Cria no início e apaga no final
- `update`: Atualiza o schema sem perder dados

---

### 1.2 DataSource Configuration - HikariCP

```java
@Configuration
public class DataBaseConfiguration {

    @Value("${spring.datasource.url}")
    String url;
    
    @Value("${spring.datasource.username}")
    String username;
    
    @Value("${spring.datasource.password}")
    String password;
    
    @Value("${spring.datasource.driver-class-name}")
    String driver;

    @Bean
    public DataSource hikariDataSource(){
        HikariConfig config = new HikariConfig();
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);

        // Configurações do Pool de Conexões
        config.setMaximumPoolSize(10);      // Máximo de conexões
        config.setMinimumIdle(1);           // Tamanho inicial do pool
        config.setPoolName("library-db-pool");
        config.setMaxLifetime(600000);      // 10 minutos em ms
        config.setConnectionTimeout(100000); // 100 segundos
        config.setConnectionTestQuery("select 1");

        return new HikariDataSource(config);
    }
}
```

**HikariCP** é o pool de conexões padrão do Spring Boot, oferecendo:
- Gerenciamento eficiente de conexões
- Alto desempenho
- Configuração flexível

---

## 2. Mapeamento de Entidades JPA

### 2.1 Entidade Autor

```java
@Entity
@Table(name = "autor", schema = "public")
@Getter
@Setter
@ToString(exclude = "livros")
public class Autor {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", length = 100, nullable = false)
    private String name;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "nacionalidade", length = 50, nullable = false)
    private String nacionalidade;

    @OneToMany(mappedBy = "autor", fetch = FetchType.LAZY)
    private List<Livro> livros;
}
```

**Anotações principais:**
- `@Entity`: Marca a classe como entidade JPA
- `@Table`: Define nome da tabela e schema
- `@Id`: Define a chave primária
- `@GeneratedValue(strategy = GenerationType.UUID)`: Gera UUID automaticamente
- `@Column`: Configura a coluna (nome, tamanho, nullable)
- `@OneToMany`: Relacionamento um-para-muitos
- `@ToString(exclude = "livros")`: Exclui livros do toString (evita loop infinito)

---

### 2.2 Enum GeneroLivro

```java
public enum GeneroLivro {
    FICCAO,
    FANTASIA,
    MISTERIO,
    ROMANCE,
    BIOGRAFIA,
    CIENCIA
}
```

---

### 2.3 Entidade Livro

```java
@Entity
@Table(name = "livro")
@Data  // @Getter @Setter @ToString @EqualsAndHashCode @RequiredArgsConstructor
@ToString(exclude = "autor")
public class Livro {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "isbn", length = 20, nullable = false)
    private String isbn;

    @Column(name = "titulo", length = 150, nullable = false)
    private String titulo;

    @Column(name = "data_publicacao")
    private LocalDate dataPublicacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", length = 30, nullable = false)
    private GeneroLivro genero;

    @Column(name = "preco", precision = 18, scale = 2)
    private BigDecimal preco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor")
    private Autor autor;
}
```

**Pontos importantes:**
- `@Enumerated(EnumType.STRING)`: Salva o enum como string no banco
- `@Column(precision = 18, scale = 2)`: Para valores decimais (dinheiro)
- `@ManyToOne`: Relacionamento muitos-para-um
- `@JoinColumn(name = "id_autor")`: Define a FK na tabela

---

## 3. Relacionamentos JPA

### 3.1 @OneToMany vs @ManyToOne

**No Autor (lado "One"):**
```java
@OneToMany(mappedBy = "autor", fetch = FetchType.LAZY)
private List<Livro> livros;
```
- `mappedBy = "autor"`: Indica que o relacionamento é gerenciado pela entidade Livro
- Não possui coluna FK na tabela Autor

**No Livro (lado "Many"):**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "id_autor")
private Autor autor;
```
- `@JoinColumn`: Cria a coluna FK `id_autor` na tabela Livro

---

### 3.2 FetchType

- **LAZY**: Carrega os dados apenas quando acessados
- **EAGER**: Carrega os dados imediatamente junto com a entidade

```java
// LAZY - Recomendado para performance
@ManyToOne(fetch = FetchType.LAZY)
private Autor autor;

// Ao buscar livro, autor não vem automaticamente
// Precisa estar em @Transactional para acessar
```

---

## 4. Repositories - Spring Data JPA

### 4.1 AutorRepository

```java
@Repository
public interface AutorRepository extends JpaRepository<Autor, UUID> {
}
```

**JpaRepository** fornece métodos prontos:
- `save(entity)`
- `findById(id)`
- `findAll()`
- `deleteById(id)`
- `count()`
- `existsById(id)`

---

### 4.2 LivroRepository - Query Methods

```java
public interface LivroRepository extends JpaRepository<Livro, UUID> {

    // Query Methods - Spring gera SQL automaticamente
    List<Livro> findByAutor(Autor autor);
    
    List<Livro> findByTitulo(String titulo);
    
    List<Livro> findByIsbn(String isbn);
    
    // AND
    List<Livro> findByTituloAndPreco(String titulo, BigDecimal preco);
    
    // OR
    List<Livro> findByTituloOrIsbn(String titulo, String isbn);
    
    // BETWEEN
    List<Livro> findByDataPublicacaoBetween(LocalDate inicio, LocalDate fim);
}
```

**Query Methods**: Spring Data interpreta o nome do método e cria a query automaticamente.

---

### 4.3 JPQL - @Query

```java
// Ordenação simples
@Query("select l from Livro as l order by l.titulo, l.preco")
List<Livro> listaTodosOrdenadoPorTituloEPreco();

// JOIN
@Query("select a from Livro l join l.autor a")
List<Autor> listarAutoresDosLivros();

// DISTINCT
@Query("select distinct l.titulo from Livro l")
List<String> listarNomesDiferentesLivros();

// Query complexa com JOIN e WHERE
@Query("""
    select l.genero
    from Livro l
    join l.autor a
    where a.nacionalidade = 'Brasileira'
    order by l.genero
""")
List<String> listarGenerosAutoresBrasileiros();
```

**JPQL vs SQL:**
- JPQL usa nomes de **entidades** e **propriedades**
- SQL usa nomes de **tabelas** e **colunas**

---

### 4.4 Parâmetros em @Query

**Named Parameters (Recomendado):**
```java
@Query("select l from Livro l where l.genero = :genero order by :paramOrdenacao")
List<Livro> findByGenero(
    @Param("genero") GeneroLivro generoLivro,
    @Param("paramOrdenacao") String nomeDaPropriedade
);
```

**Positional Parameters:**
```java
@Query("select l from Livro l where l.genero = ?1 order by ?2")
List<Livro> findByGeneroPositionalParameters(
    GeneroLivro generoLivro,
    String nomeDaPropriedade
);
```

---

### 4.5 DELETE e UPDATE com @Query

```java
@Transactional
@Modifying
@Query("delete from Livro where genero = ?1")
void deleteByGenero(GeneroLivro generoLivro);

@Transactional
@Modifying
@Query("update Livro set dataPublicacao = ?1")
void updateDataPublicacao(LocalDate novaData);
```

**Anotações obrigatórias:**
- `@Transactional`: Abre transação
- `@Modifying`: Indica modificação no banco (INSERT, UPDATE, DELETE)

---

## 5. Transações (@Transactional)

### 5.1 Conceitos Básicos

**Transação**: Conjunto de operações tratadas como uma unidade única.

**Comportamento:**
- **Commit**: Confirma as alterações no banco
- **Rollback**: Desfaz todas as alterações se houver erro

```java
@Transactional
public void executar() {
    // Salvar autor
    autorRepository.save(autor);
    
    // Salvar livro
    livroRepository.save(livro);
    
    // Se houver exception, faz ROLLBACK de tudo
    if (autor.getName().equals("test France")) {
        throw new RuntimeException("Rollback");
    }
    // Se chegou aqui, faz COMMIT
}
```

---

### 5.2 Estado Managed (Gerenciado)

```java
@Transactional
public void atualizacaoSemAtualizar() {
    Livro livro = livroRepository.findById(id).orElse(null);
    
    // Alteração na entidade
    livro.setDataPublicacao(LocalDate.of(2020, 4, 1));
    
    // NÃO precisa chamar save()!
    // A entidade está no estado MANAGED
    // No commit, as alterações são enviadas automaticamente
}
```

**Estados de uma Entidade:**
- **Transient**: Nova entidade, não gerenciada
- **Managed**: Gerenciada pelo EntityManager (dentro de @Transactional)
- **Detached**: Foi gerenciada, mas saiu do contexto
- **Removed**: Marcada para remoção

---

### 5.3 Quando usar @Transactional

**Operações de ESCRITA sempre precisam:**
```java
@Transactional
public void salvarDados() {
    // INSERT, UPDATE, DELETE
}
```

**Operações de LEITURA com LAZY:**
```java
@Transactional
public void buscarLivro() {
    Livro livro = repository.findById(id).orElse(null);
    // Acessar autor que está LAZY
    System.out.println(livro.getAutor().getName()); // OK
}
```

Sem `@Transactional`, ao acessar `livro.getAutor()` com FetchType.LAZY, ocorre **LazyInitializationException**.

---

## 6. Operações CRUD - Exemplos Práticos

### 6.1 Salvar (Create)

```java
@Test
public void salvarAutor() {
    Autor autor = new Autor();
    autor.setName("Mario");
    autor.setNacionalidade("Brasileiro");
    autor.setDataNascimento(LocalDate.of(1950, 1, 21));
    
    Autor salvo = repository.save(autor);
}
```

---

### 6.2 Atualizar (Update)

```java
@Test
public void atualizar() {
    UUID id = UUID.fromString("373fdf03-e95f-47a0-9c47-c961cf2702ef");
    Optional<Autor> possivelAutor = repository.findById(id);
    
    if (possivelAutor.isPresent()) {
        Autor autor = possivelAutor.get();
        autor.setDataNascimento(LocalDate.of(1990, 10, 22));
        repository.save(autor);
    }
}
```

---

### 6.3 Listar (Read)

```java
@Test
public void listar() {
    List<Autor> autores = repository.findAll();
    autores.forEach(System.out::println);
}
```

---

### 6.4 Deletar (Delete)

```java
@Test
public void deletar() {
    UUID id = UUID.fromString("72b4631d-321d-4062-8799-a23feaf3b7cb");
    repository.deleteById(id);
}
```

---

### 6.5 Salvar com Relacionamento

```java
@Test
@Transactional
public void salvarAutorComLivros() {
    // Criar autor
    Autor autor = new Autor();
    autor.setName("Pedro");
    autor.setNacionalidade("Brasileiro");
    autor.setDataNascimento(LocalDate.of(1900, 12, 10));
    
    // Criar livros
    Livro livro1 = new Livro();
    livro1.setTitulo("Árvores");
    livro1.setAutor(autor);
    
    Livro livro2 = new Livro();
    livro2.setTitulo("Ciência");
    livro2.setAutor(autor);
    
    // Associar livros ao autor
    autor.setLivros(new ArrayList<>());
    autor.getLivros().add(livro1);
    autor.getLivros().add(livro2);
    
    // Salvar
    repository.save(autor);
    livroRepository.saveAll(autor.getLivros());
}
```

---

## 7. Boas Práticas

### 7.1 FetchType
- Use **LAZY** por padrão para melhor performance
- Use **EAGER** apenas quando sempre precisar dos dados relacionados

### 7.2 ToString
- Sempre exclua relacionamentos bidirecionais para evitar loops infinitos:
```java
@ToString(exclude = "livros")
```

### 7.3 Transações
- Use `@Transactional` em métodos de serviço, não em repositories
- Mantenha transações curtas e focadas

### 7.4 Query Methods vs @Query
- **Query Methods**: Para consultas simples
- **@Query**: Para consultas complexas com joins, subqueries, etc.

### 7.5 Named Parameters
- Prefira named parameters (`@Param`) ao invés de posicionais
- Código mais legível e menos propenso a erros

---

## 8. Resumo de Anotações

| Anotação | Uso |
|----------|-----|
| `@Entity` | Marca classe como entidade JPA |
| `@Table` | Define nome da tabela |
| `@Id` | Define chave primária |
| `@GeneratedValue` | Estratégia de geração de ID |
| `@Column` | Configura coluna (nome, tamanho, nullable) |
| `@OneToMany` | Relacionamento 1:N |
| `@ManyToOne` | Relacionamento N:1 |
| `@JoinColumn` | Define coluna FK |
| `@Enumerated` | Mapeia enum |
| `@Transactional` | Controle de transação |
| `@Query` | JPQL/SQL customizado |
| `@Modifying` | Queries de modificação |
| `@Param` | Named parameter em @Query |

---

## 9. Documentação Oficial

- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html)
- [Spring Boot Application Properties](https://docs.spring.io/spring-boot/appendix/application-properties/index.html)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)