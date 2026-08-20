package com.dicsys.assistant.ai.tools;

import com.dicsys.assistant.repository.ReadOnlyQueryRepository;
import com.dicsys.assistant.security.QueryValidator;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("dbTools")
public class DbTools {

    private final ReadOnlyQueryRepository queryRepository;
    private final QueryValidator queryValidator;

    // 1. Inyectamos la plantilla externa text-to-sql-prompt.st
    @Value("classpath:prompts/text-to-sql-prompt.st")
    private Resource textToSqlPromptResource;

    public DbTools(ReadOnlyQueryRepository queryRepository, QueryValidator queryValidator) {
        this.queryRepository = queryRepository;
        this.queryValidator = queryValidator;
    }

    /**
     * Herramienta para obtener el contexto del esquema SQL formateado por la plantilla .st
     */
    @Tool(description = "Obtiene el esquema de las tablas de la base de datos relacional y las instrucciones para generar consultas SQL válidas.")
    public String obtenerGuiaYEsquemaSql(String consultaUsuario) {
        try {
            String esquemaTablas = """
                TABLAS DISPONIBLES:
                - transacciones (id VARCHAR PRIMARY KEY, estado VARCHAR, monto DECIMAL, moneda VARCHAR, fecha_creacion TIMESTAMP, motivo_rechazo TEXT)
                - usuarios (id VARCHAR PRIMARY KEY, email VARCHAR, nombre VARCHAR, rol VARCHAR, bloqueado BOOLEAN)
                - auditoria_accesos (id SERIAL, user_id VARCHAR, fecha TIMESTAMP, ip VARCHAR, exitoso BOOLEAN)
                """;

            // Renderizamos el prompt usando PromptTemplate de Spring AI
            PromptTemplate template = new PromptTemplate(textToSqlPromptResource);
            return template.render(Map.of(
                    "schema", esquemaTablas,
                    "userQuery", consultaUsuario != null ? consultaUsuario : ""
            ));
        } catch (Exception e) {
            return "No se pudo cargar la plantilla de SQL: " + e.getMessage();
        }
    }

    /**
     * Herramienta 1: Ejecución segura de consultas SQL validadas
     */
    @Tool(description = "Ejecuta una consulta SQL de solo lectura (SELECT) en PostgreSQL para obtener datos de transacciones, métricas o usuarios.")
    public String ejecutarConsultaSql(String sqlQuery) {
        try {
            // 1. Validar estrictamente solo SELECT
            if (!queryValidator.esConsultaValidaReadOnly(sqlQuery)) {
                return "ERROR DE SEGURIDAD: La consulta contiene operaciones no permitidas (solo se permite SELECT).";
            }

            // 2. Ejecutar consulta
            List<Map<String, Object>> resultados = queryRepository.ejecutarConsultaReadOnly(sqlQuery);

            if (resultados.isEmpty()) {
                return "La consulta no devolvió ningún registro.";
            }

            return resultados.toString();
        } catch (Exception e) {
            return "Error al ejecutar la consulta SQL: " + e.getMessage();
        }
    }

    /**
     * Herramienta 2: Consulta directa de transacciones por ID
     */
    @Tool(description = "Consulta directamente el estado, monto y detalles de una transacción mediante su ID único (ej. TX-12345).")
    public String consultarEstadoTransaccion(String idTransaccion) {
        String sql = String.format(
            "SELECT id, estado, monto, moneda, fecha_creacion, motivo_rechazo " +
            "FROM transacciones WHERE id = '%s'", idTransaccion
        );

        return ejecutarConsultaSql(sql);
    }
}