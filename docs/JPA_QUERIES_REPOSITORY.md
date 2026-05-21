# Spring Data JPA, Repositories, SQL e JPQL

Este documento explica como o Spring lida com queries usando Spring Data JPA, com foco no exemplo real do `BookingRepository`.

O objetivo e entender:

- o que o `Repository` faz;
- como o Spring cria queries pelo nome do metodo;
- como funciona `@Query`;
- diferenca entre SQL e JPQL;
- quando as queries sao lidas;
- quando elas sao executadas;
- como o fluxo passa de Controller para Service, Repository e banco.

## Visao Geral do Fluxo

No projeto, uma requisicao normalmente segue este caminho:

```text
Navegador / Cliente HTTP
        |
        v
Controller
        |
        v
Service
        |
        v
Repository
        |
        v
JPA / Hibernate
        |
        v
MySQL
```

Cada camada tem uma responsabilidade:

```text
Controller
Recebe a requisicao HTTP e chama o Service.

Service
Contem regra de negocio.

Repository
Consulta e salva dados.

JPA / Hibernate
Transforma objetos Java em dados do banco e queries em SQL.

MySQL
Armazena os dados de verdade.
```

No caso de reservas, o arquivo principal e:

```text
src/main/java/com/househost/booking/repository/BookingRepository.java
```

## O Que e JPA

JPA significa Java Persistence API.

Ela e uma especificacao do Java para persistencia de dados. Persistencia significa salvar objetos Java em um banco de dados e depois conseguir busca-los novamente.

JPA define conceitos como:

- entidade;
- tabela;
- id;
- relacionamento;
- repository;
- query;
- transacao;
- persistencia.

Importante: JPA e uma especificacao, nao e exatamente a ferramenta concreta que executa tudo.

Quem normalmente implementa JPA em projetos Spring Boot e o Hibernate.

```text
JPA = regra / especificacao
Hibernate = implementacao mais comum
Spring Data JPA = camada do Spring que facilita o uso disso
```

## O Que e Hibernate

Hibernate e a ferramenta que faz grande parte do trabalho pesado:

- le as entidades anotadas com `@Entity`;
- entende os campos;
- entende relacionamentos como `@ManyToOne`;
- gera SQL para MySQL;
- transforma linhas do banco em objetos Java;
- transforma objetos Java em `INSERT`, `UPDATE`, `DELETE`;
- executa queries JPQL.

Quando voce chama:

```java
bookingRepository.save(booking);
```

voce nao escreveu `INSERT` manualmente, mas o Hibernate sabe gerar o SQL necessario.

## Entidade: A Ponte Entre Java e Banco

Veja um trecho da entidade `Booking`:

```java
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;
}
```

Essa classe diz ao JPA:

```text
Booking e uma entidade.
Ela sera gravada na tabela bookings.
O campo id e a chave primaria.
guest e um relacionamento com Guest.
room e um relacionamento com Room.
checkInDate e checkOutDate sao colunas da tabela.
```

Com isso, o JPA consegue mapear entre objeto e tabela:

```text
Classe Java: Booking
Tabela SQL:  bookings

Campo Java: id
Coluna SQL: id

Campo Java: room
Coluna SQL: room_id

Campo Java: guest
Coluna SQL: guest_id
```

## Repository

O repository e a interface que o Service usa para acessar o banco.

No projeto:

```java
public interface BookingRepository extends JpaRepository<Booking, Long> {
}
```

Esse trecho e muito importante:

```java
JpaRepository<Booking, Long>
```

Ele significa:

```text
Este repository trabalha com a entidade Booking.
O tipo do ID da entidade Booking e Long.
```

So por extender `JpaRepository`, o Spring ja entrega metodos prontos:

```java
findAll()
findById(id)
save(entity)
delete(entity)
existsById(id)
count()
```

Por isso, no Service podemos chamar:

```java
bookingRepository.findAll();
bookingRepository.findById(id);
bookingRepository.save(booking);
bookingRepository.delete(booking);
```

