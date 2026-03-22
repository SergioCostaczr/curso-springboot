# Spring MVC — Anotações do Módulo

## O que é Spring MVC?

Spring MVC é o módulo do Spring responsável por receber requisições HTTP, processá-las e retornar respostas. O "MVC" vem do padrão **Model-View-Controller**:

| Camada | Responsabilidade | No Spring |
|---|---|---|
| **Model** | Dados e regras de negócio | Entidades, Services, Repositories |
| **View** | Apresentação (HTML, JSON) | Thymeleaf, JSON via Jackson |
| **Controller** | Recebe requisição e coordena | `@Controller`, `@RestController` |

Em APIs REST, a "View" é basicamente o JSON que o Jackson serializa automaticamente.

---

## DispatcherServlet — O Coração do Spring MVC

Toda requisição HTTP passa pelo `DispatcherServlet` antes de chegar ao controller. Ele é configurado automaticamente pelo Spring Boot.

```
Cliente HTTP
    ↓
DispatcherServlet  ←── ponto de entrada único
    ↓
HandlerMapping (descobre qual controller atender)
    ↓
Controller (executa a lógica)
    ↓
HandlerAdapter (serializa a resposta)
    ↓
Resposta HTTP
```

---

## @Controller vs @RestController

```java
// @Controller — usado quando há view (HTML, Thymeleaf)
// O retorno é o nome da template
@Controller
public class PaginaController {
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("titulo", "Biblioteca");
        return "home"; // resolve para templates/home.html
    }
}

// @RestController — usado em APIs REST
// Equivale a @Controller + @ResponseBody em todos os métodos
// O retorno é serializado direto para JSON
@RestController
public class AutorController {
    @GetMapping("/autores")
    public List<AutorDTO> listar() {
        return service.listar(); // vira JSON automaticamente
    }
}
```

---

## Thymeleaf — Templates HTML com Spring MVC

Thymeleaf é a engine de templates padrão do Spring Boot para gerar páginas HTML no servidor. Os atributos `th:*` são processados pelo servidor antes de o HTML chegar ao navegador — o resultado final é HTML puro.

### Dependência

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

Os templates ficam em `src/main/resources/templates/` e são resolvidos automaticamente pelo `ThymeleafViewResolver`.

---

### Como o Controller passa dados para o template

O objeto `Model` é o "pacote" que carrega dados do controller para a view. Tudo que for adicionado com `model.addAttribute()` fica acessível no HTML via `${nomeDaVariavel}`.

```java
@Controller
public class AutorController {

    @GetMapping("/autores")
    public String listar(Model model) {
        List<Autor> autores = service.listar();
        model.addAttribute("autores", autores);      // lista disponível no template
        model.addAttribute("titulo", "Autores");     // string simples
        return "autores/listar";                     // → templates/autores/listar.html
    }

    @GetMapping("/autores/{id}")
    public String detalhe(@PathVariable UUID id, Model model) {
        Autor autor = service.buscar(id);
        model.addAttribute("autor", autor);
        return "autores/detalhe";                    // → templates/autores/detalhe.html
    }
}
```

> O retorno do método é sempre o **caminho relativo** dentro de `templates/`, sem a extensão `.html`.

---

### Sintaxe essencial do Thymeleaf

#### Expressões

| Expressão | Uso | Exemplo |
|---|---|---|
| `${...}` | Variável do Model | `${autor.nome}` |
| `@{...}` | URL / link | `@{/autores/{id}(id=${autor.id})}` |
| `#{...}` | Mensagem i18n | `#{label.nome}` |
| `*{...}` | Campo de objeto selecionado (`th:object`) | `*{nome}` |

#### Exibindo dados — `th:text` e `th:utext`

```html
<!-- th:text escapa HTML (seguro) -->
<h1 th:text="${titulo}">Título padrão</h1>

<!-- th:utext renderiza HTML sem escapar (cuidado com XSS) -->
<p th:utext="${descricaoHtml}">Descrição</p>

<!-- Concatenação -->
<p th:text="'Bem-vindo, ' + ${usuario.nome} + '!'"></p>

<!-- Operador ternário -->
<span th:text="${autor.ativo} ? 'Ativo' : 'Inativo'"></span>
```

