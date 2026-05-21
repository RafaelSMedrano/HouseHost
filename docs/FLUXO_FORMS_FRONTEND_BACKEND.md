# Fluxo dos Formularios no HouseHost

Este documento explica o caminho que os dados percorrem quando um formulario e usado no frontend ate chegar na API do backend.

Os dois fluxos principais implementados sao:

- cadastro/edicao de hospede;
- criacao de nova reserva.

## Visao geral

O frontend funciona como SPA.

Isso significa que a pagina inteira nao e recarregada a cada navegacao. O `UICOntroller` troca o conteudo dentro do painel principal:

```txt
sidebar/topbar
-> UICOntroller
-> view especifica
-> formulario
-> submit
-> payload
-> api.js
-> fetch
-> controller Java
-> service Java
-> banco
```

O `container` usado nas views normalmente e o elemento:

```txt
main-pannel-container
```

Ele nao guarda os valores dos campos. Ele apenas contem o HTML da tela. Os valores ficam nos proprios inputs, selects, textareas e checkboxes.

Exemplo:

```js
container.querySelector("#guest-fullName").value
```

Aqui o `container` serve para encontrar o input. O valor esta no input.

## Fluxo do formulario de hospede

### 1. A tela e aberta

O menu `Hospedes` da sidebar abre a lista de hospedes.

Arquivo:

```txt
frontend/js/controllers/UICOntroller.js
```

Fluxo:

```js
renderGuestsPanel()
```

Essa funcao renderiza:

```js
renderGuestsView("main-pannel-container", ...)
```

A lista de hospedes fica em:

```txt
frontend/js/views/guestsView.js
```

Ela busca dados reais usando:

```js
findAllGuests()
```

em:

```txt
frontend/js/api.js
```

que chama:

```txt
GET /guests
```

### 2. O formulario e aberto

O formulario de hospede nao e aberto diretamente pela sidebar.

Ele e aberto por botoes como:

- `Novo hospede` na topbar;
- `Novo hospede` na lista de hospedes;
- `Cadastrar hospede` dentro da tela de nova reserva;
- botao de editar na lista de hospedes.

O `UICOntroller` chama:

```js
renderGuestFormPanel()
```

que por sua vez chama:

```js
renderGuestFormView("main-pannel-container", {
    guestId,
    onCancel,
    onSaved,
    onDeleted,
});
```

Arquivo:

```txt
frontend/js/views/guestFormView.js
```

### 3. A view cria o HTML

A funcao:

```js
renderGuestFormView(containerId, options)
```

procura o container:

```js
const container = document.getElementById(containerId);
```

e coloca o HTML do formulario dentro dele:

```js
container.innerHTML = `... formulario ...`;
```

Depois chama:

```js
bindGuestForm(container, { ...options, guestId });
```

### 4. O bind liga os eventos

Depois que o HTML existe no DOM, a view consegue buscar os elementos e conectar eventos.

Isso e feito em:

```js
bindGuestForm(container, options)
```

Essa funcao cria um estado local:

```js
const state = {
    preferences: [],
    rating: 0,
    guestId: options.guestId || null
};
```

Esse `state` existe porque nem tudo no formulario e um input simples.

- `preferences`: chips de preferencias;
- `rating`: estrelas de avaliacao;
- `guestId`: id usado para decidir entre criar ou atualizar.

Exemplo:

```txt
guestId null -> POST /guests
guestId 10   -> PUT /guests/10
```

O bind tambem liga o submit:

```js
form.addEventListener("submit", (event) => handleSubmit(event, container, options, state));
```

### 5. O submit acontece

Quando o usuario clica em `Salvar hospede`, o navegador dispara o evento `submit`.

Esse evento chega em:

```js
handleSubmit(event, container, options, state)
```

O primeiro passo e:

```js
event.preventDefault();
```

Isso impede o comportamento tradicional do formulario HTML, que recarregaria a pagina.

Depois o payload e montado:

```js
const payload = collectPayload(container, state);
```

### 6. O payload e montado

A funcao:

```js
collectPayload(container, state)
```

le os campos do formulario e monta um objeto JavaScript.

Exemplo simplificado:

```js
{
    fullName: value(container, "fullName"),
    documentNumber: value(container, "documentNumber"),
    phone: value(container, "phone"),
    email: value(container, "email"),
    travelsWithPets: checked(container, "travelsWithPets"),
    rating: state.rating,
    preferences: state.preferences
}
```

Ela usa helpers:

```js
value(container, "fullName")
```

que busca:

```js
container.querySelector("#guest-fullName").value
```

Tambem usa:

```js
checked(container, "travelsWithPets")
```

para checkboxes.

E:

```js
numberValue(container, "stayCount")
```

para converter campos numericos.

### 7. O payload vai para api.js

Depois de montar o payload, `handleSubmit` decide se vai criar ou atualizar:

```js
const response = state.guestId
    ? await updateGuest(state.guestId, payload)
    : await createGuest(payload);
```

Essas funcoes ficam em:

```txt
frontend/js/api.js
```

Criacao:

```js
createGuest(payload)
```

chama:

```txt
POST /guests
```

Edicao:

```js
updateGuest(id, payload)
```

chama:

```txt
PUT /guests/{id}
```

### 8. O backend recebe

O backend recebe em:

```txt
src/main/java/com/househost/guest/controller/GuestController.java
```

Criacao:

```java
@PostMapping
public ResponseDTO create(@RequestBody GuestRequestDTO request)
```

Edicao:

```java
@PutMapping("/{id}")
public ResponseDTO update(@PathVariable Long id, @RequestBody GuestRequestDTO request)
```

O service processa em:

```txt
src/main/java/com/househost/guest/service/GuestService.java
```

Ele valida, normaliza, salva e devolve um `ResponseDTO`.

## Fluxo do formulario de reserva

### 1. A tela e aberta

A tela de nova reserva e aberta pelo botao `Nova reserva` na topbar ou por botoes da lista.

O `UICOntroller` chama:

```js
renderNewReservationPanel()
```

que chama:

```js
renderNewReservationView("main-pannel-container", ...)
```

Arquivo:

```txt
frontend/js/views/newReservationView.js
```

### 2. A view cria o HTML

Assim como no formulario de hospede, a view coloca o formulario no container:

```js
container.innerHTML = `... formulario de reserva ...`;
```

Depois chama:

```js
bindNewReservationView(container, options);
```

### 3. O bind liga os eventos

Na reserva, o bind cria dois objetos:

```js
const counts = { adultos: 2, criancas: 0, pets: 0 };
const mins = { adultos: 1, criancas: 0, pets: 0 };
```

`counts` e um estado local dos contadores.

Ele existe porque adultos, criancas e pets sao controlados por botoes `+` e `-`, nao por inputs comuns.

O bind tambem liga:

- botao cancelar;
- botao cadastrar hospede;
- mascara de CPF;
- abrir/fechar secoes;
- selecao de quarto;
- preview de noites e total;
- submit do formulario.

O submit e ligado aqui:

```js
container.querySelector("#new-reservation-form")
    .addEventListener("submit", (event) => handleReservationSubmit(event, container, counts, options));
```

### 4. O submit da reserva

Quando o usuario clica em `Confirmar reserva`, roda:

```js
handleReservationSubmit(event, container, counts, options)
```

Primeiro:

```js
event.preventDefault();
```

Depois:

```js
const payload = collectReservationPayload(container, counts);
```

### 5. O payload da reserva

A funcao:

```js
collectReservationPayload(container, counts)
```

monta um objeto no formato esperado pelo backend.

Formato:

```js
{
    guest: {
        fullName,
        documentNumber
    },
    reservation: {
        roomCode,
        checkInDate,
        checkOutDate,
        adults,
        children,
        pets
    },
    payment: {
        paymentMethod,
        installments,
        dailyRate,
        discount,
        paidAmount,
        paymentDate
    },
    origin,
    status,
    specialRequests,
    internalNotes
}
```

