# Aula de Stream API em Java

Este documento explica a Stream API de Java de forma didatica, com exemplos e exercicios progressivos.

## 1. Objetivo da Aula

Ao final desta aula, voce deve conseguir:

- entender o que e uma `Stream`;
- diferenciar `Collection` de `Stream`;
- usar `filter`, `map`, `sorted`, `forEach`, `collect`, `count`, `anyMatch`, `allMatch`, `findFirst` e `reduce`;
- transformar listas sem escrever muitos `for`;
- aplicar Stream API em cenarios reais do HouseHost, como listar e filtrar hospedes.

## 2. O Que e Stream API

Stream API e uma API do Java para processar sequencias de dados.

Ela foi adicionada no Java 8 e fica no pacote:

```java
java.util.stream
```

Uma `Stream` nao e uma lista. Ela nao armazena dados.

Uma `Stream` e um fluxo de processamento sobre uma fonte de dados.

Fonte de dados pode ser:

- `List`;
- `Set`;
- array;
- resultado de uma consulta;
- valores gerados dinamicamente.

Exemplo simples:

```java
List<String> names = List.of("Ana", "Bruno", "Carla");

names.stream()
        .forEach(name -> System.out.println(name));
```

Nesse exemplo:

- `names` e a fonte;
- `stream()` cria o fluxo;
- `forEach(...)` executa uma acao para cada item.

## 3. Collection vs Stream

Uma `Collection`, como `List`, guarda dados.

Exemplo:

```java
List<String> names = List.of("Ana", "Bruno", "Carla");
```

A lista contem os valores.

Uma `Stream` processa os valores.

Exemplo:

```java
names.stream()
        .filter(name -> name.startsWith("A"))
        .forEach(System.out::println);
```

A stream nao substitui a lista. Ela apenas cria um pipeline para trabalhar com os dados.

## 4. Pipeline de Stream

Um pipeline de Stream tem tres partes:

1. fonte;
2. operacoes intermediarias;
3. operacao terminal.

Exemplo:

```java
List<String> names = List.of("Ana", "Bruno", "Carla", "Amanda");

List<String> result = names.stream()
        .filter(name -> name.startsWith("A"))
        .map(String::toUpperCase)
        .toList();
```

Separando:

Fonte:

```java
names
```

Criacao da Stream:

```java
stream()
```

Operacoes intermediarias:

```java
filter(...)
map(...)
```

Operacao terminal:

```java
toList()
```

Operacoes intermediarias retornam outra Stream.

Operacoes terminais encerram o pipeline e produzem um resultado.

## 5. Operacoes Intermediarias

Operacoes intermediarias sao etapas no meio do pipeline.

Elas nao executam sozinhas. Elas esperam uma operacao terminal.

### 5.1. filter

`filter` serve para manter apenas os elementos que passam em uma condicao.

Exemplo:

```java
List<String> names = List.of("Ana", "Bruno", "Carla", "Amanda");

List<String> namesStartingWithA = names.stream()
        .filter(name -> name.startsWith("A"))
        .toList();
```

Resultado:

```text
Ana
Amanda
```

A expressao:

```java
name -> name.startsWith("A")
```

e um `Predicate`.

Um `Predicate` e uma funcao que recebe um valor e retorna `true` ou `false`.

### 5.2. map

`map` transforma cada elemento em outro valor.

Exemplo:

```java
List<String> names = List.of("Ana", "Bruno", "Carla");

List<String> upperNames = names.stream()
        .map(name -> name.toUpperCase())
        .toList();
```

Resultado:

```text
ANA
BRUNO
CARLA
```

Tambem podemos usar method reference:

```java
List<String> upperNames = names.stream()
        .map(String::toUpperCase)
        .toList();
```

As duas versoes fazem a mesma coisa.

### 5.3. sorted

`sorted` ordena os elementos.

Exemplo:

```java
List<String> names = List.of("Carla", "Ana", "Bruno");

List<String> orderedNames = names.stream()
        .sorted()
        .toList();
```

Resultado:

```text
Ana
Bruno
Carla
```

Para objetos, normalmente usamos `Comparator`.

Exemplo:

```java
List<GuestResponseDTO> orderedGuests = guests.stream()
        .sorted(Comparator.comparing(GuestResponseDTO::getFullName))
        .toList();
```

