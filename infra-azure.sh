#!/bin/bash
# ============================================================
#  SpaceWeather Guard — Infraestrutura Azure
#  Disciplina: DevOps Tools & Cloud Computing — FIAP 2026/1
#  Aluno: Vinicius Oliveira — RM559611 | Turma: 2TDSPS
#
#  Execute este script no Azure Cloud Shell ou
#  localmente com Azure CLI instalado e autenticado:
#    az login
#    bash infra-azure.sh
# ============================================================

set -e  # Para na primeira falha

# ── Variáveis ──────────────────────────────────────────────
rg="rg-spaceweather-rm559611"
location="brazilsouth"
appServicePlanName="plan-spaceweather-rm559611"
webAppName="app-spaceweather-rm559611"   # deve ser único globalmente
sku="F1"                                  # Free tier (F1) para avaliação
runtime="JAVA:21-java21"                  # Java 21 LTS no App Service Linux

echo "=================================================="
echo " SpaceWeather Guard — Criando infraestrutura Azure"
echo " Aluno: Vinicius Oliveira — RM559611"
echo "=================================================="

# ── 1. Resource Group ─────────────────────────────────────
echo ""
echo "[1/4] Criando Resource Group: $rg em $location..."
az group create \
  --name "$rg" \
  --location "$location"

echo "✓ Resource Group criado."

# ── 2. App Service Plan ────────────────────────────────────
echo ""
echo "[2/4] Criando App Service Plan: $appServicePlanName (SKU: $sku)..."
az appservice plan create \
  --name "$appServicePlanName" \
  --resource-group "$rg" \
  --location "$location" \
  --sku "$sku" \
  --is-linux

echo "✓ App Service Plan criado."

# ── 3. Web App ─────────────────────────────────────────────
echo ""
echo "[3/4] Criando Web App: $webAppName (runtime: $runtime)..."
az webapp create \
  --name "$webAppName" \
  --resource-group "$rg" \
  --plan "$appServicePlanName" \
  --runtime "$runtime"

echo "✓ Web App criado."

# ── 4. Variáveis de ambiente no App Service ────────────────
echo ""
echo "[4/4] Configurando variáveis de ambiente..."
echo "      (substitua os valores abaixo pelas suas credenciais reais)"

az webapp config appsettings set \
  --name "$webAppName" \
  --resource-group "$rg" \
  --settings \
    ORACLE_USER="rm559611" \
    ORACLE_PASSWORD="031290" \
    KAFKA_SERVERS="localhost:9092" \
    JWT_SECRET="spaceweather-secret-key-fiap-2026-gs-must-be-very-long" \
    SPRING_PROFILES_ACTIVE="prod" \
    SERVER_PORT="8080" \
    WEBSITES_PORT="8080"

echo "✓ Variáveis configuradas."

# ── Resumo ─────────────────────────────────────────────────
echo ""
echo "=================================================="
echo " Infraestrutura criada com sucesso!"
echo ""
echo " URL da aplicação:"
echo "   https://${webAppName}.azurewebsites.net"
echo ""
echo " Swagger UI (após deploy):"
echo "   https://${webAppName}.azurewebsites.net/swagger-ui.html"
echo ""
echo " Health Check:"
echo "   https://${webAppName}.azurewebsites.net/actuator/health"
echo ""
echo " IMPORTANTE: Atualize as variáveis de ambiente"
echo " ORACLE_USER e ORACLE_PASSWORD no portal Azure"
echo " com as suas credenciais reais FIAP."
echo "=================================================="