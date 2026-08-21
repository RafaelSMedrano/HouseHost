# Module Architecture Spec

## Specification

Module Architecture is the project-wide structural capability that organizes
each backend module according to hexagonal architecture, with three principal
areas: `domain`, `application` and `adapter`.

It establishes a common architectural language, defines where each kind of
component belongs, controls dependency direction and keeps business concepts
independent from delivery, persistence and integration technologies.

## Scope

This spec governs the creation and structural refactoring of every backend
module in Cantinho das Lavandas. It applies to domain models, use cases, DTOs,
application services, ports, inbound and outbound adapters, JPA persistence,
cross-module communication, validation and audit integration.

Business behavior remains governed by the applicable product specs. A module
must not contradict this architecture without an explicit SDD architecture
change. A structural migration must preserve existing intended behavior unless
a governing product spec separately authorizes a functional change.

Incremental migration is permitted: not every module must be migrated in one
change, but every module declared aligned with this spec must respect its
boundaries and dependency rules.

As convenções de Clean Code desta spec aplicam-se obrigatoriamente ao código
novo e ao código alterado pela tarefa em execução. Elas não autorizam uma
renomeação em massa de código legado sem relação com o escopo da tarefa.

## Capabilities

<a id="objetivos"></a>

### Establish Architectural Objectives

A arquitetura deve separar regra de negócio, orquestração de casos de uso e detalhes técnicos. Essa separação reduz o impacto de mudanças e torna claro onde cada responsabilidade deve ser implementada.

Os principais objetivos são:

- manter o domínio independente de HTTP, Spring MVC, JPA e banco de dados;
- fazer controllers dependerem de casos de uso, e não de implementações concretas;
- permitir que a aplicação declare as dependências externas de que precisa por meio de ports;
- manter detalhes técnicos nos adapters;
- separar services de acordo com suas responsabilidades;
- evitar métodos de conversão desnecessários quando o próprio sistema de tipos pode representar o dado;
- permitir a refatoração gradual dos módulos sem exigir a migração simultânea de todo o projeto.

A arquitetura hexagonal não é apenas uma organização de pastas. A estrutura somente cumpre seu propósito quando as dependências apontam para o núcleo e quando cada classe respeita a responsabilidade da camada em que está localizada.

---

<a id="estrutura"></a>

### Organize A Module

Todo módulo refatorado deve seguir, como referência, a seguinte estrutura:

```text
module/
├── domain/
│   ├── model/
│   └── exception/
├── application/
│   ├── dto/
│   ├── records/
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   └── service/
└── adapter/
    ├── in/
    │   └── rest/
    └── out/
        ├── integration/
        └── persistence/
            └── entity/
```

Nem todo módulo precisa preencher imediatamente todas as pastas. Uma pasta deve existir quando houver uma responsabilidade correspondente. A estrutura não deve estimular a criação de classes vazias ou abstrações sem uso.

<a id="estrutura-domain"></a>

#### Domain

`domain` contém os conceitos e comportamentos fundamentais do módulo. Seus modelos representam a linguagem do negócio e devem poder existir sem conhecimento de controller, JSON, Spring Data ou JPA.

Exemplos:

```text
booking/domain/model/Booking.java
booking/domain/model/BookingStatus.java
booking/domain/model/BookingOrigin.java
```

O domínio pode conter:

- entidades e modelos de negócio;
- enums de negócio;
- comportamentos que alteram o estado dessas entidades;
- exceções genuinamente pertencentes ao domínio.

O domínio não deve conter:

- `@Entity`, `@Table`, `@ManyToOne` ou outras anotações JPA;
- repositories Spring Data;
- DTOs HTTP;
- controllers;
- dependências de adapters;
- lógica específica de serialização ou persistência.

<a id="estrutura-application"></a>

#### Application

`application` coordena os casos de uso. Nessa camada ficam os DTOs, as portas de entrada, as portas de saída e os services.

Ela responde a perguntas como:

- qual operação o sistema oferece?
- quais validações devem ocorrer antes da operação?
- quais entidades precisam ser carregadas?
- quais componentes especializados precisam participar?
- qual persistência ou integração externa será necessária?
- qual resposta deve ser devolvida?

A application pode conhecer o domínio e seus enums. Ela não deve conhecer implementações concretas de infraestrutura quando a dependência precisa ser desacoplada por uma porta.

<a id="estrutura-adapters"></a>

#### Adapters

Adapters conectam a aplicação ao mundo externo. Eles traduzem protocolos, frameworks e mecanismos técnicos para contratos compreendidos pela aplicação.

Existem dois sentidos:

```text
adapter/in  → mundo externo inicia uma operação da aplicação
adapter/out → aplicação solicita persistência, auditoria ou integração
```

Exemplos de adapters de entrada:

- controller REST;
- consumidor de mensagens;
- tarefa agendada que chama um use case.

