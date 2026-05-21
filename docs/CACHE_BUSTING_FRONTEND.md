# Cache Busting no Frontend

## O problema

Quando o navegador baixa um arquivo JavaScript ou CSS, ele pode guardar esse arquivo em cache.

Exemplo:

```html
<script type="module" src="js/controllers/main.js"></script>
```

Se o usuario acessa o site hoje, o navegador baixa:

```text
js/controllers/main.js
```

Depois, se voce altera esse arquivo no servidor, o navegador do usuario pode continuar usando a versao antiga que ja estava salva localmente.

Isso pode durar minutos, horas, dias ou mais, dependendo das regras de cache.

## O que e cache

Cache e uma copia local de um arquivo.

O navegador faz isso para o site carregar mais rapido.

Em vez de baixar de novo:

```text
home.css
api.js
main.js
```

ele pode reaproveitar o que ja tem salvo.

Isso e bom para performance, mas pode atrapalhar quando voce publica uma versao nova do frontend.

## O que e cache busting

Cache busting e uma tecnica para forcar o navegador a baixar uma versao nova do arquivo.

A ideia e mudar a URL do arquivo.

Antes:

```js
import { login } from "../api.js";
```

Depois:

```js
import { login } from "../api.js?v=2026-05-17-01";
```

O arquivo fisico continua sendo:

```text
api.js
```

Mas a URL vista pelo navegador mudou:

```text
api.js?v=2026-05-17-01
```

Para o navegador, estas duas URLs sao diferentes:

```text
api.js?v=2026-05-16-02
api.js?v=2026-05-17-01
```

Entao ele baixa novamente.

## O `v=` executa alguma coisa?

Nao.

O `v=` e apenas um parametro de URL, chamado query string.

Exemplo:

```text
api.js?v=2026-05-17-01
```

O JavaScript nao interpreta esse valor.

O navegador tambem nao considera `v` uma palavra especial.

Poderia ser:

```text
api.js?v=1
api.js?v=2
api.js?versao=abc
api.js?cache=novo
```

O importante e a URL mudar.

## Por que usamos data no `v=`

No projeto, usamos algo como:

```text
2026-05-17-01
```

Isso significa:

```text
ano-mes-dia-versao-do-dia
```

Exemplo:

```html
<script type="module" src="js/controllers/main.js?v=2026-05-17-01"></script>
```

Se fizermos outra alteracao no mesmo dia, podemos usar:

```text
2026-05-17-02
```

Isso facilita saber quando aquele arquivo foi atualizado.

## Quando o navegador nao baixa de novo

Se a URL continuar igual, o navegador pode usar o cache.

Exemplo:

```js
import { login } from "../api.js?v=2026-05-17-01";
```

Se voce altera `api.js`, mas deixa:

```js
import { login } from "../api.js?v=2026-05-17-01";
```

o navegador pode continuar usando a versao antiga.

Para forcar baixar de novo, voce muda para:

```js
import { login } from "../api.js?v=2026-05-17-02";
```

Regra pratica:

```text
URL igual     -> pode usar cache
URL diferente -> baixa de novo
```

## Onde isso aparece no nosso projeto

No `index.html`:

```html
<link rel="stylesheet" href="css/home.css?v=2026-05-17-01">
<script type="module" src="js/controllers/main.js?v=2026-05-17-01"></script>
```

Nos imports JavaScript:

```js
import { login } from "../api.js?v=2026-05-17-01";
```

Isso foi feito porque os modulos JavaScript tambem podem ficar em cache.

## Exemplo do problema que tivemos

Antes, o `api.js` fazia chamadas relativas:

```js
fetch("/auth/login")
```

Quando o frontend local rodava em:

```text
http://localhost:8000
```

o navegador chamava:

```text
http://localhost:8000/auth/login
```

Mas o backend Spring Boot estava em:

```text
http://localhost:8080
```

Entao corrigimos o `api.js`.

Depois da correcao, tambem mudamos o import:

```js
import { login } from "../api.js?v=2026-05-17-01";
```

Sem isso, o navegador poderia continuar usando o `api.js` antigo em cache.

## Comparacao simples

### Sem cache busting

```html
<script src="app.js"></script>
```

Se o usuario ja tem `app.js` salvo, ele pode nao baixar a versao nova.

### Com cache busting

```html
<script src="app.js?v=2"></script>
```

Agora a URL mudou.

O navegador baixa de novo.

## Como projetos maiores fazem isso

Projetos com ferramentas como Vite, Webpack, Angular, React ou Vue normalmente geram arquivos com hash.

Exemplo:

```text
app.8f3a91.js
style.7ac22.css
```

Quando o conteudo muda, o nome do arquivo muda.

Isso tambem quebra o cache.

No nosso frontend estatico, estamos fazendo manualmente com:

```text
?v=2026-05-17-01
```

## Boas praticas no nosso projeto

Sempre que alterar CSS ou JS usado pelo navegador, atualize o `v=`.

Exemplo:

```text
2026-05-17-01
2026-05-17-02
2026-05-17-03
```

Se alterar `api.js`, atualize tambem os imports que apontam para ele.

Exemplo:

```js
import { login } from "../api.js?v=2026-05-17-02";
```

Se alterar `main.js`, atualize o `index.html`:

```html
<script type="module" src="js/controllers/main.js?v=2026-05-17-02"></script>
```

## Resumo

Cache busting serve para evitar que o usuario continue usando arquivos antigos depois de uma atualizacao.

O `?v=` nao executa nada.

Ele apenas muda a URL.

Quando a URL muda, o navegador baixa o arquivo novamente.
