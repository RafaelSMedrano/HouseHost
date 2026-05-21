# Origem de Entradas e Saidas a Partir de Transacoes Financeiras

Este documento explica o modelo atual para relacionar entradas e saidas de caixa com transacoes financeiras.

A decisao principal foi: `CashierEntry` e `CashierExpense` nao herdam mais de `FinancialTransaction`.

Elas agora sao entidades proprias de movimento de caixa e podem apontar para uma `FinancialTransaction` de origem por meio de `sourceTransactionId`.

## 1. Separando os conceitos

Existem dois conceitos diferentes no modulo financeiro:

```text
FinancialTransaction = registro financeiro, cobranca, promessa, pagamento esperado ou pagamento recebido.
CashierEntry = entrada efetiva no caixa.
CashierExpense = saida efetiva do caixa.
```

Antes, entrada e saida tinham sido modeladas como subclasses de `FinancialTransaction`.

O problema desse desenho e que ele mistura duas perguntas:

```text
Que tipo de registro financeiro e esse?
Esse dinheiro ja movimentou o caixa?
```

Agora o modelo separa melhor:

```text
FinancialTransaction registra o fato financeiro.
CashierEntry registra dinheiro entrando no caixa.
CashierExpense registra dinheiro saindo do caixa.
```

Quando uma entrada ou saida nasce de uma transacao financeira, ela guarda a origem no campo `sourceTransaction`.

## 2. Modelo atual

O modelo atual fica assim:

```text
FinancialTransaction
  ├── senderType/senderId -> CASHIER ou GUEST
  ├── receiverType/receiverId -> CASHIER ou GUEST
  ├── InstallmentPlanTransaction
  └── InstallmentTransaction

CashierEntry
  ├── cashier -> Cashier
  └── sourceTransaction -> FinancialTransaction

CashierExpense
  ├── cashier -> Cashier
  └── sourceTransaction -> FinancialTransaction
```

Ou seja:

- plano parcelado ainda e uma transacao financeira;
- parcela ainda e uma transacao financeira;
- entrada nao e mais uma transacao financeira;
- saida nao e mais uma transacao financeira;
- transacao financeira conhece o participante pagante e o participante recebedor;
- participante pode ser `CASHIER` ou `GUEST`;
- caixa continua registrando dinheiro real pelas entradas e saidas;
- entrada pode nascer de qualquer transacao financeira;
- saida pode nascer de qualquer transacao financeira.

## 3. Por que ficou mais abrangente?

Porque `sourceTransaction` aponta para a classe mae `FinancialTransaction`.

Entao uma entrada ou saida pode apontar para:

```text
FinancialTransaction
InstallmentPlanTransaction
InstallmentTransaction
```

Isso cobre os casos principais:

```text
entrada a partir de uma transacao simples
entrada a partir de um plano parcelado
entrada a partir de uma parcela
saida a partir de uma transacao simples
saida a partir de um plano parcelado
saida a partir de uma parcela
```

Sem obrigar entrada e saida a fazerem parte da hierarquia de transacoes financeiras.

## 4. Como ficou no banco

As tabelas ficam conceitualmente assim:

```text
financial_transactions
id | sender_type | sender_id | receiver_type | receiver_id | guest_id | type | amount | status | method | transaction_date | description

installment_plan_transactions
financial_transaction_id | installments_quantity | installment_plan_status

installment_transactions
financial_transaction_id | installment_plan_id | installment_number | total_installments | installment_status

cashier_entries
id | cashier_id | guest_id | source_transaction_id | amount | entry_date | status | source | description

cashier_expenses
id | cashier_id | guest_id | source_transaction_id | amount | expense_date | status | category | description
```

O ponto central e:

```text
cashier_entries.source_transaction_id  -> financial_transactions.id
cashier_expenses.source_transaction_id -> financial_transactions.id
```

`source_transaction_id` e opcional. Isso permite registrar entradas e saidas manuais que nao vieram de uma transacao financeira especifica.

Outro ponto importante:

```text
financial_transactions nao tem um cashier_id generico.
financial_transactions tem sender_type/sender_id e receiver_type/receiver_id.
cashier_entries tem cashier_id.
cashier_expenses tem cashier_id.
```