Exemplos de adapters de saída:

- implementação de persistência JPA;
- adapter de auditoria;
- adapter de integração com outro módulo desacoplado;
- cliente de serviço externo.

---

<a id="dependencias"></a>

### Direct Dependencies Toward The Core

A direção conceitual principal é:

```text
HTTP
  ↓
adapter/in/rest
  ↓
application/port/in
  ↓
application/service
  ↓
domain
```

Quando o caso de uso necessita de infraestrutura, a aplicação declara uma porta de saída:

```text
application/service
  ↓ depende do contrato
application/port/out
  ↑ implementado por
adapter/out
  ↓
JPA, auditoria ou integração
```

O adapter conhece a porta. A porta não conhece o adapter. O service conhece o contrato, e não o mecanismo técnico usado para atendê-lo.

Uma dependência é considerada incorreta quando força o núcleo a conhecer detalhes externos. Por exemplo:

```text
domain/model → BookingJpaRepository      incorreto
application/service → AuditEventService  incorreto nas exceções definidas neste documento
controller → BookingService concreto     incorreto quando existe use case
```

---

<a id="ports"></a>

### Define Ports

Uma port é um contrato localizado na camada de aplicação. Ela representa uma capacidade oferecida pela aplicação ou uma dependência necessária para executar um caso de uso.

<a id="ports-entrada"></a>

#### Inbound Ports And Use Cases

As interfaces em `application/port/in` são os use cases. Elas descrevem as operações que o módulo oferece aos adapters de entrada.

Exemplo:

```java
public interface BookingUseCase {
    BookingResponseDTO create(BookingRequestDTO request);
    BookingResponseDTO findById(Long id);
    BookingResponseDTO update(Long id, BookingRequestDTO request);
    void delete(Long id);
}
```

O controller deve depender dessa interface:

```java
private final BookingUseCase bookingUseCase;
```

Isso impede que o adapter de entrada fique acoplado à implementação do service. Também deixa explícito quais operações são públicas para aquele adapter.

Use cases diferentes podem ser criados quando representam entradas funcionalmente distintas. Um fluxo especializado de formulário, por exemplo, pode ter um `BookingFormUseCase`, enquanto o fluxo central usa `BookingUseCase`.

Não se deve criar uma interface para cada método sem necessidade. A divisão deve refletir capacidades coerentes, consumidores diferentes ou fronteiras funcionais reais.

<a id="ports-saida"></a>

#### Outbound Ports

As interfaces em `application/port/out` representam recursos dos quais a aplicação depende.

Exemplos:

```text
BookingPersistencePort
BookingAuditPort
BookingRoomQueryPort
FinancialTransactionPersistencePort
GuestRelationQueryPort
```

Uma port de saída deve expressar o que o caso de uso precisa, e não copiar automaticamente toda a API de um framework. A aplicação não precisa conhecer `JpaRepository`; precisa de operações como `save`, `findById` ou uma consulta específica de disponibilidade.

---

<a id="controllers"></a>

### Keep Controllers As Inbound Adapters

Controllers pertencem a `adapter/in/rest`. Sua responsabilidade é receber a requisição HTTP, extrair parâmetros, chamar um use case e montar a resposta HTTP esperada pelo sistema.

O controller não deve:

- acessar repositories;
- implementar regra de negócio;
- calcular valores;
- decidir disponibilidade;
- executar auditoria diretamente;
- depender do service concreto quando existe uma porta de entrada.

Fluxo esperado:

```text
BookingController
  → BookingUseCase
      → BookingService
```

As anotações Spring MVC, como `@RestController`, `@GetMapping` e `@RequestBody`, permanecem no adapter de entrada. Elas não devem chegar ao domínio.

---

<a id="formatacao-java"></a>

### Standardize Java Source Formatting

O código Java deve seguir um padrão visual consistente que torne a estrutura da classe identificável sem exigir a leitura detalhada de cada instrução. A formatação deve favorecer legibilidade, revisão e manutenção, sem alterar comportamento para atender apenas a preferências estéticas.

As seguintes regras se aplicam a controllers, services, adapters, configurações, modelos e demais classes Java:

- usar quatro espaços por nível de indentação e não usar tabulações;
- manter uma anotação de classe ou método em sua própria linha;
- declarar a assinatura do método separadamente de sua anotação e colocar o corpo do método nas linhas seguintes;
- usar uma instrução por linha;
- incluir espaços ao redor de operadores de atribuição e demais operadores binários;
- abrir a chave no final da declaração e fechar a chave em uma linha própria, alinhada à declaração correspondente;
- separar métodos e construtores consecutivos com uma linha em branco;
- quebrar chamadas, construtores ou assinaturas longas em múltiplas linhas quando a leitura linear ficar prejudicada;
- indentar os argumentos quebrados um nível além da instrução que os contém;
- manter argumentos curtos na mesma linha quando isso continuar legível;
- evitar que a formatação esconda etapas distintas de um fluxo em uma única linha.

