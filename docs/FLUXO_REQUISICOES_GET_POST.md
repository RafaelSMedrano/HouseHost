# Fluxo de Requisicoes GET, POST, PUT e DELETE

Este documento explica a diferenca entre os fluxos de leitura e escrita no frontend do HouseHost.

Usaremos exemplos reais do projeto:

- lista de hospedes;
- formulario de hospede;
- lista de reservas;
- formulario de nova reserva.

## Ideia principal

Existem dois tipos grandes de fluxo:

```txt
GET
```

Busca dados no backend para mostrar na tela.

```txt
POST / PUT / DELETE
```

Envia dados ou comandos para o backend alterar alguma coisa.

## GET: fluxo de leitura

`GET` e usado quando a tela precisa buscar dados existentes.

Exemplos:

```txt
GET /guests
GET /bookings
```

Essas requisicoes nao nascem de um formulario sendo enviado. Elas normalmente nascem quando uma tela e aberta.

### Exemplo: lista de hospedes

Quando o usuario clica em `Hospedes` na sidebar, a aplicacao chama:

```js
renderGuestsPanel()
```

No `UICOntroller`, essa funcao renderiza a lista:

```js
renderGuestsView("main-pannel-container", ...)
```

Dentro de `guestsView.js`, a view chama:

```js
loadGuests(container, state, options)
```

Essa funcao faz:

```js
const response = await findAllGuests();
state.guests = response.data || [];
```

`findAllGuests()` fica em:

```txt
frontend/js/api.js
```

e executa:

```js
fetch(apiUrl("/guests"))
```

Ou seja:

```txt
Sidebar Hospedes
-> renderGuestsPanel()
-> renderGuestsView()
-> bindGuestsView()
-> loadGuests()
-> findAllGuests()
-> fetch GET /guests
-> state.guests = response.data
-> renderGuestsTable()
```

### Onde os dados ficam no GET?

No caso da lista de hospedes, os dados retornados pela API sao salvos em:

```js
state.guests
```

Esse `state` e a memoria temporaria da tela.

Depois a tabela e renderizada com:

```js
renderGuestsTable(container, state, options)
```

### Exemplo: lista de reservas

Reservas seguem o mesmo modelo.

Quando a tela de reservas abre:

```js
renderReservationsView("main-pannel-container")
```

Depois:

```js
loadReservations(container, state)
```

Essa funcao chama:

```js
const response = await findAllBookings();
state.reservations = (response.data || []).map(normalizeBooking);
```

`findAllBookings()` faz:

```txt
GET /bookings
```

Fluxo:

```txt
Sidebar Reservas
-> renderReservationsPanel()
-> renderReservationsView()
-> bindReservationsView()
-> loadReservations()
-> findAllBookings()
-> fetch GET /bookings
-> normalizeBooking()
-> state.reservations
-> renderTable()
-> renderCalendar()
```

### Por que existe normalizeBooking?

O backend devolve reservas no formato do DTO Java.

Exemplo:

```js
{
    id: 1,
    guestName: "Maria",
    roomNumber: "Suite Lavanda",
    checkInDate: "2026-05-20",
    checkOutDate: "2026-05-22",
    status: "CONFIRMED",
    totalAmount: 760
}
```

A tabela do frontend espera outro formato:

```js
{
    id: "#001",
    guest: "Maria",
    room: "Suite Lavanda",
    checkin: "2026-05-20",
    checkout: "2026-05-22",
    status: "confirmada",
    total: 760
}
```

Por isso existe:

```js
normalizeBooking(booking)
```

Ela adapta o dado do backend para o formato visual da tela.

## POST: fluxo de criacao

`POST` e usado quando o usuario cria algo novo.

Exemplos:

```txt
POST /guests
POST /bookings/form
```

Esse fluxo normalmente nasce de um formulario.

## Fluxo POST do formulario de hospede

### 1. A tela renderiza o formulario

O controller chama:

```js
renderGuestFormView("main-pannel-container", options)
```

A view coloca o HTML dentro do container:

```js
container.innerHTML = `... formulario ...`;
```

Depois liga os eventos:

```js
bindGuestForm(container, options)
```

### 2. O usuario preenche os campos

Enquanto o usuario digita, os valores ficam nos proprios elementos HTML.

Exemplo:

```html
<input id="guest-fullName">
```

O valor fica em:

```js
container.querySelector("#guest-fullName").value
```

O `container` nao guarda o valor. Ele so ajuda a encontrar o input.

### 3. O submit dispara

O submit e ligado assim:

```js
form.addEventListener("submit", (event) => handleSubmit(event, container, options, state));
```

Quando o usuario clica em `Salvar hospede`, roda:

```js
handleSubmit(...)
```

### 4. O navegador e impedido de recarregar

Dentro do submit:

```js
event.preventDefault();
```

Isso impede o submit tradicional do HTML.

Sem isso, o navegador recarregaria a pagina.

### 5. O payload e montado

Depois:

```js
const payload = collectPayload(container, state);
```

Esse e o primeiro momento em que o formulario inteiro vira uma variavel JavaScript.

`payload` e o objeto que sera enviado para a API.

