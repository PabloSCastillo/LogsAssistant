package com.dicsys.assistant.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ReadOnlyQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReadOnlyQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Ejecuta una consulta nativa de solo lectura sobre la base de datos de producción.
     * * @param sqlQuery Consulta SQL validada previamente por el QueryValidator
     * @return Lista de registros mapeados en formato clave-valor
     */
    public List<Map<String, Object>> ejecutarConsultaReadOnly(String sqlQuery) {
        String sqlConLimite = sqlQuery.trim();

        // Blindaje extra: asegurar un límite máximo de registros por consulta si no lo trae
        if (!sqlConLimite.toUpperCase().contains("LIMIT")) {
            sqlConLimite += " LIMIT 50";
        }

        return jdbcTemplate.queryForList(sqlConLimite);
    }
}