Exemplo de controller formatado:

```java
@PostMapping("/login")
public ResponseDTO login(@RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {
    LoginRequestContext context = new LoginRequestContext(
            originResolver.resolve(httpRequest),
            httpRequest.getHeader("User-Agent")
    );
    LoginResponseDTO data = authUseCase.login(request, context);
    return new ResponseDTO("success", "Login realizado com sucesso", data);
}
```

O exemplo não exige que toda chamada use múltiplas linhas. A quebra é necessária quando evidencia melhor os argumentos, as etapas do fluxo ou a hierarquia visual. Formatação não autoriza renomear elementos, reorganizar operações, extrair métodos ou mudar contratos sem que a tarefa também inclua refatoração.

---

<a id="services"></a>

### Structure Application Services

Services executam e coordenam os casos de uso. Eles ficam em `application/service` e devem seguir os princípios SOLID, principalmente responsabilidade única, coesão e inversão das dependências externas definidas por ports.

<a id="services-responsabilidades"></a>

#### Separate Services By Responsibility

O projeto adota como regra a separação de services por responsabilidade. Um service não deve acumular validação, segurança, pagamento, formulário, auditoria técnica, integração e montagem de perfis apenas porque todas essas operações envolvem a mesma entidade.

Exemplo de divisão:

```text
BookingService
  → coordena criação, atualização, exclusão e consultas

BookingValidationService
  → valida dados e conflitos de reserva

BookingPaymentService
  → trata efeitos relativos a pagamento da reserva

BookingFormService
  → traduz e coordena o fluxo específico do formulário
```

Essa divisão não significa criar um service para cada função privada. Uma nova classe se justifica quando existe uma responsabilidade funcional própria, um conjunto coerente de regras ou uma razão concreta para evoluir e testar aquele comportamento separadamente.

<a id="services-principal"></a>

#### Main Service

O service principal implementa o use case central do módulo. Ele orquestra a operação, mas delega responsabilidades especializadas.

Exemplo:

```text
BookingService.create(request)
  → solicita validação ao BookingValidationService
  → obtém guest e room pelas abstrações locais de resolução
  → cria o modelo Booking
  → persiste pela BookingPersistencePort
  → notifica efeitos em outros módulos pelos Notifiers locais
  → registra auditoria pela BookingAuditPort
  → devolve BookingResponseDTO
```

Orquestrar não significa executar internamente todas as regras. Significa garantir que os participantes corretos sejam chamados na ordem correta.

<a id="services-especializados"></a>

#### Specialized Services

Services especializados devem ter nomes que representem claramente sua responsabilidade.

Exemplos:

- `ValidationService`: regras de aceitação, unicidade e consistência;
- `PaymentService`: comportamento relacionado a pagamentos;
- `FormService`: tratamento de um fluxo específico de formulário;
- `DataSecurityService`: mascaramento e revelação de dados;
- `FinancialService`: interação funcional específica com finanças;
- `ParticipantNotifier`: ponto central do módulo para propagar operações
  concluídas e transições aos participantes externos afetados;
- `Resolver`: componente especializado chamado pelo `ParticipantNotifier` para
  resolver e executar uma mutação ou efeito complexo em determinado módulo de
  destino.

Um service especializado não deve se tornar um depósito genérico de métodos auxiliares. Um cálculo de preço da reserva, por exemplo, não deve ser movido para um service de liquidação de pagamentos apenas porque ambos trabalham com valores monetários. A responsabilidade funcional, e não o tipo do dado, determina onde o comportamento pertence.

<a id="services-comunicacao"></a>

#### Communication Between Services

O service principal pode depender diretamente do service público de outro
módulo para consultas simples e síncronas, como localizar uma entidade por
identificador, documento, nome ou outro critério de leitura. Essa permissão vale
somente para operações sem alteração de estado, sem efeito colateral e sem
orquestração de regra pertencente ao módulo consultado.

Alterações de estado, atualização de status, criação ou remoção de relações e
outras lógicas de integração mais complexas não podem ser executadas pelo
service principal diretamente no service de destino. Esses efeitos devem ser
encapsulados por componentes locais do módulo de origem.

Quando a operação produz alteração de estado, atualização de status, criação de
relação ou outro efeito em um módulo externo, o fluxo obrigatório é:

```text
Service principal do módulo de origem
  → ParticipantNotifier do módulo de origem
      → Resolver específico do participante ou efeito
          → contrato público, service permitido ou adapter do módulo de destino
```

Cada módulo deve possuir um único `ParticipantNotifier` como ponto central para
as mutações e efeitos dirigidos aos demais módulos. Seu nome combina o módulo
de origem com o sufixo `ParticipantNotifier`, como
`BookingParticipantNotifier`, `CheckInParticipantNotifier` ou
`FinancialParticipantNotifier`.

