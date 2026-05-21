# API REST de Login

Este documento explica a primeira base da API REST de login do HouseHost.

A estrutura segue o padrao usado nos projetos anteriores: `Controller -> Service -> Repository -> Model`, com DTOs para entrada e saida.

## 1. pom.xml

Arquivo:

```text
pom.xml
```

O `pom.xml` transforma o HouseHost em um projeto Maven com Spring Boot.

Dependencias principais:

```text
spring-boot-starter-web
```

Usado para criar endpoints REST, como:

```http
POST /auth/login
```

```text
spring-boot-starter-data-jpa
```

Usado para trabalhar com banco de dados usando entidades `@Entity` e repositories.

```text
spring-security-crypto
```

Usado para acessar o `BCryptPasswordEncoder`, que compara senha informada com hash salvo no banco.

Importante: neste momento nao foi adicionado o Spring Security completo. Foi adicionada apenas a parte de criptografia de senha.

```text
mysql-connector-j
```

Usado para conectar com MySQL.

## 2. Classe Principal da Aplicacao

Arquivo:

```text
src/main/java/com/househost/HouseHostApplication.java
```

Codigo:

```java
@SpringBootApplication
public class HouseHostApplication {

    public static void main(String[] args) {
        SpringApplication.run(HouseHostApplication.class, args);
    }
}
```

Essa classe inicia o backend Spring Boot.

Sem ela, o projeto nao sobe como aplicacao Spring.

## 3. application.properties

Arquivo:

```text
src/main/resources/application.properties
```

Conteudo:

```properties
spring.application.name=househost

spring.datasource.url=${HOUSEHOST_DB_URL:jdbc:mysql://localhost:3306/househost?serverTimezone=UTC}
spring.datasource.username=${HOUSEHOST_DB_USERNAME:root}
spring.datasource.password=${HOUSEHOST_DB_PASSWORD:}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Configuracoes criadas:

- nome da aplicacao;
- URL do banco;
- usuario do banco;
- senha do banco;
- JPA com `ddl-auto=update`.

Foi usado o formato com variaveis de ambiente e fallback:

```properties
${HOUSEHOST_DB_USERNAME:root}
```

Isso significa:

- se existir `HOUSEHOST_DB_USERNAME`, usa ela;
- se nao existir, usa `root`.

Essa decisao evita deixar senha fixa no codigo.

## 4. DTO Padrao de Resposta

Arquivo:

```text
src/main/java/com/househost/shared/dto/ResponseDTO.java
```

Codigo:

```java
public class ResponseDTO {
    private String status;
    private String message;
    private Object data;
}
```

Esse DTO padroniza as respostas da API.

Exemplo de sucesso:

```json
{
  "status": "success",
  "message": "Login realizado com sucesso",
  "data": {
    "username": "admin",
    "role": "ADMIN"
  }
}
```

Esse formato segue o estilo usado no Lumina.

## 5. Tratamento de Erro de Login

Arquivos:

```text
src/main/java/com/househost/shared/exception/InvalidLoginException.java
src/main/java/com/househost/shared/exception/GlobalExceptionHandler.java
```

Exception:

```java
public class InvalidLoginException extends RuntimeException {
    public InvalidLoginException() {
        super("Usuario ou senha invalidos");
    }
}
```

Ela representa erro de login.

Handler:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ResponseDTO> handleInvalidLogin(InvalidLoginException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
```

Esse handler faz com que erro de login retorne HTTP `401 Unauthorized`.

Resposta:

```json
{
  "status": "error",
  "message": "Usuario ou senha invalidos",
  "data": null
}
```

A mensagem e generica de proposito. A API nao informa se o usuario nao existe ou se a senha esta errada, porque isso revelaria informacao desnecessaria.

## 6. Configuracao do Encoder de Senha

Arquivo:

```text
src/main/java/com/househost/auth/config/PasswordConfig.java
```

Codigo:

```java
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

Essa classe cria um `PasswordEncoder` disponivel para injecao no Spring.

Ele e usado no login:

```java
passwordEncoder.matches(request.password, user.getPasswordHash())
```

Ou seja: a senha enviada no login e comparada contra o hash salvo no banco.

## 7. Controller de Autenticacao

Arquivo:

```text
src/main/java/com/househost/auth/controller/AuthController.java
```

Codigo:

```java
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {
```

Esse controller atende rotas que comecam com:

```http
/auth
```

Endpoint criado:

```java
@PostMapping("/login")
public ResponseDTO login(@RequestBody LoginRequestDTO request) {
    return authService.login(request);
}
```

Rota final:

```http
POST /auth/login
```

O controller nao faz regra de negocio. Ele apenas recebe o request e chama o service.

## 8. DTO de Entrada do Login

Arquivo:

```text
src/main/java/com/househost/auth/dto/LoginRequestDTO.java
```

Codigo:

```java
public class LoginRequestDTO {
    public String email;
    public String password;
}
```

Corpo esperado pela API:

```json
{
  "email": "admin@cantinho.com",
  "password": "senha"
}
```

Os campos estao publicos para manter o estilo simples usado nos projetos anteriores.

## 9. DTO de Resposta do Login

Arquivo:

```text
src/main/java/com/househost/auth/dto/LoginResponseDTO.java
```

Codigo:

```java
public class LoginResponseDTO {
    private String username;
    private String role;
}
```

Ele retorna apenas o que o frontend precisa saber no momento:

```json
{
  "username": "admin",
  "role": "ADMIN"
}
```

Ele nao retorna:

- `id`;
- senha;
- hash;
- dados internos.

Isso evita exposicao desnecessaria.

## 10. Entidade User

Arquivo:

```text
src/main/java/com/househost/auth/model/User.java
```

Codigo principal:

```java
@Entity
@Table(name = "users")
public class User {
```

Essa classe representa a tabela `users`.

Campos:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

ID automatico.

```java
@Column(nullable = false, unique = true)
private String username;
```

Nome de usuario obrigatorio e unico.

```java
@Column(nullable = false)
private String passwordHash;
```

Aqui fica o hash da senha, nao a senha pura.

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private UserRole role;
```

Perfil do usuario salvo como texto no banco.

Exemplos:

```text
ADMIN
MANAGER
RECEPTION
```

## 11. Enum de Perfil

Arquivo:

```text
src/main/java/com/househost/auth/model/UserRole.java
```

Codigo:

```java
public enum UserRole {
    ADMIN,
    MANAGER,
    RECEPTION
}
```

Esses perfis servem para diferenciar permissoes depois.

Por enquanto eles apenas aparecem na resposta do login.

## 12. Repository

Arquivo:

```text
src/main/java/com/househost/auth/repository/UserRepository.java
```

Codigo:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

Esse repository permite buscar usuario no banco.

O Spring Data JPA entende automaticamente o metodo:

```java
findByEmail
```

E monta a query com base no nome do campo `email`.

Foi usado `Optional<User>` em vez de retornar `User` direto porque deixa explicito que o usuario pode nao existir.

## 13. Service de Autenticacao

Arquivo:

```text
src/main/java/com/househost/auth/service/AuthService.java
```

Esse service contem a regra de login.

Dependencias:

```java
private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;
```

Fluxo:

```java
public ResponseDTO login(LoginRequestDTO request) {
```

Primeiro valida se veio request, email e password:

```java
if (request == null || isBlank(request.email) || isBlank(request.password)) {
    throw new InvalidLoginException();
}
```

Depois busca o usuario pelo email:

```java
User user = userRepository.findByEmail(request.email.trim())
        .orElseThrow(InvalidLoginException::new);
```

Se nao existir, retorna erro generico.

Depois compara senha:

```java
if (!passwordEncoder.matches(request.password, user.getPasswordHash())) {
    throw new InvalidLoginException();
}
```

Se a senha estiver correta, monta a resposta:

```java
LoginResponseDTO loginData = new LoginResponseDTO(
        user.getUsername(),
        user.getRole().name()
);
```

E retorna:

```java
return new ResponseDTO("success", "Login realizado com sucesso", loginData);
```

## Estado Atual

A API de login esta estruturada, mas ainda falta uma forma de criar usuarios.

Como o login usa `passwordHash`, nao adianta inserir uma senha comum no banco e esperar que funcione.

A tabela precisa ter algo como:

```text
username: admin
password_hash: hash BCrypt da senha
role: ADMIN
```

## Endpoint Criado

```http
POST /auth/login
```

Request:

```json
{
  "email": "admin@cantinho.com",
  "password": "senha"
}
```

Resposta de sucesso:

```json
{
  "status": "success",
  "message": "Login realizado com sucesso",
  "data": {
    "username": "admin",
    "role": "ADMIN"
  }
}
```

Resposta de erro:

```json
{
  "status": "error",
  "message": "Usuario ou senha invalidos",
  "data": null
}
```

## Observacao Sobre Compilacao

Foi tentado rodar:

```bash
mvn test
```

Mas o ambiente respondeu:

```text
zsh:1: command not found: mvn
```

Ou seja, o Maven nao esta disponivel pelo comando `mvn`, e o projeto ainda nao tem `mvnw`.

Por isso, a compilacao local ainda nao foi validada.

## Proximo Passo Recomendado

O proximo passo tecnico e criar um usuario inicial automaticamente quando a aplicacao subir, ou criar uma API de cadastro restrita para admins.

Sem isso, ainda nao existe um usuario com `passwordHash` valido para testar o login de ponta a ponta.
