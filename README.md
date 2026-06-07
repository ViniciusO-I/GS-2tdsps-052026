# 🛰️ SpaceWeather Guard — Satellite Ingestion Service

**Global Solution 2026/1 — Space Economy**  
**Disciplina:** Java Advanced  
**Aluno:** Vinicius Oliveira — RM559611  
**Turma:** 2TDSPS

---

## 📌 Sobre o Projeto

O **SpaceWeather Guard** é um sistema de monitoramento climático baseado em dados simulados de satélites, capaz de detectar anomalias ambientais e acionar protocolos de emergência da Defesa Civil de forma automatizada.

O sistema recebe leituras de sensores de satélite (temperatura do solo, umidade do ar, coordenadas geográficas), processa essas informações com um motor de análise inteligente baseado em protocolos da Defesa Civil e, em caso de risco crítico, emite alertas automáticos via mensageria Kafka.

### Problema que resolve

Desastres climáticos como incêndios florestais, enchentes e secas extremas causam perdas humanas e materiais que poderiam ser minimizadas com detecção antecipada. O SpaceWeather Guard automatiza essa detecção utilizando dados de satélite e inteligência artificial para gerar pareceres técnicos e acionar protocolos de evacuação antes que o desastre se concretize.

---

## 🏗️ Arquitetura

```
[Satélite / Sensor] 
        │
        ▼
┌─────────────────────────────────┐
│   satelite-ingestion-service    │  ← Este projeto
│                                 │
│  POST /api/alertas/analisar     │
│         │                       │
│   ┌─────▼──────┐                │
│   │  Feign     │──► API INPE    │  Dados históricos externos
│   └─────┬──────┘                │
│         │                       │
│   ┌─────▼──────┐                │
│   │  AI Analyst│  Motor de      │
│   │  (RAG)     │  análise com   │
│   └─────┬──────┘  protocolos   │
│         │         Defesa Civil  │
│   ┌─────▼──────┐                │
│   │  Oracle DB │  Persiste      │
│   └─────┬──────┘  alertas      │
│         │                       │
│   ┌─────▼──────┐                │
│   │   Kafka    │──► alertas-desastre-confirmados
│   └────────────┘                │
└─────────────────────────────────┘
```

---

## 🚀 Tecnologias Utilizadas

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.4.4 | Framework base |
| Spring Data JPA | — | Persistência com Oracle |
| Spring Security + JWT | jjwt 0.12.5 | Autenticação stateless RBAC |
| Spring HATEOAS | — | Links dinâmicos nos responses |
| Spring Cache | — | Cache em memória (`@Cacheable`) |
| Spring Kafka | — | Mensageria assíncrona |
| Spring Cloud OpenFeign | 2024.0.1 | Cliente HTTP para API externa |
| Springdoc OpenAPI | 2.5.0 | Documentação Swagger automática |
| Oracle JDBC (ojdbc11) | — | Banco de dados FIAP |
| Lombok | — | Redução de boilerplate |
| JUnit 5 + Mockito | — | Testes unitários (padrão AAA) |

---

## 📋 Pré-requisitos

- Java 21+
- Maven 3.9+ (ou usar o `./mvnw` incluso)
- Docker Desktop (para o Kafka)
- Acesso à rede/VPN da FIAP (para o Oracle)
- Credenciais Oracle FIAP (RM e senha do portal)

---

## ▶️ Como Executar

### 1. Clone o repositório

```bash
git clone https://github.com/ViniciusO-I/GS-2tdsps-052026.git
cd GS-2tdsps-052026
```

### 2. Suba o Kafka com Docker

```bash
docker run -d --name kafka \
  -p 9092:9092 \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  apache/kafka:3.7.0
```

Verifique se subiu:
```bash
docker ps
# Deve aparecer o container "kafka" com status Up
```

### 3. Configure as credenciais Oracle

```bash
export ORACLE_USER=SEU_RM_AQUI
export ORACLE_PASSWORD=SUA_SENHA_PORTAL_FIAP
```

> **Banco de dados:** `jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl`  
> As tabelas são criadas automaticamente pelo Hibernate (`ddl-auto=update`) na primeira execução.

### 4. Execute a aplicação

```bash
./mvnw spring-boot:run
```

Aguarde a mensagem:
```
Started SateliteIngestionServiceApplication in X.XXX seconds
```

### 5. Acesse o Swagger

```
http://localhost:8080/swagger-ui.html
```

---

## 🔐 Autenticação

A API utiliza **JWT stateless**. Dois usuários estão pré-configurados em memória:

| Usuário | Senha | Role | Permissão |
|---|---|---|---|
| `defesa_civil` | `defesa123` | `DEFESA_CIVIL` | Analisar alertas (POST) |
| `analista` | `analista123` | `ANALISTA` | Somente leitura |

### Obtendo o token

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "defesa_civil",
  "password": "defesa123"
}
```

Resposta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Use o token em todas as requisições seguintes:
```
Authorization: Bearer <token>
```

---

## 📡 Endpoints da API

### Analisar anomalia de satélite

```http
POST /api/alertas/analisar
Authorization: Bearer <token>
Content-Type: application/json

