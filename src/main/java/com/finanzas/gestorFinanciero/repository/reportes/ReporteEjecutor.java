/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.repository.reportes;

import com.finanzas.gestorFinanciero.dto.reportes.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

/**
 *
 * @author adris
 */
@Component
public class ReporteEjecutor {
    private static final Pattern IDENT = Pattern.compile("^[A-Z][A-Z0-9_]{0,29}$");
    private final JdbcTemplate jdbc;
    public ReporteEjecutor(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public ResultadoReporte ejecutar(ReporteDef r, Long codCliente, LocalDate desde, LocalDate hasta) {
        validar(r.paquete());
        validar(r.procedimiento());
        boolean texto = "X".equals(r.formato());

        SqlOutParameter salida = texto
            ? new SqlOutParameter("P_TEXTO", Types.CLOB)
            : new SqlOutParameter("P_CURSOR", Types.REF_CURSOR, ReporteEjecutor::leerCursor);

        var call = new SimpleJdbcCall(jdbc)
            .withCatalogName(r.paquete())
            .withProcedureName(r.procedimiento())
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("P_COD_CLIENTE", Types.NUMERIC),
                new SqlParameter("P_DESDE", Types.TIMESTAMP),
                new SqlParameter("P_HASTA", Types.TIMESTAMP),
                salida);

        Map<String, Object> out = call.execute(Map.of(
            "P_COD_CLIENTE", codCliente,
            "P_DESDE", Timestamp.valueOf(desde.atStartOfDay()),
            "P_HASTA", Timestamp.valueOf(hasta.atTime(23, 59, 59))));

        return texto ? ResultadoReporte.texto(leerTexto(out.get("P_TEXTO")))
                     : (ResultadoReporte) out.get("P_CURSOR");
    }

    private static void validar(String id) {
        if (id == null || !IDENT.matcher(id).matches())
            throw new IllegalStateException("Identificador de reporte inválido: " + id);
    }

    private static ResultadoReporte leerCursor(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int n = md.getColumnCount();
        List<String> cols = new ArrayList<>();
        for (int i = 1; i <= n; i++) cols.add(md.getColumnLabel(i));
        List<List<Object>> filas = new ArrayList<>();
        while (rs.next()) {
            List<Object> fila = new ArrayList<>(n);
            for (int i = 1; i <= n; i++) fila.add(rs.getObject(i));
            filas.add(fila);
        }
        return ResultadoReporte.tabla(cols, filas);
    }

    private static String leerTexto(Object o) {
        try {
            if (o instanceof Clob c) return c.getSubString(1, (int) c.length());
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo leer el texto del reporte", e);
        }
        return o == null ? "" : o.toString();
    }
}