#### Iteração — `th:each`

```html
<table>
  <tr>
    <th>Nome</th>
    <th>Nacionalidade</th>
  </tr>
  <!-- th:each cria um <tr> para cada autor na lista -->
  <tr th:each="autor : ${autores}">
    <td th:text="${autor.nome}"></td>
    <td th:text="${autor.nacionalidade}"></td>
  </tr>
</table>

<!-- Acessando o estado da iteração -->
<tr th:each="autor, stat : ${autores}">
  <td th:text="${stat.index}"></td>    <!-- índice (começa em 0) -->
  <td th:text="${stat.count}"></td>    <!-- posição (começa em 1) -->
  <td th:text="${stat.even}"></td>     <!-- true se posição par -->
  <td th:text="${autor.nome}"></td>
</tr>
```

#### Condicionais — `th:if` e `th:unless`

```html
<!-- Renderiza o elemento só se a condição for verdadeira -->
<p th:if="${autores.empty}">Nenhum autor cadastrado.</p>

<!-- Inverso de th:if -->
<table th:unless="${autores.empty}">
  ...
</table>

<!-- th:switch / th:case -->
<span th:switch="${usuario.role}">
  <span th:case="'ADMIN'">Administrador</span>
  <span th:case="'OPERADOR'">Operador</span>
  <span th:case="*">Visitante</span>  <!-- default -->
</span>
```

#### Links e URLs — `@{...}`

```html
<!-- Link simples -->
<a th:href="@{/autores}">Ver todos</a>

<!-- Link com PathVariable -->
<a th:href="@{/autores/{id}(id=${autor.id})}">Detalhes</a>

<!-- Link com QueryParam -->
<a th:href="@{/autores(nome=${filtro.nome})}">Buscar</a>

<!-- Combinando PathVariable + QueryParam -->
<a th:href="@{/autores/{id}/livros(id=${autor.id}, pagina=1)}">Livros</a>

<!-- URL de recurso estático (CSS, JS, imagens) -->
<link th:href="@{/css/estilo.css}" rel="stylesheet"/>
<img th:src="@{/images/logo.png}" alt="Logo"/>
```

---

### Formulários

A comunicação de volta do HTML para o controller acontece via formulário. O Thymeleaf integra com o Spring usando `th:object` e `th:field`.

```java
// Controller — envia objeto vazio para o formulário de criação
@GetMapping("/autores/novo")
public String formulario(Model model) {
    model.addAttribute("autor", new AutorForm()); // objeto que o form vai preencher
    return "autores/formulario";
}

// Controller — recebe os dados do formulário
@PostMapping("/autores")
public String salvar(@ModelAttribute @Valid AutorForm form,
                     BindingResult result,
                     RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
        return "autores/formulario"; // volta ao form com os erros
    }
    service.salvar(form);
    redirectAttributes.addFlashAttribute("mensagem", "Autor salvo com sucesso!");
    return "redirect:/autores"; // POST → redirect → GET (padrão PRG)
}
```

```html
<!-- templates/autores/formulario.html -->
<form th:action="@{/autores}" th:object="${autor}" method="post">

  <label>Nome</label>
  <!-- th:field gera id, name e value automaticamente -->
  <input type="text" th:field="*{nome}"/>
  <!-- Exibe o erro de validação do campo nome -->
  <span th:if="${#fields.hasErrors('nome')}"
        th:errors="*{nome}"
        class="erro">
  </span>

  <label>Data de Nascimento</label>
  <input type="date" th:field="*{dataNascimento}"/>
  <span th:if="${#fields.hasErrors('dataNascimento')}"
        th:errors="*{dataNascimento}"
        class="erro">
  </span>

  <button type="submit">Salvar</button>
</form>
```