O `ParticipantNotifier` centraliza os Resolvers do módulo e decide quais devem
ser chamados e em qual ordem. Cada Resolver continua especializado por
participante ou efeito e termina em `Resolver`, como `BookingGuestResolver` ou
`CheckInRoomResolver`. O service principal não injeta esses Resolvers
individualmente para executar mutações externas.

O `ParticipantNotifier` recebe o fato ou transição produzida pelo service
principal e decide quais notificações são necessárias. Ele não acessa
repositories de outro módulo nem chama diretamente os services externos; essas
dependências ficam nos Resolvers. O Resolver concentra a resolução do destino,
tradução e chamada que atravessa a fronteira modular. Quando existem vários
destinos para um mesmo tipo, o Resolver pode selecionar a implementação
adequada por enum ou outro discriminador estável.

Exemplo para reserva e hóspede:

```text
BookingService.create(request)
  → GuestService.findGuestById(guestId)
  → RoomService.findRoomById(roomId)
  → BookingParticipantNotifier.notifyCreation(booking)
      → BookingGuestResolver.resolveGuestStatus(guestId)
          → GuestService.setStatus(guestId, guestStatus)

BookingService.setStatus(id, status)
  → BookingParticipantNotifier.notifyStatusChange(booking)
      → BookingGuestResolver.resolveGuestStatus(guestId)
          → GuestService.setStatus(guestId, guestStatus)
```

No exemplo, `BookingService` pode injetar `GuestService` e `RoomService` para as
consultas necessárias à construção da reserva. Para modificar o status do
hóspede, conhece somente seu `BookingParticipantNotifier`; a responsabilidade
de acionar a mutação pública de Guest fica no `BookingGuestResolver` chamado
pelo Notifier.

A comunicação entre módulos deve respeitar estes limites:

- o service principal pode injetar services concretos de outro módulo somente
  para consultas simples, síncronas e sem efeitos colaterais;
- o service principal não chama métodos externos que alterem estado, relações
  ou executem lógica de integração complexa;
- efeitos externos passam pelo fluxo `ParticipantNotifier → Resolver`;
- cada módulo possui um único `ParticipantNotifier` centralizando seus
  Resolvers;
- o contrato chamado no destino deve oferecer uma responsabilidade clara;
- não deve ser criado um ciclo de dependências entre módulos;
- um módulo não deve acessar o repository interno de outro módulo;
- dependências que exigem isolamento explícito devem seguir as exceções
  definidas a seguir;
- o Resolver não modifica internamente o domínio do módulo de destino sem
  passar pela operação pública apropriada.

ParticipantNotifiers e Resolvers são responsabilidades arquiteturais, não
autorização para criar wrappers sem comportamento. O `ParticipantNotifier`
deve expressar transições ou eventos do módulo de origem e coordenar os
Resolvers necessários; cada Resolver deve realmente concentrar uma decisão de
destino, tradução ou mutação entre módulos. Consultas simples não devem ganhar
um Resolver que apenas repasse a chamada ao service externo.

---

<a id="excecoes"></a>

### Preserve Explicit Decoupling Boundaries

Além do fluxo geral por Notifier e Resolver, os módulos financeiro, de
auditoria e de fornecedores exigem desacoplamento explícito por ports e
adapters. Nessas fronteiras, o Resolver não pode substituir os contratos
obrigatórios definidos a seguir.

<a id="excecoes-financeiro"></a>

#### Financial Module

O módulo financeiro representa uma fronteira especialmente sensível. Transações financeiras, participantes, origens, liquidação e efeitos de caixa não devem ficar acoplados a implementações concretas de outros módulos.

A comunicação deve usar contratos como:

- use cases financeiros quando outro módulo inicia uma operação financeira;
- ports de participantes financeiros quando o financeiro notifica um participante;
- ports de origem quando uma liquidação precisa produzir um efeito na origem;
- ports de persistência dentro do próprio módulo.

Exemplo:

```text
BookingService
  → FinancialTransactionUseCase
      → FinancialTransactionService

FinancialTransactionService
  → FinancialParticipantNotifier
      → FinancialPartyResolver
          → implementação correspondente ao participante
```

Essa exceção existe porque o financeiro possui regras transversais, efeitos relevantes e múltiplos participantes. O desacoplamento reduz o risco de um módulo depender da implementação interna de outro para movimentar ou liquidar valores.

<a id="excecoes-auditoria"></a>

#### Audit

Auditoria também deve ser acessada por port de saída. Um service de aplicação não deve depender diretamente de `AuditEventService` nem conhecer detalhes técnicos da persistência de eventos.

Exemplo:

```text
BookingService
  → BookingAuditPort
      → BookingAuditAdapter
          → AuditEventService
```

