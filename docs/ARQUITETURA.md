# Arquitetura do HouseHost

O HouseHost deve seguir uma arquitetura monolitica em camadas, parecida com a base usada nos projetos Lumina, Cantinho Das Lavandas e LuminaJavaPuro.

A ideia principal e manter um backend Spring Boot organizado por dominios, com frontend JavaScript estatico, REST para operacoes comuns e WebSocket apenas quando houver necessidade de comunicacao em tempo real.

## Visao Geral

```text
Frontend publico / Area admin
        |
        v
Controllers REST
        |
        v
Services
        |
        v
Entidades de dominio
        |
        v
Repositories
        |
        v
Banco de dados
```

O sistema deve ser dividido por modulos de negocio, nao apenas por tipo tecnico de classe. Cada modulo tera seus proprios controllers, DTOs, services, models e repositories.

## Estrutura Recomendada

```text
com.househost
├── HouseHostApplication.java
│
├── auth
│   ├── controller
│   ├── dto
│   ├── model
│   ├── repository
│   └── service
│
├── guest
│   ├── controller
│   ├── dto
│   ├── model
│   ├── repository
│   └── service
│
├── room
│   ├── controller
│   ├── dto
│   ├── model
│   ├── repository
│   └── service
│
├── booking
│   ├── controller
│   ├── dto
│   ├── model
│   ├── repository
│   └── service
│
├── finance
│   ├── controller
│   ├── dto
│   ├── model
│   ├── repository
│   └── service
│
├── site
│   ├── controller
│   └── dto
│
├── websocket
│   ├── handler
│   ├── protocol
│   ├── router
│   └── dto
│
└── shared
    ├── dto
    ├── exception
    ├── validation
    └── config
```

## Modulos Principais

### Auth

Responsavel por login, cadastro de usuarios administrativos, controle de senha e autorizacao da area admin.

Classes esperadas:

```text
User
UserRole
AuthService
UserRepository
LoginRequestDTO
LoginResponseDTO
```

### Guest

Responsavel pelos hospedes cadastrados no hotel.

Classes esperadas:

```text
Guest
GuestService
GuestRepository
CreateGuestRequestDTO
GuestResponseDTO
```

### Room

Responsavel pelos quartos, tipos de quarto, status e disponibilidade.

Classes esperadas:

```text
Room
RoomType
RoomStatus
RoomService
RoomRepository
```

Exemplos de status:

```text
AVAILABLE
OCCUPIED
MAINTENANCE
CLEANING
INACTIVE
```

### Booking

Responsavel pelas reservas, estadias, check-in e check-out.

Classes esperadas:

```text
Booking
BookingStatus
StayPeriod
BookingService
BookingRepository
```

Exemplos de status:

```text
PENDING
CONFIRMED
CHECKED_IN
CHECKED_OUT
CANCELED
```

### Site

Responsavel pelos dados exibidos no site publico, como quartos disponiveis, informacoes do hotel, formulario de reserva e conteudo institucional.

Esse modulo nao deve conter regras financeiras nem regras administrativas pesadas. Ele deve apenas expor dados publicos ou receber solicitacoes iniciais de reserva.

## Tratamento de Financas

Financas devem ser tratadas como um modulo proprio, separado de reservas.

Uma reserva pode gerar uma cobranca, mas a cobranca nao deve ser apenas um campo dentro da reserva. O financeiro precisa evoluir de forma independente, porque um hotel pode ter pagamentos parciais, descontos, taxas extras, multas, reembolsos e despesas internas.

Relacao principal:

```text
Booking
   |
   v
Invoice
   |
   v
Payment
   |
   v
FinancialTransaction
```

## Estrutura do Modulo Finance