**O que `th:field="*{nome}"` gera automaticamente:**
```html
<!-- antes do Thymeleaf processar -->
<input type="text" th:field="*{nome}"/>

<!-- depois (HTML enviado ao navegador) -->
<input type="text" id="nome" name="nome" value="Machado de Assis"/>
```

#### Padrão PRG (Post-Redirect-Get)

Após um POST bem-sucedido, **sempre redirecione** com `redirect:` para evitar que o usuário re-submeta o formulário ao atualizar a página:

```java
return "redirect:/autores";        // redireciona para GET /autores
return "redirect:/autores/" + id;  // redireciona para GET /autores/{id}
```

#### Flash Attributes — mensagens após redirect

Como o redirect é uma nova requisição, dados comuns do `Model` se perdem. `RedirectAttributes` resolve isso:

```java
// No POST — adiciona mensagem antes do redirect
redirectAttributes.addFlashAttribute("mensagem", "Autor salvo!");
return "redirect:/autores";

// No GET — a mensagem já está no Model automaticamente
@GetMapping("/autores")
public String listar(Model model) {
    // "mensagem" estará disponível no template se vier de um redirect
    return "autores/listar";
}
```

```html
<!-- No template — exibe a mensagem se existir -->
<div th:if="${mensagem}" th:text="${mensagem}" class="alert"></div>
```

---

### Layouts com Fragments

Fragments evitam repetição de HTML (navbar, footer, head) entre templates.

```html
<!-- templates/layout/base.html — fragmentos reutilizáveis -->

<!-- Definindo um fragment -->
<nav th:fragment="navbar">
  <a th:href="@{/}">Home</a>
  <a th:href="@{/autores}">Autores</a>
  <a th:href="@{/livros}">Livros</a>
</nav>

<footer th:fragment="rodape">
  <p>© 2025 Library API</p>
</footer>
```

```html
<!-- templates/autores/listar.html — usando os fragments -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <title>Autores</title>
</head>
<body>

  <!-- th:replace substitui o elemento inteiro pelo fragment -->
  <nav th:replace="~{layout/base :: navbar}"></nav>

  <main>
    <h1>Lista de Autores</h1>
    <!-- conteúdo da página -->
  </main>

  <!-- th:insert mantém o elemento pai e insere o fragment dentro -->
  <div th:insert="~{layout/base :: rodape}"></div>

</body>
</html>
```

| Atributo | Comportamento |
|---|---|
| `th:replace` | Substitui o elemento inteiro pelo fragment |
| `th:insert` | Insere o fragment dentro do elemento, mantendo a tag pai |

---

### Integração com Spring Security no Thymeleaf

Com a dependência `thymeleaf-extras-springsecurity6`, é possível usar o contexto de segurança direto no HTML:

```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

```html
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<!-- Exibe apenas se o usuário estiver autenticado -->
<div sec:authorize="isAuthenticated()">
  Bem-vindo, <span sec:authentication="name"></span>!
</div>

<!-- Exibe apenas para roles específicas -->
<a sec:authorize="hasRole('ADMIN')" th:href="@{/admin}">Painel Admin</a>

<!-- Exibe apenas para não autenticados -->
<a sec:authorize="isAnonymous()" th:href="@{/login}">Entrar</a>

</html>
```

---

### Fluxo Completo com Thymeleaf

```
GET /autores
    ↓
DispatcherServlet
    ↓
AutorController.listar(Model model)
    ↓
model.addAttribute("autores", lista)
return "autores/listar"
    ↓
ThymeleafViewResolver resolve → templates/autores/listar.html
    ↓
Thymeleaf processa th:each, th:text, th:href...
    ↓
HTML final gerado no servidor
    ↓
