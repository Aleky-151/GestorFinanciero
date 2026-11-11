/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;
import com.finanzas.gestorFinanciero.dto.reportes.ResultadoReporte;
import com.finanzas.gestorFinanciero.dto.security.UsuarioPrincipal;
import com.finanzas.gestorFinanciero.service.ReporteService;
import java.time.LocalDate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
/**
 *
 * @author adris
 */
@RestController
public class PruebaReporteController {
    private final ReporteService service;
    public PruebaReporteController(ReporteService service) { this.service = service; }

    @GetMapping("/prueba/{id}")
    public ResultadoReporte probar(@PathVariable long id,
                                   @AuthenticationPrincipal UsuarioPrincipal u) {
        return service.generar(id, u.getCodCliente(), true,
                               LocalDate.now().minusMonths(6), LocalDate.now());
    }
}