```text
finance
├── model
│   ├── Money.java
│   ├── Invoice.java
│   ├── InvoiceItem.java
│   ├── Payment.java
│   ├── PaymentMethod.java
│   ├── PaymentStatus.java
│   ├── FinancialTransaction.java
│   └── TransactionType.java
│
├── service
│   ├── InvoiceService.java
│   ├── PaymentService.java
│   └── FinancialReportService.java
│
├── repository
│   ├── InvoiceRepository.java
│   ├── PaymentRepository.java
│   └── FinancialTransactionRepository.java
│
└── dto
    ├── CreatePaymentRequestDTO.java
    ├── InvoiceResponseDTO.java
    ├── PaymentResponseDTO.java
    └── FinancialSummaryResponseDTO.java
```

## POO Aplicada ao Financeiro

O modulo financeiro nao deve usar `double` para valores monetarios. Valores de dinheiro devem usar `BigDecimal`, preferencialmente encapsulado em uma classe `Money`.

Exemplo conceitual:

```java
public class Money {
    private BigDecimal amount;
    private String currency;

    public Money add(Money other) {
        // soma valores da mesma moeda
    }

    public Money subtract(Money other) {
        // subtrai valores da mesma moeda
    }

    public boolean isPositive() {
        // verifica se o valor e maior que zero
    }
}
```

Essa abordagem evita espalhar calculos financeiros pelo sistema.

### Invoice

Representa uma conta ou cobranca.

Responsabilidades:

- guardar os itens cobrados;
- calcular total;
- informar status da cobranca;
- manter relacao com a reserva.

Exemplos de itens:

```text
diaria
taxa de limpeza
consumo
desconto
multa
servico extra
```

### InvoiceItem

Representa um item especifico dentro de uma cobranca.

Campos provaveis:

```text
description
quantity
unitPrice
total
```

### Payment

Representa um pagamento realizado ou tentado.

Campos provaveis:

```text
invoice
amount
method
status
paidAt
```

### FinancialTransaction

Representa o movimento no caixa.

Campos provaveis:

```text
type
category
amount
description
occurredAt
```

Tipos:

```text
INCOME
EXPENSE
REFUND
ADJUSTMENT
```

## Fluxo Financeiro Recomendado

```text
Admin cria reserva
        |
        v
BookingService cria Booking
        |
        v
InvoiceService gera Invoice com diarias e taxas
        |
        v
PaymentService registra pagamento
        |
        v
FinancialTransactionService registra entrada no caixa
        |
        v
FinancialReportService gera resumo financeiro
```

## Regras de Separacao

`Booking` deve saber sobre reserva, periodo, hospede, quarto e status da estadia.

`Invoice` deve saber sobre cobranca, itens e total.

`Payment` deve saber sobre valor pago, metodo e status do pagamento.

`FinancialTransaction` deve saber sobre fluxo de caixa e relatorios.

Essa separacao evita que a classe `Booking` vire uma classe grande demais, misturando hospedagem, pagamento, caixa e relatorio.

## DTOs

DTOs devem ser usados em toda entrada e saida da API.

Regras:

- request DTO deve conter apenas campos que o cliente pode enviar;
- response DTO deve conter apenas campos que o cliente pode ver;
- entidades JPA nao devem ser expostas diretamente pela API;
- campos sensiveis devem ser definidos pelo backend, nao pelo frontend;
- validacoes principais devem ficar nos services ou em validadores proprios.

## REST e WebSocket

REST deve ser usado para:

- login;
- cadastro de hospedes;
- CRUD de quartos;
- criacao e consulta de reservas;
- registro de pagamentos;
- relatorios financeiros;
- dados publicos do site.

WebSocket deve ser usado apenas para eventos em tempo real:

- nova reserva recebida;
- check-in realizado;
- check-out realizado;
- pagamento registrado;
- quarto liberado;
- alerta financeiro;
- atualizacao do painel administrativo.

## Decisao Principal

O HouseHost deve nascer como um monolito modular em Spring Boot.

Essa escolha combina com os projetos anteriores, mantem simplicidade operacional e permite crescer com organizacao. Separar em microservicos neste momento aumentaria complexidade sem necessidade.

O ponto mais importante e manter o financeiro separado desde o inicio. Reservas, pagamentos e caixa se relacionam, mas nao devem ser a mesma coisa dentro do codigo.
