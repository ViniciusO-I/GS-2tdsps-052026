package br.com.fiap.satelite.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClimaExternoDto {

    private Double latitude;
    private Double longitude;
    private Double temperaturaMedia;
    private Double precipitacaoAcumulada;
    private Double umidadeRelativaMedia;
    private String condicaoClimatica;
    private String dataReferencia;

    @Override
    public String toString() {
        return String.format(
                "Temp média: %.1f°C | Precipitação: %.1fmm | Umidade: %.1f%% | Condição: %s | Data: %s",
                temperaturaMedia, precipitacaoAcumulada, umidadeRelativaMedia,
                condicaoClimatica, dataReferencia
        );
    }
}