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
- **Descrição:** Cria um novo cliente com os dados fornecidos.
- **Request Body:**
  ```json
  {
    "cpf": "12345678900",
    "nome": "João Silva",
    "dataNascimento": "1990-01-01"
  }
  ```
- 201 Created: Quando o cliente é criado com sucesso.


- **Response Body:**
- ```json
  {
  "id": 1,
  "cpf": "12345678900",
  "nome": "João Silva",
  "dataNascimento": "1990-01-01"
  }
  ```
- **Erros possíveis:**
- 400 Bad Request - Quando o CPF, nome ou data de nascimento são inválidos ou estão faltando.
- ```json
  {
    "mensagem": "O nome não pode ser nulo ou vazio."
  }
  ```
- 409 Conflict - Quando o CPF já está registrado no sistema.
- ```json
  {
    "mensagem": "CPF já cadastrado: 12345678901"
  }
  ```  
### Remover cliente
**DELETE** `/api/clients/{id}`
- **Descrição:** Remove um cliente pelo seu ID.
- **Parâmetros da URL:**
- id (Long): O ID do cliente a ser removido.

- 204 No Content: Quando o cliente é removido com sucesso.

- **Erros possíveis:**
- 404 Not Found: Quando o cliente com o ID especificado não é encontrado.
- 400 Bad Request: Quando o cliente tem contas associadas e não pode ser removido.
- ```json
  {
    "mensagem": "Cliente não encontrado com ID: {id}"
  }
  ```
- 400 Bad Request: Quando o cliente tem contas associadas e não pode ser removido.
- ```json
  {
    "Cliente não pode ser removido porque possui contas associadas."
  }
  ```    

  
### Criação da Conta
**POST** `/api/accounts`
- **Descrição:** Cria uma nova conta vinculada a um cliente por CPF.
- **Request Body:**
  ```json
   {
    "cpf": "string"
  }
  ```    
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
  ```
- Status 201 Created

- **Erros possíveis:**
- 404 Not Found: Cliente não encontrado com o CPF fornecido.
- ```json
  {
    "mensagem": "Cliente não encontrado com CPF: {id}"
  }
  ```
- 400 Bad Request: Erro na validação dos dados do request.
- ```json
  {
    "Cliente não pode ser removido porque possui contas associadas."
  }
  ```   

### Depósito
**PUT** `/api/accounts/{accountId}/deposit`
- **Descrição:** Realiza um depósito em uma conta específica.

- **Request Body:**
- ```json
  {
  "amount": 100.00
  }
  ```

- **Response Body:**
- ```json
  {
  "id": 1,
  "numeroConta": "00000001",
  "agencia": "1234",
  "saldo": 600.00,
  "bloqueada": false,
  "limiteDiarioSaque": 1000
  }
  ```


- Status 200 OK

- **Erros possíveis:**
- 400 Bad Request: Valor de depósito inválido.
- 403 Forbidden: Conta bloqueada.

### Saque
**PUT** `/api/accounts/{accountId}/withdraw`
- **Descrição:** Realiza um saque em uma conta específica.

- **Request Body:**
- ```json
  {
  "amount": 100.00
  }
  ```

- **Response Body:**
- ```json
  {
  "id": 1,
  "numeroConta": "00000001",
  "agencia": "1234",
  "saldo": 400.00,
  "bloqueada": false,
  "limiteDiarioSaque": 1000
  }
  ```

- Status 200 OK

- **Erros possíveis:**
- 400 Bad Request: Valor de depósito inválido.
- 403 Forbidden: Conta bloqueada.
- 400 Bad Request: Limite diário de saque excedido.

### Consulta de Saldo
**GET** `/api/accounts/{accountId}/balance`
- **Descrição:** Consulta o saldo de uma conta específica.

- **Response Body:**
- ```json
  {
  "saldo": 500.00
  }
  ```
- Status 200 OK

- **Erros possíveis:**
- 404 Not Found: Conta não encontrada com o ID fornecido.

### Transações por Período
**GET** `/api/accounts/{accountId}/transactions`
- **Descrição:** Consulta as transações de uma conta em um período específico.

- **Request Params:**
- start: Data e hora de início do período (ISO 8601).
- end: Data e hora de término do período (ISO 8601).

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
  ```

- Status 200 OK

- **Erros possíveis:**
- 400 Bad Request: Data de início posterior à data de término.
- 404 Not Found: Conta não encontrada.

### Bloqueio de Conta
**POST** `/api/accounts/{accountId}/block`
- **Descrição:** Bloqueia uma conta específica.

- Status 204 No Content

- **Erros possíveis:**
- 400 Bad Request: Conta já está bloqueada.
- 404 Not Found: Conta não encontrada.

### Desbloqueio de Conta
**POST** `/api/accounts/{accountId}/unblock`
- **Descrição:** Desbloqueia uma conta específica.

- Status 204 No Content

- **Erros possíveis:**
- 400 Bad Request: Conta já está desbloqueada.
- 404 Not Found: Conta não encontrada.

### Deletar Conta
**DELETE** `/api/accounts/{accountId}`
- **Descrição:** Deleta uma conta específica.

- Status 204 No Content

- **Erros possíveis:**
- 400 Bad Request: Conta está bloqueada ou possui transações associadas.
- 404 Not Found: Conta não encontrada.