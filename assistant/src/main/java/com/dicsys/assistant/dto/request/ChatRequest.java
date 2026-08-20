package com.dicsys.assistant.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ChatRequest {
    @NotBlank(message = "El prompt no puede estar vacío")
    private String prompt; // Ejemplo: "¿Por qué falló la transacción TX-98765?"

    @NotBlank(message = "El userId es obligatorio")
    private String userId; // Ejemplo: "usr_soporte_01"

    private String userRole; // Ejemplo: "ANALISTA_FUNCIONAL"
    private String conversationId; // Opcional: ID para mantener contexto
}