Sem criar implementacao manual.

## Como o Spring Cria a Implementacao do Repository

Voce escreve apenas a interface:

```java
public interface BookingRepository extends JpaRepository<Booking, Long> {
}
```

Mas em tempo de execucao o Spring cria uma implementacao por tras.

O processo simplificado e:

```text
1. A aplicacao Spring Boot inicia.
2. O Spring escaneia as classes e interfaces do projeto.
3. Ele encontra interfaces que estendem JpaRepository.
4. Ele cria objetos proxy para essas interfaces.
5. Esses proxies sabem chamar o Hibernate.
6. O Service recebe esse repository por injecao de dependencia.
```

Por isso o construtor do `BookingService` funciona:

```java
public BookingService(BookingRepository bookingRepository, GuestRepository guestRepository, RoomRepository roomRepository) {
    this.bookingRepository = bookingRepository;
    this.guestRepository = guestRepository;
    this.roomRepository = roomRepository;
}
```

Voce nao instancia `new BookingRepository()`.

O Spring injeta uma implementacao gerada automaticamente.

## Quando o Spring Le as Queries

O Spring analisa os repositories quando a aplicacao sobe.

Isso acontece durante a inicializacao do contexto Spring.

Nesse momento ele:

- encontra os repositories;
- identifica quais entidades eles usam;
- analisa metodos como `findByGuestId`;
- valida parte das queries anotadas com `@Query`;
- prepara proxies para execucao posterior.

Mas isso nao significa que todas as queries sao executadas quando a aplicacao inicia.

Existe uma diferenca importante:

```text
Ler / interpretar query
acontece na inicializacao da aplicacao.

Executar query
acontece quando algum metodo do repository e chamado.
```

Exemplo:

```java
List<Booking> bookings = bookingRepository.findByGuestId(guestId);
```

A query desse metodo pode ter sido interpretada na inicializacao, mas ela so roda no banco quando essa linha e executada.

## Query Methods

Query Method e quando o Spring cria uma query com base no nome do metodo.

No `BookingRepository`:

```java
List<Booking> findByGuestId(Long guestId);

List<Booking> findByRoomId(Long roomId);
```

O Spring le o nome `findByGuestId` e divide em partes:

```text
find
By
Guest
Id
```

Ele entende:

```text
Buscar Booking onde booking.guest.id = parametro
```

Como `Booking` tem:

```java
private Guest guest;
```

e `Guest` tem um campo `id`, o Spring consegue navegar:

```text
booking.guest.id
```

O equivalente conceitual em SQL seria:

```sql
SELECT *
FROM bookings
WHERE guest_id = ?;
```

Para:

```java
List<Booking> findByRoomId(Long roomId);
```

O Spring entende:

```text
Buscar Booking onde booking.room.id = parametro
```

Equivalente conceitual:

```sql
SELECT *
FROM bookings
WHERE room_id = ?;
```

## Sintaxe de Query Methods

Alguns padroes comuns:

```java
findByStatus(BookingStatus status)
```

Busca por igualdade:

```sql
WHERE status = ?
```

```java
findByRoomId(Long roomId)
```

Busca por relacionamento:

```sql
WHERE room_id = ?
```

```java
findByStatusIn(Collection<BookingStatus> statuses)
```

Busca usando `IN`:

```sql
WHERE status IN (?, ?)
```

```java
findByCheckInDateBefore(LocalDate date)
```

Busca datas antes de uma data:

```sql
WHERE check_in_date < ?
```

```java
findByCheckOutDateAfter(LocalDate date)
```

Busca datas depois de uma data:

```sql
WHERE check_out_date > ?
```

```java
findByRoomIdAndStatus(Long roomId, BookingStatus status)
```

Combina condicoes com `AND`:

```sql
WHERE room_id = ? AND status = ?
```

```java
findByStatusOrStatus(BookingStatus first, BookingStatus second)
```

Combina condicoes com `OR`:

```sql
WHERE status = ? OR status = ?
```

