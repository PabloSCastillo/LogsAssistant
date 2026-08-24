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
