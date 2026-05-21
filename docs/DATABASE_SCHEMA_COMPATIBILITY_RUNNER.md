# DatabaseSchemaCompatibilityRunner

Este documento explica de forma conceitual e didatica a classe `DatabaseSchemaCompatibilityRunner`, localizada em:

```text
src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java
```

Ela existe para executar pequenos ajustes de compatibilidade no banco de dados quando a aplicacao Spring Boot inicia.

## Ideia central

Durante o desenvolvimento, o modelo Java muda. Exemplos:

- um enum ganha novo valor;
- um enum troca nome de valor;
- uma coluna antiga precisa ser normalizada;
- uma tabela pode existir em uma maquina, mas ainda nao existir em outra;
- dados antigos precisam ser convertidos para o novo formato.

O Hibernate com:

```properties
spring.jpa.hibernate.ddl-auto=update
```

ajuda em algumas mudancas de schema, mas nao resolve todos os casos. Ele nao e uma ferramenta completa de migracao de dados.

Por isso foi criado um runner que roda SQL manualmente na inicializacao da aplicacao.

## O que e um runner no Spring Boot

A classe implementa:

```java
CommandLineRunner
```

Um `CommandLineRunner` e um componente Spring executado quando a aplicacao esta iniciando, depois que o contexto do Spring foi preparado.

Neste projeto:

```java
@Component
public class DatabaseSchemaCompatibilityRunner implements CommandLineRunner
```

O `@Component` faz o Spring descobrir a classe automaticamente.

O metodo executado e:

```java
public void run(String... args) throws Exception
```

Ou seja: sempre que a aplicacao sobe, esse metodo roda.

Referencia oficial:

- Spring Boot `CommandLineRunner`: https://docs.spring.io/spring-boot/3.5/api/java/org/springframework/boot/CommandLineRunner.html

## Por que usar JdbcTemplate

O runner recebe:

```java
private final DataSource dataSource;
private final JdbcTemplate jdbcTemplate;
```

O `DataSource` fornece conexao com o banco.

O `JdbcTemplate` executa SQL diretamente:

```java
jdbcTemplate.execute("alter table ...");
```

Ele e usado aqui porque as operacoes sao de banco puro:

- `alter table`;
- `update`;
- consulta em `information_schema`;
- verificacao de coluna existente.

Essas tarefas nao encaixam bem em repositories JPA, porque repositories trabalham com entidades, nao com alteracao estrutural de tabelas.

Referencia oficial:

- Spring Framework `JdbcTemplate`: https://docs.spring.io/spring-framework/docs/6.1.21/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html

## Por que o runner verifica se o banco e MySQL

No inicio do `run`, existe:

```java
try (Connection connection = dataSource.getConnection()) {
    if (!isMysql(connection)) {
        return;
    }
}
```

A funcao:

```java
private boolean isMysql(Connection connection) throws Exception {
    return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("mysql");
}
```

Isso impede que SQL especifico de MySQL rode em outro banco.

Essa protecao e importante porque o runner usa sintaxe como:

```sql
enum('PENDING','CONFIRMED','CANCELLED','GOT_CHECKIN')
```

Esse formato de `ENUM` e especifico do MySQL. Outro banco pode nao aceitar.

Referencia oficial:

- MySQL `ENUM`: https://dev.mysql.com/doc/refman/8.4/en/enum.html

## Fluxo geral do runner

O metodo `run` executa quatro etapas:

```java
jdbcTemplate.execute("""
        alter table bookings
        modify column status enum('PENDING','CONFIRMED','CANCELLED','GOT_CHECKIN') not null
        """);
ensureGuestStatusColumn();
syncGuestStatusFromStays();
ensureFinancialStatusColumns();
```

Cada etapa tem um objetivo diferente.

## 1. Compatibilidade do status de reservas

O primeiro ajuste garante que a coluna `bookings.status` aceite os valores atuais do enum Java:

```sql
alter table bookings
modify column status enum('PENDING','CONFIRMED','CANCELLED','GOT_CHECKIN') not null
```

Conceitualmente, isso sincroniza a definicao do banco com a enum:

```java
BookingStatus
```

Sem esse ajuste, poderia acontecer o seguinte:

1. O Java tenta salvar `GOT_CHECKIN`.
2. A coluna MySQL ainda so aceita `PENDING`, `CONFIRMED`, `CANCELLED`.
3. O banco rejeita o valor.

## 2. Garantia da coluna `guests.status`

A funcao:

```java
ensureGuestStatusColumn();
```

faz duas coisas:

1. verifica se a coluna existe;
2. se existir, converte valores antigos para os novos.

Primeiro ela consulta:

```sql
select count(*)
from information_schema.columns
where table_schema = database()
  and table_name = 'guests'
  and column_name = 'status'
```

Essa consulta usa `information_schema.columns`, que e uma area do MySQL com metadados do banco.

Metadado significa informacao sobre a estrutura do banco, como:

- nomes de tabelas;
- nomes de colunas;
- tipos de colunas;
- schema ao qual pertencem.