```java
existsByRoomId(Long roomId)
```

Retorna booleano:

```text
true se existir algum registro
false se nao existir
```

```java
countByStatus(BookingStatus status)
```

Conta registros:

```sql
SELECT COUNT(*)
FROM bookings
WHERE status = ?
```

## Quando Usar Query Method

Use Query Method quando a consulta e simples e legivel pelo nome.

Bom:

```java
findByGuestId(Long guestId)
findByRoomId(Long roomId)
findByStatus(BookingStatus status)
existsByRoomId(Long roomId)
```

Ruim:

```java
findByRoomIdAndStatusInAndCheckInDateBeforeAndCheckOutDateAfter(...)
```

Esse nome fica longo e dificil de ler.

Para consultas mais complexas, prefira `@Query`.

## @Query

`@Query` permite escrever a consulta manualmente.

No projeto:

```java
@Query("""
        select count(booking) > 0
        from Booking booking
        where booking.room.id = :roomId
          and booking.status in :statuses
          and booking.checkInDate < :checkOutDate
          and booking.checkOutDate > :checkInDate
        """)
boolean existsOverlappingBooking(
        @Param("roomId") Long roomId,
        @Param("checkInDate") LocalDate checkInDate,
        @Param("checkOutDate") LocalDate checkOutDate,
        @Param("statuses") Collection<BookingStatus> statuses
);
```

Essa query verifica se existe reserva conflitante para um quarto.

## JPQL

A query acima nao e SQL puro. Ela e JPQL.

JPQL significa Java Persistence Query Language.

A diferenca principal:

```text
SQL fala com tabelas e colunas.
JPQL fala com entidades e atributos Java.
```

SQL:

```sql
SELECT COUNT(*) > 0
FROM bookings b
WHERE b.room_id = ?
  AND b.status IN (?, ?)
  AND b.check_in_date < ?
  AND b.check_out_date > ?;
```

JPQL:

```jpql
select count(booking) > 0
from Booking booking
where booking.room.id = :roomId
  and booking.status in :statuses
  and booking.checkInDate < :checkOutDate
  and booking.checkOutDate > :checkInDate
```

Repare as diferencas:

```text
SQL usa bookings.
JPQL usa Booking.

SQL usa room_id.
JPQL usa booking.room.id.

SQL usa check_in_date.
JPQL usa checkInDate.
```

## Sintaxe Basica de JPQL

Forma geral:

```jpql
select alias
from Entidade alias
where condicao
```

Exemplo:

```jpql
select booking
from Booking booking
where booking.status = :status
```

Aqui:

```text
Booking
e a classe Java anotada com @Entity.

booking
e um alias, um apelido usado dentro da query.

booking.status
e o atributo Java status dentro da classe Booking.

:status
e um parametro nomeado.
```

## Parametros Nomeados

Na query:

```jpql
where booking.room.id = :roomId
```

`:roomId` e um parametro.

Ele e ligado ao parametro Java com `@Param`:

```java
@Param("roomId") Long roomId
```

O nome precisa bater:

```text
:roomId na query
@Param("roomId") no metodo
```

Se os nomes nao baterem, a aplicacao pode falhar ao iniciar ou ao executar a query.

## select count(booking) > 0

No repository usamos:

```jpql
select count(booking) > 0
```

Isso pergunta:

```text
Existe pelo menos um booking que bate com essas condicoes?
```

O retorno do metodo e boolean:

```java
boolean existsOverlappingBooking(...)
```

Entao o resultado esperado e:

```text
true
se encontrou conflito

false
se nao encontrou conflito
```

Outra forma comum seria retornar numero:

```jpql
select count(booking)
from Booking booking
where ...
```

E o metodo retornaria:

```java
long countOverlappingBookings(...)
```

Mas como o Service so precisa saber se existe conflito, retornar `boolean` deixa a intencao clara.

## A Regra de Sobreposicao de Datas

Esta e a parte central da query:

