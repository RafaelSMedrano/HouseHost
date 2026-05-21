# HouseHost

Sistema de gestao operacional e financeira para pousadas, hoteis pequenos e hospedagens independentes.

O HouseHost centraliza reservas, hospedes, quartos, check-ins, check-outs, caixa, transacoes financeiras e metricas do negocio em uma aplicacao web administrativa. O projeto foi construido com backend em Spring Boot, persistencia com JPA/MySQL e frontend estatico modular em HTML, CSS e JavaScript.

---

## Indice

1. [Resumo](#resumo)
2. [Objetivo do Projeto](#objetivo-do-projeto)
3. [Tecnologias Utilizadas](#tecnologias-utilizadas)
4. [Arquitetura Geral](#arquitetura-geral)
5. [Organizacao do Codigo](#organizacao-do-codigo)
6. [Como Rodar o Projeto](#como-rodar-o-projeto)
7. [Configuracao do Banco de Dados](#configuracao-do-banco-de-dados)
8. [Modelo de Dominio](#modelo-de-dominio)
9. [Fluxos Principais](#fluxos-principais)
10. [Modulo Financeiro](#modulo-financeiro)
11. [Metricas e Dashboard](#metricas-e-dashboard)
12. [Frontend](#frontend)
13. [API REST](#api-rest)
14. [Tratamento de Erros](#tratamento-de-erros)
15. [Decisoes de Modelagem](#decisoes-de-modelagem)
16. [Decisoes de Design de Software](#decisoes-de-design-de-software)
17. [Documentacao Complementar](#documentacao-complementar)
18. [Proximos Passos](#proximos-passos)

---

## Resumo

O HouseHost nasceu para resolver um problema comum em operacoes de hospedagem: dados importantes ficam espalhados entre agenda, planilhas, mensagens, anotacoes e controles financeiros manuais. A aplicacao organiza esses dados em entidades de negocio conectadas:

- `Guest`: hospede cadastrado.
- `Room`: quarto ou unidade de hospedagem.
- `Booking`: reserva.
- `Stay`: estadia ativa ou finalizada.
- `CheckIn` e `CheckOut`: eventos operacionais de entrada e saida.
- `FinancialTransaction`: intencao ou registro financeiro.
- `Cashier`, `CashierEntry` e `CashierExpense`: caixa real e movimentacoes.
- `MetricsSummaryDTO`: consolidado operacional para dashboards.

A ideia central e separar claramente tres dimensoes:

- reserva: o compromisso comercial de hospedagem;
- estadia: a presenca real do hospede no imovel;
- financeiro: o direito/obrigacao de pagamento e seus efeitos no caixa.

Essa separacao evita que uma reserva futura ocupe automaticamente um quarto, evita que um pagamento aguardando seja confundido com dinheiro em caixa e permite que cada tela mostre um estado coerente com a realidade operacional.

---

## Objetivo do Projeto

O HouseHost tem como objetivo oferecer uma base solida para uma area administrativa de hospedagem. A aplicacao cobre:

- cadastro e gestao de hospedes;
- cadastro e gestao de quartos;
- criacao, edicao, exclusao e visualizacao de reservas;
- controle de status de reserva;
- check-in com criacao de estadia;
- check-out com encerramento de estadia;
- criacao de transacoes financeiras associadas a reservas;
- liquidacao de pagamentos;
- criacao automatica de entradas e saidas no caixa;
- exibicao de metricas operacionais e financeiras;
- perfil de hospede, reserva e usuario;
- autenticacao simples por e-mail e senha;
- organizacao visual em dashboard administrativo.

O projeto tambem funciona como estudo pratico de arquitetura em camadas usando Spring Boot, JPA, DTOs, services, repositories e frontend estatico modular.

---

## Tecnologias Utilizadas

### Backend

- Java 21
- Spring Boot 3.2.5
- Spring Web
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- Spring Security Crypto para hash de senha

### Frontend

- HTML estatico
- CSS modular por area visual
- JavaScript ES Modules
- Fetch API para comunicacao REST
- Tabler Icons via CDN
- Google Fonts

### Infraestrutura e Desenvolvimento

- Maven Wrapper (`./mvnw`)
- Arquivo `.env` para credenciais locais
- Script `scripts/run-dev.sh`
- Compatibilidade de schema via `DatabaseSchemaCompatibilityRunner`
- Frontend estatico separado em `frontend/`

---

## Arquitetura Geral

Fluxo simplificado:

```text
Frontend estatico
    |
    | fetch HTTP/JSON
    v
Controllers REST
    |
    v
Services de negocio
    |
    v
Repositories JPA
    |
    v
MySQL
```

A aplicacao segue uma arquitetura em camadas:

- **Controller**: recebe requisicoes HTTP, delega para services e retorna `ResponseDTO`.
- **DTO**: define os contratos de entrada e saida da API.
- **Service**: concentra regras de negocio, validacoes e orquestracao entre modulos.
- **Repository**: encapsula acesso ao banco via Spring Data JPA.
- **Model/Entity**: representa o dominio persistido.
- **Frontend Views/Widgets**: compoem telas e componentes visuais no navegador.

Essa divisao deixa as regras de negocio no backend e mantem o frontend como consumidor da API.

---

## Organizacao do Codigo

Estrutura principal:

```text
src/main/java/com/househost
  auth/       autenticacao, usuarios e cargos
  booking/    reservas e pagamentos associados a reservas
  config/     compatibilidade de schema e configuracoes de startup
  finance/    caixa, transacoes financeiras, entradas e saidas
  guest/      hospedes e status financeiro do hospede
  metrics/    metricas consolidadas para dashboard
  room/       quartos
  shared/     DTO padrao e excecoes globais
  stay/       estadias, check-ins e check-outs

frontend
  index.html
  css/
  js/
    api.js
    controllers/
    views/
    widgets/
```

Padrao interno por modulo backend:

```text
module/
  controller/
  dto/
  model/
  repository/
  service/
```

Nem todo modulo tem exatamente os mesmos subpacotes, mas a intencao arquitetural e consistente.

---

## Como Rodar o Projeto

### Pre-requisitos

- Java 21
- MySQL
- Maven Wrapper incluso no projeto

### Configurar ambiente

Crie um arquivo `.env` na raiz, baseado em `.env.example`:

```bash
HOUSEHOST_DB_URL=jdbc:mysql://localhost:3306/househost?createDatabaseIfNotExist=true&serverTimezone=UTC
HOUSEHOST_DB_USERNAME=root
HOUSEHOST_DB_PASSWORD=sua_senha
```

### Rodar backend

```bash
./scripts/run-dev.sh
```

Ou, exportando as variaveis manualmente:

```bash
./mvnw spring-boot:run
```

Por padrao, o backend roda em:

```text
http://localhost:8080
```

### Rodar testes

```bash
./mvnw test
```

### Abrir frontend

O frontend esta em:

```text
frontend/index.html
```

Quando aberto fora da porta `8080`, `frontend/js/api.js` usa `http://localhost:8080` como base da API.

---

## Configuracao do Banco de Dados

Configuracao principal em `src/main/resources/application.properties`:

```properties
spring.datasource.url=${HOUSEHOST_DB_URL:jdbc:mysql://localhost:3306/househost?createDatabaseIfNotExist=true&serverTimezone=UTC}
spring.datasource.username=${HOUSEHOST_DB_USERNAME:root}
spring.datasource.password=${HOUSEHOST_DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

O projeto usa `ddl-auto=update` para evolucao automatica basica do schema em desenvolvimento.

Tambem existe o `DatabaseSchemaCompatibilityRunner`, que roda no startup e aplica ajustes especificos no MySQL. Ele foi criado porque algumas mudancas de enum, colunas novas e migracoes de valores antigos nao sao sempre resolvidas bem apenas com `ddl-auto=update`.

Exemplos de responsabilidades do runner:

- garantir enums atualizados;
- adicionar colunas novas;
- normalizar valores antigos;
- criar o caixa principal quando necessario;
- manter compatibilidade com bancos ja existentes;
- sincronizar status de hospedes a partir de estadias.

Documento detalhado: [docs/DATABASE_SCHEMA_COMPATIBILITY_RUNNER.md](docs/DATABASE_SCHEMA_COMPATIBILITY_RUNNER.md)

---

## Modelo de Dominio

### Usuario

Entidade: `User`

Representa usuario administrativo da plataforma.

Campos principais:

- `username`
- `email`
- `phone`
- `passwordHash`
- `role`
- `photoUrl`

Cargos:

- `CEO`
- `CTO`
- `ADMIN`
- `MANAGER`
- `RECEPTION`
- `HOUSEKEEPING`

A senha nunca e salva em texto puro. O backend usa `PasswordEncoder` com BCrypt via `PasswordConfig`.

### Hospede

Entidade: `Guest`

Representa a pessoa hospedada ou cliente que possui reservas.

Campos e conceitos principais:

- dados pessoais: nome, e-mail, telefone, documento, cidade, estado, endereco;
- tipo: `NOVO`, `REGULAR`, `VIP`;
- situacao operacional: `IN_BOOKING`, `IN_STAY`, `GOT_CHECKOUT`;
- status financeiro: `WAITING_PAYMENT`, `PAYMENT_SETTLED`, `DEBTOR`;
- historico: reservas, estadias e transacoes financeiras;
- preferencias e observacoes.

O status financeiro do hospede e derivado das transacoes associadas a ele:

1. se existe transacao pendente com data passada, o hospede fica `DEBTOR`;
2. se nao esta em debito, o sistema olha a transacao mais recente;
3. se a ultima esta liquidada/paga, fica `PAYMENT_SETTLED`;
4. se a ultima esta aguardando, fica `WAITING_PAYMENT`.

### Quarto

Entidade: `Room`

Campos principais:

- numero/nome do quarto;
- tipo;
- capacidade;
- diaria;
- status.

Tipos:

- `SINGLE`
- `DOUBLE`
- `SUITE`
- `FAMILY`
- `STANDARD`

Status:

- `AVAILABLE`
- `OCCUPIED`
- `MAINTENANCE`
- `INACTIVE`

Decisao importante: uma reserva futura nao deve, sozinha, tornar o quarto ocupado. Ocupacao real depende de estadia ativa/check-in.

### Reserva

Entidade: `Booking`

Representa a intencao comercial de hospedagem em um periodo.

Campos principais:

- hospede;
- quarto;
- data de check-in;
- data de check-out;
- status;
- origem;
- quantidade de adultos, criancas e pets;
- forma de pagamento;
- parcelas;
- diaria;
- desconto;
- valor pago;
- valor total;
- observacoes.

Status:

- `PENDING`
- `CONFIRMED`
- `CANCELLED`
- `GOT_CHECKIN`

Origem:

- `DIRETO_TELEFONE`
- `WHATSAPP`
- `INSTAGRAM`
- `BOOKING`
- `AIRBNB`
- `INDICACAO`

Status de pagamento da reserva nao e salvo como campo independente. Ele e calculado a partir de `totalAmount` e `paidAmount`:

- `WAITING`: nada pago;
- `PARTIAL`: valor parcialmente pago;
- `PAID`: valor pago cobre o total.

### Estadia

Entidade: `Stay`

Representa a presenca real do hospede no hotel.

Uma reserva pode existir antes da estadia. A estadia nasce quando o check-in e realizado.

Status:

- `ACTIVE`
- `CHECKED_OUT`
- `CANCELLED`

### Check-in

Entidade: `CheckIn`

Registra o evento operacional de entrada. Pode ser criado a partir de:

- uma reserva;
- uma estadia existente;
- hospede + quarto.

Quando criado como `COMPLETED`:

- cria uma `Stay` ativa se ainda nao existir;
- muda a reserva para `GOT_CHECKIN`;
- muda o hospede para `IN_STAY`.

### Check-out

Entidade: `CheckOut`

Registra o evento operacional de saida.

Quando criado como `COMPLETED`:

- atualiza a estadia para `CHECKED_OUT`;
- define a data real de checkout;
- muda o hospede para `GOT_CHECKOUT`.

---

## Fluxos Principais

### 1. Login

Fluxo:

1. usuario informa e-mail e senha;
2. frontend chama `POST /auth/login`;
3. backend busca usuario por e-mail;
4. senha enviada e comparada com `passwordHash`;
5. resposta retorna dados seguros do usuario: id, nome, e-mail, telefone, cargo e foto.

A tela de login tambem carrega:

- metricas reais de ocupacao via `/metrics/summary`;
- usuarios reais para acesso rapido via `/auth/users/quick-access`.

Os cards de acesso rapido preenchem o e-mail, mas nao preenchem senha fixa.

### 2. Cadastro de Usuario

Fluxo:

1. usuario informa nome, e-mail, senha e cargo;
2. backend valida duplicidade de username/e-mail;
3. cargo e normalizado para `UserRole`;
4. senha e codificada;
5. usuario e salvo.

O sistema aceita cargos como CEO e CTO e tambem labels amigaveis vindas do frontend.

### 3. Cadastro de Hospede

Fluxo:

1. frontend envia dados do hospede para `/guests`;
2. backend valida campos obrigatorios;
3. normaliza tipo, situacao e preferencias;
4. salva `Guest`;
5. lista e perfil passam a refletir reservas, estadias e financeiro.

O perfil do hospede agrega dados pessoais, historico e transacoes. Se houver pagamentos pendentes ou em debito, o perfil exibe acoes para liquidacao.

### 4. Criacao de Reserva

Fluxo principal:

1. usuario escolhe hospede por nome ou CPF;
2. escolhe quarto, periodo, origem e detalhes da hospedagem;
3. define dados de pagamento;
4. frontend envia para `POST /bookings/form`;
5. backend valida hospede, quarto e periodo;
6. backend calcula valor total: diaria x noites - desconto;
7. reserva e salva;
8. se ha pagamento informado, cria uma transacao financeira ou uma transacao parcelada;
9. se `paymentCompleted = true`, a transacao e liquidada imediatamente;
10. se `paymentCompleted = false`, a transacao e suas entradas/saidas ficam aguardando.

Regra de disponibilidade:

- reservas `PENDING` e `CONFIRMED` bloqueiam periodo;
- reserva cancelada nao bloqueia periodo;
- check-in realizado muda status para `GOT_CHECKIN`.

### 5. Edicao e Exclusao de Reserva

A lista de reservas permite abrir o perfil da reserva, editar dados e excluir.

Na edicao:

- dados de hospedagem e hospede aparecem primeiro;
- pagamento fica ao final da tela;
- alteracoes passam pelo endpoint `PUT /bookings/{id}`.

Na exclusao:

- o objeto `Booking` e apagado do banco via `DELETE /bookings/{id}`.

### 6. Check-in

Fluxo:

1. operador abre tela de check-in;
2. pode vir de uma reserva pre-preenchida pela dashboard;
3. confirma documentos, pagamento, regras e entrega de chaves;
4. salva check-in;
5. backend cria ou associa `Stay`;
6. reserva vira `GOT_CHECKIN`;
7. hospede vira `IN_STAY`.

Esse fluxo e o que torna a hospedagem operacionalmente ativa.

### 7. Check-out

Fluxo:

1. operador escolhe estadia ativa;
2. registra vistoria, devolucao de chaves, consumo e pendencias;
3. salva check-out;
4. backend encerra a estadia;
5. hospede vira `GOT_CHECKOUT`.

### 8. Perfil de Reserva

O perfil de reserva apresenta:

- dados do hospede;
- periodo;
- quarto;
- valor total;
- status da reserva;
- status de pagamento;
- link para perfil do hospede;
- botao para editar reserva;
- botao de pagamento quando ainda nao esta liquidada.

Quando uma transacao financeira de origem `BOOKING` e liquidada, o backend atualiza o pagamento da reserva via `registerSettledPayment`.

### 9. Perfil de Hospede

O perfil do hospede apresenta:

- dados pessoais;
- tipo do hospede;
- status operacional;
- status financeiro;
- reservas associadas;
- transacoes associadas;
- acoes de liquidacao para pagamentos em espera ou em debito.

O status financeiro e derivado das transacoes. Isso evita duplicidade de verdade entre hospede e financeiro.

### 10. Caixa

A aba Caixa nao lista mais transacoes financeiras diretamente. Ela lista entradas e saidas vinculadas ao caixa principal.

As metricas financeiras da tela sao calculadas a partir de:

- `CashierEntry`;
- `CashierExpense`;
- `Cashier.cashOnHand`;
- `Cashier.onWaiting`;
- status das movimentacoes.

Isso preserva a diferenca entre:

- transacao financeira: intencao/contrato financeiro;
- entrada/saida: movimento de caixa;
- caixa: saldo e acumuladores.

---

## Modulo Financeiro

O modulo financeiro e o ponto mais importante da modelagem atual.

### FinancialTransaction como API de acesso ao caixa

`FinancialTransaction` funciona como uma API conceitual para solicitar movimentacao financeira entre participantes.

Ela exige:

- tipo do pagante (`senderType`);
- id do pagante (`senderId`);
- tipo do recebedor (`receiverType`);
- id do recebedor (`receiverId`);
- valor;
- tipo da transacao;
- data;
- descricao.

Participantes possiveis:

- `CASHIER`
- `GUEST`

Tipos:

- `ENTRY`
- `EXPENSE`
- `TRANSFER`

Status:

- `WAITING`
- `SETTLED`
- `PAID`
- `ON_TIME`
- `LATE`
- `NOT_REALIZED`
- `PARTIALLY_REALIZED`
- `CANCELED`

### Origem da transacao

A transacao pode guardar uma origem:

- `MANUAL`
- `BOOKING`
- `STAY`
- `CHECK_IN`
- `CHECK_OUT`
- `INSTALLMENT`
- `GUEST`

Para reservas, o sistema usa:

```text
sourceType = BOOKING
sourceId = booking.id
```

Essa decisao evita uma relacao rigida direta entre `Booking` e `FinancialTransaction`, mantendo a transacao flexivel como API financeira.

### Guest e FinancialTransaction

O hospede pode estar relacionado a transacoes financeiras.

Essa relacao permite:

- calcular status financeiro do hospede;
- listar pagamentos pendentes no perfil;
- identificar debitos passados;
- liquidar transacoes associadas a um hospede.

### Transacao parcelada

Quando uma reserva e criada com mais de uma parcela, o backend instancia `InstallmentPlanTransaction`, que herda de `FinancialTransaction`.

Esse plano possui parcelas (`InstallmentTransaction`) relacionadas. A liquidacao do plano tambem atualiza as parcelas para status liquidado.

### Cashier, Entry e Expense

O caixa nao se relaciona diretamente com `FinancialTransaction`.

Ele se relaciona com:

- `CashierEntry`: entrada;
- `CashierExpense`: saida.

Cada entrada/saida pode guardar a transacao financeira de origem.

Essa modelagem foi escolhida porque:

- o caixa deve conhecer movimentos de caixa, nao contratos financeiros abstratos;
- uma transacao pode gerar movimento de entrada, saida ou ambos;
- a tela de caixa deve refletir saldo real e valores aguardando.

### WAITING, SETTLED e onWaiting

Quando uma transacao e criada com status `WAITING`:

- entradas e saidas tambem nascem `WAITING`;
- o valor nao entra em `cashOnHand`;
- o montante esperado altera `Cashier.onWaiting`.

Quando `toSettle` e chamado:

- transacao vira `SETTLED`;
- entradas e saidas relacionadas viram `SETTLED`;
- valores saem de `onWaiting`;
- entradas aumentam `cashOnHand`;
- saidas diminuem `cashOnHand`;
- se a origem for `BOOKING`, a reserva registra pagamento.

### toSettle

Metodo principal: `FinancialTransactionService.toSettle(Long id)`.

Fluxo:

1. busca a transacao;
2. impede liquidar novamente se ja estiver `SETTLED`;
3. valida pagante e recebedor;
4. valida valor positivo;
5. muda status da transacao para `SETTLED`;
6. se for transacao parcelada, muda plano e parcelas para liquidado;
7. chama `cashierService.settleMovementsForTransaction`;
8. se a origem for reserva, registra pagamento na reserva;
9. atualiza status financeiro do hospede.

Esse desenho garante que liquidar uma transacao nao seja apenas trocar um texto de status. Liquidar tem efeitos contabeis e operacionais.

---

## Metricas e Dashboard

Endpoint principal:

```text
GET /metrics/summary
```

O `MetricsService` consolida dados de:

- reservas;
- hospedes;
- quartos;
- estadias;
- check-ins;
- check-outs;
- entradas do caixa;
- saidas do caixa.

Exemplos de metricas:

- total de reservas;
- reservas confirmadas;
- reservas com check-in realizado;
- hospedes em estadia;
- hospedes com reserva;
- quartos ocupados;
- quartos livres;
- check-ins esperados hoje;
- check-ins realizados hoje;
- check-outs esperados;
- receita mensal;
- saldo mensal do caixa.

Decisao importante: quartos ocupados sao calculados por estadia ativa ou status operacional real, nao por reserva futura isolada.

---

## Frontend

O frontend e uma SPA simples sem framework, estruturada em ES Modules.

### Entrada

Arquivo:

```text
frontend/index.html
```

Ele carrega:

- `loginWidget.css`;
- `metricsResumeWidget.css`;
- `sidebarWidget.css`;
- `home.css`;
- `js/controllers/main.js`.

### API Client

Arquivo:

```text
frontend/js/api.js
```

Responsavel por centralizar chamadas `fetch`. Ele resolve a base da API assim:

- se estiver em `file:` ou porta diferente de `8080`, usa `http://localhost:8080`;
- caso contrario, usa caminho relativo.

### Controllers

`main.js` controla:

- tela de login;
- tela de cadastro;
- entrada no dashboard apos login.

`UICOntroller.js` controla:

- layout administrativo;
- sidebar;
- topbar;
- navegacao entre views;
- abertura de perfis.

### Widgets

Widgets sao componentes reaproveitaveis:

- `loginWidget.js`;
- `registrationWidget.js`;
- `metricsResumeWidget.js`;
- `sidebarWidget.js`;
- `dashboardTopbarWidget.js`;
- `roomTimelineWidget.js`.

### Views

Views representam telas completas:

- dashboard;
- reservas;
- nova reserva;
- edicao de reserva;
- perfil de reserva;
- hospedes;
- perfil de hospede;
- formulario de hospede;
- quartos;
- formulario de quarto;
- check-in;
- check-out;
- caixa;
- perfil de usuario.

### Design visual

Diretrizes aplicadas:

- dashboard operacional, nao landing page;
- cards compactos para metricas;
- cores de status consistentes entre listagens e cards;
- sidebar persistente;
- botoes com icones;
- telas focadas em operacao recorrente;
- cache-busting por query string nos imports para evitar navegador usando JS antigo.

### Dropdowns de hospede na nova reserva

Na tela de nova reserva, os campos de nome e CPF sugerem hospedes cadastrados.

Esse fluxo reduz erro de digitacao e garante que a reserva seja criada para um hospede existente.

Documento detalhado: [docs/DROPDOWN_HOSPEDES_NOVA_RESERVA.md](docs/DROPDOWN_HOSPEDES_NOVA_RESERVA.md)

---

## API REST

Todas as respostas seguem o padrao `ResponseDTO`:

```json
{
  "status": "success",
  "message": "Mensagem da operacao",
  "data": {}
}
```

### Autenticacao

```text
POST /auth/login
POST /auth/registration
GET  /auth/users/quick-access
PUT  /auth/users/{id}
PUT  /auth/users/{id}/photo
```

### Hospedes

```text
POST   /guests
POST   /guests/register
GET    /guests
GET    /guests/{id}
PUT    /guests/{id}
DELETE /guests/{id}
```

### Reservas

```text
POST   /bookings
POST   /bookings/form
GET    /bookings
GET    /bookings/{id}
GET    /bookings/guest/{guestId}
GET    /bookings/room/{roomId}
PUT    /bookings/{id}
DELETE /bookings/{id}
```

### Quartos

```text
POST   /rooms
GET    /rooms
GET    /rooms/{id}
PUT    /rooms/{id}
DELETE /rooms/{id}
```

### Estadias

```text
POST   /stays
GET    /stays
GET    /stays/{id}
GET    /stays/guest/{guestId}
GET    /stays/room/{roomId}
GET    /stays/booking/{bookingId}
PUT    /stays/{id}
DELETE /stays/{id}
```

### Check-ins

```text
POST   /check-ins
GET    /check-ins
GET    /check-ins/{id}
PUT    /check-ins/{id}
DELETE /check-ins/{id}
```

### Check-outs

```text
POST   /check-outs
GET    /check-outs
GET    /check-outs/{id}
PUT    /check-outs/{id}
DELETE /check-outs/{id}
```

### Financeiro

```text
POST   /financial-transactions
GET    /financial-transactions
GET    /financial-transactions/{id}
PUT    /financial-transactions/{id}
PUT    /financial-transactions/{id}/settle
DELETE /financial-transactions/{id}
```

### Caixa

```text
POST   /cashiers
GET    /cashiers
GET    /cashiers/{id}
PUT    /cashiers/{id}
DELETE /cashiers/{id}
```

### Entradas

```text
POST   /cashier-entries
GET    /cashier-entries
GET    /cashier-entries/cashier/{cashierId}
GET    /cashier-entries/{id}
PUT    /cashier-entries/{id}
DELETE /cashier-entries/{id}
```

### Saidas

```text
POST   /cashier-expenses
GET    /cashier-expenses
GET    /cashier-expenses/cashier/{cashierId}
GET    /cashier-expenses/{id}
PUT    /cashier-expenses/{id}
DELETE /cashier-expenses/{id}
```

### Metricas

```text
GET /metrics/summary
```

---

## Tratamento de Erros

O projeto centraliza excecoes em `GlobalExceptionHandler`.

Cada modulo tem excecoes proprias:

- `BookingException`
- `FinanceException`
- `GuestException`
- `InvalidLoginException`
- `RegistrationException`
- `RoomException`
- `StayException`

Isso permite retornar mensagens de negocio mais claras para o frontend.

---

## Decisoes de Modelagem

### Reserva nao e estadia

Reserva e uma promessa de hospedagem. Estadia e a hospedagem acontecendo.

Essa separacao evita problemas como:

- quarto aparecer ocupado antes do hospede chegar;
- hospede aparecer em estadia sem check-in;
- check-out encerrar algo que nunca foi iniciado.

### Status de pagamento da reserva e calculado

`BookingPaymentStatus` e derivado de `totalAmount` e `paidAmount`.

Motivo: evita divergencia entre campo salvo e valores reais.

### FinancialTransaction nao pertence rigidamente a Booking

Em vez de `Booking` ter uma lista fixa de transacoes, a transacao guarda:

```text
sourceType
sourceId
```

Motivo: a mesma estrutura serve para reserva, estadia, check-in, check-out, parcela, hospede e origem manual.

### Caixa nao conhece FinancialTransaction diretamente

O caixa conhece entradas e saidas.

Motivo: a tela de caixa e a contabilidade operacional precisam de movimentos, nao de intencoes financeiras abstratas.

### WAITING movimenta expectativa, nao saldo real

Entradas e saidas `WAITING` alteram `onWaiting`, mas nao `cashOnHand`.

Motivo: dinheiro esperado nao e dinheiro em caixa.

### GuestFinancialStatus e derivado

O hospede tem `financialStatus`, mas ele e recalculado a partir das transacoes associadas.

Motivo: o status financeiro do hospede e uma leitura do historico financeiro, nao um dado independente.

### DatabaseSchemaCompatibilityRunner existe por pragmatismo

O projeto evoluiu com muitas mudancas de enum e schema. O runner reduz friccao em bancos locais e de producao ja existentes.

Motivo: `ddl-auto=update` e util, mas nao resolve todas as migracoes de dados e enums em MySQL.

---

## Decisoes de Design de Software

### Services concentram regras de negocio

Controllers sao pequenos. Eles recebem a request e delegam para service.

Vantagens:

- regras testaveis;
- controllers simples;
- menor duplicacao;
- fluxo de negocio mais facil de ler.

### DTOs isolam API de entidades

Requests e responses usam DTOs.

Vantagens:

- nao expor entidade JPA diretamente;
- controlar formato enviado ao frontend;
- evitar vazamento de campos sensiveis;
- facilitar labels e campos derivados.

### Frontend sem framework

O frontend usa JavaScript modular puro.

Vantagens:

- baixa complexidade de build;
- facil abrir e inspecionar;
- bom para iteracao rapida;
- fluxo simples de widgets e views.

Trade-off:

- estado global e navegacao precisam ser controlados manualmente;
- componentes exigem disciplina para nao crescer demais;
- nao ha tipagem em tempo de compilacao.

### Cache-busting manual

Imports e CSS usam query strings como:

```text
?v=2026-05-20-login-real-data
```

Motivo: navegador pode manter versoes antigas de arquivos estaticos. Mudar a query forca recarregamento.

### Modulo de metricas centralizado

As telas usam `/metrics/summary` em vez de recalcular tudo no frontend.

Vantagens:

- uma fonte de verdade;
- menos duplicacao;
- calculos financeiros e operacionais ficam no backend;
- frontend apenas renderiza.

---

## Documentacao Complementar

Documentos existentes no projeto:

- [API_LOGIN.md](docs/API_LOGIN.md)
- [ARQUITETURA.md](docs/ARQUITETURA.md)
- [CACHE_BUSTING_FRONTEND.md](docs/CACHE_BUSTING_FRONTEND.md)
- [CONFIGURACAO_DB.md](docs/CONFIGURACAO_DB.md)
- [DATABASE_SCHEMA_COMPATIBILITY_RUNNER.md](docs/DATABASE_SCHEMA_COMPATIBILITY_RUNNER.md)
- [DEPLOY_PRODUCAO_EC2.md](docs/DEPLOY_PRODUCAO_EC2.md)
- [DROPDOWN_HOSPEDES_NOVA_RESERVA.md](docs/DROPDOWN_HOSPEDES_NOVA_RESERVA.md)
- [FLUXO_FORMS_FRONTEND_BACKEND.md](docs/FLUXO_FORMS_FRONTEND_BACKEND.md)
- [FLUXO_REQUISICOES_GET_POST.md](docs/FLUXO_REQUISICOES_GET_POST.md)
- [FRONTEND_ESTATICO_FORA_DO_JAR.md](docs/FRONTEND_ESTATICO_FORA_DO_JAR.md)
- [JPA_QUERIES_REPOSITORY.md](docs/JPA_QUERIES_REPOSITORY.md)
- [ORIGEM_ENTRADA_SAIDA_TRANSACAO_FINANCEIRA.md](docs/ORIGEM_ENTRADA_SAIDA_TRANSACAO_FINANCEIRA.md)
- [STREAM_API.md](docs/STREAM_API.md)

---

## Proximos Passos

Sugestoes tecnicas para evolucao:

- adicionar autenticacao com sessao ou JWT;
- criar autorizacao por cargo;
- substituir `ddl-auto=update` por Flyway ou Liquibase;
- adicionar testes unitarios para services principais;
- adicionar testes de integracao para fluxos de reserva, check-in e financeiro;
- criar build formal do frontend ou migrar para ferramenta leve se necessario;
- versionar contratos da API;
- adicionar auditoria de movimentacoes financeiras;
- melhorar relatorios financeiros;
- criar agenda/calendario de reservas com filtros;
- adicionar controle de consumo e manutencao quando esses modulos forem retomados.

---

## Autor

Rafael Moreno dos Santos Medrano

Projeto desenvolvido como sistema administrativo para gestao de hospedagem, com foco em modelagem de dominio, arquitetura em camadas, operacao hoteleira e controle financeiro.