Campos de texto sao lidos com:

```js
value(container, "#checkin")
```

Radios sao lidos com:

```js
checkedValue(container, 'input[name="quarto"]')
```

Numeros sao lidos com:

```js
numberValue(container, "#valorDiaria")
```

Contadores vem de:

```js
counts.adultos
counts.criancas
counts.pets
```

### 6. O payload vai para api.js

Depois de montar o payload:

```js
const response = await createBookingFromForm(payload);
```

Arquivo:

```txt
frontend/js/api.js
```

A funcao:

```js
createBookingFromForm(booking)
```

faz:

```txt
POST /bookings/form
```

com:

```js
body: JSON.stringify(booking)
```

### 7. O backend recebe a reserva

O endpoint fica em:

```txt
src/main/java/com/househost/booking/controller/BookingController.java
```

Metodo:

```java
@PostMapping("/form")
public ResponseDTO createFromForm(@RequestBody BookingFormCreateRequestDTO request)
```

O DTO esperado e:

```txt
src/main/java/com/househost/booking/dto/BookingFormCreateRequestDTO.java
```

Ele tem:

```java
public GuestData guest;
public ReservationData reservation;
public PaymentData payment;
public String origin;
public String status;
public String specialRequests;
public String internalNotes;
```

O service processa em:

```txt
src/main/java/com/househost/booking/service/BookingService.java
```

Metodo:

```java
createFromForm(...)
```

Ele faz:

- valida o request;
- busca hospede existente por CPF ou nome;
- busca quarto por `roomCode`;
- interpreta status;
- valida disponibilidade do quarto;
- calcula total;
- salva a reserva;
- cria transacao financeira quando houver pagamento;
- retorna `ResponseDTO`.

## Diferenca entre lista e formulario de hospede

O menu da sidebar:

```txt
Hospedes
```

abre a lista:

```js
renderGuestsPanel()
```

A lista usa:

```js
renderGuestsView()
```

Ja o formulario e aberto por acoes especificas:

```txt
Novo hospede
Editar hospede
Cadastrar hospede na reserva
```

Essas acoes chamam:

```js
renderGuestFormPanel()
```

Essa separacao e importante porque:

- sidebar representa navegacao de modulo;
- botoes representam acoes dentro do modulo;
- lista e formulario sao telas diferentes.

## querySelector neste fluxo

`querySelector` e usado para encontrar elementos HTML dentro do container.

Exemplo:

```js
container.querySelector("#guest-fullName")
```

procura o input:

```html
<input id="guest-fullName">
```

O container nao guarda o valor. O valor fica no input:

```js
container.querySelector("#guest-fullName").value
```

Usar `container.querySelector` evita procurar na pagina inteira e limita a busca a tela atual.

## Onde os dados ficam antes do submit

Enquanto o usuario digita:

- inputs guardam texto em `.value`;
- selects guardam opcao em `.value`;
- checkboxes guardam boolean em `.checked`;
- estados locais guardam dados que nao sao inputs simples.

No formulario de hospede:

```js
state.preferences
state.rating
state.guestId
```

No formulario de reserva:

```js
counts.adultos
counts.criancas
counts.pets
```

O objeto completo so nasce no submit:

```js
const payload = collectPayload(...)
```

ou:

```js
const payload = collectReservationPayload(...)
```

## Resumo final

Hospede:

```txt
renderGuestFormView
-> bindGuestForm
-> submit
-> handleSubmit
-> collectPayload
-> createGuest/updateGuest
-> fetch
-> GuestController
-> GuestService
```

Reserva:

```txt
renderNewReservationView
-> bindNewReservationView
-> submit
-> handleReservationSubmit
-> collectReservationPayload
-> createBookingFromForm
-> fetch
-> BookingController
-> BookingService
```

