package br.com.fiap.satelite.service.ai;

import br.com.fiap.satelite.domain.AlertaClimatico;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import java.util.function.Function;

@Configuration
public class SateliteAiAnalyst {

    private final ChatClient chatClient;

    public SateliteAiAnalyst(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        // Configuração de RAG (Busca aumentada por documentos de contingência da Defesa Civil)
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }

    public String analisarAnomaliaComRAG(AlertaClimatico alerta, String dadosExternos) {
        String prompt = String.format(
                "Analise a seguinte anomalia climática detectada pelo satélite %s. " +
                        "Temperatura do Solo: %.2f°C, Umidade: %.2f%%. Coordenadas: Lati: %.4f, Long: %.4f. " +
                        "Dados Históricos do INPE: %s. " +
                        "Consulte os manuais de contingência da Defesa Civil através do seu contexto (RAG) e " +
                        "gere um parecer técnico com nível de risco e plano de evacuação estruturado.",
                alerta.getSateliteId(), alerta.getTemperaturaSolo(), alerta.getUmidadeAr(),
                alerta.getLatitude(), alerta.getLongitude(), dadosExternos
        );

        return this.chatClient.prompt()
                .user(prompt)
                .functions("verificarProtocoloDeSeguranca") // Tooling injetado na IA
                .call()
                .content();
    }

    @Bean
    @Description("Verifica o protocolo de criticidade padrão do sistema para a região do desastre")
    public Function<MockRegiaoRequest, String> verificarProtocoloDeSeguranca() {
        return request -> {
            if (request.latitude() < -20.0) return "Protocolo Sul-Sudeste: Alto risco de queimadas florestais.";
            return "Protocolo Norte-Nordeste: Alto risco de seca extrema.";
        };
    }
}

record MockRegiaoRequest(Double latitude, Double longitude) {}