Referencia oficial:

- MySQL `INFORMATION_SCHEMA`: https://dev.mysql.com/doc/mysql/en/information-schema.html
- MySQL `INFORMATION_SCHEMA.COLUMNS`: https://dev.mysql.com/doc/refman/8.4/en/information-schema-columns-table.html

### Quando a coluna nao existe

Se a coluna `guests.status` nao existir, o runner cria:

```sql
alter table guests
add column status enum('IN_BOOKING','IN_STAY','GOT_CHECKOUT') not null default 'IN_BOOKING'
```

Esse caso e comum em bancos criados antes da funcionalidade de status do hospede.

### Quando a coluna ja existe

Se a coluna existe, ela pode ter valores antigos em portugues:

- `COM_RESERVA`
- `EM_ESTADIA`
- `COM_CHECK_OUT`

O runner primeiro amplia temporariamente o enum:

```sql
alter table guests
modify column status enum(
    'COM_RESERVA',
    'EM_ESTADIA',
    'COM_CHECK_OUT',
    'IN_BOOKING',
    'IN_STAY',
    'GOT_CHECKOUT'
) not null default 'IN_BOOKING'
```

Depois converte os dados:

```sql
update guests
set status = case status
    when 'COM_RESERVA' then 'IN_BOOKING'
    when 'EM_ESTADIA' then 'IN_STAY'
    when 'COM_CHECK_OUT' then 'GOT_CHECKOUT'
    else status
end
```

Por fim, fecha o enum apenas com os valores atuais:

```sql
alter table guests
modify column status enum('IN_BOOKING','IN_STAY','GOT_CHECKOUT') not null default 'IN_BOOKING'
```

Esse padrao em tres passos e importante:

1. aceitar valores antigos e novos;
2. converter dados antigos;
3. remover valores antigos da definicao.

## 3. Sincronizacao do status do hospede com estadias

A funcao:

```java
syncGuestStatusFromStays();
```

atualiza o status dos hospedes com base nas estadias.

Primeiro, se o hospede tem uma estadia ativa:

```sql
update guests guest
set guest.status = 'IN_STAY'
where exists (
    select 1
    from stays stay
    where stay.guest_id = guest.id
      and stay.status = 'ACTIVE'
)
```

Depois, se ele nao esta em estadia ativa, mas ja teve checkout:

```sql
update guests guest
set guest.status = 'GOT_CHECKOUT'
where guest.status <> 'IN_STAY'
  and exists (
    select 1
    from stays stay
    where stay.guest_id = guest.id
      and stay.status = 'CHECKED_OUT'
  )
```

Aqui o `exists` funciona como uma pergunta:

> existe pelo menos uma linha em `stays` que corresponda a esse hospede e a esse status?

Se sim, o `update` e aplicado.

## 4. Compatibilidade dos status financeiros

A funcao:

```java
ensureFinancialStatusColumns();
```

foi adicionada para resolver a troca de nome:

```text
CONSUMED -> SETTLED
```

Essa troca afeta dois lugares:

- `financial_transactions.status`
- `installment_transactions.installment_status`

### Por que isso precisa de migracao no banco

Os enums Java sao persistidos no banco como texto.

Antes, uma linha podia ter:

```text
CONSUMED
```

Depois da troca no Java, esse valor deixou de existir:

```java
public enum FinancialTransactionStatus {
    SETTLED,
    WAITING,
    CANCELED,
    LATE,
    NOT_REALIZED,
    PARTIALLY_REALIZED,
    PAID,
    ON_TIME
}
```

Se o banco continuasse com `CONSUMED`, ao tentar carregar essa linha o Java poderia falhar, porque nao conseguiria converter o texto antigo para um valor valido da enum atual.

### Migracao em `financial_transactions.status`

O runner primeiro verifica se a coluna existe:

```java
if (columnExists("financial_transactions", "status")) {
```

Depois abre temporariamente o enum para aceitar o valor antigo e o novo:

```sql
alter table financial_transactions
modify column status enum(
    'CONSUMED',
    'SETTLED',
    'WAITING',
    'CANCELED',
    'LATE',
    'NOT_REALIZED',
    'PARTIALLY_REALIZED',
    'PAID',
    'ON_TIME'
) not null
```

Em seguida, converte os dados:

```sql
update financial_transactions
set status = 'SETTLED'
where status = 'CONSUMED'
```

Por fim, remove o valor antigo:

```sql
alter table financial_transactions
modify column status enum(
    'SETTLED',
    'WAITING',
    'CANCELED',
    'LATE',
    'NOT_REALIZED',
    'PARTIALLY_REALIZED',
    'PAID',
    'ON_TIME'
) not null
```

### Migracao em `installment_transactions.installment_status`

O mesmo raciocinio e aplicado nas parcelas:

```sql
alter table installment_transactions
modify column installment_status enum(
    'CONSUMED',
    'SETTLED',
    'WAITING',
    'LATE',
    'NOT_REALIZED'
) not null
```

Depois:

