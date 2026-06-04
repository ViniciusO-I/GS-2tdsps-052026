package br.com.fiap.satelite.client;

import br.com.fiap.satelite.dto.ClimaExternoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "api-clima-externo", url = "https://api.cptec.inpe.br/v1")
public interface ClimaExternoClient {

    @GetMapping("/clima/coordenadas")
    ClimaExternoDto buscarDadosHistoricos(
            @RequestParam("lat") Double latitude,
            @RequestParam("lon") Double longitude
    );
}