200 OK — HTML enviado ao navegador
```

---

## Mapeamento de Rotas

### Anotações de método HTTP

```java
@GetMapping("/autores")          // GET    — listar / buscar
@PostMapping("/autores")         // POST   — criar
@PutMapping("/autores/{id}")     // PUT    — atualizar completo
@DeleteMapping("/autores/{id}")  // DELETE — remover
@PatchMapping("/autores/{id}")   // PATCH  — atualizar parcial
```

### @RequestMapping — mapeamento na classe

Evita repetição de prefixo em todos os métodos:

```java
@RestController
@RequestMapping("/autores")  // prefixo comum a todos os métodos
public class AutorController {

    @GetMapping            // GET /autores
    public List<AutorDTO> listar() { ... }

    @GetMapping("/{id}")   // GET /autores/{id}
    public AutorDTO buscar(@PathVariable UUID id) { ... }

    @PostMapping           // POST /autores
    public ResponseEntity<AutorDTO> salvar(@RequestBody AutorDTO dto) { ... }
}
```

---

## Recebendo Dados da Requisição

### @PathVariable — variável na URL

```java
// GET /autores/123e4567-e89b-12d3-a456-426614174000
@GetMapping("/{id}")
public AutorDTO buscar(@PathVariable UUID id) { ... }

// Múltiplas variáveis
@GetMapping("/{autorId}/livros/{livroId}")
public LivroDTO buscarLivro(@PathVariable UUID autorId,
                             @PathVariable UUID livroId) { ... }
```

### @RequestParam — parâmetro de query string

```java
// GET /autores?nome=Machado&nacionalidade=Brasileiro
@GetMapping
public List<AutorDTO> listar(
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) String nacionalidade) { ... }
```

### @RequestBody — corpo da requisição (JSON → objeto)

```java
// POST /autores
// Body: { "nome": "Machado", "dataNascimento": "1839-06-21" }
@PostMapping
public ResponseEntity<AutorDTO> salvar(@RequestBody @Valid AutorDTO dto) { ... }
```

> O Jackson deserializa o JSON para o objeto automaticamente.

### @RequestHeader — cabeçalhos HTTP

```java
@GetMapping("/info")
public String info(@RequestHeader("Authorization") String token) { ... }
```

---

## ResponseEntity — Controlando a Resposta HTTP

`ResponseEntity<T>` permite controlar o **status HTTP**, os **headers** e o **body** da resposta.

```java
// Retorno simples (status 200 implícito)
@GetMapping("/{id}")
public AutorDTO buscar(@PathVariable UUID id) {
    return service.buscar(id);
}

// Com ResponseEntity — controle total
@GetMapping("/{id}")
public ResponseEntity<AutorDTO> buscar(@PathVariable UUID id) {
    AutorDTO autor = service.buscar(id);

    if (autor == null) {
        return ResponseEntity.notFound().build();        // 404
    }
    return ResponseEntity.ok(autor);                    // 200 + body
}

// POST — retorna 201 Created com Location header
@PostMapping
public ResponseEntity<Void> salvar(@RequestBody @Valid AutorDTO dto) {
    Autor autor = service.salvar(mapper.toEntity(dto));

    URI location = UriComponentsBuilder
            .fromPath("/autores/{id}")
            .buildAndExpand(autor.getId())
            .toUri();

    return ResponseEntity.created(location).build();    // 201 + Location: /autores/{id}
}

