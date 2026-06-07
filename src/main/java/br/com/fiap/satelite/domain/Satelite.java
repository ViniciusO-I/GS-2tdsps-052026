package br.com.fiap.satelite.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_satelites")
public class Satelite extends RepresentationModel<Satelite> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_satelites")
    @SequenceGenerator(name = "seq_satelites", sequenceName = "seq_satelites", allocationSize = 1)
    private Long id;

    @NotBlank
    @Column(name = "codigo_satelite", unique = true, nullable = false, length = 20)
    private String codigoSatelite; // ex: SAT-BR-01

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 100)
    private String operador;

    @Column(name = "altitude_km")
    private Double altitudeKm;

    /**
     * Tipo de órbita: LEO (Baixa), GEO (Geoestacionária), MEO (Média)
     */
    @Column(name = "tipo_orbita", length = 10)
    private String tipoOrbita;

    @Column(name = "data_lancamento")
    private LocalDateTime dataLancamento;

    @NotNull
    @Column(nullable = false)
    private Boolean ativo = true;
}
