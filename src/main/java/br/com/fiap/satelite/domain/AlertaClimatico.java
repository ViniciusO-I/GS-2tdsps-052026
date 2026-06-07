package br.com.fiap.satelite.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_alertas_climaticos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaClimatico extends RepresentationModel<AlertaClimatico> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sateliteId;
    private Double latitude;
    private Double longitude;
    private Double temperaturaSolo;
    private Double umidadeAr;
    private LocalDateTime dataHoraRegistro;

    @Enumerated(EnumType.STRING)
    private StatusAlerta status;

    @Column(length = 2000)
    private String parecerIa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "satelite_fk")
    private Satelite satelite;
}
