package br.com.fiap.satelite.service;

import br.com.fiap.satelite.client.ClimaExternoClient;
import br.com.fiap.satelite.domain.AlertaClimatico;
import br.com.fiap.satelite.domain.StatusAlerta;
import br.com.fiap.satelite.domain.dto.ClimaExternoDto;
import br.com.fiap.satelite.repository.AlertaClimaticoRepository;
import br.com.fiap.satelite.service.ai.SateliteAiAnalyst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaClimaticoService — testes unitários")
class AlertaClimaticoServiceTest {

    @Mock private AlertaClimaticoRepository repository;
    @Mock private ClimaExternoClient climaClient;
    @Mock private SateliteAiAnalyst aiAnalyst;
    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private AlertaClimaticoService service;

    private AlertaClimatico alertaSuspeito;

    @BeforeEach
    void setUp() {
        alertaSuspeito = AlertaClimatico.builder()
                .sateliteId("SAT-001")
                .latitude(-23.55)
                .longitude(-46.63)
                .temperaturaSolo(42.0)
                .umidadeAr(10.0)
                .status(StatusAlerta.SUSPEITO)
                .build();
    }

    @Test
    @DisplayName("IA retorna CRÍTICO → status CONFIRMADO + Kafka publicado")
    void quandoIaRetornaCritico_deveConfirmarAlertaEPublicarKafka() {
        // Arrange
        when(climaClient.buscarDadosHistoricos(anyDouble(), anyDouble()))
                .thenReturn(new ClimaExternoDto());
        when(aiAnalyst.analisarAnomaliaComRAG(any(), anyString()))
                .thenReturn("Nível de risco CRÍTICO — recomenda-se EVACUAÇÃO imediata.");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AlertaClimatico resultado = service.executarAnaliseDeDesastre(alertaSuspeito);

        // Assert
        assertThat(resultado.getStatus()).isEqualTo(StatusAlerta.CONFIRMADO);
        assertThat(resultado.getParecerIa()).contains("CRÍTICO");
        verify(kafkaTemplate).send(eq("alertas-desastre-confirmados"), anyString());
        verify(repository).save(alertaSuspeito);
    }

    @Test
    @DisplayName("IA sem risco → status FALSO_POSITIVO + Kafka silencioso")
    void quandoIaNaoDetectaRisco_deveMarcarFalsoPositivo() {
        // Arrange
        when(climaClient.buscarDadosHistoricos(anyDouble(), anyDouble()))
                .thenReturn(new ClimaExternoDto());
        when(aiAnalyst.analisarAnomaliaComRAG(any(), anyString()))
                .thenReturn("Condições normais. Nenhuma ação necessária.");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AlertaClimatico resultado = service.executarAnaliseDeDesastre(alertaSuspeito);

        // Assert
        assertThat(resultado.getStatus()).isEqualTo(StatusAlerta.FALSO_POSITIVO);
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    @DisplayName("API externa falha → fallback resiliente, serviço continua")
    void quandoApiExternaFalha_deveUsarFallbackEContinuar() {
        // Arrange
        when(climaClient.buscarDadosHistoricos(anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("Timeout INPE"));
        when(aiAnalyst.analisarAnomaliaComRAG(any(), eq("Sem dados externos no momento")))
                .thenReturn("Análise inconclusiva.");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AlertaClimatico resultado = service.executarAnaliseDeDesastre(alertaSuspeito);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getParecerIa()).isEqualTo("Análise inconclusiva.");
    }

    @Test
    @DisplayName("buscarPorId com ID inexistente → RuntimeException")
    void quandoIdNaoExiste_deveLancarExcecao() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Alerta não encontrado");
    }
}