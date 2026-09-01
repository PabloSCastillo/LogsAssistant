package com.dicsys.assistant.repository.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId; // ID o username del usuario que realizó la consulta

    @Column(nullable = false)
    private String userRole; // Rol del usuario (ej. ANALISTA_FUNCIONAL, SOPORTE)

    @Column(length = 2000)
    private String userPrompt; // Pregunta original en lenguaje natural

    @Column(length = 4000)
    private String queryEjecutada; // SQL o comando de log generado por la IA

    @Column(length = 4000)
    private String respuestaResumida;// Respuesta que se le entregó al usuario (Sanitizada)

    @Column(nullable = false)
    private boolean exitoso; // true si respondió correctamente, false si falló o violó seguridad

    private String motivoError; // Detalle en caso de error o violación de seguridad

    @Column(nullable = false)
    private Long tiempoEjecucionMs; // Tiempo de respuesta en milisegundos

    // Nuevos campos para consumo de tokens
    private Integer promptTokens; // Tokens consumidos en el prompt/entrada
    private Integer generationTokens; // Tokens consumidos en la respuesta/salida
    private Integer totalTokens; // Total consumido

    @Column(nullable = false)
    private LocalDateTime fechaCreacion; // Timestamp de la consulta

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}
