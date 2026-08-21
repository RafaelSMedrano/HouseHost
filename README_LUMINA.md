# Lumina, conexão cliente-servidor em tempo real

![Java](https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?logo=springboot&logoColor=white) ![WebSocket](https://img.shields.io/badge/WebSocket-010101?logo=socketdotio&logoColor=white) ![REST API](https://img.shields.io/badge/REST_API-02569B?logo=fastapi&logoColor=white) ![MySQL](https://img.shields.io/badge/MySQL-00000F?logo=mysql&logoColor=white) ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black) ![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white) ![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white)

## Resumo

O Lumina é um projeto fullstack voltado à comunicação em tempo real entre cliente e servidor por meio de WebSocket. Com backend Java/Spring Boot e um front-end web estático em JavaScript, permite que cliente e servidor mantenham um canal aberto para troca contínua de mensagens, eventos e atualizações. A aplicação tem a utilidade de funcionar como uma fundação reutilizável para diferentes tipos de serviços, como ferramentas colaborativas, chats, dashboards, sistemas de rastreamento de geolocalização em tempo real, ou qualquer produto que precise de um backend organizado e de uma interface inicial para interação com usuário.

Como feature inicial, o Lumina também possui um sistema CRUD nativo para registro em banco de dados MySQL e autenticação de usuários.
Na prática, o Lumina oferece uma estrutura inicial para:

- receber requisições HTTP por endpoints REST;
- manter conexões WebSocket abertas com clientes;
- oferecer CRUD nativo de registro e autenticação de usuários;
- rotear mensagens por tipo;
- separar controller, handler, service, DTO e estado do servidor;
- padronizar respostas;
- guardar dados permanentes em banco;
- manter dados temporários em memória durante a execução.

## Sumário

- [1. Introdução e motivação](#1-introdução-e-motivação)
  - [1.1. Lumina como base para qualquer aplicação](#11-lumina-como-base-para-qualquer-aplicação)
- [2. Visão geral da arquitetura](#2-visão-geral-da-arquitetura)
- [3. Protocolos e DTOs](#3-protocolos-e-dtos)
  - [3.1. Protocolo de request](#31-protocolo-de-request)
  - [3.2. Protocolo de response](#32-protocolo-de-response)
- [4. Fluxo de request e response](#4-fluxo-de-request-e-response)
  - [4.1. Backend](#41-backend)
    - [4.1.1. REST: registro e login](#411-rest-registro-e-login)
    - [4.1.2. WebSocket: conexão e mensagens](#412-websocket-conexão-e-mensagens)
  - [4.2. Frontend](#42-frontend)
    - [4.2.1. Inicialização da interface](#421-inicialização-da-interface)
    - [4.2.2. Registro de usuário](#422-registro-de-usuário)
    - [4.2.3. Login de usuário](#423-login-de-usuário)
    - [4.2.4. Conexão WebSocket no JavaScript](#424-conexão-websocket-no-javascript)
- [5. Como o roteamento WebSocket funciona](#5-como-o-roteamento-websocket-funciona)
- [6. Papel do ServerState](#6-papel-do-serverstate)
- [7. Como rodar o Lumina](#7-como-rodar-o-lumina)
- [8. Como adicionar novos fluxos](#8-como-adicionar-novos-fluxos)

## 1. Introdução e motivação

Em muitas aplicações, APIs REST resolvem bem chamadas pontuais: o cliente faz uma requisição, o servidor responde e a comunicação termina ali. O WebSocket atende outro tipo de necessidade: ele mantém uma conexão aberta entre cliente e servidor, permitindo troca de mensagens em tempo real sem depender de uma nova requisição HTTP para cada evento.

Essa motivação fica ainda mais clara em serviços que dependem de comunicação em tempo real. Aplicações grandes como WhatsApp, Discord, Slack, Trello, Google Docs e Uber usam canais contínuos para entregar mensagens, presença online, edição colaborativa, notificações, localização e atualizações instantâneas. O Lumina segue essa mesma ideia em uma base simples: ele não limita a aplicação a um domínio específico, mas fornece um caminho claro para criar novos fluxos, seja para jogos, chats, dashboards, ferramentas colaborativas ou sistemas administrativos.

### 1.1. Lumina como base para qualquer aplicação

O Lumina pode servir como ponto de partida para aplicações de vários tipos porque o seu núcleo não depende de uma regra específica de jogo. O que ele fornece são capacidades comuns a muitos sistemas modernos.

Uma aplicação administrativa pode usar REST para login, cadastro e operações CRUD, enquanto usa WebSocket para avisos em tempo real. Um chat pode usar WebSocket para envio e recebimento de mensagens, REST para histórico e autenticação. Uma plataforma de ensino pode usar REST para dados de cursos e WebSocket para presença online, notificações ou colaboração ao vivo. Um sistema de monitoramento pode usar WebSocket para transmitir eventos, métricas e alertas.

O ponto central é que o Lumina separa transporte, roteamento e regra de negócio. O transporte pode ser HTTP ou WebSocket. O roteamento decide qual fluxo será executado. A regra de negócio fica isolada em services. Essa separação permite adaptar o servidor a diferentes domínios sem reescrever sua estrutura principal.


## 2. Visão geral da arquitetura

O Lumina segue uma arquitetura monolítica em camadas. Isso significa que backend, regras de negócio, acesso a dados e comunicação em tempo real vivem no mesmo projeto, mas separados por responsabilidades claras.

Ele está organizado em alguns blocos principais:

- **Aplicação Spring Boot**: ponto de partida do servidor, responsável por subir a aplicação e carregar os componentes.
- **REST controllers**: expõem endpoints HTTP tradicionais, como autenticação e cadastro.
- **Services**: concentram regras de negócio e evitam que controllers ou handlers façam trabalho demais.
- **DTOs**: definem os formatos de entrada e saída usados nas requisições e respostas.
- **WebSocket handler**: recebe conexões, mensagens e desconexões WebSocket.
- **Message router**: escolhe qual handler deve processar uma mensagem com base no campo `type`.
- **Message handlers**: processam tipos específicos de mensagem WebSocket.
- **ServerState**: mantém dados temporários da execução atual, como sockets conectados.
- **Repository**: acessa dados persistentes no banco.
- **Cliente estático**: contém exemplos de chamadas HTTP, conexão WebSocket e roteamento de respostas no front-end.

Essa divisão permite que cada parte tenha uma responsabilidade clara. O controller não precisa saber como o banco funciona em detalhe. O WebSocket handler não precisa conhecer todas as regras de negócio. O router não precisa processar a mensagem; ele apenas encontra quem sabe processar. O service não precisa saber se foi chamado por HTTP, WebSocket ou outro mecanismo.


## 3. Protocolos e DTOs

Protocolos e DTOs são contratos usados para organizar a comunicação da aplicação, mas atuam em níveis diferentes. O protocolo é o envelope final trocado entre cliente e servidor: ele define a estrutura comum da mensagem, como `type`, `status`, `message` e `data`.

O DTO é o conteúdo específico que viaja dentro desse envelope. Enquanto o protocolo responde à pergunta "como a mensagem é estruturada?", o DTO responde "quais dados essa ação precisa carregar?". Assim, uma mensagem WebSocket pode seguir sempre o mesmo protocolo, mas carregar DTOs diferentes para login, registro, ping ou qualquer outro fluxo.

### 3.1. Protocolo de request

O protocolo `Request` representa a mensagem recebida pelo servidor via WebSocket.

```java
public class Request {
    public String type;
    public Object data;
}
```

- `type`: identifica qual ação deve ser executada.
- `data`: carrega os dados específicos daquela ação.

### 3.2. Protocolo de response

O protocolo `Response` representa a mensagem enviada pelo servidor de volta ao cliente.

```java
public class Response {
    public String type;
    public String status;
    public String message;
    public Object data;
}
```

- `type`: identifica o tipo da resposta, normalmente usando o sufixo `.response`.
- `status`: informa se o processamento foi bem-sucedido ou falhou.
- `message`: traz uma mensagem textual sobre o resultado.
- `data`: carrega o conteúdo específico da resposta.

## 4. Fluxo de request e response

O Lumina usa REST para ações pontuais e WebSocket para comunicação contínua.

### 4.1. Backend

#### 4.1.1. REST: registro e login

- `POST /auth/registration`
- `POST /auth/login`

```text
Cliente
  |
  | JSON: username, password, email
  v
AuthController
  |
  v
AuthService
  |
  v
UserRepository / Banco
  |
  v
ResponseDTO: status, message, data
  |
  v
Cliente
```

#### 4.1.2. WebSocket: conexão e mensagens

```text
ws://localhost:8080/ws
```

```text
Cliente conectado
  |
  v
WebSocketHandler
  |
  +--> ServerState guarda a sessão
  |
  | Request: type + data
  v
MessageRouter
  |
  v
MessageHandler
  |
  v
Service
  |
  v
Response: type.response + status + data
  |
  v
Cliente conectado
```

### 4.2. Frontend

#### 4.2.1. Inicialização da interface

```text
HTML carregado
  |
  v
main.js
  |
  +--> registerRoute("ping.response", pingHandler)
  |
  v
renderLoginWidget()
```

#### 4.2.2. Registro de usuário

```text
registrationWidget.js
  |
  | username + email + password
  v
api.js -> registration()
  |
  | fetch POST /auth/registration
  v
Backend
  |
  | success
  v
Mensagem de conta criada
  |
  v
renderLoginWidget()
```

#### 4.2.3. Login de usuário

```text
loginWidget.js
  |
  | username + password
  v
api.js -> login()
  |
  | fetch POST /auth/login
  v
Backend
  |
  | success
  v
renderHomeView()
  |
  v
initSocket()
```

#### 4.2.4. Conexão WebSocket no JavaScript

```text
initSocket()
  |
  v
socket.js
  |
  v
new WebSocket("ws://localhost:8080/ws")
  |
  +--> onopen: envia mensagens pendentes
  |
  +--> onmessage: JSON -> routeMessage()
                         |
                         v
                    messageRouter.js
                         |
                         v
                    handler visual
```

## 5. Como o roteamento WebSocket funciona

O container do Spring facilita esse fluxo porque armazena os handlers como beans. Com isso, o `MessageRouter` recebe uma `List<MessageHandler>` com todos os filhos da interface mãe `MessageHandler` e registra as rotas sem criar cada handler manualmente.

```text
Request
{
  type: "ping",
  data: {...}
}
  |
  v
MessageRouter
  |
  v
PingHandler
  |
  v
Response
{
  type: "ping.response",
  status: "success",
  data: {...}
}
```

## 6. Papel do ServerState

O `ServerState` representa o estado vivo do servidor. Ele não substitui o banco de dados. O banco guarda informações permanentes; o `ServerState` guarda informações temporárias da execução atual.

Exemplos de dados que podem ficar no `ServerState`:

- sockets conectados;
- usuários online;
- sessões ativas;
- salas abertas;
- documentos sendo editados;
- assinaturas de notificações;
- estados temporários de colaboração;
- qualquer informação que só faça sentido enquanto o servidor está rodando.

No estado atual do projeto, ele guarda os sockets conectados usando o `sessionId` como chave. Isso permite que o servidor saiba quais conexões estão ativas e remova a sessão quando o cliente desconectar.

## 7. Como rodar o Lumina

Antes de iniciar, é necessário ter Java 21 e MySQL instalados. O projeto usa Maven Wrapper, então não é obrigatório instalar Maven manualmente.

O primeiro passo é criar um banco MySQL chamado `lumina`:

```sql
CREATE DATABASE lumina;
```

Depois disso, confira as credenciais do banco em:

```text
src/main/resources/application.properties
```

O projeto usa a URL:

```text
jdbc:mysql://localhost:3306/lumina?serverTimezone=UTC
```

Com o banco preparado, inicie o servidor pela raiz do projeto:

```bash
./mvnw spring-boot:run
```

Quando a aplicação subir, o backend ficará disponível em:

```text
http://localhost:8080
```

Com o servidor rodando, abra a página HTML pelo navegador:

```text
src/main/resources/statics/index.html
```

Essa página carrega o `main.js`, renderiza a tela inicial do CRUD de usuários e direciona o usuário para o fluxo de login e registro.

Os principais pontos da aplicação são:

```text
REST login       -> POST http://localhost:8080/auth/login
REST registro    -> POST http://localhost:8080/auth/registration
WebSocket        -> ws://localhost:8080/ws
```

## 8. Como adicionar novos fluxos

Para adicionar um novo fluxo REST, normalmente o caminho é:

1. Criar DTOs de request e response.
2. Criar ou atualizar um service com a regra de negócio.
3. Criar um endpoint em um controller.
4. Chamar o endpoint pelo front-end ou por outro cliente.

Para adicionar um novo fluxo WebSocket, normalmente o caminho é:

1. Definir o nome do `type` da mensagem.
2. Criar um DTO para o campo `data` da request.
3. Criar um DTO para os dados da response.
4. Criar um service com a regra de negócio.
5. Criar um handler que implemente `MessageHandler`.
6. Fazer o handler retornar o `type` no método `getType`.
7. Registrar no front-end um handler para `type.response`.
8. Enviar mensagens no formato `{ "type": "...", "data": {...} }`.

Como os handlers são componentes Spring, o `MessageRouter` consegue recebê-los automaticamente pela injeção de dependência. Isso reduz a necessidade de mexer no núcleo do WebSocket sempre que uma nova mensagem é adicionada.

---

Rafael Medrano

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?logo=linkedin&logoColor=white)](https://www.linkedin.com/in/rafaelsmedrano/) [![Gmail](https://img.shields.io/badge/Gmail-333333?logo=gmail&logoColor=red)](mailto:rafael.smedrano@gmail.com)
