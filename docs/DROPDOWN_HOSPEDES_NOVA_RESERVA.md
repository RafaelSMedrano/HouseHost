# Dropdown de hospedes na tela de nova reserva

Este documento explica como foi adicionada a lista de sugestoes de hospedes nos campos de nome e CPF da pagina de nova reserva.

## Objetivo

Na tela **Nova reserva**, o usuario precisa selecionar um hospede ja cadastrado. Antes da alteracao, era necessario digitar manualmente o nome ou o CPF.

Agora, ao clicar no campo:

- **Nome do hospede cadastrado**
- **CPF do hospede**

o sistema mostra uma lista de hospedes existentes. Ao escolher um hospede, os dois campos sao preenchidos automaticamente.

## Arquivos alterados

### `frontend/js/views/newReservationView.js`

Este arquivo contem a estrutura HTML da tela e a logica JavaScript da nova reserva.

Alteracoes principais:

- importacao de `findAllGuests`;
- criacao dos containers dos dropdowns;
- carregamento da lista de hospedes;
- filtro por nome ou CPF;
- preenchimento automatico dos campos ao selecionar uma opcao.

### `frontend/css/home.css`

Este arquivo recebeu os estilos visuais do dropdown:

- posicionamento abaixo do input;
- altura maxima com scroll;
- card de cada hospede;
- avatar com iniciais;
- estado de hover/focus.

### `frontend/js/controllers/UICOntroller.js`

Foi atualizada a query string de import da view:

```js
v=2026-05-19-guest-lookup
```

Isso forca o navegador a buscar a versao nova do arquivo, evitando cache antigo.

## Como o HTML foi preparado

Antes, os inputs eram simples:

```html
<input id="new-reservation-guest-name" type="text">
<input id="new-reservation-guest-document" type="text">
```

Depois, cada campo passou a ter um container de sugestoes logo abaixo:

```html
<input id="new-reservation-guest-name" type="text" autocomplete="off">
<div id="new-reservation-guest-name-options" class="guest-lookup-options"></div>
```

E no CPF:

```html
<input id="new-reservation-guest-document" type="text" autocomplete="off">
<div id="new-reservation-guest-document-options" class="guest-lookup-options"></div>
```

A classe `guest-lookup-field` fica no `label` para servir como area do componente. Ela ajuda a detectar se o clique aconteceu dentro ou fora do dropdown.

## Carregamento dos hospedes

A funcao `setupGuestLookup(container)` e chamada no bind da tela:

```js
setupGuestLookup(container);
```

Ela cria um estado simples:

```js
const state = { guests: [], activeField: null };
```

Depois busca os hospedes no backend:

```js
const response = await findAllGuests();
state.guests = response.data || [];
```

Essa chamada usa a funcao ja existente em `frontend/js/api.js`:

```js
export async function findAllGuests() {
    const response = await fetch(apiUrl("/guests"));
    return parseJsonResponse(response);
}
```

## Eventos usados nos campos

Cada input recebe eventos de:

- `focus`: mostra a lista ao clicar ou navegar ate o campo;
- `click`: garante que a lista abra ao clicar novamente;
- `input`: filtra enquanto o usuario digita.

No campo de CPF, o mesmo evento tambem aplica a mascara:

```js
if (field === "document") {
    maskCpf(input);
}
```

## Como o filtro funciona

A funcao principal do filtro e `filterGuests(guests, term, field)`.

Ela compara o texto digitado com:

- nome do hospede;
- CPF/documento do hospede.

Mesmo quando o usuario esta no campo de nome, o CPF tambem e considerado. Mesmo quando esta no campo de CPF, o nome tambem e considerado.

Isso deixa a busca mais tolerante.

Exemplo:

```js
return primary.includes(term) || name.includes(term) || documentNumber.includes(term);
```

O resultado e limitado aos 8 primeiros hospedes:

```js
const guests = filterGuests(state.guests, term, field).slice(0, 8);
```

## Como a selecao preenche os campos

Cada item do dropdown e um botao:

```html
<button type="button" class="guest-lookup-option" data-guest-id="...">
```

Ao clicar, o sistema encontra o hospede pelo `id`:

```js
const guest = state.guests.find((item) => String(item.id) === button.dataset.guestId);
```

Depois chama:

```js
selectGuest(container, guest);
```

Essa funcao preenche os dois inputs:

```js
container.querySelector("#new-reservation-guest-name").value = guest.fullName || "";
documentInput.value = guest.documentNumber || "";
maskCpf(documentInput);
```

Por fim, fecha o dropdown:

```js
hideGuestLookupOptions(container);
```

## Estados vazios

Existem duas mensagens simples:

### Nenhum hospede carregado

Aparece quando a lista ainda esta vazia ou a requisicao falhou:

```js
Nenhum hospede carregado.
```

### Nenhum hospede encontrado

Aparece quando ha hospedes carregados, mas nenhum bate com o texto digitado:

```js
Nenhum hospede encontrado.
```

## Como o dropdown fecha

Foi adicionado um listener de clique no documento:

```js
document.addEventListener("click", (event) => {
    if (!container.contains(event.target) || !event.target.closest(".guest-lookup-field")) {
        hideGuestLookupOptions(container);
    }
});
```

Assim, se o usuario clicar fora do campo de busca, as sugestoes somem.

## CSS do dropdown

A classe principal e:

```css
.guest-lookup-options
```

