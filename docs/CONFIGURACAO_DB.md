# Configuracao do Banco de Dados

Este documento explica a classe responsavel por preparar as configuracoes do banco de dados antes da aplicacao Spring Boot iniciar.

Arquivo principal:

```text
src/main/java/com/househost/config/DatabaseStartupProperties.java
```

Arquivo que chama essa configuracao:

```text
src/main/java/com/househost/HouseHostApplication.java
```

## 1. Problema Resolvido

Antes dessa classe, a aplicacao dependia apenas do `application.properties` e das variaveis de ambiente.

Isso funcionava bem quando a aplicacao era iniciada pelo script:

```bash
./scripts/run-dev.sh
```

O script carregava o arquivo `.env` e perguntava a senha do MySQL quando ela nao estava configurada.

O problema era ao iniciar pelo IntelliJ. Quando o botao Run era usado, o script nao era executado. Entao a aplicacao podia tentar conectar no MySQL com:

- senha vazia;
- senha placeholder, como `coloque_sua_senha_aqui`;
- nenhuma variavel de ambiente carregada.

Para resolver isso, a aplicacao agora prepara as propriedades do datasource diretamente no Java, antes do Spring criar a conexao com o banco.

## 2. Como a Classe e Chamada

Arquivo:

```text
src/main/java/com/househost/HouseHostApplication.java
```

Codigo:

```java
public static void main(String[] args) {
    DatabaseStartupProperties.configure();
    SpringApplication.run(HouseHostApplication.class, args);
}
```

A chamada:

```java
DatabaseStartupProperties.configure();
```

precisa acontecer antes de:

```java
SpringApplication.run(...)
```

Isso e importante porque o Spring Boot cria o `DataSource` durante a inicializacao. Se a senha, usuario ou URL forem definidos depois, ja sera tarde demais.

## 3. Objetivo da Classe

A classe `DatabaseStartupProperties` faz quatro coisas principais:

1. le configuracoes do banco;
2. aplica valores padrao quando algo nao foi configurado;
3. pergunta a senha do MySQL quando ela estiver ausente ou com placeholder;
4. registra as configuracoes finais como propriedades do Spring.

No final, ela define:

```text
spring.datasource.url
spring.datasource.username
spring.datasource.password
```

Essas sao as propriedades que o Spring Boot usa para abrir a conexao com o banco.

## 4. Valores Padrao

Na classe existem dois valores padrao:

```java
private static final String DEFAULT_MYSQL_URL = "jdbc:mysql://localhost:3306/househost?createDatabaseIfNotExist=true&serverTimezone=UTC";
private static final String DEFAULT_USERNAME = "root";
```

O valor padrao da URL aponta para:

```text
localhost:3306
```

Banco:

```text
househost
```

E inclui:

```text
createDatabaseIfNotExist=true
```

Esse parametro e do MySQL Connector/J. Ele permite que o banco `househost` seja criado automaticamente se ainda nao existir.

Importante: isso so funciona se o usuario informado tiver permissao para criar banco de dados no MySQL.

## 5. Ordem de Prioridade das Configuracoes

A classe usa o metodo:

```java
firstConfiguredValue(...)
```

Esse metodo procura o valor em uma ordem especifica.

Ordem usada:

1. propriedade Java/Spring ja definida com `System.getProperty`;
2. variavel de ambiente real do sistema com `System.getenv`;
3. valor lido do arquivo `.env`;
4. valor padrao definido no codigo.

Exemplo para URL:

```java
String url = firstConfiguredValue(
    "spring.datasource.url",
    "HOUSEHOST_DB_URL",
    dotenv,
    DEFAULT_MYSQL_URL
);
```

Isso significa:

- se `spring.datasource.url` ja foi definido, ele vence;
- senao, tenta `HOUSEHOST_DB_URL`;
- senao, tenta ler `HOUSEHOST_DB_URL` do `.env`;
- senao, usa a URL padrao.

A mesma logica vale para usuario e senha.

## 6. Leitura do Arquivo .env

O metodo:

```java
readDotenv()
```

procura um arquivo chamado:

```text
.env
```

na raiz do projeto.

Exemplo de `.env`:

```env
HOUSEHOST_DB_URL=jdbc:mysql://localhost:3306/househost?createDatabaseIfNotExist=true&serverTimezone=UTC
HOUSEHOST_DB_USERNAME=root
HOUSEHOST_DB_PASSWORD=coloque_sua_senha_aqui
```

A leitura ignora:

- linhas vazias;
- linhas com comentario iniciadas por `#`;
- linhas sem `=`.

Cada linha valida e separada assim:

```text
CHAVE=VALOR
```

Tambem existe tratamento para remover aspas simples ou duplas quando o valor estiver assim:

```env
HOUSEHOST_DB_USERNAME="root"
```

Nesse caso, a aplicacao usa:

```text
root
```

sem as aspas.

## 7. Quando a Senha e Perguntada

