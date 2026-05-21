# Frontend Estatico Fora do JAR

Este documento explica como separar o frontend estatico da compilacao do `.jar` da aplicacao Spring Boot.

A ideia e permitir atualizar HTML, CSS e JavaScript sem precisar rebuildar o backend Java.

## Estrutura Atual do Projeto

Os arquivos do frontend agora ficam fora de `src/main/resources`, em:

```text
frontend/
```

Exemplo:

```text
frontend/index.html
frontend/css/loginWidget.css
frontend/css/home.css
frontend/js/api.js
frontend/js/controllers/main.js
frontend/js/widgets/loginWidget.js
frontend/js/views/homeView.js
```

Antes, esses arquivos ficavam em `src/main/resources/static`. Quando o Maven gerava o `.jar`, ele copiava o frontend para dentro do pacote final.

Agora o fluxo fica separado:

```text
Codigo Java
application.properties
        |
        v
./mvnw clean package
        |
        v
target/househost-0.0.1-SNAPSHOT.jar

frontend/
        |
        v
Nginx /var/www/cantinho-das-lavandas
```

Isso significa que o `.jar` contem:

- classes Java compiladas;
- configuracoes;

E nao precisa conter:

- `index.html`;
- CSS;
- JavaScript.

Na pratica, se voce altera apenas um CSS, por exemplo:

```text
frontend/css/home.css
```

o arquivo novo pode ir para producao copiando apenas a pasta `frontend/`.

Nao precisa gerar outro `.jar` por causa de uma mudanca de frontend.

## Objetivo da Separacao

Queremos que o backend Java e o frontend estatico tenham ciclos de deploy diferentes.

Depois da separacao:

```text
Backend Java mudou
        |
        v
Rebuild do JAR + restart do systemd

Frontend HTML/CSS/JS mudou
        |
        v
Copiar arquivos estaticos para o Nginx
```

Ou seja:

- mudou Java: rebuilda `.jar`;
- mudou `pom.xml`: rebuilda `.jar`;
- mudou entidade JPA: rebuilda `.jar`;
- mudou Service/Controller/Repository: rebuilda `.jar`;
- mudou HTML/CSS/JS: nao precisa rebuildar `.jar`;
- mudou imagem/font asset: nao precisa rebuildar `.jar`.

## Conceito: Backend e Frontend

Neste projeto, o backend e o Spring Boot.

Ele e responsavel por:

- autenticar usuario;
- consultar banco;
- salvar reservas;
- salvar estadias;
- aplicar regras de negocio;
- expor APIs REST.

Exemplos de endpoints backend:

```text
POST /auth/login
GET  /rooms
POST /bookings
GET  /stays
```

O frontend estatico e composto por arquivos que rodam no navegador:

```text
index.html
css/*.css
js/*.js
```

Ele e responsavel por:

- desenhar a tela;
- capturar clique do usuario;
- chamar a API;
- mostrar os dados retornados;
- aplicar comportamento visual.

## Conceito: Arquivo Estatico

Arquivo estatico e um arquivo que o servidor entrega como esta.

Exemplos:

```text
HTML
CSS
JavaScript
PNG
JPG
SVG
ICO
WOFF
```

O servidor nao precisa compilar Java para entregar esses arquivos.

Quando o navegador acessa:

```text
/css/home.css
```

o servidor pode simplesmente procurar:

```text
/var/www/cantinho-das-lavandas/css/home.css
```

e devolver o conteudo.

## Conceito: Nginx Servindo Frontend

Nginx e um servidor web muito usado para servir arquivos estaticos e fazer proxy reverso.

Depois da separacao, o Nginx tera duas responsabilidades:

1. servir o frontend estatico;
2. encaminhar chamadas de API para o Spring Boot.

Fluxo desejado:

```text
Navegador
    |
    | GET /
    v
Nginx
    |
    | entrega /var/www/cantinho-das-lavandas/index.html
    v
Frontend carrega no navegador
```

Depois, quando o frontend chama a API:

```text
Navegador
    |
    | POST /auth/login
    v
Nginx
    |
    | proxy para http://localhost:8080/auth/login
    v
Spring Boot
    |
    v
MySQL
```

## Arquitetura Final

```text
Internet
   |
   v
Nginx porta 80/443
   |
   |-- arquivos estaticos
   |      /var/www/cantinho-das-lavandas/index.html
   |      /var/www/cantinho-das-lavandas/css/*
   |      /var/www/cantinho-das-lavandas/js/*
   |
   |-- proxy API
          http://localhost:8080/auth/*
          http://localhost:8080/rooms
          http://localhost:8080/bookings
          http://localhost:8080/stays
```

O Spring Boot continua rodando em:

```text
localhost:8080
```

Mas o publico acessa:

```text
http://IP_DA_EC2
```

ou:

```text
https://seu-dominio.com.br
```

## Onde Colocar o Frontend na EC2

Um caminho comum para arquivos servidos pelo Nginx e:

```text
/var/www/cantinho-das-lavandas
```

Estrutura:

```text
/var/www/cantinho-das-lavandas/
├── index.html
├── css/
│   ├── loginWidget.css
│   └── home.css
└── js/
    ├── api.js
    ├── controllers/
    │   └── main.js
    ├── views/
    │   └── homeView.js
    └── widgets/
        ├── loginWidget.js
        └── registrationWidget.js
```

Essa pasta fica fora do `.jar`.

## O Que Continua Dentro do JAR

Mesmo separando o frontend, o `.jar` continua contendo o backend.

Dentro do `.jar` ficam:

- classes Java;
- controllers;
- services;
- repositories;
- entidades JPA;
- DTOs;
- exceptions;
- configuracoes do Spring;
- dependencias empacotadas pelo Spring Boot.

O `.jar` continua sendo iniciado pelo `systemd`.

Exemplo:

```text
/home/ubuntu/CantinhoDasLavandas/target/househost-0.0.1-SNAPSHOT.jar
```

## O Que Sai do JAR

Na pratica, queremos que o frontend de producao seja lido desta pasta:

```text
/var/www/cantinho-das-lavandas
```

E que a origem dos arquivos no repositorio seja:

```text
frontend/
```

O backend fica em:

```text
src/main/java
src/main/resources/application.properties
```

Vantagens dessa organizacao:

- separacao mais clara;
- o `.jar` nao carrega frontend;
- deploy fica mentalmente mais limpo.
- a EC2 pode receber o `.jar` e o frontend separadamente;
- mudancas visuais nao obrigam restart do backend.

## Configuracao do Nginx

O Nginx precisa servir arquivos estaticos e encaminhar APIs para o Spring Boot.

Arquivo:

```text
/etc/nginx/sites-available/cantinho-das-lavandas
```

Configuracao sugerida:

```nginx
server {
    listen 80;
    server_name seu-dominio.com.br www.seu-dominio.com.br;

    root /var/www/cantinho-das-lavandas;
    index index.html;

    location / {
        try_files $uri $uri/ @spring;
    }

    location @spring {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Se ainda nao tiver dominio:

```nginx
server {
    listen 80;
    server_name _;

    root /var/www/cantinho-das-lavandas;
    index index.html;

    location / {
        try_files $uri $uri/ @spring;
    }

    location @spring {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## Como Ler Essa Configuracao

```nginx
root /var/www/cantinho-das-lavandas;
```

Define a pasta onde os arquivos estaticos ficam.

Se o navegador pedir:

```text
/css/home.css
```

o Nginx procura:

```text
/var/www/cantinho-das-lavandas/css/home.css
```

```nginx
index index.html;
```

Define o arquivo padrao quando a URL aponta para uma pasta.

```nginx
location / {
    try_files $uri $uri/ @spring;
}
```

Essa e a parte mais importante.

Ela diz:

```text
Primeiro tente achar um arquivo real.
Depois tente achar uma pasta real.
Se nao existir, encaminhe para o Spring Boot.
```

Exemplos:

```text
GET /
Nginx entrega index.html.

GET /css/home.css
Nginx entrega css/home.css.

GET /js/api.js
Nginx entrega js/api.js.

POST /auth/login
Nao existe arquivo /auth/login.
Nginx encaminha para Spring Boot.

GET /rooms
Nao existe arquivo /rooms.
Nginx encaminha para Spring Boot.
```

```nginx
location @spring {
    proxy_pass http://localhost:8080;
}
```

Essa parte encaminha para o backend Java.

## Cuidado Com api.js

Hoje o `api.js` usa:

```js
fetch("http://localhost:8080/auth/login", ...)
```

Isso funciona quando voce esta rodando localmente no seu computador.

Mas em producao isso tem um problema importante.

No navegador do usuario, `localhost` significa a maquina do usuario, nao a EC2.

Entao, em producao, este codigo:

```js
fetch("http://localhost:8080/auth/login")
```

tentaria chamar o computador de quem abriu o site.

O correto para frontend servido pelo Nginx e usar rota relativa:

```js
fetch("/auth/login", ...)
```

Assim, o navegador chama o mesmo dominio atual.

Exemplo:

```text
https://cantinhodaslavandas.com.br/auth/login
```

O Nginx recebe essa chamada e encaminha para:

```text
http://localhost:8080/auth/login
```

Portanto, para separar frontend e backend corretamente, o `api.js` deve evitar URL fixa com `localhost`.

## Ajuste Recomendado no api.js

Atual:

```js
fetch("http://localhost:8080/auth/login", ...)
```

Recomendado:

```js
fetch("/auth/login", ...)
```

No projeto, o `api.js` tambem aceita uma base opcional:

```js
const API_BASE_URL = globalThis.HOUSEHOST_API_BASE_URL || "";
```

Em producao, esse valor fica vazio. Assim as chamadas usam o mesmo dominio:

```text
/auth/login
/auth/registration
```

Se em algum teste local voce servir o frontend em uma porta diferente do backend, pode definir `HOUSEHOST_API_BASE_URL` antes de carregar os modulos JavaScript:

```html
<script>
  window.HOUSEHOST_API_BASE_URL = "http://localhost:8080";
</script>
```

Isso e util apenas para desenvolvimento local. Em producao, prefira deixar vazio e usar Nginx como proxy.

O mesmo vale para outras chamadas futuras:

```js
fetch("/rooms")
fetch("/bookings")
fetch("/stays")
```

## Primeiro Deploy do Frontend Estatico

Na EC2, crie a pasta:

```bash
sudo mkdir -p /var/www/cantinho-das-lavandas
```

Copie os arquivos do frontend:

```bash
sudo cp -r ~/CantinhoDasLavandas/frontend/* /var/www/cantinho-das-lavandas/
```

Ajuste dono e permissao:

```bash
sudo chown -R www-data:www-data /var/www/cantinho-das-lavandas
sudo find /var/www/cantinho-das-lavandas -type d -exec chmod 755 {} \;
sudo find /var/www/cantinho-das-lavandas -type f -exec chmod 644 {} \;
```

Teste o Nginx:

```bash
sudo nginx -t
```

Recarregue:

```bash
sudo systemctl reload nginx
```

Agora o frontend deve ser servido pelo Nginx.

## Deploy de Mudanca Apenas no Frontend

Se voce alterou apenas:

```text
frontend/index.html
frontend/css/*
frontend/js/*
```

Na EC2, faca:

```bash
cd ~/CantinhoDasLavandas
git pull
sudo rsync -av --delete frontend/ /var/www/cantinho-das-lavandas/
```

Nao precisa:

```bash
./mvnw clean package
sudo systemctl restart cantinho-das-lavandas
```

Tambem nao precisa recarregar o Nginx se voce so alterou arquivos estaticos.

O Nginx le os arquivos diretamente do disco a cada request.

## Deploy de Mudanca no Backend

Se voce alterou:

```text
src/main/java
pom.xml
src/main/resources/application.properties
```

Na EC2:

```bash
cd ~/CantinhoDasLavandas
git pull
./mvnw clean package -DskipTests
sudo systemctl restart cantinho-das-lavandas
```

Se o frontend tambem mudou no mesmo deploy:

```bash
sudo rsync -av --delete frontend/ /var/www/cantinho-das-lavandas/
```

## Deploy Buildado Localmente

Se a EC2 for pequena, voce pode buildar o `.jar` na sua maquina e copiar para a EC2.

Na sua maquina:

```bash
./mvnw clean package -DskipTests
```

Enviar o `.jar`:

```bash
scp -i sua-chave.pem target/househost-0.0.1-SNAPSHOT.jar ubuntu@IP_DA_EC2:/home/ubuntu/CantinhoDasLavandas/target/
```

Enviar frontend:

```bash
rsync -av --delete -e "ssh -i sua-chave.pem" frontend/ ubuntu@IP_DA_EC2:/tmp/cantinho-static/
ssh -i sua-chave.pem ubuntu@IP_DA_EC2 "sudo rsync -av --delete /tmp/cantinho-static/ /var/www/cantinho-das-lavandas/"
```

Reiniciar backend se o `.jar` mudou:

```bash
ssh -i sua-chave.pem ubuntu@IP_DA_EC2 "sudo systemctl restart cantinho-das-lavandas"
```

Se mudou apenas frontend, nao reinicie o backend.

## Cache do Navegador

Ao separar frontend, pode acontecer de o navegador manter CSS ou JS antigo em cache.

Exemplo:

```html
<link rel="stylesheet" href="css/home.css">
```

O navegador pode guardar esse arquivo e nao baixar imediatamente a versao nova.

Solucoes simples:

Adicionar versao na URL:

```html
<link rel="stylesheet" href="css/home.css?v=2026-05-14-1">
<script type="module" src="js/controllers/main.js?v=2026-05-14-1"></script>
```

Quando alterar CSS/JS, muda a versao.

Outra abordagem futura:

- usar Vite;
- gerar arquivos com hash no nome;
- configurar cache-control no Nginx.

Para agora, query string com `?v=` ja resolve bem.

## CORS

Hoje os controllers usam:

```java
@CrossOrigin(origins = "*")
```

Se frontend e backend forem acessados pelo mesmo dominio via Nginx, CORS praticamente deixa de ser problema.

Exemplo:

```text
Frontend: https://cantinhodaslavandas.com.br
API:      https://cantinhodaslavandas.com.br/auth/login
```

Mesmo dominio, mesma origem.

O Nginx encaminha internamente para:

```text
http://localhost:8080
```

Mas o navegador nao enxerga esse `localhost:8080`.

Por isso, o ideal e usar rotas relativas no frontend:

```js
fetch("/auth/login")
```

## Security Group

Com frontend no Nginx, nao exponha a porta 8080 publicamente.

No Security Group:

```text
22   SSH    apenas seu IP
80   HTTP   0.0.0.0/0
443  HTTPS  0.0.0.0/0
```

Nao precisa liberar:

```text
8080
```

O Spring Boot fica acessivel apenas localmente na EC2:

```text
localhost:8080
```

## Checklist de Implementacao

1. Ajustar `api.js` para usar rotas relativas.

```js
fetch("/auth/login")
```

2. Criar pasta do frontend na EC2.

```bash
sudo mkdir -p /var/www/cantinho-das-lavandas
```

3. Copiar arquivos estaticos.

```bash
sudo rsync -av --delete ~/CantinhoDasLavandas/frontend/ /var/www/cantinho-das-lavandas/
```

4. Configurar Nginx com `root` apontando para `/var/www/cantinho-das-lavandas`.

5. Configurar `try_files` para servir arquivo primeiro e mandar o resto para Spring.

6. Testar Nginx.

```bash
sudo nginx -t
```

7. Recarregar Nginx.

```bash
sudo systemctl reload nginx
```

8. Acessar pelo IP ou dominio.

```text
http://IP_DA_EC2
```

9. Testar login.

10. Fechar porta 8080 no Security Group se ela estiver aberta.

## Comandos de Rotina

Atualizar somente frontend:

```bash
cd ~/CantinhoDasLavandas
git pull
sudo rsync -av --delete frontend/ /var/www/cantinho-das-lavandas/
```

Atualizar backend:

```bash
cd ~/CantinhoDasLavandas
git pull
./mvnw clean package -DskipTests
sudo systemctl restart cantinho-das-lavandas
```

Atualizar backend e frontend:

```bash
cd ~/CantinhoDasLavandas
git pull
./mvnw clean package -DskipTests
sudo systemctl restart cantinho-das-lavandas
sudo rsync -av --delete frontend/ /var/www/cantinho-das-lavandas/
```

Ver logs do backend:

```bash
journalctl -u cantinho-das-lavandas -f
```

Ver status do Nginx:

```bash
sudo systemctl status nginx
```

Testar Nginx:

```bash
sudo nginx -t
```

## Problemas Comuns

### Tela abre, mas login nao funciona

Verifique `api.js`.

Se estiver assim:

```js
fetch("http://localhost:8080/auth/login")
```

troque para:

```js
fetch("/auth/login")
```

### CSS nao atualizou

Pode ser cache do navegador.

Tente:

- hard refresh;
- aba anonima;
- mudar versao no HTML: `home.css?v=2`;
- conferir se o arquivo foi copiado para `/var/www`.

### Nginx mostra 404

Verifique se o arquivo existe:

```bash
ls -la /var/www/cantinho-das-lavandas
```

Verifique configuracao:

```bash
sudo nginx -t
```

### API retorna 404

Verifique se o Spring Boot esta rodando:

```bash
sudo systemctl status cantinho-das-lavandas
```

Verifique logs:

```bash
journalctl -u cantinho-das-lavandas -n 100
```

### API funciona localmente, mas nao no site

Possiveis causas:

- `api.js` usando `localhost`;
- Nginx nao esta encaminhando para `localhost:8080`;
- backend nao esta rodando;
- rota do backend mudou;
- metodo HTTP errado.

## Resumo

Separar frontend estatico do `.jar` significa que o Nginx passa a servir `index.html`, CSS e JS diretamente de uma pasta como:

```text
/var/www/cantinho-das-lavandas
```

O Spring Boot continua rodando como backend na porta 8080.

O Nginx decide:

```text
Se existe arquivo estatico, entrega o arquivo.
Se nao existe, encaminha para o Spring Boot.
```

Com isso:

- mudancas de frontend ficam mais rapidas;
- nao precisa rebuildar `.jar` para alterar CSS/JS/HTML;
- o backend fica mais isolado;
- a porta 8080 pode ficar fechada para a internet;
- o deploy fica mais parecido com producao real.
