package com.dicsys.assistant.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class ChatResponse {
    private String respuesta;        // Explicación simplificada generada por la IA
    private String conversationId;   // ID del hilo de la conversación
    private LocalDateTime timestamp; // Fecha y hora de la respuesta
}
