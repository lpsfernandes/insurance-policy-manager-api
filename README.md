# 📘 Projeto: Gestão de Apólices

Sistema para gerenciamento de apólices de seguro, com integração a serviços externos, mensageria via Kafka, monitoramento com Prometheus e Grafana, e persistência em PostgreSQL.

---

## 🚀 Funcionalidades da API

- Cadastro e gerenciamento de apólices de seguro
- Integração com serviço de prevenção à fraude via OpenFeign
- Processamento assíncrono de eventos via Kafka
- Migração de banco de dados com Flyway
- Exposição de métricas via Actuator e Prometheus
- Observabilidade com Micrometer Tracing

---

## 🔌 Integrações

| Tecnologia     | Finalidade                                |
|----------------|--------------------------------------------|
| **Kafka**      | Processamento de eventos (`topico-eventos`, `topico-pagamentos`, `topico-subscricao`)  
| **Zookeeper**  | Coordenação para o Kafka  
| **PostgreSQL** | Persistência de dados da aplicação  
| **PgAdmin**    | Interface web para gerenciamento do banco  
| **Prometheus** | Coleta de métricas da aplicação  
| **Grafana**    | Visualização de métricas e dashboards  
| **MockServer** | Simulação de serviço externo de fraude  

---

## 🐳 Subindo o ambiente com Docker

1. Clone o repositório:

```bash
git clone https://github.com/lpsfernandes/gestao_apolices.git
cd gestao_apolices
```

- Suba os containers:
docker-compose up -d


- Verifique os serviços:
- API: http://localhost:8080
- PgAdmin: http://localhost:5050 (login: admin@admin.com / senha: admin123)
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (login: admin / senha: admin)
- MockServer: http://localhost:1080

##  📈 Monitoramento
- Métricas expostas em: http://localhost:8080/actuator/prometheus
- Prometheus coleta essas métricas a cada 15s (configurável)
- Grafana pode importar dashboards como:
- Spring Boot JVM: ID 4701
- Métricas customizadas: ID 17053


## 📡 Endpoints da API
A API está disponível em: http://localhost:8080/api/v1/policy
## 🔹 1. Criar nova apólice
- POST /api/v1/policy
- Descrição: Registra uma nova apólice de seguro para processamento.
- Payload de entrada:
```json
{
  "clientId": "CUSTOMERID_HIGH_RISK",
  "productId": 123456,
  "category": "LIFE",
  "monthlyPremium": 75.25,
  "insuredAmount": 125000.00,
  "coverages": {
    "Roubo": 100000.25,
    "Perda Total": 100000.25,
    "Colisão com Terceiros": 75000.00
  },
  "assistances": [
    "Guincho até 250km",
    "Troca de Óleo",
    "Chaveiro 24h"
  ],
  "paymentMethod": "CREDIT_CARD",
  "salesChannel": "MOBILE"
}
```
- Resposta:
```json
{
  "id": "45a68bb6-94a4-4c66-b009-b0f2a56fb23f",
  "createdAt": "2025-10-05T05:16:14.5296693Z"
}
```
## 🔹 2. Buscar apólice por ID
- GET /api/v1/policy/{id}
- Descrição: Retorna os dados de uma apólice específica.
- Resposta:
```json
{
  "id": "8c9b6900-dc6c-4c3e-9063-8b81bffce63f",
  "clientId": "CUSTOMERID_HIGH_RISK",
  "productId": 123456,
  "category": "LIFE",
  "status": "REJECTED",
  "riskClassification": "HIGH_RISK",
  "monthlyPremium": 75.25,
  "insuredAmount": 125000,
  "coverages": {
    "Roubo": 100000.25,
    "Perda Total": 100000.25,
    "Colisão com Terceiros": 75000
  },
  "assistances": [
    "Troca de Óleo",
    "Guincho até 250km",
    "Chaveiro 24h"
  ],
  "paymentMethod": "CREDIT_CARD",
  "paymentDate": "2025-10-05T05:03:17.120667Z",
  "reason": "Subscricao nao aprovada",
  "createdAt": "2025-10-05T05:03:17.120667Z",
  "history": [
    {
      "status": "RECEIVED",
      "timestamp": "2025-10-05T05:03:17.120667Z"
    }
  ],
  "salesChannel": "MOBILE"
}
```

