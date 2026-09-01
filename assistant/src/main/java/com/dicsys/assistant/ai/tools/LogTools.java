package com.dicsys.assistant.ai.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Component("logTools")
public class LogTools {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool(description = "Busca logs de error, códigos de estado HTTP (400, 404, 500) o trazas. Índices válidos: apache-logs, linux-logs, openssh-logs, general-files-logs o *-logs para todos.")
    public String buscarLogsEnElasticsearch(
            @ToolParam(description = "Término de búsqueda, código HTTP o palabra clave (ej: '400', 'POST /usr/register', 'error')") 
            String terminoBusqueda,
            @ToolParam(description = "Nombre exacto del índice (ej: 'general-files-logs', 'apache-logs'). Si no se especifica, usa '*-logs'") 
            String nombreIndice) {
        try {
            String indiceObjetivo = (nombreIndice != null && !nombreIndice.trim().isEmpty()) 
                    ? nombreIndice.trim() 
                    : "*-logs";
                    
            String url = "http://localhost:9200/" + indiceObjetivo + "/_search?size=5";

            // Incluimos message, event.original y http_status para capturar logs web crudos
            Map<String, Object> requestBody = Map.of(
                "query", Map.of(
                    "multi_match", Map.of(
                        "query", terminoBusqueda,
                        "fields", List.of("message", "event.original", "log_message", "log_level", "auth_user", "http_status")
                    )
                )
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            return "Resultados encontrados en Elasticsearch:\n" + response.getBody();

        } catch (Exception e) {
            return "Error técnico al consultar Elasticsearch: " + e.getMessage();
        }
    }
}