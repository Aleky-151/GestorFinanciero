/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;

import com.finanzas.gestorFinanciero.dto.MovimientoEditForm;
import com.finanzas.gestorFinanciero.dto.security.UsuarioPrincipal;
import com.finanzas.gestorFinanciero.model.Cuenta;
import com.finanzas.gestorFinanciero.model.Gasto;
import com.finanzas.gestorFinanciero.model.Ingreso;
import com.finanzas.gestorFinanciero.model.Movimiento;
import com.finanzas.gestorFinanciero.repository.CategoriaGastoRepository;
import com.finanzas.gestorFinanciero.repository.CategoriaIngresoRepository;
import com.finanzas.gestorFinanciero.repository.CuentaRepository;
import com.finanzas.gestorFinanciero.repository.MovimientoRepository;
import com.finanzas.gestorFinanciero.service.CuentaService;
import com.finanzas.gestorFinanciero.service.MovimientoService;
import com.finanzas.gestorFinanciero.service.NegocioException;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/movimientos")
public class MovimientoController {
    private final MovimientoRepository movimientoRepo;
    private final MovimientoService movimientoService;
    private final CategoriaGastoRepository catGastoRepo;
    private final CategoriaIngresoRepository catIngresoRepo;
    private final CuentaRepository cuentaRepo;
    private final CuentaService cuentaService;

    public MovimientoController(MovimientoRepository movimientoRepo, MovimientoService movimientoService, CategoriaGastoRepository catGastoRepo, CategoriaIngresoRepository catIngresoRepo, CuentaRepository cuentaRepo, CuentaService cuentaService) {
        this.movimientoRepo = movimientoRepo;
        this.movimientoService = movimientoService;
        this.catGastoRepo = catGastoRepo;
        this.catIngresoRepo = catIngresoRepo;
        this.cuentaRepo = cuentaRepo;
        this.cuentaService = cuentaService;
    }
    
    @GetMapping
    private String cargarLista(@RequestParam(required=false) Long cuenta,
                               @AuthenticationPrincipal UsuarioPrincipal p, Model model) {
                
        Cuenta seleccionada = (cuenta == null)
            ? null
            : cuentaService.buscarDelCliente(cuenta, p.getCodCliente()).orElse(null);

        List<Movimiento> movimientos = (seleccionada == null)
            ? movimientoRepo.listarPorCliente(p.getCodCliente())
            : movimientoRepo.listarPorCuenta(seleccionada.getId());
        
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("cuentas", cuentaRepo.listarPorCliente(p.getCodCliente()));
        model.addAttribute("cuentaSel", seleccionada);
        return "movimientos";
    }
    
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal p,
                           RedirectAttributes ra) {
        try {
            movimientoService.eliminar(id, p.getCodCliente());
            ra.addFlashAttribute("mensaje", "Movimiento eliminado ✅");
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/movimientos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal p,
                         Model model, RedirectAttributes ra) {
        try {
            Movimiento m = movimientoService.obtener(id, p.getCodCliente());
            MovimientoEditForm form = new MovimientoEditForm();
            form.setId(m.getId());
            form.setDescripcion(m.getDescripcion());
            form.setMonto(m.getMonto());
            form.setFecha(m.getFecha());
            if (m instanceof Gasto g) form.setIdCategoria(g.getCategoria().getId());
            else if (m instanceof Ingreso i) form.setIdCategoria(i.getCategoria().getId());

            model.addAttribute("movimientoForm", form);
            cargarCategorias(m instanceof Gasto, model);
            return "movimiento-form";
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/movimientos";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("movimientoForm") MovimientoEditForm form,
                             BindingResult result, @AuthenticationPrincipal UsuarioPrincipal p,
                             Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                movimientoService.actualizar(id, form, p.getCodCliente());
                ra.addFlashAttribute("mensaje", "Movimiento actualizado ✅");
                return "redirect:/movimientos";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());
            }
        }
        // al volver al formulario hay que recargar las categorías del tipo correcto
        Movimiento m = movimientoService.obtener(id, p.getCodCliente());
        cargarCategorias(m instanceof Gasto, model);
        return "movimiento-form";
    }

    private void cargarCategorias(boolean esGasto, Model model) {
        model.addAttribute("categorias", esGasto ? catGastoRepo.findAll() : catIngresoRepo.findAll());
    }
}