## 🔹 3. Listar apólices (todas ou por cliente)
- GET /api/v1/policy
- Descrição: Lista todas as apólices ou apenas as de um cliente específico.
  - Parâmetros opcionais:
    - clientId: filtra por cliente
    - page, size: paginação
    - sortField, sortDirection: ordenação
- Exemplo:
```bash
  GET /api/v1/policy?clientId=123456&page=0&size=5&sortField=createdAt&sortDirection=DESC
```
- Resposta:
```json
{
  "content": [
    {
      "id": "45a68bb6-94a4-4c66-b009-b0f2a56fb23f",
      "clientId": "CUSTOMERID_HIGH_RISK",
      "productId": 123456,
      "category": "LIFE",
      "status": "PENDING",
      "riskClassification": "HIGH_RISK",
      "monthlyPremium": 75.25,
      "insuredAmount": 125000,
      "coverages": {
        "Roubo": 100000.25,
        "Perda Total": 100000.25,
        "Colisão com Terceiros": 75000
      },
      "assistances": [
        "Guincho até 250km",
        "Troca de Óleo",
        "Chaveiro 24h"
      ],
      "paymentMethod": "CREDIT_CARD",
      "createdAt": "2025-10-05T05:16:14.529669Z",
      "history": [
        {
          "status": "RECEIVED",
          "timestamp": "2025-10-05T05:16:14.529669Z"
        }
      ],
      "salesChannel": "MOBILE"
    },
    {
      "id": "8f006ee1-e23f-49ed-b7b1-e2a39a07cb48",
      "clientId": "CUSTOMERID_HIGH_RISK",
      "productId": 123456,
      "category": "LIFE",
      "status": "APPROVED",
      "riskClassification": "HIGH_RISK",
      "monthlyPremium": 75.25,
      "insuredAmount": 125000,
      "coverages": {
        "Roubo": 100000.25,
        "Perda Total": 100000.25,
        "Colisão com Terceiros": 75000
      },
      "assistances": [
        "Troca de Óleo",
        "Guincho até 250km",
        "Chaveiro 24h"
      ],
      "paymentMethod": "CREDIT_CARD",
      "paymentDate": "2025-10-05T05:03:17.120667Z",
      "subscriptionDate": "2025-10-05T05:03:17.120667Z",
      "createdAt": "2025-10-05T05:14:07.265225Z",
      "history": [
        {
          "status": "RECEIVED",
          "timestamp": "2025-10-05T05:14:07.265225Z"
        }
      ],
      "salesChannel": "MOBILE"
    },
    {
      "id": "8c9b6900-dc6c-4c3e-9063-8b81bffce63f",
      "clientId": "CUSTOMERID_HIGH_RISK",
      "productId": 123456,
      "category": "LIFE",
      "status": "REJECTED",
      "riskClassification": "HIGH_RISK",
      "monthlyPremium": 75.25,
      "insuredAmount": 125000,
      "coverages": {
        "Roubo": 100000.25,
        "Perda Total": 100000.25,
        "Colisão com Terceiros": 75000
      },
      "assistances": [
        "Troca de Óleo",
        "Guincho até 250km",
        "Chaveiro 24h"
      ],
      "paymentMethod": "CREDIT_CARD",
      "paymentDate": "2025-10-05T05:03:17.120667Z",
      "reason": "Subscricao nao aprovada",
      "createdAt": "2025-10-05T05:03:17.120667Z",
      "history": [
        {
          "status": "RECEIVED",
          "timestamp": "2025-10-05T05:03:17.120667Z"
        }
      ],
      "salesChannel": "MOBILE"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": false,
      "unsorted": false,
      "sorted": true
    },
    "offset": 0,
    "unpaged": false,
    "paged": true
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 3,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "unsorted": false,
    "sorted": true
  },
  "first": true,
  "numberOfElements": 3,
  "empty": false
}
```
## 🔹 4. Atualizar status da apólice
- PATCH /api/v1/policy/{id}
- Descrição: Atualiza o status de uma apólice.
- Payload de entrada:
```json
{
"status": "APPROVED"
}
```
> Status reconhecidos pelo payload: VALIDATED, PENDING, REJECTED, APPROVED, CANCELED```
- Resposta:
```json
{
  "id": "45a68bb6-94a4-4c66-b009-b0f2a56fb23f",
  "clientId": "CUSTOMERID_HIGH_RISK",
  "productId": 123456,
  "category": "LIFE",
  "status": "CANCELED",
  "riskClassification": "HIGH_RISK",
  "monthlyPremium": 75.25,
  "insuredAmount": 125000,
  "coverages": {
    "Roubo": 100000.25,
    "Perda Total": 100000.25,
    "Colisão com Terceiros": 75000
  },
  "assistances": [
    "Guincho até 250km",
    "Troca de Óleo",
    "Chaveiro 24h"
  ],
  "paymentMethod": "CREDIT_CARD",
  "createdAt": "2025-10-05T05:16:14.529669Z",
  "history": [
    {
      "status": "RECEIVED",
      "timestamp": "2025-10-05T05:16:14.529669Z"
    }
  ],
  "salesChannel": "MOBILE"
}
```
### Mais detalhes: http://localhost:8080/swagger-ui/index.html#

## 📊 Integração com Kafka e uso do Offset Explorer 3

A aplicação utiliza Kafka para comunicação assíncrona com os serviços de pagamento e subscrição. Os tópicos estão divididos em dois grupos:

### 🔸 Tópicos para Consulta (Producer)

Esta API **gera eventos** que podem ser acompanhados via Offset Explorer:

- `topico-eventos`: Ficam registrados os eventos referentes a mudança dos estados de uma apólice

#### 📍 Como acompanhar os eventos gerados:

1. Abra o **Offset Explorer 3**.
2. Conecte-se ao broker Kafka: `localhost:9092`.
3. Navegue até os tópicos listados acima.
4. Selecione a partição desejada e navegua até a aba Data, altere em Properties o tipo de dados para String e clique no Play.
5. Observe os eventos gerados pela API em tempo real.

> Útil para validar se os eventos estão sendo publicados corretamente após chamadas à API.

---

### 🔸 Tópicos de Consumo (Consumer)

Esta API **consome eventos** para processar ações internas. Para simular cenários, você pode **inserir mensagens manualmente** via Offset Explorer:

- `topico-pagamentos`: Topico para resultado de processamento dos pagamento, seja aprovado ou não
- `topico-subscricao`: Topico para resultado da analise da subscricao, seja aprovado ou não

#### 🧪 Como simular eventos:

1. No Offset Explorer, conecte-se ao broker `localhost:9092`.
2. Selecione o tópico desejado, Navegua até a aba Data e altere em Properties o tipo de dados para String.
3. Selecione a partição onde a mensagem sera gerada, navegua até a aba Data e clique no botão de +
4. Insira o payload JSON conforme o schema esperado pela API.
5. Envie a mensagem para a partição desejada.

##### 💡 Exemplo de payload para `topico-pagamentos`:

Este evento representa uma confirmação ou rejeição de pagamento relacionada a uma apólice, publicado no tópico Kafka `topico-pagamentos`. Abaixo estão os campos que compõem o contrato e suas respectivas finalidades:

| Campo               | Tipo            | Obrigatório | Finalidade                                                             |
|---------------------|-----------------|-------------|------------------------------------------------------------------------|
| `id`                | `String`        | ✅ Sim       | Identificador único do evento de pagamento. Usado para rastreamento.   |
| `orderId`           | `String`        | ✅ Sim       | Referência ao pedido de apólice associado ao pagamento.                |
| `paymentDateTime`   | `ZonedDateTime` | ❌ Não       | Data e hora do pagamento, incluindo fuso horário. Útil para auditoria. |
| `reason`            | `String`        | ❌ Não       | Detalhe de forma descritiva o motivo do status do evento               |
| `status`            | `Enum`          | ✅ Sim       | Resultado do pagamento: `APPROVED` ou `REJECTED`.                      |

```json
{
  "id": "pay-001",
  "orderId": "ord-789",
  "paymentDateTime": "2025-10-05T09:15:00Z",
  "reason": "Pagamento confirmado via PIX",
  "status": "APPROVED"
}
```

#### 💡 topico-subscricao:

Este evento representa uma solicitação de subscrição de seguro que será publicada no tópico Kafka `topico-subscricao`. Abaixo estão os campos que compõem o contrato e suas respectivas finalidades:

| Campo                  | Tipo            | Obrigatório | Finalidade                                                                 |
|------------------------|-----------------|-------------|----------------------------------------------------------------------------|
| `id`                   | `String`        | ✅ Sim       | Identificador único da subscrição. Usado para rastrear o evento.          |
| `orderId`              | `String`        | ✅ Sim       | Referência ao pedido de apólice associado à subscrição.                   |
| `subscriptionDateTime`| `ZonedDateTime` | ❌ Não       | Data e hora da subscrição, incluindo fuso horário. Útil para auditoria.   |
| `reason`               | `String`        | ❌ Não       | Detalhe de forma descritiva o motivo do status do evento                |
| `status`               | `Enum`          | ✅ Sim       | Resultado da análise: `APPROVED` ou `REJECTED`. Define o desfecho da subscrição. |

```json
{
  "id": "sub-001",
  "orderId": "ord-789",
  "subscriptionDateTime": "2025-10-05T09:00:00Z",
  "reason": "Cliente elegível com perfil preferencial",
  "status": "APPROVED"
}
```

## 🔐 Regras de Validação

## 🔹 Regras de Validação por Classificação de Risco e Tipo de Seguro

A solicitação de apólice é validada conforme o perfil de risco do cliente e o tipo de seguro contratado. Abaixo estão os limites máximos permitidos para aprovação automática. Caso os valores ultrapassem os limites definidos, a apólice será rejeitada.

| Risco      | Seguro Vida           | Seguro Residencial     | Seguro Auto           | Outros Tipos de Seguro | Regra de Aprovação                         |
|-----------------------|------------------------|--------------------------|------------------------|-------------------------|--------------------------------------------|
| **LOW_RISK**           | ≤ R$ 500.000,00        | ≤ R$ 500.000,00          | ≤ R$ 350.000,00        | ≤ R$ 255.000,00         | Aprovado se dentro dos limites             |
| **HIGH_RISK**        | —                      | ≤ R$ 150.000,00          | ≤ R$ 250.000,00        | ≤ R$ 125.000,00         | Aprovado se dentro dos limites             |
| **MEDIUM_RISK**      | < R$ 800.000,00        | < R$ 450.000,00          | < R$ 450.000,00        | ≤ R$ 375.000,00         | Aprovado se dentro dos limites             |
| **UNCLASSIFIED_RISK**    | ≤ R$ 200.000,00        | ≤ R$ 200.000,00          | ≤ R$ 75.000,00         | ≤ R$ 55.000,00          | Aprovado se dentro dos limites             |

> * A classificação do cliente é obtida via serviço de análise de risco. 
> 
> * Caso os valores excedam os limites definidos para o perfil do cliente, a apólice será rejeitada.
>
> * Todos os valores são configuraveis na tabela tb_rules
### 🔹 Regras de transição de status

O endpoint de atualização de status segue uma lógica de transição definida pelo enum `Status`. Nem todos os status podem ser alterados livremente — há restrições específicas:

| Status atual | Transições permitidas                      |
|--------------|---------------------------------------------|
| RECEIVED     | VALIDATED, CANCELED                         |
| VALIDATED    | PENDING, REJECTED                           |
| PENDING      | APPROVED, REJECTED, CANCELED, PENDING       |
| REJECTED     | *(não permite transição)*                   |
| APPROVED     | *(não permite transição)*                   |
| CANCELED     | *(não permite transição)*                   |

###  🔹 Regra deprocessamento de Apólice

A API é responsável por processar eventos Kafka relacionados a apólices, atualizando seu status com base nos dados recebidos. Ele consome dois tipos de eventos:

---

#### 🧾 1. Evento de Pagamento 

- Busca a apólice pelo `orderId` contido no evento.
- Se o pagamento **não for aprovado** (`status != APPROVED`):
  - A apólice é marcada como `REJECTED`.
  - O campo `reason` é preenchido com a justificativa do evento ou com `"Pagamento não realizado"`.
- Se o pagamento **for aprovado**:
  - Se a apólice já tiver uma subscrição registrada, ela é marcada como `APPROVED`.
  - Caso contrário, permanece como `PENDING`.
  - A data de pagamento é registrada via `paymentDateTime`.

---

#### 🧾 2. Evento de Subscrição

- Busca a apólice pelo `orderId` contido no evento.
- Se a subscrição **não for aprovada** (`status != APPROVED`):
  - A apólice é marcada como `REJECTED`.
  - O campo `reason` é preenchido com a justificativa do evento ou com `"Subscrição não aprovada"`.
- Se a subscrição **for aprovada**:
  - Se a apólice já tiver um pagamento registrado, ela é marcada como `APPROVED`.
  - Caso contrário, permanece como `PENDING`.
  - A data de subscrição é registrada via `subscriptionDateTime`.

---

#### ✅ Finalização do Ciclo de Vida

Após qualquer o processamento de qualquer um dos evento verifica se o ciclo de vida da apólice está concluído:

Se estiver concluído:
- A data de finalização (finishedAt) é registrada.
- Um evento de saída é inserido na outbox para integração com outros sistemas.
- A apólice é persistida no banco de dados.

Essa lógica garante que a apólice só seja considerada finalizada quando tiver passado por subscrição e pagamento, com status final de APPROVED ou REJECTED.


### 🧪 Simulações de Risco via MockServer

O serviço de análise de risco é simulado pelo MockServer e responde ao endpoint `/analyze-risk` com base no campo `clientId` enviado no corpo da requisição de registro da apólice. Abaixo estão os matches configurados e suas respectivas classificações:

| clientId enviado           | clientId retornado           | Classificação de Risco | Ocorrências simuladas | Observações                                                  |
|----------------------------|------------------------------|------------------------|------------------------|--------------------------------------------------------------|
| `CUSTOMERID_HIGH_RISK`     | `CLIENTID_HIGH_RISK`         | `HIGH_RISK`            | 1 ocorrência de fraude | Transação fraudulenta simulada com `productId: 78900069`     |
| `CLIENTID_LOW_RISK`        | `CLIENTID_LOW_RISK`          | `LOW_RISK`             | Nenhuma                | Cliente com perfil seguro                                    |
| `CLIENTID_MEDIUM_RISK`     | `CLIENTID_MEDIUM_RISK`       | `MEDIUM_RISK`          | Nenhuma                | Cliente com risco intermediário                              |
| *(qualquer outro valor)*   | `7c2a27ba-71ef-4dd8-a3cf...` | `UNCLASSIFIED_RISK`    | Nenhuma                | Cliente sem histórico ou não mapeado                         |

##  📈 Métricas

#### 🔍 Principais métricas de saude da API

| Categoria         | Métricas coletadas                                                                 |
|-------------------|------------------------------------------------------------------------------------|
| **JVM**           | Uso de memória, GC (garbage collection), threads, buffers, classes carregadas     |
| **Sistema**       | Carga da CPU, tempo de uptime, uso de disco e swap (dependente do ambiente)       |
| **Processo**      | Tempo de execução, número de arquivos abertos, uso de CPU por processo            |
| **Tomcat/Undertow** | Conexões, tempo de resposta, requisições ativas (se aplicável ao servidor usado) |

#### 📊 Principais métricas de negócio da aplicação

| Categoria                                    | Métricas coletadas                                                   |
|----------------------------------------------|----------------------------------------------------------------------|
| **Apólices**                                 | Total de apólices criadas, aprovadas e rejeitadas                    |
| **Eventos de Pagamento**                     | Eventos consumidos, aprovados e rejeitados relacionados a pagamento  |
| **Eventos de Subscrição**                    | Eventos consumidos, aprovados e rejeitados relacionados a assinatura |
| **Eventos de Alteração do status da Apólice** | Total de eventos produzidos para tópicos Kafka                       |
| **Análise de Risco**                         | Classificação de risco por apólice                                   |
| **Tempo de Processamento**                   | Duração do processamento de apólices                                 |



## 🛠️ Configurações importantes
- Banco de dados: appdb com usuário admin e senha admin123
- Flyway: migrações em classpath:db/migration
- Kafka Listener com observabilidade ativada
- OpenFeign configurado para fraud-prevention-engine via MockServe

## ✅ Requisitos
- Java 17+
- Maven
- Docker e Docker Compose

## 📬 Contato
Desenvolvido por Luis Fernandes




