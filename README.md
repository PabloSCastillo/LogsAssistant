# 🤖 Log Assistant — AI-Powered Technical & Production Log Assistant

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring_AI-1.0.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-ai)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.13.0-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)](https://www.elastic.co/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Slack API](https://img.shields.io/badge/Slack_Integration-Event_API-4A154B?style=for-the-badge&logo=slack&logoColor=white)](https://api.slack.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

> **Asistente conversacional inteligente** diseñado para democratizar el acceso a métricas de negocio, incidentes de infraestructura y diagnóstico de trazas técnicas en producción. Permite a analistas funcionales, soporte y operaciones consultar en lenguaje natural sin depender continuamente del equipo de desarrollo.

---

## 📌 Tabla de Contenidos
- [Características Principales](#-características-principales)
- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Pila Tecnológica](#-pila-tecnológica)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Seguridad y Gobernanza de Datos](#-seguridad-y-gobernanza-de-datos)
- [Pipelines de Ingesta (ELK & Docker)](#-pipelines-de-ingesta-elk--docker)
- [Configuración e Instalación](#-configuración-e-instalación)
- [Pruebas y Casos de Uso (Swagger & Slack)](#-pruebas-y-casos-de-uso-swagger--slack)

---

## 🚀 Características Principales

- **Diagnóstico Inteligente de Logs (Elasticsearch + Grok):** Búsqueda semántica y por palabras clave en índices estructurados (`apache-logs`, `linux-logs`, `openssh-logs`, `general-files-logs`).
- **Consultas Seguras a Base de Datos (Text-to-SQL):** Traducción de preguntas de negocio a consultas SQL sobre PostgreSQL con aislamiento de solo lectura.
- **Defensa en Profundidad (AST SQL Validator):** Análisis sintáctico mediante *JSqlParser* que rechaza en tiempo de ejecución cualquier instrucción destructiva o no autorizada (`INSERT`, `UPDATE`, `DELETE`, `DROP`).
- **Simplificación de Stack Traces:** Transformación de errores y volcados de consola crudos en explicaciones claras y estructuradas con causas probables y pasos de remediación.
- **Auditoría Asíncrona Inmutable:** Registro desacoplado (`@Async`) de prompts, consultas ejecutadas, tiempos de latencia y respuestas generadas para cumplimiento normativo (SOC2 / ISO 27001).
- **Integración Omnicanal:** Soporte simultáneo para interfaces REST/Swagger UI y bots colaborativos en Slack vía Webhooks y Event Subscriptions.

---

## 🏗 Arquitectura del Sistema

```text
                                [ Usuario / Analista / Slack ]
                                               │
                                               ▼
                                [ ChatController / SlackController ]
                                               │
                                               ▼
                              [ OrchestratorService (Core) ]
                                ├─── (Async) ───► [ AuditService ] ──► [(PostgreSQL: audit_logs)]
                                │
                                ▼
                     [ Spring AI (ChatClient) ] ◄──► [ LLM (Gemini / Ollama) ]
                                │
                         (Function Calling)
                                │
      ┌─────────────────────────┴─────────────────────────┐
      ▼                                                   ▼
[ LogTools ]                                        [ DbTools ]
      │                                                   │
      ▼                                                   ▼
[(Elasticsearch 8.13)]                        [ QueryValidator (JSqlParser) ]
  • apache-logs                                     (Valida SELECT estricto)
  • linux-logs                                            │
  • openssh-logs                                          ▼
  • general-files-logs                        [ ReadOnlyQueryRepository ]
                                                          │
                                                          ▼
                                             [(PostgreSQL: Transacciones)]
```

## 🛠 Pila Tecnológica
Backend: Java 17+ / 21, Spring Boot 3.x, Spring Web, Spring Data JPA, Spring Security.

Inteligencia Artificial: Spring AI (Tool / Function Calling, Prompt Templates, ChatMemory).

Modelos LLM: Google Gemini API (gemini-1.5-pro / gemini-1.5-flash) / Modelos Locales vía Ollama (qwen2.5, llama3).

Almacenamiento de Datos:

PostgreSQL 16: Datos transaccionales y registro inmutable de auditoría.

Elasticsearch 8.13.0: Motor distribuido de búsqueda e indexación de logs.

Ingesta y Procesamiento: Logstash 8.13 con filtros Grok estructurados.

Integraciones: Slack API Client Java SDK, OpenAPI / Swagger UI 3, Ngrok / Dev Tunnels.

Contenedores: Docker & Docker Compose.

## 📂 Estructura del Proyecto
```text
log-assistant/
├── docker/
│   ├── docker-compose.yml             # Contenedores de Postgres, Elasticsearch y Logstash
│   ├── logstash.conf                  # Pipelines y filtros Grok por tipo de log
│   └── logs/                          # Datasets de logs crudos (Apache, Linux, OpenSSH, etc.)
│
└── src/
    └── main/
        ├── java/com/dicsys/assistant/
        │   ├── Application.java       # Punto de entrada de Spring Boot
        │   │
        │   ├── config/                # Configuraciones de Seguridad, CORS y Beans
        │   │   ├── SecurityConfig.java
        │   │   └── AiConfig.java
        │   │
        │   ├── controller/            # Endpoints REST y Webhooks
        │   │   ├── ChatController.java
        │   │   └── SlackController.java
        │   │
        │   ├── service/               # Orquestación de IA y Auditoría
        │   │   ├── OrchestratorService.java
        │   │   └── AuditService.java
        │   │
        │   ├── ai/tools/              # Herramientas ejecutables por el LLM (@Tool)
        │   │   ├── DbTools.java       # Text-to-SQL y consultas transaccionales
        │   │   └── LogTools.java      # Búsqueda full-text en Elasticsearch
        │   │
        │   ├── security/              # Capa de Gobierno y Blindaje
        │   │   ├── QueryValidator.java# Validación AST (JSqlParser) de solo lectura
        │   │   └── PiiSanitizer.java  # Enmascaramiento de datos sensibles
        │   │
        │   └── repository/            # Acceso a datos
        │       ├── ReadOnlyQueryRepository.java
        │       └── AuditRepository.java
        │
        └── resources/
            ├── application.properties # Parámetros de entorno y conexiones
            └── prompts/               # Plantillas externas StringTemplate (.st)
                ├── system-prompt.st
                ├── text-to-sql-prompt.st
                └── log-simplifier-prompt.st
```

## 🛡 Seguridad y Gobernanza de Datos
Garantía Read-Only (Doble Barrera):

A nivel base de datos: Usuario con permisos estrictos de lectura (SELECT).

A nivel aplicación: QueryValidator inspecciona el árbol sintáctico (AST) con JSqlParser rechazando cualquier intento de inyección o comando no SELECT.

Limitación de Volumen y Latencia: Inyección automática de cláusulas LIMIT 50 y paginación en Elasticsearch (size=5) para prevenir sobrecarga y optimizar el consumo de tokens.

Auditoría Total: Registro persistente de usuario, rol, prompt original, acción ejecutada, tiempo de respuesta en ms y estado de finalización.
## 🐳 Pipelines de Ingesta (ELK & Docker)
# Levantar el clúster local de Elasticsearch, PostgreSQL y Logstash
docker compose up -d

Parsing automático de logs con Logstash
Apache Web Logs: Extracción de IP cliente, método HTTP, URI y http_status.

Syslog / Linux: Detección de fallos en servicios del sistema (systemd, caídas de memoria Out of memory).

OpenSSH: Extracción de eventos de autenticación (Failed password, usuario e IP origen).

## ⚙️ Configuración e Instalación
1. Variables de Entorno (.env)
Crea un archivo .env en la raíz del proyecto:
# Proveedor LLM
GEMINI_API_KEY=tu_api_key_aqui

# Base de Datos PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/asistente
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Elasticsearch
SPRING_ELASTICSEARCH_URIS=http://localhost:9200

# Integración con Slack (Opcional)
SLACK_BOT_TOKEN=xoxb-tu-slack-bot-token
SLACK_SIGNING_SECRET=tu-slack-signing-secret

# Compilar el proyecto
./gradlew clean build

# Iniciar la aplicación
./gradlew bootRun

## 🧪 Pruebas y Casos de Uso (Swagger & Slack)
Accede a la documentación interactiva en:
👉 http://localhost:8081/swagger-ui.html

Payloads de prueba en POST /api/v1/chat
🔹 Caso 1: Detección de Incidentes Web y Códigos HTTP

{
  "prompt": "Revisa el índice general-files-logs y dime cuándo ocurrieron los últimos errores 400 o fallos en peticiones POST.",
  "userId": "analista_soporte",
  "userRole": "SOPORTE",
  "conversationId": "sesion-http-01"
}

🔹 Caso 2: Auditoría de Seguridad SSH
{
  "prompt": "Revisa los registros de openssh y dime si hubo intentos de acceso fallidos para el usuario root.",
  "userId": "admin_seguridad",
  "userRole": "ADMIN_SEGURIDAD",
  "conversationId": "sesion-ssh-02"
}

🔹 Caso 3: Text-to-SQL sobre Transacciones
{
  "prompt": "¿Cuáles son las últimas transacciones rechazadas y cuál fue el motivo?",
  "userId": "analista_funcional",
  "userRole": "ANALISTA_FUNCIONAL",
  "conversationId": "sesion-sql-03"
}

## 📄 Licencia