O adapter define o código de operação LGPD e conecta o evento do módulo à infraestrutura geral de auditoria.

Os services devem chamar a porta diretamente:

```java
bookingAuditPort.record(eventType, entityType, entityId, metadata);
```

Não deve existir um método privado `record()` que apenas receba os mesmos parâmetros e os repasse sem acrescentar comportamento. Métodos privados de auditoria continuam permitidos quando realmente montam metadados, agrupam um evento específico ou expressam uma regra relevante.

<a id="excecoes-fornecedores"></a>

#### Supplier Module

O módulo de fornecedores constitui uma fronteira independente. Seus services não devem chamar diretamente services de autenticação, auditoria, financeiro, privacidade ou qualquer outro módulo, e services externos também não devem chamar diretamente `SupplierService`.

Toda comunicação entre o módulo de fornecedores e outro módulo deve atravessar um contrato explícito e um adapter. Quando fornecedores consome uma capacidade externa, o próprio módulo declara uma port de saída específica:

```text
SupplierService
  → SupplierAuditPort
      → SupplierAuditAdapter
          → infraestrutura pública de auditoria
```

Quando outro módulo consome uma capacidade de fornecedores, o módulo consumidor declara sua própria port e seu adapter chama o use case público de fornecedores:

```text
ModuleService
  → ModuleSupplierQueryPort
      → ModuleSupplierQueryAdapter
          → SupplierUseCase
```

O domínio e a camada de aplicação de fornecedores não devem importar services, repositories, entidades JPA ou adapters de outros módulos. Adapters de integração podem conhecer o contrato público necessário para realizar a tradução entre as fronteiras.

---

<a id="persistencia"></a>

### Separate Domain And JPA Persistence

O modelo de domínio e sua representação persistente são conceitos distintos. O domínio descreve o negócio. A entidade JPA descreve como o estado será armazenado e reconstruído.

<a id="persistencia-dominio"></a>

#### Infrastructure-Independent Domain Models

Os modelos em `domain/model` devem ser genéricos em relação à infraestrutura. Eles podem conter atributos, construtores, comportamentos e métodos de reconstrução de estado, mas não devem depender de JPA.

Exemplo:

```java
public class Booking {
    private Long id;
    private BookingStatus status;

    public void changeStatus(BookingStatus status) {
        this.status = status;
    }
}
```

O termo genérico significa, neste contexto, independente do mecanismo de persistência. O modelo não deve saber se será salvo por JPA, memória, arquivo ou qualquer outra tecnologia.

<a id="persistencia-jpa"></a>

#### JPA Entities

A representação JPA deve ficar em:

```text
adapter/out/persistence/entity
```

Exemplo:

```text
BookingJpaEntity
GuestJpaEntity
FinancialTransactionJpaEntity
```

É nessa classe que devem aparecer:

- `@Entity`;
- `@Table`;
- `@Id`;
- `@GeneratedValue`;
- `@ManyToOne`, `@OneToMany` e demais associações;
- detalhes de coluna;
- callbacks estritamente relacionados à persistência.

O nome `JpaEntity` torna explícito que a classe representa infraestrutura e não o conceito puro do domínio.

<a id="persistencia-port"></a>

#### Persistence Ports

O service deve depender de uma port localizada em `application/port/out`:

```java
public interface BookingPersistencePort {
    Booking save(Booking booking);
    Optional<Booking> findById(Long id);
    List<Booking> findAll();
    void delete(Booking booking);
}
```

A port trabalha com modelos de domínio. Ela não deve expor `BookingJpaEntity`, `Page<JpaEntity>` ou detalhes internos do Spring Data sem uma necessidade arquitetural explícita.

Consultas específicas podem fazer parte da port quando representam uma necessidade real do caso de uso, como verificar sobreposição de datas.

<a id="persistencia-adapter"></a>

#### Persistence Adapters

O adapter implementa a port e usa o repository JPA:

```text
BookingService
  → BookingPersistencePort
      → BookingPersistenceAdapter
          → BookingJpaRepository
              → banco de dados
```

O repository Spring Data deve permanecer no adapter de saída. Ele não deve ser injetado diretamente no service de aplicação de um módulo hexagonal.

<a id="persistencia-mapper"></a>

#### Mappers

Um mapper deve ser usado quando o modelo de domínio e a entidade JPA são classes distintas ou quando é necessário controlar explicitamente a reconstrução de estado.

Responsabilidades típicas:

```text
toDomain(JpaEntity) → reconstrói o modelo de domínio
toEntity(Domain)    → prepara o estado para persistência
```

O mapper deve copiar todos os atributos relevantes, incluindo identidade, estado, relacionamentos e datas persistidas. Esquecer um atributo pode produzir perda silenciosa de informação após salvar e recarregar a entidade.

