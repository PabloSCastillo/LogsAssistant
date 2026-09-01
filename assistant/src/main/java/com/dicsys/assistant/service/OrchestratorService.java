package com.dicsys.assistant.service;

import com.dicsys.assistant.ai.tools.DbTools;
import com.dicsys.assistant.ai.tools.LogTools;
import com.dicsys.assistant.dto.request.ChatRequest;
import com.dicsys.assistant.dto.response.ChatResponse;
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

                // 1. Invocar Spring AI una sola vez y obtener la respuesta completa con
                // metadatos
                org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt()
                                .system(systemPromptResource)
                                .user(request.getPrompt())
                                .tools(logTools, dbTools)
                                .call()
                                .chatResponse();

                long tiempoEjecucion = System.currentTimeMillis() - startTime;

                // 2. Extraer texto de la respuesta
                String respuestaIa = (aiResponse != null && aiResponse.getResult() != null
                                && aiResponse.getResult().getOutput() != null)
                                                ? aiResponse.getResult().getOutput().getText()
                                                : "";

                // 3. Extraer métricas de consumo de tokens
                Integer promptTokens = 0;
                Integer generationTokens = 0;
                Integer totalTokens = 0;

                if (aiResponse != null && aiResponse.getMetadata() != null
                                && aiResponse.getMetadata().getUsage() != null) {
                        var usage = aiResponse.getMetadata().getUsage();

                        // getPromptTokens() / getInputTokens()
                        promptTokens = usage.getPromptTokens() != null ? usage.getPromptTokens().intValue() : 0;

                        // getCompletionTokens() / getOutputTokens()
                        generationTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens().intValue()
                                        : 0;

                        // getTotalTokens()
                        totalTokens = usage.getTotalTokens() != null ? usage.getTotalTokens().intValue() : 0;
                }

                // 4. Auditoría asíncrona incluyendo métricas de tokens
                auditService.registrarAuditoria(
                                request.getUserId(),
                                request.getUserRole(),
                                request.getPrompt(),
                                "Ejecución mediante Spring AI Tools",
                                respuestaIa,
                                true,
                                null,
                                tiempoEjecucion,
                                promptTokens,
                                generationTokens,
                                totalTokens);

                // 5. Retorno del DTO de respuesta
                return ChatResponse.builder()
                                .respuesta(respuestaIa)
                                .conversationId(convId)
                                .timestamp(LocalDateTime.now())
                                .build();
        }
}