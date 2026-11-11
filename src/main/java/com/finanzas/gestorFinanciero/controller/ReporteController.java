/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;

import com.finanzas.gestorFinanciero.dto.reportes.ReporteDef;
import com.finanzas.gestorFinanciero.dto.security.UsuarioPrincipal;
import com.finanzas.gestorFinanciero.service.ReporteService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author adris
 */
@Controller
@RequestMapping("/reportes")
public class ReporteController {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private final ReporteService service;
    public ReporteController(ReporteService service) { this.service = service; }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "false") boolean descargar,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {

        LocalDate h = hasta != null ? hasta : LocalDate.now(LIMA);
        LocalDate d = desde != null ? desde : h.minusMonths(6);

        var pdf = service.generarPdf(id, usuario.getCodCliente(), esAdmin(usuario), d, h);

        ContentDisposition cd = (descargar ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(pdf.nombreArchivo()).build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .body(pdf.bytes());
    }
    
    private static final Map<String, String> TITULOS = Map.of(
    "B", "Reportes básicos", "E", "Reportes específicos",
    "C", "Reportes complejos", "K", "Reportes de control");

    private boolean esAdmin(UsuarioPrincipal u) { return u.isAdmin(); }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "B") String categoria,
                        @AuthenticationPrincipal UsuarioPrincipal usuario, Model model) {
        String cat = TITULOS.containsKey(categoria) ? categoria : "B";
        model.addAttribute("tituloCat", TITULOS.get(cat));
        model.addAttribute("reportes", service.listar(cat, esAdmin(usuario)));
        return "reportes/lista";
    }

    @GetMapping("/{id}")
    public String visor(@PathVariable long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @AuthenticationPrincipal UsuarioPrincipal usuario, Model model) {
        ReporteDef r = service.obtener(id, esAdmin(usuario));
        LocalDate h = hasta != null ? hasta : LocalDate.now(LIMA);
        LocalDate d = desde != null ? desde : h.minusMonths(6);
        model.addAttribute("reporte", r);
        model.addAttribute("desde", d);
        model.addAttribute("hasta", h);
        return "reportes/visor";
    }
}