```sql
update installment_transactions
set installment_status = 'SETTLED'
where installment_status = 'CONSUMED'
```

E finalmente:

```sql
alter table installment_transactions
modify column installment_status enum(
    'SETTLED',
    'WAITING',
    'LATE',
    'NOT_REALIZED'
) not null
```

## A funcao `columnExists`

Para nao tentar alterar colunas que ainda nao existem, o runner usa:

```java
private boolean columnExists(String tableName, String columnName)
```

Ela consulta:

```sql
select count(*)
from information_schema.columns
where table_schema = database()
  and table_name = ?
  and column_name = ?
```

Repare nos `?`.

Eles sao parametros passados pelo `JdbcTemplate`:

```java
jdbcTemplate.queryForObject(sql, Integer.class, tableName, columnName)
```

Isso evita concatenar strings manualmente no SQL.

## Idempotencia

Uma migracao de inicializacao deve tentar ser idempotente.

Idempotente significa:

> pode rodar mais de uma vez sem quebrar ou duplicar efeito indevido.

Exemplo:

```sql
update financial_transactions
set status = 'SETTLED'
where status = 'CONSUMED'
```

Na primeira vez, converte os registros antigos.

Na segunda vez, nao encontra mais `CONSUMED`, entao nao altera nada.

O `columnExists` tambem ajuda nisso, porque evita rodar alteracoes em tabelas/colunas inexistentes.

## Beneficios desse runner

### 1. Corrige bancos locais antigos

Cada desenvolvedor pode ter um banco local em um estado ligeiramente diferente.

O runner reduz o problema de:

> "na minha maquina funciona, na sua nao".

### 2. Evita migracao manual repetitiva

Em vez de cada pessoa executar SQL manualmente, a aplicacao aplica ajustes conhecidos.

### 3. Protege mudancas de enum

Mudancas de enum em Java sao perigosas quando os valores estao persistidos como texto.

O runner cria uma ponte entre:

- dados antigos;
- enum atual no codigo.

### 4. Centraliza compatibilidade temporaria

O codigo de compatibilidade fica em um lugar especifico:

```text
DatabaseSchemaCompatibilityRunner
```

Isso e melhor do que espalhar ajustes pelo service, controller ou repository.

## Limites e cuidados

### 1. Nao substitui uma ferramenta formal de migrations

Esse runner resolve bem ajustes pequenos, mas nao e ideal para historico longo de migrations.

Para projetos maiores, ferramentas como Flyway ou Liquibase costumam ser melhores porque:

- versionam scripts;
- registram quais migrations ja rodaram;
- permitem rollback planejado;
- sao mais auditaveis.

Referencias:

- Flyway documentation: https://documentation.red-gate.com/fd
- Liquibase documentation: https://docs.liquibase.com/

### 2. `ALTER TABLE` pode bloquear tabela

Operacoes como:

```sql
alter table ...
```

podem ser custosas em tabelas grandes.

Em producao, isso deve ser planejado com cuidado.

### 3. Rodar em toda inicializacao pode ser desnecessario

Como o runner executa sempre que a aplicacao sobe, ele deve conter apenas operacoes:

- seguras;
- rapidas;
- idempotentes;
- especificas.

### 4. Cuidado com remocao de valores enum

Antes de remover um valor antigo de um enum no banco, e preciso garantir que nenhum registro ainda usa esse valor.

Por isso o padrao correto e:

1. permitir valor antigo e novo;
2. atualizar dados antigos;
3. remover valor antigo.

## Quando adicionar algo nesse runner

Use esse runner quando:

- a mudanca for pequena;
- precisar corrigir bancos locais existentes;
- a alteracao for segura para rodar mais de uma vez;
- o SQL for especifico de compatibilidade;
- nao houver ainda um sistema formal de migrations no projeto.

Evite usar esse runner quando:

- a migracao for grande;
- envolver perda de dados;
- depender de ordem complexa;
- precisar de rollback;
- for uma mudanca sensivel em producao.

## Resumo mental

Pense no `DatabaseSchemaCompatibilityRunner` como um "adaptador de bancos antigos".

Ele nao e a regra de negocio do sistema.

Ele nao e o modelo JPA.

Ele e uma camada de inicializacao que garante que o banco esteja minimamente compativel com o codigo atual antes da aplicacao ser usada.

## Referencias

- Spring Boot `CommandLineRunner`: https://docs.spring.io/spring-boot/3.5/api/java/org/springframework/boot/CommandLineRunner.html
- Spring Framework `JdbcTemplate`: https://docs.spring.io/spring-framework/docs/6.1.21/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html
- MySQL `ENUM`: https://dev.mysql.com/doc/refman/8.4/en/enum.html
- MySQL `INFORMATION_SCHEMA`: https://dev.mysql.com/doc/mysql/en/information-schema.html
- MySQL `INFORMATION_SCHEMA.COLUMNS`: https://dev.mysql.com/doc/refman/8.4/en/information-schema-columns-table.html
- Flyway documentation: https://documentation.red-gate.com/fd
- Liquibase documentation: https://docs.liquibase.com/

