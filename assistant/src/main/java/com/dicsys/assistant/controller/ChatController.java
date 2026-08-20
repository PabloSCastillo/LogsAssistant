package com.dicsys.assistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dicsys.assistant.service.OrchestratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.dicsys.assistant.dto.request.ChatRequest;
import com.dicsys.assistant.dto.response.ChatResponse;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Asistente de Logs", description = "Endpoints para interactuar con el bot de consultas técnicas")
public class ChatController {
    private final OrchestratorService orchestratorService;

    public ChatController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping
    @Operation(summary = "Enviar una consulta al asistente", 
               description = "Envía una pregunta en lenguaje natural. La IA decidirá si busca en Elasticsearch o en la BD.")
    public ResponseEntity<ChatResponse> procesarConsulta(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = orchestratorService.procesarMensaje(request);
        return ResponseEntity.ok(response);
    }
}
