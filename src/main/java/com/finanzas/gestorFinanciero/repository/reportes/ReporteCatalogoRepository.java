/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.repository.reportes;

import com.finanzas.gestorFinanciero.dto.reportes.ReporteDef;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author adris
 */
@Repository
public class ReporteCatalogoRepository {
    private final JdbcTemplate jdbc;
    public ReporteCatalogoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final String BASE =
        "SELECT id_reporte, categoria, nombre, descripcion, paquete, procedimiento, " +
        "usa_fechas, solo_admin, formato FROM reportes WHERE activo = 'S'";

    private static final RowMapper<ReporteDef> MAPPER = (rs, i) -> new ReporteDef(
        rs.getLong("id_reporte"), rs.getString("categoria"), rs.getString("nombre"),
        rs.getString("descripcion"), rs.getString("paquete"), rs.getString("procedimiento"),
        "S".equals(rs.getString("usa_fechas")), "S".equals(rs.getString("solo_admin")),
        rs.getString("formato"));
    
    public String nombreCliente(long cod) {
    return jdbc.query(
        "SELECT INITCAP(TRIM(nombres || ' ' || ap_paterno || ' ' || NVL(ap_materno, ''))) " +
        "FROM cliente WHERE cod_cliente = ?",
        rs -> rs.next() ? rs.getString(1) : "Usuario", cod);
    }

    public List<ReporteDef> listar(String categoria) {
        return jdbc.query(BASE + " AND categoria = ? ORDER BY nombre", MAPPER, categoria);
    }

    public Optional<ReporteDef> buscar(long id) {
        return jdbc.query(BASE + " AND id_reporte = ?", MAPPER, id).stream().findFirst();
    }
}