{
  "sateliteId": "SAT-BR-01",
  "latitude": -23.55,
  "longitude": -46.63,
  "temperaturaSolo": 44.5,
  "umidadeAr": 12.0,
  "status": "SUSPEITO"
}
```

**Regras de análise:**
- `temperaturaSolo > 40°C` e `umidadeAr < 15%` → **INCÊNDIO FLORESTAL CRÍTICO** → status `CONFIRMADO` + Kafka
- `temperaturaSolo > 44°C` → **CALOR EXTREMO CRÍTICO** → status `CONFIRMADO` + Kafka
- Demais condições → status `FALSO_POSITIVO`

Resposta (com HATEOAS):
```json
{
  "id": 1,
  "sateliteId": "SAT-BR-01",
  "status": "CONFIRMADO",
  "parecerIa": "⚠ ALERTA CRÍTICO — CALOR EXTREMO | ...",
  "dataHoraRegistro": "2026-06-06T14:39:00",
  "_links": {
    "self": { "href": "http://localhost:8080/api/alertas/1" }
  }
}
```

### Buscar alerta por ID (cacheado)

```http
GET /api/alertas/{id}
Authorization: Bearer <token>
```

### Health Check (público)

```http
GET /actuator/health
```

---

## 🧪 Executar Testes

Os testes unitários rodam **sem necessidade de Oracle ou Kafka** (100% em memória com Mockito):

```bash
./mvnw test
```

Resultado esperado:
```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**Cenários testados (`AlertaClimaticoServiceTest`):**
1. IA retorna risco CRÍTICO → status `CONFIRMADO` + Kafka publicado
2. IA sem risco detectado → status `FALSO_POSITIVO` + Kafka silencioso
3. API externa (INPE) falha → fallback resiliente, serviço continua
4. Busca por ID inexistente → `RuntimeException` com mensagem correta

---

## 📐 Requisitos da Disciplina Atendidos

| Requisito Java Advanced | Implementação |
|---|---|
| Spring Data JPA + Oracle | `AlertaClimaticoRepository`, `tb_alertas_climaticos` |
| Spring Security + JWT | `SecurityConfig`, `JwtUtil`, `JwtAuthFilter`, `AuthController` |
| HATEOAS | `AlertaClimatico extends RepresentationModel`, `linkTo/methodOn` |
| Cache | `@EnableCaching`, `@Cacheable("alertas")`, `@CacheEvict` |
| CORS | `CorsConfigurationSource` em `SecurityConfig` |
| Swagger/OpenAPI | `springdoc-openapi-starter-webmvc-ui`, `@Tag`, `@Operation` |
| Consumo de API externa | `ClimaExternoClient` via OpenFeign → API INPE |
| Mensageria | `KafkaTemplate` → tópico `alertas-desastre-confirmados` |
| IA com RAG | `SateliteAiAnalyst` com motor de regras baseado em protocolos Defesa Civil |
| Testes unitários | `AlertaClimaticoServiceTest` — 4 cenários, padrão AAA + Mockito |

---

## 📁 Estrutura do Projeto

```
satelite-ingestion-service/
├── src/main/java/br/com/fiap/satelite/
│   ├── SateliteIngestionServiceApplication.java
│   ├── client/
│   │   └── ClimaExternoClient.java          # Feign → API INPE
│   ├── config/
│   │   ├── SecurityConfig.java              # JWT + RBAC + CORS
│   │   ├── jwt/
│   │   │   ├── JwtUtil.java                 # Geração e validação de tokens
│   │   │   └── JwtAuthFilter.java           # Filtro de autenticação
│   │   └── vector/
│   │       └── VectorStoreConfig.java       # Base de conhecimento RAG
│   ├── controller/
│   │   ├── AlertaController.java            # POST /analisar, GET /{id}
│   │   └── AuthController.java              # POST /auth/login
│   ├── domain/
│   │   ├── AlertaClimatico.java             # Entidade JPA + HATEOAS
│   │   ├── StatusAlerta.java                # Enum: SUSPEITO, CONFIRMADO, FALSO_POSITIVO
│   │   └── dto/
│   │       └── ClimaExternoDto.java         # DTO para API INPE
│   ├── repository/
│   │   └── AlertaClimaticoRepository.java
│   └── service/
│       ├── AlertaClimaticoService.java      # Orquestração: Feign + IA + Kafka + JPA
│       └── ai/
│           └── SateliteAiAnalyst.java       # Motor de análise RAG
└── src/test/java/br/com/fiap/satelite/
    ├── SateliteIngestionServiceApplicationTests.java
    └── service/
        └── AlertaClimaticoServiceTest.java  # 4 testes unitários Mockito
```

---

## ⚙️ Variáveis de Ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `ORACLE_USER` | `SEU_RM_AQUI` | RM do aluno (usuário Oracle FIAP) |
| `ORACLE_PASSWORD` | `SUA_SENHA_AQUI` | Senha do portal FIAP |
| `KAFKA_SERVERS` | `localhost:9092` | Endereço do broker Kafka |
| `JWT_SECRET` | valor padrão longo | Chave de assinatura JWT |

---

*Projeto desenvolvido para a Global Solution 2026/1 — FIAP*