Ela usa `position: absolute`, ficando logo abaixo do input:

```css
top: calc(100% + 6px);
left: 0;
right: 0;
```

O dropdown nao empurra o formulario para baixo. Ele fica sobreposto, com `z-index: 30`.

Cada opcao usa:

```css
.guest-lookup-option
```

E o avatar usa:

```css
.guest-lookup-avatar
```

As iniciais sao geradas pela funcao:

```js
initialsFor(guest.fullName)
```

## Case: dropdown cortado dentro do card

### Problema observado

Depois que o dropdown foi adicionado, ele aparecia limitado ao espaco da primeira secao da reserva.

Na pratica, ao clicar no input de hospede, a lista ate era renderizada, mas ficava escondida ou cortada dentro da area de:

```css
.guest-reservation-row
```

ou da propria secao:

```css
.booking-section
```

### Causa

O dropdown foi criado com:

```css
position: absolute;
```

Isso faz a lista flutuar em relacao ao campo. Porem, um elemento pai tinha:

```css
overflow: hidden;
```

No caso da tela, a regra estava em:

```css
.booking-section {
    overflow: hidden;
}
```

Esse `overflow: hidden` e util para manter o card visualmente limpo, respeitando bordas arredondadas e evitando que conteudos internos vazem.

Mas ele tambem corta filhos posicionados de forma absoluta. Por isso o dropdown nao conseguia aparecer para fora da altura da secao.

### Solucao aplicada

A solucao foi manter o comportamento original da secao fechada, mas liberar o overflow apenas quando o dropdown estiver aberto.

No CSS, a secao passou a ter `position: relative`:

```css
.booking-section {
    position: relative;
    overflow: hidden;
}
```

Depois foi criada uma classe temporaria:

```css
.booking-section.lookup-open {
    z-index: 35;
    overflow: visible;
}
```

Essa classe faz duas coisas:

- `overflow: visible`: permite que o dropdown apareca para fora do card;
- `z-index: 35`: coloca a secao acima das proximas secoes enquanto a lista esta aberta.

No JavaScript, quando o dropdown abre:

```js
function showGuestLookupOptions(container, field) {
    hideGuestLookupOptions(container);
    const options = container.querySelector(`#new-reservation-guest-${field}-options`);
    options.classList.add("show");
    options.closest(".booking-section")?.classList.add("lookup-open");
}
```

Quando o dropdown fecha:

```js
function hideGuestLookupOptions(container) {
    container.querySelectorAll(".guest-lookup-options").forEach((options) => options.classList.remove("show"));
    container.querySelectorAll(".booking-section.lookup-open").forEach((section) => section.classList.remove("lookup-open"));
}
```

### Por que nao remover simplesmente o `overflow: hidden`?

Remover `overflow: hidden` de `.booking-section` resolveria o dropdown, mas poderia alterar visualmente outros pontos da tela.

Como `.booking-section` e usada por varias secoes da reserva, a mudanca global poderia permitir que outros conteudos vazassem dos cards.

Por isso a solucao mais controlada foi:

1. manter `overflow: hidden` como padrao;
2. adicionar `overflow: visible` apenas durante o uso do dropdown;
3. remover a classe ao fechar a lista.

### Outras solucoes possiveis

#### 1. Portal no final do `body`

Uma solucao mais robusta seria renderizar o dropdown fora da estrutura do card, diretamente no final do `body`.

Exemplo conceitual:

```html
<body>
  <main>...</main>
  <div id="floating-dropdown-root"></div>
</body>
```

Nesse caso, o JavaScript calcularia a posicao do input com `getBoundingClientRect()` e posicionaria a lista manualmente.

Vantagem:

- evita cortes por `overflow` em qualquer pai.

Desvantagem:

- exige mais codigo;
- precisa recalcular posicao em scroll e resize;
- e mais complexo para uma tela simples.

#### 2. Transformar a secao inteira em `overflow: visible`

Outra opcao seria:

```css
.booking-section {
    overflow: visible;
}
```

Vantagem:

- simples.

Desvantagem:

- afeta todos os cards;
- pode quebrar bordas arredondadas;
- pode permitir vazamento visual de outros elementos.

#### 3. Aumentar a altura da secao

Tambem seria possivel aumentar a altura da secao para caber o dropdown.

Vantagem:

- nao precisa mexer em z-index.

Desvantagem:

- empurra o formulario para baixo;
- deixa a interface instavel;
- nao resolve bem quando a lista tem muitos itens.

### Solucao escolhida

A solucao escolhida foi a classe temporaria:

```css
.booking-section.lookup-open
```

Ela e simples, local e preserva o comportamento visual original da tela.

## Resultado final

Com essa alteracao, o fluxo de nova reserva ficou assim:

1. O usuario abre a tela de nova reserva.
2. O frontend carrega os hospedes cadastrados.
3. O usuario clica no campo de nome ou CPF.
4. A lista de hospedes aparece.
5. O usuario digita para filtrar, se quiser.
6. O usuario seleciona um hospede.
7. Nome e CPF sao preenchidos automaticamente.
8. O envio da reserva continua usando o mesmo payload de antes.

## Observacao importante

O backend nao precisou mudar para essa funcionalidade. A tela continua enviando:

```js
guest: {
    fullName: "...",
    documentNumber: "..."
}
```

Ou seja, o autocomplete melhora a experiencia do usuario, mas nao altera o contrato do endpoint `/bookings/form`.
