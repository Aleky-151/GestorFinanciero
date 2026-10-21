/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;

import com.finanzas.gestorFinanciero.dto.CuentaForm;
import com.finanzas.gestorFinanciero.dto.security.UsuarioPrincipal;
import com.finanzas.gestorFinanciero.model.Cuenta;
import com.finanzas.gestorFinanciero.repository.CategoriaCuentaRepository;
import com.finanzas.gestorFinanciero.repository.CuentaRepository;
import com.finanzas.gestorFinanciero.service.CuentaService;
import com.finanzas.gestorFinanciero.service.NegocioException;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author adris
 */
@Controller
@RequestMapping("/cuentas")
public class CuentaController {
    private final CuentaRepository cuentaRepo;
    private final CuentaService cuentaService;
    private final CategoriaCuentaRepository catCuentaRepo;

    public CuentaController(CuentaRepository cuentaRepo, CuentaService cuentaService, CategoriaCuentaRepository catCuentaRepo) {
        this.cuentaRepo = cuentaRepo;
        this.cuentaService = cuentaService;
        this.catCuentaRepo = catCuentaRepo;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioPrincipal p, Model model) {
        model.addAttribute("cuentas", cuentaRepo.listarPorCliente(p.getCodCliente()));
        return "cuentas";
    }
    
    @GetMapping("/nuevo")
    public String nuevo(Model model){
        CuentaForm form = new CuentaForm();
        model.addAttribute("cuentaForm", form);
        cargarListas(model);
        return "cuenta-form";
    }
    
    @PostMapping
    public String guardar(@AuthenticationPrincipal UsuarioPrincipal p,
                          @Valid @ModelAttribute("cuentaForm") CuentaForm form,
                          BindingResult result, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                cuentaService.crear(form, p.getCodCliente());
                ra.addFlashAttribute("mensaje", "Cuenta registrada ✅");
                return "redirect:/cuentas";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());   // error general del formulario
            }
        }
        cargarListas(model);
        return "cuenta-form";
    }
    
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Cuenta cuenta = cuentaRepo.findById(id)
                .orElseThrow(() -> new NegocioException("La cuenta no existe"));

        CuentaForm form = new CuentaForm();
        form.setIdCuenta(cuenta.getId());
        form.setNombreCuenta(cuenta.getNombreCuenta());
        form.setIdCategoria(cuenta.getCategoria().getId());
        model.addAttribute("cuentaForm", form);
        cargarListas(model);
        return "cuenta-form";
    }
    
    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @AuthenticationPrincipal UsuarioPrincipal p,
                             @Valid @ModelAttribute("cuentaForm") CuentaForm form,
                             BindingResult result, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                cuentaService.actualizar(id, p.getCodCliente(), form);
                ra.addFlashAttribute("mensaje", "Cuenta actualizada ✅");
                return "redirect:/cuentas";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());
            }
        }
        cargarListas(model);
        return "cuenta-form";
    }
    
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, 
                           @AuthenticationPrincipal UsuarioPrincipal p,
                           Model model, 
                           RedirectAttributes ra){
        
        try{
            cuentaService.eliminar(id, p.getCodCliente());
            ra.addFlashAttribute("mensaje", "Cuenta eliminada ✅");
            return "redirect:/cuentas";
        } catch (NegocioException e){
            ra.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/cuentas";
    }
    
    private void cargarListas(Model model) {
        model.addAttribute("categorias", catCuentaRepo.findAll());
    }
}
