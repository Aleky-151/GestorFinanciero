/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;

import com.finanzas.gestorFinanciero.dto.security.UsuarioPrincipal;
import com.finanzas.gestorFinanciero.repository.MovimientoRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author adris
 */
@Controller
public class MovimientoController {
    private final MovimientoRepository movimientoRepo;

    public MovimientoController(MovimientoRepository movimientoRepo) {
        this.movimientoRepo = movimientoRepo;
    }
    
    @GetMapping("/movimientos")
    private String cargarLista(@AuthenticationPrincipal UsuarioPrincipal p, Model model) {
        model.addAttribute("movimientos", movimientoRepo.listarPorCuenta(p.getCodCliente()));
        return "movimientos";
    }
}