Nem toda persistência exige um mapper complexo. Ele deve existir quando a separação entre domínio e JPA exigir tradução. Não é necessário criar um mapper apenas para satisfazer uma convenção se domínio e representação já estiverem adequadamente conectados por um mecanismo simples e seguro.

---

<a id="dtos"></a>

### Name And Locate Data-Carrying Types

DTOs pertencem a `application/dto`. Eles representam os dados que entram ou saem dos casos de uso e podem depender diretamente dos enums do domínio.

Objetos de transferência de uma operação HTTP ou de um use case devem ter nome
terminado em `DTO`, como `LoginRequestDTO`.

Java records usados como transportadores internos e imutáveis da camada de
aplicação pertencem a `application/records` e devem ter nome terminado em
`Record`, como `LoginRequestContextRecord`.

O sufixo `DTO` não deve ser adicionado a um modelo de domínio, contexto de
aplicação, resultado interno ou mensagem de integração apenas porque o tipo
carrega dados. O nome e a localização devem expressar seu papel arquitetural.

#### Use Domain Enums In DTOs

Quando um campo representa um conjunto fechado de valores, o DTO deve usar o enum correspondente:

```java
public class BookingRequestDTO {
    public BookingStatus status;
    public BookingOrigin origin;
}
```

O DTO não deve usar `String` para depois exigir métodos como:

```text
parseBookingStatus
parseBookingOrigin
parsePaymentMethod
```

Com enums, o próprio processo de desserialização valida se o valor recebido pertence ao conjunto aceito. Isso elimina conversões repetitivas, reduz possibilidades de interpretação divergente e torna o contrato da API explícito.

O frontend deve enviar os nomes oficiais dos enums:

```json
{
  "status": "CONFIRMED",
  "origin": "DIRETO_TELEFONE"
}
```

Labels de apresentação, traduções e textos amigáveis pertencem à interface ou à resposta adequada. Eles não devem substituir os valores estáveis do contrato de entrada.

Métodos de parse ainda podem existir quando o sistema realmente recebe um formato externo que não controla ou precisa sustentar um contrato legado. Essa deve ser uma exceção consciente, e não o padrão dos DTOs internos da aplicação.

---

<a id="identificadores"></a>

### Name Identifiers According To Their Types

Variáveis, campos e parâmetros devem tornar explícitos os tipos estruturais que
afetam a leitura e o uso do valor:

- todo identificador cujo tipo seja um Java record deve conter `Record`, como
  `loginRequestContextRecord`;
- todo identificador cujo tipo seja `List` deve conter `List`, como `userList`;
- todo identificador cujo tipo seja `Map` deve conter `Map`, como
  `metadataMap`;
- todo identificador cujo tipo seja `Optional` deve terminar com `Optional`,
  como `candidateOptional` ou `activeLoginRestrictionRecordOptional`;
- quando os tipos forem aninhados, os sufixos devem ser combinados do tipo
  contido para o container, como `loginSecurityFailureResultRecordList` para
  `List<LoginSecurityFailureResultRecord>` e `userRecordMap` para um map cujos
  valores sejam records de usuário;
- quando o tipo envolvido já exigir um sufixo, ele deve ser preservado antes de
  `Optional`.

Factories inline como `Map.of(...)` e `List.of(...)` não precisam receber um
nome quando o resultado não é atribuído a um identificador.

Os sufixos não substituem o significado. Devem ser preferidos nomes semânticos
e específicos a nomes genéricos como `data`, `item`, `update`, `result` ou
`context` sempre que houver uma descrição mais precisa disponível.

#### Name Service Instances After Their Classes

Toda variável, campo ou parâmetro cujo tipo concreto seja um service deve ter
exatamente o nome da classe do service, alterando apenas a primeira letra para
minúscula conforme `lowerCamelCase`.

Exemplos obrigatórios:

```java
private final BookingService bookingService;
private final ClientLogValidationService clientLogValidationService;
```

A regra se aplica tanto a services instanciados diretamente quanto a services
injetados por construtor ou framework, recebidos como parâmetros ou armazenados
em campos. Não são permitidas abreviações, reduções ou aliases como `service`,
`validationService`, `bookingSvc` ou `handler` quando o tipo da instância é
`BookingService` ou `ClientLogValidationService`.

Essa convenção é definida pelo tipo concreto do service. Dependências declaradas
por uma interface de use case ou port continuam seguindo o nome do contrato e
não são renomeadas como se fossem o service concreto.

---

<a id="validacao"></a>

### Separate Validation And Normalization

Validação deve ser separada em um service especializado quando formar uma responsabilidade coerente do módulo.

Exemplo:

```text
GuestValidationService
BookingValidationService
FinancialTransactionValidationService
```

Esse service pode validar:

- obrigatoriedade;
- intervalo de valores;
- unicidade;
- consistência de datas;
- conflitos de agenda;
- campos imutáveis;
- compatibilidade entre participantes.

