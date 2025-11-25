package com.levelupgamer.productos;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Asegura que la tabla producto tenga la columna usuario_id necesaria para el control de ownership.
 * Esto cubre entornos donde la tabla fue creada antes de introducir la relación con vendedor.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class ProductoSchemaUpdater implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureUsuarioColumn();
    }

    private void ensureUsuarioColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE producto ADD COLUMN IF NOT EXISTS usuario_id BIGINT");
        } catch (DataAccessException ex) {
            log.warn("No se pudo agregar (o verificar) la columna usuario_id en producto", ex);
            return;
        }

        Integer anyUsuarioId = fetchAnyUsuarioId();
        if (anyUsuarioId != null) {
            int updated = jdbcTemplate.update(
                    "UPDATE producto SET usuario_id = ? WHERE usuario_id IS NULL",
                    anyUsuarioId
            );
            if (updated > 0) {
                log.info("Asignados {} productos existentes al usuario {} para mantener integridad.", updated, anyUsuarioId);
            }
            tryAlterColumnNotNull();
        } else {
            log.warn("No se encontraron usuarios en la tabla usuario; usuario_id permanecerá NULL hasta que se cree uno.");
        }

        tryAddForeignKeyConstraint();
    }

    private Integer fetchAnyUsuarioId() {
        try {
            return jdbcTemplate.query(
                    "SELECT id FROM usuario ORDER BY id LIMIT 1",
                    rs -> rs.next() ? rs.getInt(1) : null
            );
        } catch (DataAccessException ex) {
            log.warn("No fue posible consultar la tabla usuario para inicializar producto.usuario_id", ex);
            return null;
        }
    }

    private void tryAlterColumnNotNull() {
        try {
            jdbcTemplate.execute("ALTER TABLE producto ALTER COLUMN usuario_id SET NOT NULL");
        } catch (DataAccessException ex) {
            log.warn("No se pudo marcar usuario_id como NOT NULL (¿existen productos sin dueño?).", ex);
        }
    }

    private void tryAddForeignKeyConstraint() {
        try {
            jdbcTemplate.execute("ALTER TABLE producto ADD CONSTRAINT fk_producto_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)");
        } catch (DataAccessException ex) {
            if (!alreadyExists(ex)) {
                log.warn("No se pudo crear la FK fk_producto_usuario", ex);
            }
        }
    }

    private boolean alreadyExists(DataAccessException ex) {
        String message = Objects.toString(ex.getMessage(), "").toLowerCase();
        return message.contains("already exists") || message.contains("duplicate") || message.contains("exists");
    }
}
