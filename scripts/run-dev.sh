#!/usr/bin/env bash
while IFS='=' read -r key value; do
  if [[ -n "$key" && "$key" != \#* ]]; then
    export "$key=$value"
  fi
done < .env

if [[ -z "$HOUSEHOST_DB_PASSWORD" || "$HOUSEHOST_DB_PASSWORD" == "coloque_sua_senha_aqui" || "$HOUSEHOST_DB_PASSWORD" == "troque_esta_senha" ]]; then
  read -r -s -p "Senha do MySQL para ${HOUSEHOST_DB_USERNAME}: " HOUSEHOST_DB_PASSWORD
  echo
  export HOUSEHOST_DB_PASSWORD
fi

./mvnw spring-boot:run
