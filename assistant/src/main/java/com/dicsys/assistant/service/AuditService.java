package com.dicsys.assistant.service;

import java.time.LocalDateTime;
import com.dicsys.assistant.repository.AuditRepository;
import com.dicsys.assistant.repository.entity.AuditLog; // <--- IMPORT CORREGIDO
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Guarda el registro de auditoría.
     * Se anota con @Async para ejecutar el guardado en segundo plano
     * y no penalizar el tiempo de respuesta al usuario.
     */
    @Async
    public void registrarAuditoria(
            String userId,
            String role,
            String prompt,
            String queryEjecutada,
            String respuesta,
            boolean exitoso,
            String error,
            long tiempoMs,
            Integer promptTokens,
            Integer generationTokens,
            Integer totalTokens) {

        AuditLog log = AuditLog.builder()
                .userId(userId)
                .userRole(role)
                .userPrompt(prompt)
                .queryEjecutada(queryEjecutada)
                .respuestaResumida(respuesta)
                .exitoso(exitoso)
                .motivoError(error)
                .tiempoEjecucionMs(tiempoMs)
                .promptTokens(promptTokens)
                .generationTokens(generationTokens)
                .totalTokens(totalTokens)
                .fechaCreacion(LocalDateTime.now())
                .build();

        auditRepository.save(log);
    }
}