### 5.4. distinct

`distinct` remove valores repetidos.

Exemplo:

```java
List<String> cities = List.of("Sao Paulo", "Recife", "Sao Paulo", "Curitiba");

List<String> uniqueCities = cities.stream()
        .distinct()
        .toList();
```

Resultado:

```text
Sao Paulo
Recife
Curitiba
```

### 5.5. limit

`limit` limita a quantidade de resultados.

Exemplo:

```java
List<String> names = List.of("Ana", "Bruno", "Carla", "Daniel");

List<String> firstTwo = names.stream()
        .limit(2)
        .toList();
```

Resultado:

```text
Ana
Bruno
```

### 5.6. skip

`skip` pula uma quantidade de elementos.

Exemplo:

```java
List<String> names = List.of("Ana", "Bruno", "Carla", "Daniel");

List<String> afterFirstTwo = names.stream()
        .skip(2)
        .toList();
```

Resultado:

```text
Carla
Daniel
```

## 6. Operacoes Terminais

Operacoes terminais encerram o pipeline.

Sem uma operacao terminal, a Stream nao executa.

### 6.1. forEach

Executa uma acao para cada item.

```java
List<String> names = List.of("Ana", "Bruno", "Carla");

names.stream()
        .forEach(System.out::println);
```

`forEach` e util para imprimir, logar ou executar uma acao.

Evite usar `forEach` para montar listas manualmente. Para isso, prefira `map` com `toList`.

### 6.2. toList

Converte o resultado da Stream para lista.

```java
List<String> names = List.of("Ana", "Bruno", "Carla");

List<String> filteredNames = names.stream()
        .filter(name -> name.length() > 3)
        .toList();
```

Resultado:

```text
Bruno
Carla
```

### 6.3. count

Conta quantos elementos existem no resultado.

```java
long total = names.stream()
        .filter(name -> name.startsWith("A"))
        .count();
```

### 6.4. anyMatch

Verifica se pelo menos um item atende a condicao.

```java
boolean hasAna = names.stream()
        .anyMatch(name -> name.equals("Ana"));
```

### 6.5. allMatch

Verifica se todos os itens atendem a condicao.

```java
boolean allHaveAtLeastThreeLetters = names.stream()
        .allMatch(name -> name.length() >= 3);
```

### 6.6. noneMatch

Verifica se nenhum item atende a condicao.

```java
boolean noneIsEmpty = names.stream()
        .noneMatch(String::isBlank);
```

### 6.7. findFirst

Retorna o primeiro item encontrado.

```java
Optional<String> firstName = names.stream()
        .filter(name -> name.startsWith("A"))
        .findFirst();
```

O retorno e `Optional<String>`, porque pode nao existir nenhum resultado.

Uso seguro:

```java
String result = firstName.orElse("Nao encontrado");
```

### 6.8. reduce

`reduce` combina varios valores em um unico resultado.

Exemplo somando numeros:

```java
List<Integer> numbers = List.of(10, 20, 30);

Integer total = numbers.stream()
        .reduce(0, (subtotal, number) -> subtotal + number);
```

Resultado:

```text
60
```

Versao com method reference:

```java
Integer total = numbers.stream()
        .reduce(0, Integer::sum);
```

`reduce` e util quando voce quer transformar uma lista inteira em um unico valor.

## 7. map vs filter

Esses dois metodos sao muito usados, mas resolvem problemas diferentes.

`filter` decide se o item fica ou sai.

```java
filter(guest -> guest.getEmail() != null)
```

`map` transforma o item.

```java
map(GuestResponseDTO::new)
```

Exemplo combinado:

```java
List<GuestResponseDTO> guests = guestRepository.findAll()
        .stream()
        .filter(guest -> guest.getEmail() != null)
        .map(GuestResponseDTO::new)
        .toList();
```

Fluxo:

1. busca todos os hospedes;
2. cria uma stream;
3. remove hospedes sem email;
4. transforma cada `Guest` em `GuestResponseDTO`;
5. retorna uma lista.

## 8. Lambdas e Method References

Stream API usa bastante lambda.

Lambda:

```java
name -> name.toUpperCase()
```

