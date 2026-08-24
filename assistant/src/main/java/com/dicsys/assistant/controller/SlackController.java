package com.dicsys.assistant.controller;

import com.dicsys.assistant.dto.request.ChatRequest;
import com.dicsys.assistant.dto.response.ChatResponse;
import com.dicsys.assistant.service.OrchestratorService;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/slack")
@CrossOrigin(origins = "*")
public class SlackController {

    private final OrchestratorService orchestratorService;
    private final String botToken;

    public SlackController(OrchestratorService orchestratorService,
                           @Value("${slack.bot.token:}") String botToken) {
        this.orchestratorService = orchestratorService;
        this.botToken = botToken;
    }

    /**
     * Endpoint receptor de eventos de Slack (Event Subscriptions)
     */
    @PostMapping("/events")
    public ResponseEntity<?> recibirEventoSlack(@RequestBody Map<String, Object> payload) {
        
        // 1. Handshake inicial obligatorio de Slack (URL Verification)
        if (payload.containsKey("type") && "url_verification".equals(payload.get("type"))) {
            return ResponseEntity.ok(Map.of("challenge", payload.get("challenge")));
        }

        // 2. Procesar eventos de mensajes (app_mention o message en canal/DM)
        if (payload.containsKey("event")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = (Map<String, Object>) payload.get("event");
            
            String eventType = (String) event.get("type");
            String userText = (String) event.get("text");
            String channelId = (String) event.get("channel");
            String slackUserId = (String) event.get("user");
            String botId = (String) event.get("bot_id");

            // Evitar que el bot se responda a sí mismo en bucle
            if (botId == null && userText != null && ("app_mention".equals(eventType) || "message".equals(eventType))) {
                
                // Limpiar la mención al bot (<@U123456>) del prompt
                String promptLimpio = userText.replaceAll("<@[A-Z0-9]+>", "").trim();

                // Procesamiento Asíncrono: Slack exige responder HTTP 200 en menos de 3 segundos
                CompletableFuture.runAsync(() -> {
                    ChatRequest request = ChatRequest.builder()
                            .prompt(promptLimpio)
                            .userId(slackUserId)
                            .userRole("SLACK_USER")
                            .conversationId(channelId)
                            .build();

                    ChatResponse respuesta = orchestratorService.procesarMensaje(request);
                    enviarMensajeASlack(channelId, respuesta.getRespuesta());
                });
            }
        }

        // Responder 200 OK inmediatamente a Slack para evitar reintentos automáticos
        return ResponseEntity.ok().build();
    }       

    /**
     * Envía la respuesta procesada por la IA al canal/hilo de Slack
     */
    private void enviarMensajeASlack(String channelId, String textoRespuesta) {
        try {
            Slack slack = Slack.getInstance();
            slack.methods(botToken).chatPostMessage(
                ChatPostMessageRequest.builder()
                    .channel(channelId)
                    .text(textoRespuesta)
                    .build()
            );
        } catch (IOException | SlackApiException e) {
            System.err.println("Error al enviar mensaje a Slack: " + e.getMessage());
        }
    }
}