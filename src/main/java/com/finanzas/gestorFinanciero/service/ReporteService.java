/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.service;

import com.finanzas.gestorFinanciero.dto.reportes.*;
import com.finanzas.gestorFinanciero.repository.reportes.*;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author adris
 */
@Service
public class ReporteService {
    private final ReporteCatalogoRepository catalogo;
    private final ReporteEjecutor ejecutor;
    private final PdfService pdf;

    public ReporteService(ReporteCatalogoRepository catalogo, ReporteEjecutor ejecutor, PdfService pdf) {
        this.catalogo = catalogo; this.ejecutor = ejecutor; this.pdf = pdf;
    }

    public record PdfGenerado(String nombreArchivo, byte[] bytes) {}

    @Transactional(readOnly = true)
    public ResultadoReporte generar(long id, Long codCliente, boolean esAdmin,
                                    LocalDate desde, LocalDate hasta) {
        return ejecutar(cargar(id, esAdmin), codCliente, desde, hasta);
    }

    @Transactional(readOnly = true)
    public PdfGenerado generarPdf(long id, Long codCliente, boolean esAdmin,
                                  LocalDate desde, LocalDate hasta) {
        ReporteDef r = cargar(id, esAdmin);
        ResultadoReporte res = ejecutar(r, codCliente, desde, hasta);
        byte[] bytes = pdf.generar(r, res, catalogo.nombreCliente(codCliente), desde, hasta);
        return new PdfGenerado(nombreArchivo(r.nombre()), bytes);
    }

    private ReporteDef cargar(long id, boolean esAdmin) {
        ReporteDef r = catalogo.buscar(id)
            .orElseThrow(() -> new NegocioException("El reporte no existe"));
        if (r.soloAdmin() && !esAdmin)
            throw new AccessDeniedException("No tienes acceso a este reporte");
        return r;
    }

    private ResultadoReporte ejecutar(ReporteDef r, Long codCliente, LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta))
            throw new NegocioException("La fecha inicial no puede ser mayor a la final");
        return ejecutor.ejecutar(r, codCliente, desde, hasta);
    }

    private static String nombreArchivo(String nombre) {
        return Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]+", "_") + ".pdf";
    }
    
    public List<ReporteDef> listar(String categoria, boolean esAdmin) {
        return catalogo.listar(categoria).stream()
                .filter(r -> esAdmin || !r.soloAdmin())
                .toList();
    }

    public ReporteDef obtener(long id, boolean esAdmin) {
        return cargar(id, esAdmin);
    }
}