A senha e perguntada somente quando duas condicoes sao verdadeiras:

```java
if (isMysqlUrl(url) && isPlaceholderPassword(password)) {
    password = askMysqlPassword(username);
}
```

Primeira condicao:

```java
isMysqlUrl(url)
```

Ela verifica se a URL comeca com:

```text
jdbc:mysql:
```

Assim, a pergunta de senha so acontece para MySQL.

Segunda condicao:

```java
isPlaceholderPassword(password)
```

Ela considera a senha invalida quando for:

```text
vazia
coloque_sua_senha_aqui
troque_esta_senha
```

Esses valores existem para permitir que o `.env.example` e o `.env` tenham placeholders sem quebrar o fluxo da aplicacao.

## 8. Como a Senha e Perguntada

O metodo responsavel e:

```java
askMysqlPassword(...)
```

Ele monta a mensagem:

```text
Senha do MySQL para root (Enter para senha vazia):
```

Se a aplicacao estiver rodando em um terminal com `Console` disponivel, ela usa:

```java
console.readPassword(prompt)
```

Esse modo nao mostra os caracteres digitados.

Se o `Console` nao estiver disponivel, o que pode acontecer no IntelliJ, a classe usa:

```java
new Scanner(System.in).nextLine()
```

Nesse caso, a senha pode aparecer enquanto voce digita, dependendo de como o console do IntelliJ estiver configurado.

Se o usuario apertar Enter sem digitar nada, a senha usada sera vazia.

## 9. Como as Configuracoes Sao Entregues ao Spring

Depois de decidir URL, usuario e senha, a classe faz:

```java
System.setProperty("spring.datasource.url", url);
System.setProperty("spring.datasource.username", username);
System.setProperty("spring.datasource.password", password);
```

Isso define propriedades Java antes do Spring Boot iniciar.

Como essas propriedades ja existem quando o Spring sobe, elas passam a ter prioridade sobre o fallback do `application.properties`.

O `application.properties` continua importante, mas agora a classe garante que o IntelliJ tambem tenha o mesmo comportamento esperado.

## 10. Criacao do Banco househost

A criacao automatica do banco nao e feita manualmente pela classe Java.

Ela acontece por causa deste trecho na URL:

```text
createDatabaseIfNotExist=true
```

Fluxo esperado:

1. a aplicacao define URL, usuario e senha;
2. o Spring Boot tenta abrir conexao com MySQL;
3. o MySQL Connector/J percebe que a URL tem `createDatabaseIfNotExist=true`;
4. se o banco `househost` nao existir, o driver tenta cria-lo;
5. se o usuario tiver permissao, o banco e criado;
6. depois disso, o Hibernate cria ou atualiza as tabelas por causa de `ddl-auto=update`.

Configuracao do Hibernate:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Essa propriedade nao cria o database. Ela cria ou atualiza as tabelas dentro de um database ja acessivel.

## 11. Limitacoes Importantes

A aplicacao nao consegue descobrir a senha do MySQL sozinha.

Ela pode:

- ler a senha de variaveis de ambiente;
- ler a senha do `.env`;
- perguntar a senha no console;
- aceitar senha vazia se o usuario apertar Enter.

Ela nao pode:

- recuperar senha esquecida do MySQL;
- criar banco se o usuario MySQL nao tiver permissao;
- criar usuario MySQL automaticamente sem antes ter uma conexao administrativa valida.

Se aparecer erro de permissao, o problema nao esta na classe. O usuario configurado no MySQL precisa ter permissao para criar banco.

## 12. Exemplo de Execucao pelo IntelliJ

Ao clicar Run em `HouseHostApplication`, o fluxo agora e:

1. executa `main`;
2. chama `DatabaseStartupProperties.configure()`;
3. tenta ler variaveis e `.env`;
4. detecta senha placeholder;
5. pergunta a senha no console do IntelliJ;
6. define `spring.datasource.url`, `spring.datasource.username` e `spring.datasource.password`;
7. inicia o Spring Boot;
8. conecta no MySQL;
9. cria o banco `househost` se necessario;
10. cria ou atualiza as tabelas.

## 13. Exemplo de Execucao pelo Terminal

Rodando:

```bash
./mvnw spring-boot:run
```

ou:

```bash
./scripts/run-dev.sh
```

a classe tambem sera executada, porque ela esta dentro do `main`.

O script `run-dev.sh` ainda pode perguntar a senha antes, mas agora a aplicacao tambem tem uma protecao propria para o caso de a senha chegar vazia ou com placeholder.

## 14. Resumo

A classe `DatabaseStartupProperties` existe para deixar a configuracao do banco mais previsivel, principalmente no IntelliJ.

Ela garante que a aplicacao:

- use o `.env` mesmo quando nao for iniciada pelo script;
- pergunte a senha quando necessario;
- mantenha defaults para desenvolvimento local;
- permita criacao automatica do banco `househost`;
- entregue as propriedades corretas ao Spring antes da conexao com MySQL ser criada.
