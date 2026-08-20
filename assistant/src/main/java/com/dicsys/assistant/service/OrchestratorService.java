package com.dicsys.assistant.service;

import com.dicsys.assistant.dto.request.ChatRequest;
import com.dicsys.assistant.dto.response.ChatResponse;
import com.dicsys.assistant.ai.tools.LogTools;
import com.dicsys.assistant.ai.tools.DbTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrchestratorService {

    private final ChatClient chatClient;
    private final AuditService auditService;
    private final LogTools logTools;
    private final DbTools dbTools;

    // 1. Inyectamos la plantilla externa .st desde resources/prompts/
    @Value("classpath:prompts/system-prompt.st")
    private Resource systemPromptResource;

    public OrchestratorService(ChatClient chatClient, 
                               AuditService auditService, 
                               LogTools logTools, 
                               DbTools dbTools) {
        this.chatClient = chatClient;
        this.auditService = auditService;
        this.logTools = logTools;
        this.dbTools = dbTools;
    }

    public ChatResponse procesarMensaje(ChatRequest request) {
        String convId = request.getConversationId() != null 
                ? request.getConversationId() 
                : UUID.randomUUID().toString();
                
        long startTime = System.currentTimeMillis();

        // 2. ChatClient soporta directamente pasar el Resource al método .system()
        String respuestaIa = chatClient.prompt()
                .system(systemPromptResource) // <--- Lee el contenido de system-prompt.st
                .user(request.getPrompt())
                .tools(logTools, dbTools)     // <--- Objetos Java inyectados como herramientas
                .call()
                .content();

        long tiempoEjecucion = System.currentTimeMillis() - startTime;

        // 3. Auditoría asíncrona
        auditService.registrarAuditoria(
                request.getUserId(), 
                request.getUserRole(), 
                request.getPrompt(), 
                "Ejecución mediante Spring AI Tools", 
                respuestaIa, 
                true, 
                null, 
                tiempoEjecucion
        );

        return ChatResponse.builder()
                .respuesta(respuestaIa)
                .conversationId(convId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}