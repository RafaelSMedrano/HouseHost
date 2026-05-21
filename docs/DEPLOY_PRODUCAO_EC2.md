# Deploy em Producao na EC2

Este documento explica como colocar o Cantinho Das Lavandas para rodar em producao em uma instancia EC2 da AWS.

A ideia principal e transformar o projeto em um arquivo executavel `.jar`, deixar esse `.jar` rodando como um servico do Linux e colocar o Nginx na frente para receber acessos pela porta 80 ou 443.

## Visao Geral

Em desenvolvimento, e comum rodar a aplicacao assim:

```bash
./mvnw spring-boot:run
```

Isso funciona para testar, mas nao e o ideal para producao. Em producao, queremos que a aplicacao:

- suba automaticamente quando a EC2 ligar;
- reinicie sozinha se acontecer algum erro;
- guarde logs em um lugar facil de consultar;
- nao dependa de um terminal aberto;
- nao exponha senhas dentro do codigo;
- seja acessada por HTTP ou HTTPS usando Nginx.

O fluxo recomendado e:

```text
Usuario no navegador
        |
        v
Nginx porta 80/443
        |
        v
Spring Boot porta 8080
        |
        v
MySQL local na EC2
```

## Conceitos Importantes

### EC2

EC2 e o servico da AWS que fornece uma maquina virtual na nuvem.

Na pratica, a EC2 funciona como um computador Linux remoto. Voce acessa por SSH ou pelo console da AWS, instala Java, MySQL, Git, Nginx e roda a aplicacao nela.

### SSH

SSH e o protocolo usado para acessar o terminal da EC2 com seguranca.

Exemplo:

```bash
ssh -i chave.pem ubuntu@IP_DA_EC2
```

No console web da AWS, voce ja esta dentro de um terminal da instancia, entao nao precisa rodar esse comando.

### Git Clone

`git clone` baixa o projeto do GitHub para dentro da EC2.

Exemplo:

```bash
git clone https://github.com/RafaelSMedrano/CantinhoDasLavandas.git
```

Como o repositorio e privado, o GitHub pode pedir usuario e token.

### Java 21

Este projeto usa Java 21. Isso esta definido no `pom.xml`:

```xml
<java.version>21</java.version>
```

Por isso, a EC2 tambem precisa ter Java 21 instalado.

### Maven Wrapper

O arquivo `mvnw` e o Maven Wrapper.

Ele permite rodar comandos Maven sem depender de um Maven instalado globalmente na maquina.

Exemplos:

```bash
./mvnw test
./mvnw clean package
./mvnw spring-boot:run
```

### JAR

JAR e o arquivo final da aplicacao Java empacotada.

Quando voce roda:

```bash
./mvnw clean package
```

o Maven compila o projeto e gera um arquivo em:

```text
target/househost-0.0.1-SNAPSHOT.jar
```

Esse `.jar` contem a aplicacao Spring Boot pronta para rodar.

Em producao, o ideal e rodar:

```bash
java -jar target/househost-0.0.1-SNAPSHOT.jar
```

em vez de:

```bash
./mvnw spring-boot:run
```

### Variaveis de Ambiente

Variaveis de ambiente sao configuracoes externas ao codigo.

Elas servem para guardar informacoes que mudam de ambiente para ambiente, como:

- URL do banco;
- usuario do banco;
- senha do banco;
- porta da aplicacao;
- chaves secretas.

No projeto, o arquivo `application.properties` le estas variaveis:

```properties
spring.datasource.url=${HOUSEHOST_DB_URL:jdbc:mysql://localhost:3306/househost?createDatabaseIfNotExist=true&serverTimezone=UTC}
spring.datasource.username=${HOUSEHOST_DB_USERNAME:root}
spring.datasource.password=${HOUSEHOST_DB_PASSWORD:}
```

Isso significa:

- se `HOUSEHOST_DB_URL` existir, usa ela;
- se nao existir, usa o valor padrao;
- o mesmo vale para usuario e senha.

Em producao, nao e recomendado colocar senha direto no codigo nem subir `.env` para o GitHub. Por isso, usamos um arquivo fora do projeto:

```text
/etc/cantinho-das-lavandas.env
```

### systemd

`systemd` e o gerenciador de servicos do Linux.

Ele controla programas que devem ficar rodando em segundo plano, como:

- MySQL;
- Nginx;
- aplicacoes backend;
- jobs e daemons do sistema.

Quando configuramos a aplicacao como servico, ganhamos comandos como:

```bash
sudo systemctl start cantinho-das-lavandas
sudo systemctl stop cantinho-das-lavandas
sudo systemctl restart cantinho-das-lavandas
sudo systemctl status cantinho-das-lavandas
```

Tambem podemos fazer a aplicacao subir automaticamente quando a EC2 reiniciar:

```bash
sudo systemctl enable cantinho-das-lavandas
```

### Nginx

Nginx e um servidor web.

Neste projeto, ele fica na frente do Spring Boot.

O Spring Boot roda localmente na porta 8080:

```text
localhost:8080
```

O Nginx recebe o trafego publico na porta 80 ou 443 e encaminha para o Spring Boot:

```text
porta 80/443 -> Nginx -> localhost:8080 -> Spring Boot
```

Isso e melhor do que expor a porta 8080 diretamente, porque o Nginx facilita:

- usar dominio;
- configurar HTTPS;
- controlar headers;
- servir conteudo estatico se necessario;
- fazer proxy reverso para a aplicacao.

### Security Group

Security Group e o firewall da AWS.

Ele define quais portas da EC2 podem ser acessadas pela internet.

Para producao, a configuracao recomendada e:

```text
22   SSH    somente seu IP
80   HTTP   0.0.0.0/0
443  HTTPS  0.0.0.0/0
```

Nao e necessario liberar a porta 8080 publicamente se o Nginx estiver fazendo proxy para ela.

## Passo a Passo

### 1. Atualizar a EC2

```bash
sudo apt update
sudo apt upgrade -y
```

### 2. Instalar dependencias

```bash
sudo apt install -y git openjdk-21-jdk mysql-server nginx
```

Confirme a versao do Java:

```bash
java -version
```

O resultado deve indicar Java 21.

### 3. Ativar MySQL

```bash
sudo systemctl enable --now mysql
sudo systemctl status mysql
```

### 4. Criar banco e usuario

Entre no MySQL:

```bash
sudo mysql
```

Rode os comandos:

```sql
CREATE DATABASE househost CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'househost'@'localhost' IDENTIFIED BY 'troque_por_uma_senha_forte';

GRANT ALL PRIVILEGES ON househost.* TO 'househost'@'localhost';

FLUSH PRIVILEGES;
EXIT;
```

Troque `troque_por_uma_senha_forte` por uma senha real.

### 5. Clonar o projeto

No diretorio home do usuario da EC2:

```bash
cd ~
git clone https://github.com/RafaelSMedrano/CantinhoDasLavandas.git
cd CantinhoDasLavandas
```

Se o GitHub pedir senha, use um Personal Access Token em vez da senha normal da conta.

### 6. Criar arquivo de variaveis de ambiente

Crie o arquivo:

```bash
sudo nano /etc/cantinho-das-lavandas.env
```

Conteudo:

```env
HOUSEHOST_DB_URL=jdbc:mysql://localhost:3306/househost?serverTimezone=UTC
HOUSEHOST_DB_USERNAME=househost
HOUSEHOST_DB_PASSWORD=troque_por_uma_senha_forte
```

Proteja o arquivo:

```bash
sudo chmod 600 /etc/cantinho-das-lavandas.env
```

Esse arquivo fica fora do Git e deve existir apenas na EC2.

### 7. Buildar a aplicacao

Buildar a aplicacao significa transformar o codigo-fonte em um arquivo executavel.

No nosso caso, o codigo esta em arquivos `.java`, `.html`, `.css`, `.js` e configuracoes do Maven. A EC2 nao deve depender do IntelliJ para rodar isso. Entao usamos o Maven para compilar o projeto e gerar um arquivo `.jar`.