Exemplo:

```js
{
    fullName: "Maria Fernanda",
    phone: "(11) 99999-9999",
    email: "maria@email.com",
    documentNumber: "123.456.789-00",
    preferences: ["Sem lactose"],
    rating: 5
}
```

### 6. O payload vai para api.js

Se for novo hospede:

```js
createGuest(payload)
```

Em `api.js`:

```js
fetch(apiUrl("/guests"), {
    method: "POST",
    headers: {
        "Content-Type": "application/json"
    },
    body: JSON.stringify(guest)
});
```

Fluxo completo:

```txt
Formulario hospede
-> submit
-> handleSubmit()
-> collectPayload()
-> createGuest(payload)
-> fetch POST /guests
-> GuestController.create()
-> GuestService.create()
-> banco
```

## PUT: fluxo de edicao

`PUT` e usado quando o usuario edita algo que ja existe.

No formulario de hospede, a diferenca esta no `guestId`.

Se existe `guestId`, o submit chama:

```js
updateGuest(state.guestId, payload)
```

Em `api.js`:

```js
fetch(apiUrl(`/guests/${id}`), {
    method: "PUT",
    headers: {
        "Content-Type": "application/json"
    },
    body: JSON.stringify(guest)
});
```

Fluxo:

```txt
Formulario hospede em edicao
-> submit
-> handleSubmit()
-> collectPayload()
-> updateGuest(id, payload)
-> fetch PUT /guests/{id}
-> GuestController.update()
-> GuestService.update()
-> banco
```

## DELETE: fluxo de remocao

`DELETE` e usado para remover um registro.

Na lista de hospedes:

```js
handleDeleteGuest(container, state, options, guestId)
```

chama:

```js
deleteGuest(guestId)
```

Em `api.js`:

```js
fetch(apiUrl(`/guests/${id}`), {
    method: "DELETE"
});
```

Depois que remove no backend, a lista local tambem e atualizada:

```js
state.guests = state.guests.filter((guest) => guest.id !== guestId);
renderGuestsTable(container, state, options);
```

Fluxo:

```txt
Botao remover hospede
-> handleDeleteGuest()
-> deleteGuest(id)
-> fetch DELETE /guests/{id}
-> GuestController.delete()
-> GuestService.delete()
-> banco
-> remove do state.guests
-> renderGuestsTable()
```

## POST da nova reserva

O formulario de reserva tambem usa `POST`, mas o payload e mais agrupado.

O submit chama:

```js
handleReservationSubmit(event, container, counts, options)
```

Depois:

```js
const payload = collectReservationPayload(container, counts);
```

Formato do payload:

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

Depois:

```js
createBookingFromForm(payload)
```

Em `api.js`:

```js
fetch(apiUrl("/bookings/form"), {
    method: "POST",
    headers: {
        "Content-Type": "application/json"
    },
    body: JSON.stringify(booking)
});
```

Backend:

```txt
BookingController.createFromForm()
-> BookingService.createFromForm()
```

Fluxo:

```txt
Formulario nova reserva
-> submit
-> handleReservationSubmit()
-> collectReservationPayload()
-> createBookingFromForm(payload)
-> fetch POST /bookings/form
-> BookingController.createFromForm()
-> BookingService.createFromForm()
-> banco
```

## Depois de salvar, por que voltamos para a lista?

Depois de criar hospede ou reserva, o controller chama uma tela de lista novamente.

Exemplo reserva:

```js
onSaved: () => renderReservationsPanel()
```

Isso faz:

```txt
POST /bookings/form
-> sucesso
-> renderReservationsPanel()
-> GET /bookings
-> lista atualizada
```

Esse padrao garante que a tela mostre o que esta no banco, nao apenas o que estava na memoria local.

## Diferenca principal entre GET e POST

### GET

Usado para buscar dados.

Nao tem formulario.

Nao tem `collectPayload`.

Exemplo:

```txt
GET /guests
```

Fluxo:

```txt
abrir tela
-> loadGuests()
-> findAllGuests()
-> fetch GET /guests
-> response.data
-> state.guests
-> renderGuestsTable()
```

### POST

Usado para criar dados.

Normalmente vem de formulario.

Tem `collectPayload`.

Exemplo:

```txt
POST /guests
```

Fluxo:

```txt
submit
-> handleSubmit()
-> collectPayload()
-> createGuest(payload)
-> fetch POST /guests
-> backend salva
```

## Tabela comparativa

| Metodo | Objetivo | Envia body? | Exemplo | Fluxo |
|---|---|---:|---|---|
| GET | Buscar dados | Nao | `/guests` | API -> state -> tela |
| POST | Criar dados | Sim | `/guests` | formulario -> payload -> API |
| PUT | Atualizar dados | Sim | `/guests/{id}` | formulario -> payload -> API |
| DELETE | Remover dados | Nao geralmente | `/guests/{id}` | acao -> API -> atualiza tela |

## Resumo mental

Para listas:

```txt
GET
-> response.data
-> state
-> render
```

Para formularios:

```txt
submit
-> collectPayload
-> POST/PUT
-> backend
-> voltar para lista
-> GET
-> render atualizado
```