Method reference:

```java
String::toUpperCase
```

Os dois exemplos acima representam a mesma transformacao.

Outro exemplo:

```java
guest -> new GuestResponseDTO(guest)
```

Pode virar:

```java
GuestResponseDTO::new
```

Use method reference quando ele deixar o codigo mais claro.

Use lambda quando a regra precisar de mais contexto.

## 9. Optional com Stream

Algumas operacoes retornam `Optional`.

Exemplo:

```java
Optional<String> first = names.stream()
        .filter(name -> name.startsWith("Z"))
        .findFirst();
```

Por que `Optional`?

Porque talvez nao exista nenhum nome com `Z`.

Formas comuns de tratar:

```java
String value = first.orElse("Nao encontrado");
```

```java
String value = first.orElseThrow(() -> new RuntimeException("Nome nao encontrado"));
```

No projeto HouseHost, esse estilo aparece quando buscamos entidade por ID:

```java
return guestRepository.findById(id)
        .orElseThrow(() -> new GuestException("Hospede nao encontrado."));
```

`findById` retorna `Optional<Guest>`.

Se existir, retorna o guest.

Se nao existir, lanca a exception.

## 10. Exemplo Real no HouseHost

No service de hospedes, existe este tipo de transformacao:

```java
List<GuestResponseDTO> guests = guestRepository.findAll()
        .stream()
        .map(GuestResponseDTO::new)
        .toList();
```

Esse codigo faz:

1. `guestRepository.findAll()` busca `List<Guest>`;
2. `.stream()` cria um fluxo sobre a lista;
3. `.map(GuestResponseDTO::new)` transforma cada `Guest` em `GuestResponseDTO`;
4. `.toList()` devolve `List<GuestResponseDTO>`.

Sem Stream API, ficaria assim:

```java
List<Guest> guestEntities = guestRepository.findAll();
List<GuestResponseDTO> guests = new ArrayList<>();

for (Guest guest : guestEntities) {
    guests.add(new GuestResponseDTO(guest));
}
```

As duas versoes funcionam.

A versao com Stream API deixa mais claro que a intencao e transformar uma lista em outra.

## 11. Exemplo com Classe Guest

Considere esta classe simplificada:

```java
public class Guest {
    private String fullName;
    private String email;
    private String phone;

    public Guest(String fullName, String email, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
```

Lista de exemplo:

```java
List<Guest> guests = List.of(
        new Guest("Ana Silva", "ana@email.com", "11999990000"),
        new Guest("Bruno Costa", null, "81999990000"),
        new Guest("Carla Mendes", "carla@email.com", null),
        new Guest("Amanda Rocha", "amanda@email.com", "41999990000")
);
```

Filtrar hospedes com email:

```java
List<Guest> guestsWithEmail = guests.stream()
        .filter(guest -> guest.getEmail() != null)
        .toList();
```

Pegar apenas os nomes:

```java
List<String> guestNames = guests.stream()
        .map(Guest::getFullName)
        .toList();
```

Filtrar nomes que comecam com `A`:

```java
List<String> namesStartingWithA = guests.stream()
        .map(Guest::getFullName)
        .filter(name -> name.startsWith("A"))
        .toList();
```

Contar hospedes sem telefone:

```java
long guestsWithoutPhone = guests.stream()
        .filter(guest -> guest.getPhone() == null)
        .count();
```

Ordenar por nome:

```java
List<Guest> orderedGuests = guests.stream()
        .sorted(Comparator.comparing(Guest::getFullName))
        .toList();
```

## 12. Erros Comuns

### 12.1. Esquecer a operacao terminal

Errado:

```java
names.stream()
        .filter(name -> name.startsWith("A"));
```

Nada acontece de verdade, porque falta uma operacao terminal.

Certo:

```java
List<String> result = names.stream()
        .filter(name -> name.startsWith("A"))
        .toList();
```

### 12.2. Usar map quando queria filter

Errado:

```java
names.stream()
        .map(name -> name.startsWith("A"))
        .toList();
```

Esse codigo gera uma lista de `Boolean`, nao uma lista de nomes.

Certo:

```java
names.stream()
        .filter(name -> name.startsWith("A"))
        .toList();
```

### 12.3. Alterar objetos dentro da stream sem necessidade

