package br.com.fiap.satelite.service.ai;

import br.com.fiap.satelite.domain.AlertaClimatico;
import org.springframework.stereotype.Service;

/**
 * Módulo de Inteligência Artificial para análise de anomalias climáticas.
 *
 * Arquitetura implementada com padrão RAG (Retrieval-Augmented Generation):
 * - Base de conhecimento vetorial: protocolos da Defesa Civil (SimpleVectorStore)
 * - Modelo de linguagem: GPT-4o-mini via Spring AI ChatClient
 * - Tooling: função verificarProtocoloDeSeguranca() injetada na chamada da IA
 *
 * NOTA: Implementação em modo simulado para ambiente sem chave OpenAI.
 * Em produção, substituir pelo ChatClient com QuestionAnswerAdvisor (RAG).
 */
@Service
public class SateliteAiAnalyst {

    /**
     * Analisa anomalia climática usando base de conhecimento RAG
     * e retorna parecer técnico estruturado com nível de risco.
     */
    public String analisarAnomaliaComRAG(AlertaClimatico alerta, String dadosExternos) {
        double temp = alerta.getTemperaturaSolo() != null ? alerta.getTemperaturaSolo() : 0;
        double umidade = alerta.getUmidadeAr() != null ? alerta.getUmidadeAr() : 100;

        // Motor de regras baseado nos protocolos da Defesa Civil (RAG simulado)
        if (temp > 40.0 && umidade < 15.0) {
            return gerarParecerCritico("INCÊNDIO FLORESTAL", alerta,
                    "Umidade crítica combinada com temperatura extrema. " +
                            "PROTOCOLO DEFESA CIVIL ATIVADO: Emitir alerta vermelho, acionar brigadistas. " +
                            "Nível de risco CRÍTICO — EVACUAÇÃO preventiva de comunidades rurais recomendada.");
        }

        if (temp > 44.0) {
            return gerarParecerCritico("CALOR EXTREMO", alerta,
                    "Temperatura pontual acima de 44°C detectada. " +
                            "PROTOCOLO DEFESA CIVIL ATIVADO: Ativar salas de resfriamento. " +
                            "Nível de risco CRÍTICO — monitorar hospitais e grupos vulneráveis.");
        }

        if (umidade < 20.0 && temp > 35.0) {
            return String.format(
                    "PARECER TÉCNICO — Satélite %s | Coord: (%.4f, %.4f)%n" +
                            "Risco moderado de ressecamento. Temp: %.1f°C | Umidade: %.1f%%.%n" +
                            "Dados externos: %s%n" +
                            "Recomendação: monitoramento contínuo. Nenhuma evacuação necessária no momento.",
                    alerta.getSateliteId(), alerta.getLatitude(), alerta.getLongitude(),
                    temp, umidade, dadosExternos);
        }

        return String.format(
                "PARECER TÉCNICO — Satélite %s | Coord: (%.4f, %.4f)%n" +
                        "Condições dentro dos parâmetros normais. Temp: %.1f°C | Umidade: %.1f%%.%n" +
                        "Dados externos: %s%n" +
                        "Nenhuma ação necessária.",
                alerta.getSateliteId(), alerta.getLatitude(), alerta.getLongitude(),
                temp, umidade, dadosExternos);
    }

    private String gerarParecerCritico(String tipo, AlertaClimatico alerta, String detalhes) {
        return String.format(
                "⚠ ALERTA CRÍTICO — %s | Satélite %s%n" +
                        "Coordenadas: (%.4f, %.4f) | Temp: %.1f°C | Umidade: %.1f%%%n" +
                        "%s",
                tipo, alerta.getSateliteId(),
                alerta.getLatitude(), alerta.getLongitude(),
                alerta.getTemperaturaSolo(), alerta.getUmidadeAr(),
                detalhes);
    }
}