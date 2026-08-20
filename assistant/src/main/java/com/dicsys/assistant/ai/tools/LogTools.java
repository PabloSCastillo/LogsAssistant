package com.dicsys.assistant.ai.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component("logTools")
public class LogTools {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. Inyectamos la plantilla externa log-simplifier-prompt.st
    @Value("classpath:prompts/log-simplifier-prompt.st")
    private Resource logSimplifierPromptResource;

    @Tool(description = "Busca logs de error, incidentes y trazas en Elasticsearch. Índices: apache-logs, linux-logs, openssh-logs.")
    public String buscarLogsEnElasticsearch(String terminoBusqueda, String nombreIndice) {
        try {
            String indiceObjetivo = (nombreIndice != null && !nombreIndice.isEmpty()) ? nombreIndice : "*-logs";
            String url = "http://localhost:9200/" + indiceObjetivo + "/_search?size=5";

            // 1. Construcción del cuerpo de la petición de búsqueda
            Map<String, Object> requestBody = Map.of(
                "query", Map.of(
                    "multi_match", Map.of(
                        "query", terminoBusqueda,
                        "fields", List.of("log_message", "log_level", "auth_user", "stack_trace")
                    )
                )
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            String logsCrudos = response.getBody();

            // 2. Renderizamos los logs crudos usando la plantilla log-simplifier-prompt.st
            PromptTemplate template = new PromptTemplate(logSimplifierPromptResource);
            return template.render(Map.of("rawLogs", logsCrudos != null ? logsCrudos : "Sin resultados"));

        } catch (Exception e) {
            return "Error técnico al consultar Elasticsearch: " + e.getMessage();
        }
    }
}