Isso deixa claro que a transacao financeira descreve uma ordem entre participantes, enquanto entrada e saida registram o dinheiro efetivamente movimentado quando um dos participantes e caixa.

## 5. Exemplo: entrada gerada por parcela

Imagine este registro:

```text
financial_transactions
id | class                  | amount | status
10 | InstallmentTransaction | 300.00 | PAID
```

Quando esse dinheiro entra no caixa, a entrada pode ser:

```text
cashier_entries
id | amount | status  | source_transaction_id
45 | 300.00 | SETTLED | 10
```

Leitura conceitual:

```text
A entrada de caixa 45 veio da parcela financeira 10.
```

## 6. Exemplo: saida gerada por transacao financeira

Imagine uma transacao financeira de estorno:

```text
financial_transactions
id | type    | amount  | status
70 | EXPENSE | -150.00 | PAID
```

Quando o dinheiro sai do caixa, a saida pode ser:

```text
cashier_expenses
id | amount  | status  | source_transaction_id
90 | -150.00 | SETTLED | 70
```

Leitura conceitual:

```text
A saida de caixa 90 veio da transacao financeira 70.
```

## 7. Como ficou no Java

### FinancialTransaction

`FinancialTransaction` nao possui mais campo `entry` nem `expense`.

Ela nao aponta mais obrigatoriamente para dois caixas.
Agora existem dois participantes:

```text
senderType/senderId     = participante que paga/envia
receiverType/receiverId = participante que recebe
```

Cada participante pode ser:

```text
CASHIER
GUEST
```

Isso transforma a transacao financeira em uma ordem de transferencia/liquidacao entre participantes.

Ela continua representando registros financeiros e continua sendo mae de:

```text
InstallmentPlanTransaction
InstallmentTransaction
```

### CashierEntry

`CashierEntry` virou entidade propria:

```java
@Entity
@Table(name = "cashier_entries")
public class CashierEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_transaction_id")
    private FinancialTransaction sourceTransaction;
}
```

Ela tambem guarda seus proprios dados de caixa:

```text
cashier
guest
description
amount
entryDate
source
status
createdAt
updatedAt
```

### CashierExpense

`CashierExpense` tambem virou entidade propria:

```java
@Entity
@Table(name = "cashier_expenses")
public class CashierExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_transaction_id")
    private FinancialTransaction sourceTransaction;
}
```

Ela guarda:

```text
cashier
guest
description
amount
expenseDate
category
status
createdAt
updatedAt
```

## 8. Regras de valor e status

Entrada de caixa representa dinheiro que entrou.

Por isso:

```text
CashierEntry.amount > 0
CashierEntry.status = SETTLED
```

Saida de caixa representa dinheiro que saiu.

Por isso:

```text
CashierExpense.amount < 0
CashierExpense.status = SETTLED
```

Mesmo que o request envie outro status, as classes de entrada e saida continuam tratando movimento de caixa como `SETTLED`.

Isso preserva a diferenca entre:

```text
WAITING/PAID/etc. = estado da transacao financeira
SETTLED = movimento efetivo no caixa
```

## 9. Criando uma entrada com origem financeira

Endpoint conceitual:

```http
POST /cashier-entries
```

Payload:

```json
{
  "cashierId": 1,
  "sourceTransactionId": 10,
  "description": "Recebimento da parcela 1",
  "amount": 300.00,
  "entryDate": "2026-05-19",
  "source": "CREDIT_CARD"
}
```

Resultado:

```text
Cria uma CashierEntry independente.
Salva amount positivo.
Forca status SETTLED.
Liga source_transaction_id ao id 10 em financial_transactions.
```

## 10. Criando uma saida com origem financeira

Endpoint conceitual:

```http
POST /cashier-expenses
```

Payload:

```json
{
  "cashierId": 1,
  "sourceTransactionId": 70,
  "description": "Estorno de pagamento",
  "amount": -150.00,
  "expenseDate": "2026-05-19",
  "category": "REFUND"
}
```

Resultado:

```text
Cria uma CashierExpense independente.
Salva amount negativo.
Forca status SETTLED.
Liga source_transaction_id ao id 70 em financial_transactions.
```

## 11. Criando uma transacao financeira

Endpoint conceitual:

```http
POST /financial-transactions
```

Payload:

```json
{
  "senderType": "GUEST",
  "senderId": 5,
  "receiverType": "CASHIER",
  "receiverId": 1,
  "guestId": 5,
  "type": "ENTRY",
  "amount": 300.00,
  "transactionDate": "2026-05-19",
  "description": "Pagamento de reserva pelo hospede",
  "method": "CASH",
  "status": "WAITING"
}
```

Esse endpoint cria apenas uma `FinancialTransaction`.

Ele nao cria automaticamente uma `CashierEntry` ou uma `CashierExpense`, porque agora as duas coisas sao entidades diferentes.

Quando a transacao for liquidada, use:

```http
PUT /financial-transactions/10/settle
```

Esse fluxo cria movimentacao de caixa somente para os lados que forem `CASHIER`.

Exemplos:

```text
GUEST -> CASHIER: cria apenas CashierEntry.
CASHIER -> GUEST: cria apenas CashierExpense.
CASHIER -> CASHIER: cria CashierExpense e CashierEntry.
GUEST -> GUEST: nao cria movimento de caixa, apenas marca a transacao como SETTLED.
```

## 12. Por que nao usar heranca aqui?

Heranca funciona bem quando uma classe filha e uma versao especializada da classe mae.

Exemplo que continua fazendo sentido:

```text
InstallmentTransaction e uma FinancialTransaction.
InstallmentPlanTransaction e uma FinancialTransaction.
```

Mas entrada de caixa e saida de caixa representam outro conceito.

Uma entrada pode ter origem em uma transacao financeira, mas ela nao precisa ser a propria transacao financeira.

Mais didaticamente:

```text
Transacao financeira = documento/registro financeiro.
Entrada de caixa = dinheiro que entrou.
Saida de caixa = dinheiro que saiu.
```

Entao a relacao correta aqui e associacao, nao heranca:

```text
CashierEntry tem uma sourceTransaction.
CashierExpense tem uma sourceTransaction.
```

## 13. Beneficio pratico

Esse modelo permite registrar cenarios como:

```text
Uma transacao financeira WAITING ainda nao gerou entrada.
Uma transacao financeira PAID pode gerar uma entrada.
Uma parcela SETTLED pode gerar uma entrada.
Uma transacao de estorno pode gerar uma saida.
Uma saida manual pode existir sem sourceTransactionId.
```

Isso deixa o sistema mais flexivel e evita acoplar todo movimento de caixa a uma unica hierarquia de transacoes financeiras.

## 14. Resumo mental

Use esta regra:

```text
FinancialTransaction explica a ordem financeira entre pagante e recebedor.
CashierEntry e CashierExpense explicam o movimento real do caixa.
```

E este desenho:

```text
FinancialTransaction.senderType/senderId -> CASHIER ou GUEST
FinancialTransaction.receiverType/receiverId -> CASHIER ou GUEST
CashierEntry.sourceTransaction  -> FinancialTransaction
CashierExpense.sourceTransaction -> FinancialTransaction
```

Assim, entrada e saida podem nascer de qualquer transacao financeira sem precisar herdar dela.

## 15. Liquidacao de uma transacao financeira

A liquidacao acontece pelo metodo `toSettle`.

Fluxo conceitual:

```text
1. Busca a FinancialTransaction.
2. Confere se ainda nao esta SETTLED.
3. Valida o participante pagante.
4. Valida o participante recebedor.
5. Se o pagante for CASHIER, chama cashierService.withdraw(transactionId, senderId, amount).
6. Se o recebedor for CASHIER, chama cashierService.deposit(transactionId, receiverId, amount).
7. Se as duas operacoes funcionarem, marca a transacao como SETTLED.
```

A ordem importa:

```text
primeiro retira se o pagante for caixa;
depois deposita se o recebedor for caixa.
```

Se qualquer uma das duas operacoes falhar, a transacao inteira deve falhar. O metodo de liquidacao usa transacao de banco para evitar que apenas uma ponta fique salva.

Na pratica:

```text
withdraw cria uma CashierExpense ligada a sourceTransaction.
deposit cria uma CashierEntry ligada a sourceTransaction.
```

Assim, a `FinancialTransaction` continua sendo a origem conceitual, enquanto `CashierExpense` e `CashierEntry` registram a movimentacao real dos caixas.