```jpql
booking.checkInDate < :checkOutDate
and booking.checkOutDate > :checkInDate
```

Ela verifica se dois periodos se cruzam.

Periodo existente:

```text
booking.checkInDate
booking.checkOutDate
```

Periodo novo:

```text
:checkInDate
:checkOutDate
```

Dois periodos se sobrepoem quando:

```text
inicio_existente < fim_novo
e
fim_existente > inicio_novo
```

Exemplo com conflito:

```text
Reserva existente: 10/06 ate 15/06
Nova reserva:      14/06 ate 18/06
```

Verificacao:

```text
10/06 < 18/06 = true
15/06 > 14/06 = true
```

Como as duas condicoes sao verdadeiras, existe sobreposicao.

Exemplo sem conflito:

```text
Reserva existente: 10/06 ate 15/06
Nova reserva:      15/06 ate 18/06
```

Verificacao:

```text
10/06 < 18/06 = true
15/06 > 15/06 = false
```

Como a segunda condicao e falsa, nao existe sobreposicao.

Isso permite checkout no mesmo dia em que outro hospede faz check-in.

## Status Que Bloqueiam Reserva

No `BookingService`:

```java
private static final List<BookingStatus> BLOCKING_STATUSES = List.of(
        BookingStatus.PENDING,
        BookingStatus.CONFIRMED
);
```

Isso significa:

```text
Reservas PENDING bloqueiam o quarto.
Reservas CONFIRMED bloqueiam o quarto.
Reservas CANCELLED nao bloqueiam o quarto.
```

Na query:

```jpql
booking.status in :statuses
```

E no Service:

```java
bookingRepository.existsOverlappingBooking(
        roomId,
        request.checkInDate,
        request.checkOutDate,
        BLOCKING_STATUSES
);
```

O `BLOCKING_STATUSES` vira o parametro `:statuses`.

Conceitualmente, em SQL ficaria parecido com:

```sql
AND status IN ('PENDING', 'CONFIRMED')
```

## Query de Criacao x Query de Atualizacao

Para criar uma reserva:

```java
existsOverlappingBooking(...)
```

Para atualizar uma reserva:

```java
existsOverlappingBookingIgnoringId(...)
```

A segunda tem uma condicao extra:

```jpql
and booking.id <> :bookingId
```

Isso significa:

```text
Procure reservas conflitantes,
mas ignore a propria reserva que esta sendo editada.
```

Sem isso, uma reserva entraria em conflito com ela mesma.

Exemplo:

```text
Reserva ID 5 existe de 10/06 ate 15/06.
Usuario edita a reserva ID 5 mantendo as mesmas datas.
```

Se a query nao ignorar o ID 5, ela encontraria a propria reserva e diria que existe conflito.

Por isso usamos:

```jpql
booking.id <> :bookingId
```

Em SQL:

```sql
AND id <> ?
```

## SQL

SQL e a linguagem usada pelo banco de dados relacional.

MySQL entende SQL.

Exemplos:

```sql
SELECT *
FROM bookings;
```

Busca todas as reservas.

```sql
SELECT *
FROM bookings
WHERE room_id = 1;
```

Busca reservas do quarto 1.

```sql
SELECT *
FROM bookings
WHERE status IN ('PENDING', 'CONFIRMED');
```

Busca reservas com status pendente ou confirmada.

```sql
SELECT COUNT(*)
FROM bookings;
```

Conta reservas.

```sql
INSERT INTO bookings (guest_id, room_id, check_in_date, check_out_date, status, total_amount, created_at, updated_at)
VALUES (1, 2, '2026-06-10', '2026-06-15', 'CONFIRMED', 750.00, NOW(), NOW());
```

Insere uma reserva.

```sql
UPDATE bookings
SET status = 'CANCELLED'
WHERE id = 10;
```

Atualiza uma reserva.

```sql
DELETE FROM bookings
WHERE id = 10;
```

Remove uma reserva.

## JPQL x SQL

Comparacao direta:

