package br.com.fiap.satelite.controller;

import br.com.fiap.satelite.domain.AlertaClimatico;
import br.com.fiap.satelite.service.AlertaClimaticoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = "*") // Requisito: CORS habilitado globalmente para o Frontend
@Tag(name = "Alertas Climáticos", description = "Endpoints para gerenciamento e análise de anomalias por IA")
public class AlertaController {

    private final AlertaClimaticoService service;

    public AlertaController(AlertaClimaticoService service) {
        this.service = service;
    }

    @PostMapping("/analisar")
    @Operation(summary = "Submete dados de satélite suspeitos para análise cognitiva da IA com RAG")
    public ResponseEntity<AlertaClimatico> analisarAnomalia(@RequestBody AlertaClimatico payload) {
        AlertaClimatico processado = service.executarAnaliseDeDesastre(payload);

        // Requisito: HATEOAS adicionando links dinâmicos de navegação fluida
        Link selfLink = linkTo(methodOn(AlertaController.class).obterAlerta(processado.getId())).withSelfRel();
        processado.add(selfLink);

        return ResponseEntity.ok(processado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca o parecer completo de um alerta específico (Cacheado)")
    public ResponseEntity<AlertaClimatico> obterAlerta(@PathVariable Long id) {
        AlertaClimatico alerta = service.buscarPorId(id);
        return ResponseEntity.ok(alerta);
    }
}