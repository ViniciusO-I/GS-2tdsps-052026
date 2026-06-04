package br.com.fiap.satelite.service;

import br.com.fiap.satelite.client.ClimaExternoClient;
import br.com.fiap.satelite.domain.AlertaClimatico;
import br.com.fiap.satelite.domain.StatusAlerta;
import br.com.fiap.satelite.dto.ClimaExternoDto;
import br.com.fiap.satelite.repository.AlertaClimaticoRepository;
import br.com.fiap.satelite.service.ai.SateliteAiAnalyst;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AlertaClimaticoService {

    private final AlertaClimaticoRepository repository;
    private final ClimaExternoClient climaClient;
    private final SateliteAiAnalyst aiAnalyst;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public AlertaClimaticoService(AlertaClimaticoRepository repository, ClimaExternoClient climaClient,
                                  SateliteAiAnalyst aiAnalyst, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.climaClient = climaClient;
        this.aiAnalyst = aiAnalyst;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @CacheEvict(value = "alertas", allEntries = true) // Limpa o cache quando um novo alerta for processado
    public AlertaClimatico executarAnaliseDeDesastre(AlertaClimatico alertaSuspeito) {

        // 1. Chamada Feign (Requisito: Consumo de API externa)
        String dadosHistoricos = "Sem dados externos no momento";
        try {
            ClimaExternoDto historico = climaClient.buscarDadosHistoricos(alertaSuspeito.getLatitude(), alertaSuspeito.getLongitude());
            dadosHistoricos = historico.toString();
        } catch (Exception e) {
            // Fallback resiliente caso a API externa falhe
        }

        // 2. Processamento com IA Avançada (Requisito: Spring AI + RAG + Tooling)
        String parecerIa = aiAnalyst.analisarAnomaliaComRAG(alertaSuspeito, dadosHistoricos);
        alertaSuspeito.setParecerIa(parecerIa);
        alertaSuspeito.setDataHoraRegistro(LocalDateTime.now());

        if (parecerIa.contains("CRÍTICO") || parecerIa.contains("EVACUAÇÃO")) {
            alertaSuspeito.setStatus(StatusAlerta.CONFIRMADO);
            // 3. Mensageria (Requisito: Emitir Alerta Automático via Kafka)
            kafkaTemplate.send("alertas-desastre-confirmados", "Alerta crítico nas coordenadas: " + alertaSuspeito.getLatitude());
        } else {
            alertaSuspeito.setStatus(StatusAlerta.FALSO_POSITIVO);
        }

        return repository.save(alertaSuspeito);
    }

    @Cacheable(value = "alertas") // Requisito: Cache de performance para leitura
    public AlertaClimatico buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Alerta não encontrado"));
    }
}