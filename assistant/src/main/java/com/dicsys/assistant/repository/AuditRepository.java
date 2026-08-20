package com.dicsys.assistant.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dicsys.assistant.repository.entity.*;

public interface AuditRepository  extends JpaRepository<AuditLog, Long>{
    // Obtener el historial de auditoría ordenado desde el más reciente para un usuario específico
    List<AuditLog> findByUserIdOrderByFechaCreacionDesc(String userId);

    // Filtrar consultas que fallaron o que fueron bloqueadas por reglas de seguridad (SELECT Only, PII)
    List<AuditLog> findByExitosoFalseOrderByFechaCreacionDesc();

    // Obtener los logs de auditoría dentro de un rango de fechas determinado
    List<AuditLog> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);
}