```text
SQL:
SELECT * FROM bookings WHERE room_id = 1;

JPQL:
select booking from Booking booking where booking.room.id = 1
```

SQL usa:

```text
tabela: bookings
coluna: room_id
```

JPQL usa:

```text
entidade: Booking
atributo: room.id
```

O Hibernate pega o JPQL e transforma em SQL real para o MySQL.

Fluxo:

```text
Repository chama JPQL
        |
        v
Hibernate interpreta JPQL
        |
        v
Hibernate gera SQL
        |
        v
MySQL executa SQL
        |
        v
Hibernate transforma resultado em objeto Java
```

## nativeQuery

Tambem e possivel escrever SQL puro dentro de `@Query`.

Exemplo:

```java
@Query(value = """
        SELECT COUNT(*) > 0
        FROM bookings b
        WHERE b.room_id = :roomId
          AND b.status IN (:statuses)
          AND b.check_in_date < :checkOutDate
          AND b.check_out_date > :checkInDate
        """, nativeQuery = true)
boolean existsOverlappingBookingNative(...);
```

Quando usamos:

```java
nativeQuery = true
```

o Spring entende que a query e SQL nativo, nao JPQL.

Quando usar SQL nativo:

- query muito especifica do banco;
- recurso que JPQL nao suporta bem;
- otimizacao avancada;
- relatorio complexo;
- uso de funcoes especificas do MySQL.

Quando evitar SQL nativo:

- consulta simples;
- consulta que pode ser expressa em JPQL;
- quando voce quer manter independencia maior do banco;
- quando quer trabalhar mais perto do modelo Java.

Para o nosso caso, JPQL esta adequado.

## Como o Repository e Chamado no Service

No `BookingService`, antes de salvar uma reserva:

```java
validateRoomAvailability(room.getId(), request, status);
```

Esse metodo faz:

```java
boolean hasConflict = bookingRepository.existsOverlappingBooking(
        roomId,
        request.checkInDate,
        request.checkOutDate,
        BLOCKING_STATUSES
);

if (hasConflict) {
    throw new BookingException("Quarto ja possui reserva no periodo informado.");
}
```

O fluxo e:

```text
1. Usuario tenta criar reserva.
2. Controller recebe a requisicao.
3. Service valida dados basicos.
4. Service busca hospede e quarto.
5. Service chama repository para checar conflito.
6. Repository executa query.
7. Banco responde true ou false.
8. Se true, Service lanca excecao.
9. Se false, Service salva a reserva.
```

## save()

Quando chamamos:

```java
bookingRepository.save(booking);
```

o Spring Data JPA decide se e `INSERT` ou `UPDATE`.

Regra simplificada:

```text
Se a entidade nao tem ID ainda:
INSERT

Se a entidade ja tem ID:
UPDATE
```

Exemplo de criacao:

```java
Booking booking = new Booking(...);
bookingRepository.save(booking);
```

Como `booking.id` ainda e `null`, o Hibernate gera um `INSERT`.

Exemplo de atualizacao:

```java
Booking booking = findBookingById(id);
booking.updateBooking(...);
bookingRepository.save(booking);
```

Como a reserva ja veio do banco e tem ID, o Hibernate gera um `UPDATE`.

## findById()

`findById` vem pronto do `JpaRepository`:

```java
bookingRepository.findById(id)
```

Ele retorna:

```java
Optional<Booking>
```

Por isso usamos:

```java
return bookingRepository.findById(id)
        .orElseThrow(() -> new BookingException("Reserva nao encontrada."));
```

Isso significa:

```text
Se encontrar, retorna Booking.
Se nao encontrar, lanca BookingException.
```

## Lazy Loading e Relacionamentos

Quando uma entidade tem relacionamento:

```java
@ManyToOne
private Room room;
```

o Hibernate pode carregar esse relacionamento junto ou deixar para carregar depois, dependendo da configuracao.

Esse comportamento e chamado de loading.

Conceitos:

```text
Eager loading
carrega o relacionamento junto.

Lazy loading
deixa para carregar o relacionamento quando ele for acessado.
```

Em muitos casos, `@ManyToOne` tem comportamento eager por padrao em JPA. Mesmo assim, em sistemas maiores e comum configurar carregamento com mais cuidado.

Por que isso importa?

Porque uma query que busca reservas pode acabar carregando quartos e hospedes tambem, dependendo do que o codigo acessa depois.

Exemplo:

```java
booking.getRoom().getRoomNumber()
```

Ao acessar `getRoom()`, o Hibernate pode precisar buscar o quarto no banco se ele ainda nao tiver sido carregado.

## show-sql

No `application.properties`:

```properties
spring.jpa.show-sql=true
```

Essa configuracao faz o Hibernate imprimir no console o SQL gerado.

Isso e util para estudo, porque voce consegue ver:

```text
JPQL ou repository method
        |
        v
SQL real gerado pelo Hibernate
```

Em producao, geralmente `show-sql=true` nao e ideal, porque pode gerar muito log.

Para desenvolvimento e aprendizado, ajuda bastante.

## Erros Comuns em Queries JPA

### Nome de atributo errado

JPQL usa atributos Java.

Errado:

```jpql
where booking.room_id = :roomId
```

Certo:

```jpql
where booking.room.id = :roomId
```

### Usar nome da tabela em JPQL

Errado:

```jpql
from bookings booking
```

Certo:

```jpql
from Booking booking
```

### @Param com nome diferente

Errado:

```jpql
where booking.room.id = :roomId
```

```java
@Param("idQuarto") Long roomId
```

Certo:

```java
@Param("roomId") Long roomId
```

### Query Method grande demais

Possivel, mas ruim:

```java
findByRoomIdAndStatusInAndCheckInDateBeforeAndCheckOutDateAfter(...)
```

Melhor:

```java
@Query(...)
boolean existsOverlappingBooking(...);
```

## Como Ler Uma Query JPQL

Use esta ordem:

```text
1. Qual entidade esta no FROM?
2. Qual alias foi dado para a entidade?
3. O que o SELECT retorna?
4. Quais filtros estao no WHERE?
5. Quais filtros usam parametros?
6. Quais parametros chegam pelo metodo Java?
7. O tipo de retorno do metodo combina com o SELECT?
```

Exemplo:

```jpql
select count(booking) > 0
from Booking booking
where booking.room.id = :roomId
  and booking.status in :statuses
  and booking.checkInDate < :checkOutDate
  and booking.checkOutDate > :checkInDate
```

Leitura:

```text
FROM Booking booking
Estou consultando reservas.

SELECT count(booking) > 0
Quero saber se existe pelo menos uma.

booking.room.id = :roomId
Somente do quarto informado.

booking.status in :statuses
Somente com status que bloqueia.

booking.checkInDate < :checkOutDate
A reserva existente comeca antes do fim da nova.

booking.checkOutDate > :checkInDate
A reserva existente termina depois do inicio da nova.
```

## Como Ler Um Query Method

Exemplo:

```java
findByRoomIdAndStatus
```

Divida assim:

```text
find
By
RoomId
And
Status
```

Leitura:

```text
Buscar pela entidade do repository
onde room.id = primeiro parametro
e status = segundo parametro
```

Se o repository e:

```java
JpaRepository<Booking, Long>
```

entao a entidade buscada e `Booking`.

## Resumo

`JpaRepository` entrega CRUD pronto.

Query Methods criam consultas pelo nome do metodo.

`@Query` permite escrever consultas mais claras quando o nome do metodo ficaria grande.

JPQL usa entidades e atributos Java.

SQL usa tabelas e colunas do banco.

Hibernate transforma JPQL em SQL.

O Spring le e prepara repositories na inicializacao, mas as queries rodam quando os metodos sao chamados.

No `BookingRepository`, as queries de sobreposicao de datas protegem a regra de negocio: um quarto nao pode ter duas reservas ativas no mesmo periodo.
