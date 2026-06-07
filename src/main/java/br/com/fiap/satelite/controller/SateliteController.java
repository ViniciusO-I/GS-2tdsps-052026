package br.com.fiap.satelite.controller;

import br.com.fiap.satelite.domain.Satelite;
import br.com.fiap.satelite.repository.SateliteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/satelites")
@Tag(name = "Satélites", description = "CRUD de satélites monitorados pelo sistema")
@SecurityRequirement(name = "bearerAuth")
public class SateliteController {

    private final SateliteRepository repository;

    public SateliteController(SateliteRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @CacheEvict(value = "satelites", allEntries = true)
    @PreAuthorize("hasRole('DEFESA_CIVIL')")
    @Operation(summary = "Cadastrar satélite", description = "Registra um novo satélite no sistema")
    public ResponseEntity<Satelite> criar(@RequestBody @Valid Satelite satelite) {
        Satelite salvo = repository.save(satelite);
        adicionarLinks(salvo);
        return ResponseEntity.status(201).body(salvo);
    }

    @GetMapping
    @Cacheable("satelites")
    @Operation(summary = "Listar satélites", description = "Retorna todos os satélites cadastrados")
    public ResponseEntity<List<Satelite>> listar() {
        List<Satelite> lista = repository.findAll();
        lista.forEach(this::adicionarLinks);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "satelite", key = "#id")
    @Operation(summary = "Buscar satélite por ID")
    public ResponseEntity<Satelite> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(s -> {
                    adicionarLinks(s);
                    return ResponseEntity.ok(s);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"satelites", "satelite"}, allEntries = true)
    @PreAuthorize("hasRole('DEFESA_CIVIL')")
    @Operation(summary = "Atualizar satélite")
    public ResponseEntity<Satelite> atualizar(@PathVariable Long id,
                                              @RequestBody @Valid Satelite satelite) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        satelite.setId(id);
        Satelite atualizado = repository.save(satelite);
        adicionarLinks(atualizado);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"satelites", "satelite"}, allEntries = true)
    @PreAuthorize("hasRole('DEFESA_CIVIL')")
    @Operation(summary = "Remover satélite")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    private void adicionarLinks(Satelite s) {
        s.add(linkTo(methodOn(SateliteController.class).buscarPorId(s.getId())).withSelfRel());
        s.add(linkTo(methodOn(SateliteController.class).listar()).withRel("satelites"));
    }
}