Evite usar Stream API para efeitos colaterais confusos.

Menos claro:

```java
guests.stream()
        .forEach(guest -> guest.setActive(true));
```

Se a intencao e alterar estado, muitas vezes um `for` tradicional fica mais explicito.

Stream API costuma ser melhor para:

- filtrar;
- transformar;
- agrupar;
- contar;
- reduzir;
- buscar.

## 13. Exercicios Progressivos

Use esta lista como base para os exercicios:

```java
List<String> names = List.of(
        "Ana",
        "Bruno",
        "Carla",
        "Amanda",
        "Daniel",
        "Aline"
);
```

### Exercicio 1

Imprima todos os nomes usando `forEach`.

Objetivo:

- praticar `stream()`;
- praticar `forEach`.

Resultado esperado:

```text
Ana
Bruno
Carla
Amanda
Daniel
Aline
```

### Exercicio 2

Crie uma lista apenas com nomes que comecam com a letra `A`.

Objetivo:

- praticar `filter`;
- praticar `toList`.

Resultado esperado:

```text
Ana
Amanda
Aline
```

### Exercicio 3

Crie uma lista com todos os nomes em maiusculo.

Objetivo:

- praticar `map`;
- comparar lambda com method reference.

Resultado esperado:

```text
ANA
BRUNO
CARLA
AMANDA
DANIEL
ALINE
```

### Exercicio 4

Crie uma lista com os nomes que tem mais de 5 letras, em ordem alfabetica.

Objetivo:

- combinar `filter`;
- combinar `sorted`;
- gerar uma nova lista.

Resultado esperado:

```text
Amanda
Daniel
```

### Exercicio 5

Conte quantos nomes comecam com `A`.

Objetivo:

- praticar `count`.

Resultado esperado:

```text
3
```

### Exercicio 6

Verifique se existe algum nome chamado `Carla`.

Objetivo:

- praticar `anyMatch`.

Resultado esperado:

```text
true
```

### Exercicio 7

Verifique se todos os nomes tem pelo menos 3 letras.

Objetivo:

- praticar `allMatch`.

Resultado esperado:

```text
true
```

### Exercicio 8

Busque o primeiro nome que comeca com `D`.

Objetivo:

- praticar `findFirst`;
- praticar `Optional`.

Resultado esperado:

```text
Daniel
```

### Exercicio 9

Some o tamanho de todos os nomes.

Dica:

```java
map(String::length)
```

Objetivo:

- praticar `map`;
- praticar `reduce`.

Resultado esperado:

```text
31
```

### Exercicio 10

Transforme a lista de nomes em uma unica frase separada por virgula.

Dica:

```java
Collectors.joining(", ")
```

Objetivo:

- praticar `collect`;
- conhecer `Collectors`.

Resultado esperado:

```text
Ana, Bruno, Carla, Amanda, Daniel, Aline
```

## 14. Exercicios com Guest

Use esta lista:

```java
List<Guest> guests = List.of(
        new Guest("Ana Silva", "ana@email.com", "11999990000"),
        new Guest("Bruno Costa", null, "81999990000"),
        new Guest("Carla Mendes", "carla@email.com", null),
        new Guest("Amanda Rocha", "amanda@email.com", "41999990000")
);
```

### Exercicio 11

Crie uma lista apenas com hospedes que possuem email.

Resultado esperado:

```text
Ana Silva
Carla Mendes
Amanda Rocha
```

### Exercicio 12

Crie uma lista apenas com os emails dos hospedes.

Emails nulos nao devem entrar na lista.

Resultado esperado:

```text
ana@email.com
carla@email.com
amanda@email.com
```

### Exercicio 13

Ordene os hospedes por nome completo.

Resultado esperado:

```text
Amanda Rocha
Ana Silva
Bruno Costa
Carla Mendes
```

### Exercicio 14

Conte quantos hospedes nao possuem telefone.

Resultado esperado:

```text
1
```

### Exercicio 15

Verifique se existe algum hospede com email `ana@email.com`.

Resultado esperado:

```text
true
```

### Exercicio 16

Crie uma lista de `GuestResponseDTO` a partir de uma lista de `Guest`.

Dica:

```java
map(GuestResponseDTO::new)
```

