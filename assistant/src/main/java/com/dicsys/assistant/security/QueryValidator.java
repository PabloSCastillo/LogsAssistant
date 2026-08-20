package com.dicsys.assistant.security;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.stereotype.Component;

@Component
public class QueryValidator {

    /**
     * Valida sintácticamente que la consulta SQL generada sea estrictamente un SELECT.
     * Rechaza cualquier intento de INSERT, UPDATE, DELETE, DROP, TRUNCATE, etc.
     */
    public boolean esConsultaValidaReadOnly(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }

        try {
            // Analizar la sentencia utilizando JSqlParser
            Statement statement = CCJSqlParserUtil.parse(sql);
            
            // Retorna true solo si el nodo principal corresponde a una operación SELECT
            return statement instanceof Select;
        } catch (Exception e) {
            // Si el parser falla por errores de sintaxis o formato inválido, se rechaza
            return false;
        }
    }
}