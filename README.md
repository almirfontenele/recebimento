# API de Recebimentos

API RESTful desenvolvida em **Java 17** e **Spring Boot 4** para gerenciamento completo (CRUD), consultas agregadas e importação inteligente via arquivo CSV de registros de recebimentos financeiros, construída sob os princípios de **Arquitetura Hexagonal (Ports & Adapters)**, **Clean Code** e **SOLID**.

---

## Sumário

- [Funcionalidades](#-funcionalidades)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura do Projeto](#-arquitetura-do-projeto)
- [Pré-requisitos](#-pré-requisitos)
- [Como Executar a Aplicação](#-como-executar-a-aplicação)
- [Documentação Interativa (Swagger / OpenAPI)](#-documentação-interativa-swagger--openapi)
- [Endpoints da API](#-endpoints-da-api)
  - [1. Criar Recebimento](#1-criar-recebimento)
  - [2. Listar Todos os Recebimentos](#2-listar-todos-os-recebimentos)
  - [3. Buscar Recebimento por ID](#3-buscar-recebimento-por-id)
  - [4. Atualizar Recebimento](#4-atualizar-recebimento)
  - [5. Remover Recebimento](#5-remover-recebimento)
  - [6. Consultar Valor Total por CPF](#6-consultar-valor-total-por-cpf)
  - [7. Consultar Valor Total por Período](#7-consultar-valor-total-por-período)
  - [8. Importar Recebimentos via CSV](#8-importar-recebimentos-via-csv)
- [Importação Inteligente de CSV](#-importação-inteligente-de-csv)
- [Como Executar os Testes](#-como-executar-os-testes)
- [Documentação Complementar](#-documentação-complementar)

---

## Funcionalidades

- **CRUD Completo de Recebimentos**: Criação, listagem, busca por ID, atualização e exclusão de recebimentos.
- **Validação e Normalização de Domínio (Fail-Fast)**:
  - **CPF**: Aceita 11 dígitos numéricos ou formato com máscara (`000.000.000-00`), normalizando para 11 dígitos.
  - **E2E (End-to-End ID)**: Identificador alfanumérico da transação.
  - **Data**: Formato `YYYY-MM-DD HH:mm:ss` (ex.: `2026-10-21 14:30:00`).
  - **Valor**: Formato monetário brasileiro `999990,99` com duas casas decimais.
- **Consultas Agregadas**:
  - Cálculo do valor total recebido por **CPF**.
  - Cálculo do valor total recebido por **período** (`dataInicial` e `dataFinal`, aceitando `YYYY-MM-DD HH:mm:ss` ou `YYYY-MM-DD`).
- **Importação de CSV com Inferência Automática de Colunas**:
  - Detecta automaticamente delimitadores (`;` ou `,`) e presença ou ausência de cabeçalho.
  - Identifica dinamicamente qual coluna corresponde a `CPF`, `E2E`, `DATA` e `VALOR` independentemente da ordem no arquivo.
  - Persiste os registros na tabela `recebimento` do banco de dados SQLite.

---

## Tecnologias Utilizadas

| Tecnologia | Versão / Detalhe | Finalidade |
| :--- | :--- | :--- |
| **Java** | 17 | Linguagem base (uso de `records`, expressões modernas, imutabilidade) |
| **Spring Boot** | 4.1.1 | Framework Web MVC, Injeção de Dependência e Validação |
| **Spring Data JPA / Hibernate** | 4.1.1 | Persistência de dados e mapeamento objeto-relacional |
| **SQLite (`sqlite-jdbc`)** | Dialeto Comunitário Hibernate | Banco de dados relacional em arquivo local (`recebimentodb`) |
| **SpringDoc OpenAPI (Swagger)** | 3.1.0 | Documentação interativa da API REST |
| **JUnit 5 & MockMvc** | Spring Boot Starter Test | Testes unitários de domínio/parser e testes de integração REST |
| **Maven** | Wrapper incluso (`mvnw`) | Gerenciamento de dependências e build |

---

## Arquitetura do Projeto

O projeto adota a **Arquitetura Hexagonal (Ports & Adapters)**, isolando completamente o núcleo de negócio (`domain` e `application`, escritos em Java puro sem dependências de frameworks) dos detalhes técnicos de infraestrutura (`infrastructure`).

![Diagrama da Arquitetura Hexagonal](docs/arquitetura-hexagonal.png)

```text
com.fontecode.recebimento.recebimento
├── domain                              # Núcleo de Negócio (Java Puro)
│   ├── exception                       # Exceções de domínio (DomainValidation, CsvImport, NaoEncontrado)
│   ├── model                           # Entidade de domínio imutável e formatadores/validadores
│   └── port
│       ├── in                          # Portas de Entrada (RecebimentoUseCase, ImportarRecebimentosCsvUseCase)
│       └── out                         # Porta de Saída (RecebimentoRepositoryPort)
├── application                         # Orquestração de Casos de Uso (Java Puro)
│   ├── parser                          # CsvRecebimentoParser (inferência automática de tipos CSV)
│   └── service                         # RecebimentoService (implementação dos Use Cases)
└── infrastructure                      # Adaptadores Externos e Configuração Spring
    ├── adapter
    │   ├── in.web                      # REST Controller, DTOs (Records), WebMapper e GlobalExceptionHandler
    │   └── out.persistence             # PersistenceAdapter, JPA Entity (tabela 'recebimento') e Repository
    └── config                          # BeanConfiguration e OpenApiConfiguration
```

---

## Pré-requisitos

- **JDK 17** ou superior instalado e configurado no `PATH` (`java -version`).
- Não é necessário instalar o Maven separadamente (o projeto inclui o **Maven Wrapper** `mvnw` / `mvnw.cmd`).

---

## Como Executar a Aplicação

1. Clone o repositório e acesse a pasta raiz do projeto:

```bash
cd recebimento
```

2. Execute a aplicação utilizando o Maven Wrapper:

- **No Windows (PowerShell / CMD):**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```

- **No Linux / macOS:**
  ```bash
  ./mvnw spring-boot:run
  ```

A aplicação será iniciada por padrão na porta **`8080`** (`http://localhost:8080`).

> [!NOTE]
> Por padrão, o banco SQLite é configurado em `src/main/resources/application.properties` apontando para o arquivo `recebimentodb` na raiz do projeto, criando/atualizando automaticamente a tabela `recebimento`.

---

## Documentação Interativa (Swagger / OpenAPI)

Com a aplicação em execução, acesse a interface do **Swagger UI** para visualizar e testar todos os endpoints diretamente pelo navegador:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Endpoints da API

Base URL: `http://localhost:8080/recebimentos`

| Método | Rota | Descrição | Status de Sucesso |
| :--- | :--- | :--- | :--- |
| `POST` | `/recebimentos` | Cria um novo recebimento | `201 Created` |
| `GET` | `/recebimentos` | Lista todos os recebimentos cadastrados | `200 OK` |
| `GET` | `/recebimentos/{id}` | Busca um recebimento pelo ID | `200 OK` |
| `PUT` | `/recebimentos/{id}` | Atualiza um recebimento existente pelo ID | `200 OK` |
| `DELETE` | `/recebimentos/{id}` | Remove um recebimento pelo ID | `204 No Content` |
| `GET` | `/recebimentos/valor-total/cpf/{cpf}` | Retorna a soma dos valores recebidos por CPF | `200 OK` |
| `GET` | `/recebimentos/valor-total/periodo` | Retorna a soma dos valores entre `dataInicial` e `dataFinal` | `200 OK` |
| `POST` | `/recebimentos/importar-csv` | Importa recebimentos a partir de um arquivo CSV (`multipart/form-data`) | `201 Created` |

---

### 1. Criar Recebimento

- **Requisição (`POST /recebimentos`)**:
```json
{
  "cpf": "12345678901",
  "e2e": "E1234567820261021143000000000001",
  "data": "2026-10-21 14:30:00",
  "valor": "999990,99"
}
```

- **Resposta (`201 Created`)**:
```json
{
  "id": 1,
  "cpf": "12345678901",
  "e2e": "E1234567820261021143000000000001",
  "data": "2026-10-21 14:30:00",
  "valor": "999990,99"
}
```

---

### 2. Listar Todos os Recebimentos

- **Requisição**: `GET /recebimentos`
- **Resposta (`200 OK`)**:
```json
[
  {
    "id": 1,
    "cpf": "12345678901",
    "e2e": "E1234567820261021143000000000001",
    "data": "2026-10-21 14:30:00",
    "valor": "999990,99"
  }
]
```

---

### 3. Buscar Recebimento por ID

- **Requisição**: `GET /recebimentos/1`
- **Resposta (`200 OK`)**: Objeto `RecebimentoResponse` (ou `404 Not Found` caso não exista).

---

### 4. Atualizar Recebimento

- **Requisição (`PUT /recebimentos/1`)**:
```json
{
  "cpf": "12345678901",
  "e2e": "E1234567820261021143000000000001",
  "data": "2026-10-21 15:00:00",
  "valor": "1250,50"
}
```

---

### 5. Remover Recebimento

- **Requisição**: `DELETE /recebimentos/1`
- **Resposta**: `204 No Content` (ou `404 Not Found` caso não exista).

---

### 6. Consultar Valor Total por CPF

- **Requisição**: `GET /recebimentos/valor-total/cpf/12345678901` (aceita CPF com ou sem pontuação)
- **Resposta (`200 OK`)**:
```json
{
  "cpf": "12345678901",
  "valorTotal": "1001241,49"
}
```

---

### 7. Consultar Valor Total por Período

- **Requisição**: `GET /recebimentos/valor-total/periodo?dataInicial=2026-10-21 00:00:00&dataFinal=2026-10-22 23:59:59`
  *(Também aceita apenas a data `YYYY-MM-DD`, preenchendo automaticamente `00:00:00` para a data inicial e `23:59:59` para a data final).*
- **Resposta (`200 OK`)**:
```json
{
  "dataInicial": "2026-10-21 00:00:00",
  "dataFinal": "2026-10-22 23:59:59",
  "valorTotal": "1001491,49"
}
```

---

### 8. Importar Recebimentos via CSV

- **Requisição**: `POST /recebimentos/importar-csv` (`Content-Type: multipart/form-data`, campo `arquivo`).
- **Exemplo com `curl`**:
```bash
curl -X POST http://localhost:8080/recebimentos/importar-csv \
  -F "arquivo=@docs/exemplo-recebimentos.csv"
```
- **Resposta (`201 Created`)**:
```json
{
  "totalImportados": 3,
  "tiposIdentificadosPorColuna": {
    "cpf": "CPF",
    "e2e": "E2E",
    "data": "DATA",
    "valor": "VALOR"
  },
  "recebimentos": [
    {
      "id": 1,
      "cpf": "12345678901",
      "e2e": "E1234567820261021143000000000001",
      "data": "2026-10-21 14:30:00",
      "valor": "999990,99"
    },
    {
      "id": 2,
      "cpf": "98765432100",
      "e2e": "E1234567820261022091545000000002",
      "data": "2026-10-22 09:15:45",
      "valor": "1500,50"
    },
    {
      "id": 3,
      "cpf": "11122233344",
      "e2e": "E1234567820261023180010000000003",
      "data": "2026-10-23 18:00:10",
      "valor": "42,00"
    }
  ]
}
```

---

## Importação Inteligente de CSV

O parser [`CsvRecebimentoParser`](src/main/java/com/fontecode/recebimento/recebimento/application/parser/CsvRecebimentoParser.java) foi projetado para suportar diferentes variações de arquivos CSV:

1. **Ordem de Colunas Flexível**: As colunas podem estar em qualquer ordem (ex.: `valor;data;cpf;e2e`), pois o parser analisa o conteúdo de cada célula para classificar seu tipo (`CPF`, `E2E`, `DATA` ou `VALOR`).
2. **Com ou Sem Cabeçalho**: Identifica automaticamente se a primeira linha é um cabeçalho textual ou se já contém dados de recebimento.
3. **Delimitadores `;` ou `,`**: Suporta separação por ponto e vírgula (`;`) ou vírgula (`,`), recompondo valores decimais no padrão brasileiro (`999990,99`) mesmo quando não estão entre aspas.
4. **Arquivo de Exemplo**: Um arquivo pronto para teste está disponível em [`docs/exemplo-recebimentos.csv`](docs/exemplo-recebimentos.csv).

---

## Como Executar os Testes

Para rodar a suíte de testes unitários e de integração:

- **No Windows:**
  ```powershell
  .\mvnw.cmd test
  ```

- **No Linux / macOS:**
  ```bash
  ./mvnw test
  ```

---

## Documentação Complementar

- [Arquitetura Hexagonal (Ports & Adapters) e Clean Code no Projeto](docs/arquitetura-hexagonal-e-clean-code.md)
- [Requisitos Originais do Projeto](docs/requisitos.md)
- [Arquivo CSV de Exemplo](docs/exemplo-recebimentos.csv)
