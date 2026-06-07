# DevOps — Azure Pipelines: SpaceWeather Guard

**Disciplina:** DevOps Tools & Cloud Computing — FIAP 2026/1  
**Aluno:** Vinicius Oliveira — RM559611 | Turma: 2TDSPS

---

## Visão Geral

Este documento descreve como configurar o pipeline de CI/CD no Azure DevOps para o projeto `satelite-ingestion-service`, seguindo o modelo apresentado pelo Prof. João Menk.

```
GitHub (push) → Azure Pipelines (CI: build + test + JAR) → Azure App Service (CD: deploy)
```

---

## Arquivos criados

| Arquivo | Descrição |
|---|---|
| `azure-pipelines.yml` | Pipeline CI: build Maven, testes, publicação do JAR |
| `infra-azure.sh` | Script Azure CLI para criar a infraestrutura na nuvem |

---

## Passo 1 — Criar a infraestrutura no Azure

### Opção A — Azure Cloud Shell (recomendado para avaliação)

1. Acesse [portal.azure.com](https://portal.azure.com)
2. Clique no ícone **Cloud Shell** (ícone `>_` no topo)
3. Selecione **Bash**
4. Faça upload do arquivo `infra-azure.sh`:
    - Clique no ícone de upload no Cloud Shell
    - Selecione o arquivo
5. Execute:
   ```bash
   chmod +x infra-azure.sh
   ./infra-azure.sh
   ```

### Opção B — Azure CLI local

```bash
az login
bash infra-azure.sh
```

Após execução, a URL da aplicação será:
```
https://app-spaceweather-rm559611.azurewebsites.net
```

---

## Passo 2 — Criar organização no Azure DevOps

1. Acesse [dev.azure.com](https://dev.azure.com)
2. Clique em **New organization** (ou use uma existente)
3. Crie um **Project**: `SpaceWeatherGuard`

---

## Passo 3 — Conectar o repositório GitHub

1. No projeto, vá em **Project Settings → Service connections**
2. Clique em **New service connection → GitHub**
3. Escolha **OAuth** e autorize com sua conta GitHub
4. Nomeie a conexão como `github-connection`

---

## Passo 4 — Criar o Build Pipeline (CI)

1. No menu lateral, clique em **Pipelines → Pipelines**
2. Clique em **New pipeline**
3. Selecione **GitHub**
4. Escolha o repositório `ViniciusO-I/GS-2tdsps-052026`
5. Selecione **Existing Azure Pipelines YAML file**
6. Em **Path**, escolha `/azure-pipelines.yml`
7. Clique em **Continue** e depois **Run**

O pipeline irá:
- Fazer cache do repositório Maven
- Compilar o projeto com Java 21
- Rodar os 5 testes unitários (sem Oracle, sem Kafka — usa H2 em memória)
- Publicar o JAR como artefato `drop`

---

## Passo 5 — Criar o Release Pipeline (CD)

1. No menu lateral, clique em **Pipelines → Releases**
2. Clique em **New pipeline**
3. Selecione o template **Azure App Service deployment**
4. Configure:

### Artifact (fonte)
- Clique em **Add an artifact**
- Source type: **Build**
- Project: `SpaceWeatherGuard`
- Source (build pipeline): selecione o pipeline criado no Passo 4
- Default version: **Latest**
- Source alias: `drop`
- Clique no ícone de raio ⚡ e ative **Continuous deployment trigger**

### Stage — Deploy to Azure App Service
- Clique em **1 job, 1 task** no stage
- Selecione a task **Deploy Azure App Service**
- Preencha:

| Campo | Valor |
|---|---|
| Azure subscription | Selecione sua assinatura (autorize se necessário) |
| App type | Web App on Linux |
| App Service name | `app-spaceweather-rm559611` |
| Package or folder | `$(System.DefaultWorkingDirectory)/**/*.jar` |
| Runtime Stack | Java 21 |
| Startup command | `java -jar /home/site/wwwroot/*.jar` |

5. Clique em **Save** e depois em **Create release**

---

## Passo 6 — Configurar variáveis de ambiente no App Service

No portal Azure:

1. Acesse o App Service `app-spaceweather-rm559611`
2. Vá em **Configuration → Application settings**
3. Atualize:

| Nome | Valor |
|---|---|
| `ORACLE_USER` | Seu RM (ex: `rm559611`) |
| `ORACLE_PASSWORD` | Sua senha do portal FIAP |
| `KAFKA_SERVERS` | `localhost:9092` |
| `JWT_SECRET` | `spaceweather-secret-key-fiap-2026-gs-must-be-very-long` |

4. Clique em **Save**

---

## Fluxo completo após configuração

```
1. git push origin main
        ↓
2. Azure Pipelines detecta o push (trigger CI)
        ↓
3. Build Pipeline roda:
   - mvn package
   - 5 testes unitários
   - Publica JAR como artefato "drop"
        ↓
4. Release Pipeline dispara automaticamente (CD trigger)
        ↓
5. JAR é deployado no Azure App Service
        ↓
6. App disponível em:
   https://app-spaceweather-rm559611.azurewebsites.net/swagger-ui.html
```

---

## Verificação após deploy

```bash
# Health check
curl https://app-spaceweather-rm559611.azurewebsites.net/actuator/health

# Swagger UI
https://app-spaceweather-rm559611.azurewebsites.net/swagger-ui.html
```

---

*Projeto desenvolvido para a Global Solution 2026/1 — FIAP*
