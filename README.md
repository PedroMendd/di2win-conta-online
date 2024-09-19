# Di2win Conta Online

**Descrição**  
A aplicação Di2win Conta Online é uma API REST que permite gerenciar contas bancárias digitais, realizar depósitos, saques, consultar saldo, bloquear contas e gerenciar transações. O sistema foi desenvolvido em **Java** utilizando **Spring Boot** e **PostgreSQL** como banco de dados.

## Funcionalidades
- Criação e remoção de clientes
- Criação e remoção de contas
- Depósitos e saques em contas
- Consulta de saldo
- Bloqueio de contas
- Consulta de transações por período
- Validação de CPF

---

## Endpoints da API

### Criação de Cliente
**POST** `/api/clients`  
- **Descrição:** Cria um novo cliente.
- **Request Body:**
  ```json
  {
    "cpf": "12345678900",
    "nome": "João Silva",
    "dataNascimento": "1990-01-01"
  }
- Status 201 Created


- **Response Body:**
- ```json
  {
  "id": 1,
  "cpf": "12345678900",
  "nome": "João Silva",
  "dataNascimento": "1990-01-01"
  }

### Criação da Conta
**POST** `/api/accounts?cpf={cpf}`
- **Descrição:** Cria uma nova conta vinculada a um cliente por CPF.
- **Request Body:**
  ```json
  {
     "cpf": "12345678900",
  "agencia": "1234",
  "numeroConta": "00000001",
  "limiteDiarioSaque": 1000.00
  }
- Status 201 Created
- **Response Body:**
- ```json
  {
  "id": 1,
  "numeroConta": "00000001",
  "agencia": "1234",
  "saldo": 0.00,
  "limiteDiarioSaque": 1000.00,
  "bloqueada": false
  }
- Status 201 Created

### Depósito
**POST** `/api/accounts/{accountId}/deposit?amount={valor}`
- **Descrição:** Realiza um depósito em uma conta específica.

- **Response Body:**
- ```json
  {
  "id": 1,
  "valor": 500.00,
  "tipo": "DEPOSITO"
  }
- Status 200 OK

### Saque
**POST** `/api/accounts/{accountId}/withdraw?amount={valor}`
- **Descrição:** Realiza um saque em uma conta específica.

- **Response Body:**
- ```json
  {
  "id": 2,
  "valor": 100.00,
  "tipo": "SAQUE"
  }
- Status 200 OK

### Bloqueio de Conta
**POST** `/api/accounts/{accountId}/block`
- **Descrição:** Bloqueia uma conta específica.

- Status 204 No Content

### Consulta de Saldo
**GET** `/api/accounts/{accountId}/balance`
- **Descrição:** Consulta o saldo de uma conta específica.

- **Response Body:**
- ```json
  {
  "saldo": 500.00
  }
- Status 200 OK

### Transações por Período
**GET** `/api/transactions/{accountId}/period?start={dataInicio}&end={dataFim}`
- **Descrição:** Consulta as transações de uma conta em um período específico.

- **Response Body:**
- ```json
  [
   {
  "id": 1,
  "valor": 500.00,
  "tipo": "DEPOSITO",
  "dataHora": "2024-01-10T10:00:00"
   },
   {
  "id": 2,
  "valor": 100.00,
  "tipo": "SAQUE",
  "dataHora": "2024-01-15T15:00:00"
   }
  ]

- Status 200 OK