O service principal depende do validation service:

```java
bookingValidationService.validateCreate(request);
```

Validar não é a mesma coisa que normalizar. Validação decide se um valor pode ser aceito. Normalização padroniza um valor aceito, por exemplo removendo espaços periféricos ou transformando texto vazio em `null`.

Normalização também não equivale a parse. Parse converte uma representação para outro tipo. Quando o DTO já usa o tipo correto, parsers de enum deixam de ser necessários.

---

<a id="solid"></a>

### Apply SOLID Proportionally

Os services e componentes devem seguir SOLID como orientação prática, e não como justificativa para criar abstrações indiscriminadamente.

#### Single Responsibility

Cada service deve ter um motivo principal para mudar. Alterações nas regras de validação devem afetar o validation service; alterações no fluxo de formulário devem afetar o form service; alterações na reação a pagamentos devem afetar o payment service.

#### Open And Closed

Ports e estratégias podem permitir novas implementações sem modificar o service central. Um novo mecanismo de auditoria, por exemplo, pode implementar a port já existente.

#### Liskov Substitution

Implementações de uma port devem respeitar o contrato esperado pela aplicação. Um adapter não pode mudar silenciosamente o significado de `save`, `findById` ou `record`.

#### Interface Segregation

Uma port deve expor apenas operações coerentes e necessárias. Um controller de formulário não precisa depender de todas as operações administrativas se utiliza somente a criação pelo formulário.

#### Dependency Inversion

Quando o desacoplamento é exigido, o service depende da port e o adapter implementa essa port. O service não depende do repository JPA ou da infraestrutura concreta de auditoria.

SOLID não exige uma classe nova para cada método. A qualidade da separação depende de responsabilidades reais, não da quantidade de arquivos.

---

<a id="fluxos"></a>

### Follow Reference Flows

#### Common REST Flow

```text
requisição HTTP
  → Controller em adapter/in/rest
  → UseCase em application/port/in
  → Service em application/service
  → modelo em domain/model
  → PersistencePort em application/port/out
  → PersistenceAdapter em adapter/out/persistence
  → JpaRepository
  → banco de dados
```

O esquema demonstra a direção, mas não substitui a explicação: a requisição entra por um adapter, encontra um contrato de aplicação, é coordenada por um service e somente alcança JPA por uma porta implementada por um adapter de saída.

#### Validation Flow

```text
Controller
  → UseCase
  → MainService
      → ValidationService
      → Domain
      → PersistencePort
```

O validation service não substitui o caso de uso. Ele executa a responsabilidade especializada de decidir se a operação pode prosseguir.

#### Audit Flow

```text
ApplicationService
  → ModuleAuditPort.record(...)
  → ModuleAuditAdapter
  → AuditEventService
  → persistência do evento
```

O service define o fato relevante e seus metadados. O adapter conecta esse fato ao mecanismo geral de auditoria e ao código de tratamento apropriado.

#### Cross-Module Communication

Consulta simples:

```text
ModuleAMainService
  → ModuleBService.query(...)
```

Mutação ou efeito complexo:

```text
ModuleAMainService
  → ModuleAParticipantNotifier
      → ModuleBResolver
          → capacidade pública do módulo B
```

Para efeitos e mutações, o service principal não se comunica diretamente com o
service do módulo de destino. O `ParticipantNotifier` centraliza os Resolvers,
expressa o fato ocorrido e coordena os efeitos; cada Resolver concentra a
travessia da fronteira modular. Em consultas simples e sem efeito, o service
principal pode usar diretamente o service público do outro módulo.

---

<a id="regras"></a>

### Apply Practical Rules

