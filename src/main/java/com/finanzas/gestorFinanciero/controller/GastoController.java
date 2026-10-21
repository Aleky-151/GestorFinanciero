/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;

import com.finanzas.gestorFinanciero.dto.GastoForm;
import com.finanzas.gestorFinanciero.dto.security.UsuarioPrincipal;
import com.finanzas.gestorFinanciero.repository.CategoriaGastoRepository;
import com.finanzas.gestorFinanciero.repository.CuentaRepository;
import com.finanzas.gestorFinanciero.service.MovimientoService;
import com.finanzas.gestorFinanciero.service.NegocioException;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author adris
 */
@Controller
@RequestMapping("/gastos")
public class GastoController {

    private final MovimientoService movimientoService;
    private final CuentaRepository cuentaRepo;
    private final CategoriaGastoRepository catGastoRepo;

    public GastoController(MovimientoService movimientoService, CuentaRepository cuentaRepo,
                           CategoriaGastoRepository catGastoRepo) {
        this.movimientoService = movimientoService;
        this.cuentaRepo = cuentaRepo;
        this.catGastoRepo = catGastoRepo;
    }

    @GetMapping("/nuevo")
    public String nuevo(@AuthenticationPrincipal UsuarioPrincipal p, Model model) {
        GastoForm form = new GastoForm();
        form.setFecha(LocalDate.now());
        model.addAttribute("gastoForm", form);
        cargarListas(model, p.getCodCliente());
        return "gasto-form";
    }

    @PostMapping
    public String guardar(@AuthenticationPrincipal UsuarioPrincipal p,
                          @Valid @ModelAttribute("gastoForm") GastoForm form,
                          BindingResult result, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                movimientoService.registrarGasto(p.getCodCliente(),form.getIdCuenta(), form.getIdCategoria(),
                        form.getDescripcion(), form.getMonto(), form.getFecha());
                ra.addFlashAttribute("mensaje", "Gasto registrado ✅");
                return "redirect:/cuentas";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());   // error general del formulario
            }
        }
        cargarListas(model, p.getCodCliente());
        return "gasto-form";
    }

    private void cargarListas(Model model, Long id) {
        model.addAttribute("cuentas", cuentaRepo.listarPorCliente(id));
        model.addAttribute("categorias", catGastoRepo.findAll());
    }
}
