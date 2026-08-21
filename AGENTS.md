# Regras de autorização

- Só é permitido alterar, criar ou remover código mediante ordem explícita do usuário.
- Pedidos de análise, diagnóstico, explicação, planejamento ou revisão não concedem autorização implícita para modificar o código.
- Se houver dúvida sobre a autorização, não alterar arquivos; apresentar o diagnóstico ou plano e aguardar uma ordem explícita.

## Frontend padrão de implementação

- Toda alteração de frontend deve ser feita em `frontend/admin/generic` por padrão.
- Só é permitido alterar outro frontend, como `frontend/admin/cantinhoDasLavandas` ou `frontend/public/cantinhoDasLavandas`, mediante ordem explícita do usuário.

Before working on this project, read:

- `SDD/specs/sddSpec.md`

Use that document as the starting context for project structure, technical
direction and implementation expectations.

## Completed SDD Task Names

After an SDD task is fully implemented, verified and reported, add `DONE` to
both its filename and title using the pattern defined in
`SDD/specs/sddSpec.md`. Update every reference to the renamed task file. Do not
mark a proposed, partially executed, unverified or blocked task as `DONE`.

## SDD Task Numbering

Number backend (`b`), frontend (`f`) and design/asset (`a`) tasks in independent
sequences beginning at `001`. A task number used in one implementation area does
not consume or reserve that number in another area.

## Code References In Responses

Every response that includes code present in this project must specify the file
and line where that code is located.

## Relatório após operações no código

Sempre após realizar qualquer operação que crie, modifique ou remova código, a
resposta final exibida no console deve conter as categorias aplicáveis em
seções separadas:

- a lista completa dos arquivos modificados;
- a lista completa dos arquivos criados;
- a lista completa dos arquivos removidos;
- para cada arquivo listado, uma explicação específica do motivo da criação,
  modificação ou remoção e da função dessa alteração na operação executada.

Exibir somente as categorias que possuírem ao menos um arquivo. Quando a
operação não criar, modificar ou remover nenhum arquivo, omitir completamente
essas listas. Não substituir as listas aplicáveis por um resumo genérico ou
apenas por um link para o relatório de implementação.

## Clean Code

Apply the following conventions to new code and to code changed by the current
task. Do not perform unrelated mass renaming solely to retrofit older code.

### Java Formatting And Indentation

- Use four spaces for each indentation level and never use tabs.
- Follow the complete Java formatting rules defined in
  `SDD/specs/moduleArchitectureSpec.md`, including one instruction per line,
  annotations on their own lines, aligned braces and readable wrapping of long
  signatures and calls.

### Type Names And Locations

- HTTP or use-case transfer objects belong in `application/dto` and their class
  names must end with `DTO`, such as `LoginRequestDTO`.
- Java records used as immutable internal application carriers belong in
  `application/records` and their class names must end with `Record`, such as
  `LoginRequestContextRecord`.
- Do not add `DTO` to a domain model, application context, result or integration
  message merely because it carries data. Name and place it according to its
  architectural role.

### Identifier Suffixes

- Every variable, field or parameter whose type is a Java record must include
  `Record` in its name, such as `loginRequestContextRecord`.
- Every variable, field or parameter whose type is `List` must include `List`
  in its name, such as `userList`.
- Every variable, field or parameter whose type is `Map` must include `Map` in
  its name, such as `metadataMap`.
- Combine suffixes when types are nested. Use the contained type before the
  container type, such as `loginSecurityFailureResultRecordList` for
  `List<LoginSecurityFailureResultRecord>` and `userRecordMap` for a map whose
  values are user records.
- Every variable, field or parameter whose type is `Optional` must include
  `Optional` at the end of its name, such as `candidateOptional` or
  `activeLoginRestrictionRecordOptional`. When the wrapped type already requires
  a suffix, preserve that suffix before `Optional`.
- Inline factories such as `Map.of(...)` and `List.of(...)` do not require a
  name unless their result is assigned to an identifier.
- Prefer semantic names over generic names such as `data`, `item`, `update`,
  `result` or `context` when a more precise name is available.

### Service Instance Names

- Every variable, field or parameter whose concrete type is a service must use
  exactly the service class name with only its first letter converted to
  lowercase.
- For example, an instance of `ClientLogValidationService` must be named
  `clientLogValidationService`, and an instance of `BookingService` must be
  named `bookingService`.
- Do not abbreviate, shorten or replace the class-derived name with aliases such
  as `service`, `validationService`, `bookingSvc` or `handler`.
- This rule applies whether the service is instantiated directly, injected by a
  constructor or framework, received as a parameter or stored in a field.