O `.jar` e o pacote final da aplicacao Spring Boot. Depois que ele existe, a aplicacao pode ser iniciada com:

```bash
java -jar target/househost-0.0.1-SNAPSHOT.jar
```

Antes de buildar, entre na pasta do projeto:

```bash
cd ~/CantinhoDasLavandas
```

De permissao de execucao para o Maven Wrapper:

```bash
chmod +x mvnw
```

Esse comando so precisa ser feito uma vez. Ele permite executar o arquivo `mvnw` como programa.

Agora gere o `.jar`:

```bash
./mvnw clean package -DskipTests
```

O comando acima tem tres partes importantes:

```text
./mvnw
```

Usa o Maven Wrapper do proprio projeto.

```text
clean
```

Apaga a pasta `target/` antiga. Isso evita usar arquivos antigos de builds anteriores.

```text
package
```

Compila o projeto e empacota a aplicacao em um `.jar`.

```text
-DskipTests
```

Pula os testes nessa etapa. Isso deixa o build de producao mais rapido. Se quiser ser mais rigoroso antes de publicar, rode os testes separadamente:

```bash
./mvnw test
```

Depois rode:

```bash
./mvnw clean package -DskipTests
```

Quando o build terminar com sucesso, deve aparecer algo como:

```text
BUILD SUCCESS
```

O arquivo gerado deve ficar em:

```text
target/househost-0.0.1-SNAPSHOT.jar
```

Para confirmar:

```bash
ls -lh target/*.jar
```

Se esse arquivo nao existir, nao continue para o `systemd` ainda. Primeiro resolva o erro do build, porque o servico de producao depende desse `.jar`.

### 8. Testar manualmente o JAR

Antes de criar o servico, teste a aplicacao manualmente:

```bash
set -a
source /etc/cantinho-das-lavandas.env
set +a

java -jar target/househost-0.0.1-SNAPSHOT.jar
```

Se funcionar, voce deve ver algo parecido com:

```text
Tomcat started on port 8080
Started HouseHostApplication
```

Para parar:

```text
Ctrl + C
```

### 9. Criar servico systemd

Crie o arquivo:

```bash
sudo nano /etc/systemd/system/cantinho-das-lavandas.service
```

Se o usuario da EC2 for `ubuntu`, use:

```ini
[Unit]
Description=Cantinho Das Lavandas Spring Boot App
After=network.target mysql.service

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/CantinhoDasLavandas
EnvironmentFile=/etc/cantinho-das-lavandas.env
ExecStart=/usr/bin/java -jar /home/ubuntu/CantinhoDasLavandas/target/househost-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Se o usuario da EC2 for `ec2-user`, use:

```ini
[Unit]
Description=Cantinho Das Lavandas Spring Boot App
After=network.target mysql.service

[Service]
User=ec2-user
WorkingDirectory=/home/ec2-user/CantinhoDasLavandas
EnvironmentFile=/etc/cantinho-das-lavandas.env
ExecStart=/usr/bin/java -jar /home/ec2-user/CantinhoDasLavandas/target/househost-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Recarregue o systemd:

```bash
sudo systemctl daemon-reload
```

Ative e inicie:

```bash
sudo systemctl enable --now cantinho-das-lavandas
```

Veja o status:

```bash
sudo systemctl status cantinho-das-lavandas
```

### 10. Ver logs da aplicacao

Para acompanhar logs em tempo real:

```bash
journalctl -u cantinho-das-lavandas -f
```

Para ver logs recentes:

```bash
journalctl -u cantinho-das-lavandas -n 100
```

Se a aplicacao nao subir, normalmente o erro aparece nesses logs.

### 11. Configurar Nginx

Crie o arquivo:

```bash
sudo nano /etc/nginx/sites-available/cantinho-das-lavandas
```

Conteudo inicial:

```nginx
server {
    listen 80;
    server_name seu-dominio.com.br www.seu-dominio.com.br;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Se ainda nao tiver dominio, para teste temporario voce pode usar:

```nginx
server {
    listen 80;
    server_name _;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Ative a configuracao:

```bash
sudo ln -s /etc/nginx/sites-available/cantinho-das-lavandas /etc/nginx/sites-enabled/
```

Teste a configuracao:

```bash
sudo nginx -t
```

Recarregue o Nginx:

```bash
sudo systemctl reload nginx
```

Agora o acesso deve funcionar em:

```text
http://IP_PUBLICO_DA_EC2
```

ou:

```text
http://seu-dominio.com.br
```

### 12. Configurar HTTPS

Para producao real, use HTTPS.

Depois que o dominio estiver apontando para o IP publico da EC2, instale Certbot:

```bash
sudo apt install -y certbot python3-certbot-nginx
```

Gere o certificado:

```bash
sudo certbot --nginx -d seu-dominio.com.br -d www.seu-dominio.com.br
```

O Certbot atualiza a configuracao do Nginx e cria a renovacao automatica do certificado.

Teste a renovacao:

```bash
sudo certbot renew --dry-run
```

## Atualizar a Aplicacao em Producao

Quando voce fizer alteracoes no codigo e subir para o GitHub, atualize a EC2 assim:

```bash
cd ~/CantinhoDasLavandas
git pull
./mvnw clean package -DskipTests
sudo systemctl restart cantinho-das-lavandas
sudo systemctl status cantinho-das-lavandas
```

Confira os logs:

```bash
journalctl -u cantinho-das-lavandas -n 100
```

## Comandos Uteis

Status da aplicacao:

```bash
sudo systemctl status cantinho-das-lavandas
```

Reiniciar aplicacao:

```bash
sudo systemctl restart cantinho-das-lavandas
```

Parar aplicacao:

```bash
sudo systemctl stop cantinho-das-lavandas
```

Iniciar aplicacao:

```bash
sudo systemctl start cantinho-das-lavandas
```

Logs da aplicacao:

```bash
journalctl -u cantinho-das-lavandas -f
```

Status do Nginx:

```bash
sudo systemctl status nginx
```

Testar configuracao do Nginx:

```bash
sudo nginx -t
```

Recarregar Nginx:

```bash
sudo systemctl reload nginx
```

Status do MySQL:

```bash
sudo systemctl status mysql
```

Entrar no MySQL:

```bash
sudo mysql
```

## Problemas Comuns

### Aplicacao nao sobe

Veja os logs:

```bash
journalctl -u cantinho-das-lavandas -n 100
```

Problemas comuns:

- senha do banco errada;
- banco `househost` nao existe;
- Java 21 nao instalado;
- caminho do `.jar` errado no `ExecStart`;
- usuario errado no arquivo `.service`;
- porta 8080 ja em uso.

### Erro de permissao no arquivo de ambiente

Confira:

```bash
ls -l /etc/cantinho-das-lavandas.env
```

O arquivo deve existir e o systemd precisa conseguir le-lo.

### Porta 80 nao abre no navegador

Confira:

- Nginx esta rodando;
- Security Group libera porta 80;
- configuracao do Nginx passou em `sudo nginx -t`;
- aplicacao esta rodando na porta 8080.

Comandos:

```bash
sudo systemctl status nginx
sudo systemctl status cantinho-das-lavandas
sudo nginx -t
```

### Porta 8080 nao precisa ficar publica

Em producao, a porta 8080 pode ficar fechada no Security Group.

O fluxo deve ser:

```text
Internet -> porta 80/443 -> Nginx -> localhost:8080
```

Assim, o Spring Boot fica acessivel apenas internamente na propria EC2.

## Checklist de Producao

Antes de considerar a aplicacao pronta:

- Java 21 instalado;
- MySQL rodando;
- banco `househost` criado;
- usuario do banco criado com senha forte;
- arquivo `/etc/cantinho-das-lavandas.env` criado;
- `.env` nao versionado no Git;
- aplicacao buildada com `./mvnw clean package`;
- servico `cantinho-das-lavandas` ativo;
- Nginx configurado;
- Security Group liberando 80 e 443;
- porta 8080 nao exposta publicamente;
- dominio apontando para a EC2;
- HTTPS configurado com Certbot;
- logs conferidos apos o primeiro deploy.