Objetivo:

- entender o uso real de Stream API dentro de services;
- evitar expor entidades diretamente na API.

## 15. Gabarito dos Exercicios

### Gabarito 1

```java
names.stream()
        .forEach(System.out::println);
```

### Gabarito 2

```java
List<String> result = names.stream()
        .filter(name -> name.startsWith("A"))
        .toList();
```

### Gabarito 3

```java
List<String> result = names.stream()
        .map(String::toUpperCase)
        .toList();
```

### Gabarito 4

```java
List<String> result = names.stream()
        .filter(name -> name.length() > 5)
        .sorted()
        .toList();
```

### Gabarito 5

```java
long total = names.stream()
        .filter(name -> name.startsWith("A"))
        .count();
```

### Gabarito 6

```java
boolean result = names.stream()
        .anyMatch(name -> name.equals("Carla"));
```

### Gabarito 7

```java
boolean result = names.stream()
        .allMatch(name -> name.length() >= 3);
```

### Gabarito 8

```java
String result = names.stream()
        .filter(name -> name.startsWith("D"))
        .findFirst()
        .orElse("Nao encontrado");
```

### Gabarito 9

```java
Integer total = names.stream()
        .map(String::length)
        .reduce(0, Integer::sum);
```

### Gabarito 10

```java
String result = names.stream()
        .collect(Collectors.joining(", "));
```

### Gabarito 11

```java
List<Guest> result = guests.stream()
        .filter(guest -> guest.getEmail() != null)
        .toList();
```

### Gabarito 12

```java
List<String> result = guests.stream()
        .map(Guest::getEmail)
        .filter(email -> email != null)
        .toList();
```

Outra forma:

```java
List<String> result = guests.stream()
        .filter(guest -> guest.getEmail() != null)
        .map(Guest::getEmail)
        .toList();
```

### Gabarito 13

```java
List<Guest> result = guests.stream()
        .sorted(Comparator.comparing(Guest::getFullName))
        .toList();
```

### Gabarito 14

```java
long total = guests.stream()
        .filter(guest -> guest.getPhone() == null)
        .count();
```

### Gabarito 15

```java
boolean result = guests.stream()
        .anyMatch(guest -> "ana@email.com".equals(guest.getEmail()));
```

### Gabarito 16

```java
List<GuestResponseDTO> result = guests.stream()
        .map(GuestResponseDTO::new)
        .toList();
```

## 16. Quando Usar Stream API

Use Stream API quando voce precisa:

- transformar uma lista em outra;
- filtrar itens;
- ordenar dados;
- contar elementos;
- buscar algum item;
- agrupar ou reduzir valores.

Exemplo bom:

```java
List<GuestResponseDTO> result = guests.stream()
        .map(GuestResponseDTO::new)
        .toList();
```

Use `for` tradicional quando:

- a regra tem muitos passos imperativos;
- voce precisa alterar muitos estados;
- a Stream deixa o codigo mais dificil de entender;
- voce precisa de `break` ou `continue` de forma clara.

Stream API nao e obrigatoria. Ela e uma ferramenta.

O melhor codigo e o que deixa a intencao mais clara.

## 17. Observacoes Sobre Performance

Para listas pequenas e medias, escolha entre `for` e Stream pensando primeiro em clareza.

Stream API nao significa automaticamente mais performance.

`parallelStream` existe, mas deve ser usado com cuidado.

Evite `parallelStream` quando:

- o processamento acessa banco de dados;
- existe alteracao de estado compartilhado;
- a lista e pequena;
- voce nao mediu ganho real;
- a ordem dos resultados importa.

Na maioria dos services web comuns, `stream()` sequencial e suficiente.

## 18. Referencias

Referencias oficiais e complementares:

- Oracle Java Tutorials - Aggregate Operations: https://docs.oracle.com/javase/tutorial/collections/streams/
- Oracle Java Tutorials - Reduction: https://docs.oracle.com/javase/tutorial/collections/streams/reduction.html
- Oracle Java API - Stream: https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html
- Baeldung - Introduction to Java Streams: https://www.baeldung.com/java-8-streams-introduction
- Baeldung - The Java Stream API Tutorial: https://www.baeldung.com/java-8-streams