// DELETE — sem body
@DeleteMapping("/{id}")
public ResponseEntity<Void> deletar(@PathVariable UUID id) {
    service.deletar(id);
    return ResponseEntity.noContent().build();           // 204
}
```

### Status HTTP mais comuns

| Método | Status | Quando usar |
|---|---|---|
| `ResponseEntity.ok()` | 200 | GET com resultado |
| `ResponseEntity.created(uri)` | 201 | POST bem-sucedido |
| `ResponseEntity.noContent()` | 204 | DELETE / PUT sem body |
| `ResponseEntity.badRequest()` | 400 | Dados inválidos |
| `ResponseEntity.notFound()` | 404 | Recurso não encontrado |

---

## Validação com Bean Validation

### Anotando o DTO

```java
public record AutorDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data deve ser no passado")
        LocalDate dataNascimento,

        @Size(max = 50, message = "Nacionalidade deve ter no máximo 50 caracteres")
        String nacionalidade
) {}
```

### Ativando no controller com @Valid

```java
@PostMapping
public ResponseEntity<Void> salvar(@RequestBody @Valid AutorDTO dto) {
    // Se algum campo violar a constraint, o Spring lança MethodArgumentNotValidException
    // antes de entrar aqui
}
```

### Anotações de validação mais comuns

| Anotação | Valida |
|---|---|
| `@NotNull` | Campo não pode ser null |
| `@NotBlank` | String não pode ser null, vazia ou só espaços |
| `@NotEmpty` | Coleção/String não pode ser null ou vazia |
| `@Size(min, max)` | Tamanho de String ou coleção |
| `@Min` / `@Max` | Valor numérico mínimo/máximo |
| `@Past` / `@Future` | Data no passado/futuro |
| `@Email` | Formato de e-mail válido |
| `@Pattern(regexp)` | Expressão regular |

---

## Tratamento de Erros

### @ExceptionHandler — trata exceções em um controller específico

```java
@RestController
public class AutorController {

    @ExceptionHandler(AutorNotFoundException.class)
    public ResponseEntity<String> handleNotFound(AutorNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
```

### @ControllerAdvice — tratamento global de exceções

Centraliza o tratamento de erros para **todos os controllers** da aplicação:

```java
@RestControllerAdvice  // @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    // Erros de validação (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> erros.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(erros);
    }

    // Regras de negócio
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<String> handleRegraDeNegocio(RegraNegocioException ex) {
        return ResponseEntity.status(422).body(ex.getMessage());
    }

    // Fallback — qualquer exceção não tratada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenerico(Exception ex) {
        return ResponseEntity.internalServerError().body("Erro interno do servidor");
    }
}
```

---

## Data Transfer Object (DTO)

DTOs são objetos usados para trafegar dados entre o cliente e a aplicação, **sem expor a entidade diretamente**.

```java
// Entidade — mapeada para o banco, nunca exposta diretamente
@Entity
public class Autor {
    private UUID id;
    private String nome;
    private String senha; // campo sensível — não deve ir na resposta!
}

// DTO de requisição — o que o cliente envia
public record AutorDTO(
        @NotBlank String nome,
        @NotNull LocalDate dataNascimento,
        String nacionalidade
) {}

// DTO de resposta — o que a API retorna
public record AutorResponseDTO(UUID id, String nome, String nacionalidade) {}
```

**Por que usar DTOs?**
- Evita expor campos sensíveis (senha, auditoria interna)
- Permite que a API evolua sem afetar o banco e vice-versa
- Permite validações específicas da entrada

---

## Content Negotiation

O Spring MVC pode retornar diferentes formatos com base no header `Accept` da requisição:

```
GET /autores
Accept: application/json   → retorna JSON  (padrão)
Accept: application/xml    → retorna XML   (se configurado)
```

Na prática, APIs REST quase sempre trabalham só com JSON, então não é necessário configurar nada além do padrão.

---

## Fluxo Completo de uma Requisição REST

```
POST /autores
Content-Type: application/json
Body: { "nome": "Machado de Assis", "dataNascimento": "1839-06-21" }

    ↓
DispatcherServlet recebe a requisição

    ↓
HandlerMapping → encontra AutorController.salvar()

    ↓
Jackson deserializa o JSON → AutorDTO

    ↓
Bean Validation (@Valid) → valida os campos
  ✗ inválido → MethodArgumentNotValidException → GlobalExceptionHandler → 400
  ✓ válido   → continua

    ↓
SecurityFilterChain → verifica autenticação/autorização
  ✗ não autorizado → 401 / 403
  ✓ autorizado     → continua

    ↓
AutorController.salvar(dto) é executado

    ↓
AutorService.salvar(autor) → regras de negócio + Repository

    ↓
ResponseEntity.created(location).build()

    ↓
Jackson serializa a resposta → JSON

    ↓
201 Created
Location: /autores/123e4567-...
```