1. Controllers devem ficar em `adapter/in/rest`.
2. Controllers devem depender de use cases.
3. Use cases devem ficar em `application/port/in`.
4. Services devem ficar em `application/service`.
5. DTOs devem ficar em `application/dto`.
6. Records internos da aplicação devem ficar em `application/records` e terminar com `Record`.
7. O sufixo `DTO` deve ser reservado a objetos de transferência HTTP ou de use case.
8. Identificadores de records, listas, maps e optionals devem usar os sufixos definidos nesta spec, inclusive quando combinados.
9. Identificadores devem preferir significado funcional a nomes genéricos.
10. Models e enums de negócio devem ficar em `domain/model`.
11. Models de domínio não devem conter JPA.
12. Entidades JPA devem ficar em `adapter/out/persistence/entity`.
13. Repositories Spring Data devem ficar no adapter de persistência.
14. Services hexagonais devem acessar persistência por ports de saída.
15. Mappers devem conectar domínio e JPA quando essa tradução for necessária.
16. Services devem ser separados por responsabilidades funcionais.
17. Services principais podem depender diretamente de services concretos de outros módulos para consultas simples, síncronas e sem efeito colateral.
18. Services principais não devem chamar diretamente mutações ou lógicas complexas de services pertencentes a outros módulos.
19. Efeitos e mutações entre módulos devem seguir o fluxo `ParticipantNotifier → Resolver` no módulo de origem.
20. Cada módulo deve possuir um único `ParticipantNotifier`, responsável por centralizar seus Resolvers.
21. Consultas simples não devem receber Resolvers que apenas repassem chamadas a services externos.
22. Classes de notificação central devem terminar em `ParticipantNotifier`, e classes de resolução de efeitos devem terminar em `Resolver`.
23. Financeiro, auditoria e fornecedores devem permanecer desacoplados por ports e adapters.
24. DTOs devem usar enums diretamente para conjuntos fechados de valores.
25. Parsers de enum não devem existir quando o DTO pode receber o enum.
26. A porta de auditoria deve ser chamada diretamente quando um método intermediário apenas repassaria os argumentos.
27. Não se deve criar classes, ports ou mappers sem responsabilidade concreta.
28. Não se deve mudar lógica funcional durante uma migração exclusivamente estrutural.
29. Toda etapa de refatoração deve ser compilada e testada antes da próxima.
30. O código Java deve seguir o padrão visual de indentação, espaçamento, quebra de linhas e separação de métodos definido nesta spec.
31. Convenções de Clean Code devem ser aplicadas ao código novo ou alterado, sem renomeações em massa fora do escopo da tarefa.
32. Toda instância de tipo concreto service deve usar exatamente o nome da classe com apenas a primeira letra em minúscula.

---

<a id="checklist"></a>

### Review Module Conformity

Antes de considerar um módulo alinhado à arquitetura, verifique:

- [ ] O controller está em `adapter/in/rest`?
- [ ] O controller depende de uma interface de use case?
- [ ] Os use cases estão em `application/port/in`?
- [ ] Os services estão em `application/service`?
- [ ] As responsabilidades de validação, formulário, segurança e pagamento estão adequadamente separadas?
- [ ] Os DTOs estão em `application/dto`?
- [ ] Os objetos de transferência HTTP ou de use case terminam com `DTO`?
- [ ] Records internos estão em `application/records` e terminam com `Record`?
- [ ] Tipos que apenas carregam dados foram nomeados conforme seu papel arquitetural, sem receber `DTO` indevidamente?
- [ ] Identificadores de records, `List`, `Map` e `Optional` possuem os sufixos obrigatórios e combinados na ordem correta?
- [ ] Os identificadores possuem nomes semânticos em vez de nomes genéricos quando existe alternativa mais precisa?
- [ ] Toda instância de service concreto possui exatamente o nome da classe com apenas a primeira letra em minúscula?
- [ ] Campos fechados dos DTOs usam enums?
- [ ] Parsers desnecessários foram eliminados?
- [ ] Os modelos estão em `domain/model`?
- [ ] O domínio está livre de anotações JPA e imports de adapters?
- [ ] Existe uma entidade JPA correspondente quando o modelo é persistido?
- [ ] Existe uma port de persistência?
- [ ] Existe um adapter que implementa essa port?
- [ ] O repository Spring Data está restrito ao adapter?
- [ ] O mapper cobre todos os atributos persistidos quando necessário?
- [ ] Dependências diretas de services externos nos services principais são usadas somente para consultas simples e sem efeitos?
- [ ] Services principais evitam chamar diretamente mutações ou lógicas complexas de outros módulos?
- [ ] Efeitos e mutações entre módulos seguem o fluxo `ParticipantNotifier → Resolver`?
- [ ] Existe no máximo um `ParticipantNotifier` centralizando os Resolvers do módulo?
- [ ] Consultas simples evitam Resolvers que apenas repassariam a chamada?
- [ ] ParticipantNotifiers e Resolvers possuem responsabilidade concreta e nomes com os sufixos obrigatórios?
- [ ] Integrações financeiras utilizam os contratos definidos pelo financeiro?
- [ ] Auditoria utiliza uma port de saída do módulo?
- [ ] As portas de auditoria são chamadas diretamente?
- [ ] A camada de aplicação de fornecedores comunica-se com outros módulos somente por ports e adapters?
- [ ] Módulos consumidores acessam fornecedores por um adapter e pelo `SupplierUseCase`, sem chamar `SupplierService` ou sua persistência diretamente?
- [ ] Não existem ciclos entre services de módulos diferentes?
- [ ] O código Java segue o padrão visual definido nesta spec?
- [ ] A lógica funcional permaneceu estável durante a mudança estrutural?
- [ ] O projeto compila e os testes passam?

Essa lista deve ser usada como apoio à revisão. Marcar todos os itens não substitui a avaliação da coesão e da clareza do módulo, mas ajuda a impedir que detalhes de infraestrutura voltem a atravessar as fronteiras arquiteturais.

## Prerequisite Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`

## Spec Degree

1.
