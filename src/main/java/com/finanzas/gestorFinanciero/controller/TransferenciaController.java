/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;

import com.finanzas.gestorFinanciero.dto.TransferenciaEditForm;
import com.finanzas.gestorFinanciero.dto.TransferenciaForm;
import com.finanzas.gestorFinanciero.dto.security.UsuarioPrincipal;
import com.finanzas.gestorFinanciero.model.Transferencia;
import com.finanzas.gestorFinanciero.repository.CuentaRepository;
import com.finanzas.gestorFinanciero.repository.TransferenciaRepository;
import com.finanzas.gestorFinanciero.service.NegocioException;
import com.finanzas.gestorFinanciero.service.TransferenciaService;
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
@RequestMapping("/transferencias")
public class TransferenciaController {
    private final TransferenciaRepository transferenciaRepo;
    private final TransferenciaService transferenciaService;
    private final CuentaRepository cuentaRepo;

    public TransferenciaController(TransferenciaRepository transferenciaRepo, TransferenciaService transferenciaService, CuentaRepository cuentaRepo) {
        this.transferenciaRepo = transferenciaRepo;
        this.transferenciaService = transferenciaService;
        this.cuentaRepo = cuentaRepo;
    }
    
    @GetMapping
    private String cargarLista(@AuthenticationPrincipal UsuarioPrincipal p, Model model) {
        model.addAttribute("transferencias", transferenciaRepo.listarPorCliente(p.getCodCliente()));
        return "transferencias";
    }
    
    @GetMapping("/nuevo")
    public String nuevo(@AuthenticationPrincipal UsuarioPrincipal p, Model model) {
        TransferenciaForm form = new TransferenciaForm();
        form.setFecha(LocalDate.now());
        model.addAttribute("transferenciaForm", form);
        model.addAttribute("editando", false);
        cargarCuentas(model, p.getCodCliente());
        return "transferencia-form";
    }

    @PostMapping
    public String guardar(@AuthenticationPrincipal UsuarioPrincipal p,
                          @Valid @ModelAttribute("transferenciaForm") TransferenciaForm form,
                          BindingResult result, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                transferenciaService.transferir(p.getCodCliente(), form.getIdCuentaOg(), 
                        form.getIdCuentaDes(), form.getMonto(), form.getFecha());
                ra.addFlashAttribute("mensaje", "Transferencia exitosa ✅");
                return "redirect:/transferencias";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage()); 
            }
        }
        model.addAttribute("editando", false);
        cargarCuentas(model, p.getCodCliente());
        return "transferencia-form";
    }
    
    @PostMapping("/{id}/deshacer")
    public String deshacer(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal p,
                           RedirectAttributes ra) {
        try {
            transferenciaService.deshacer(id, p.getCodCliente());
            ra.addFlashAttribute("mensaje", "Transferencia deshecha ✅");
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transferencias";
    }
    
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal p,
                         Model model, RedirectAttributes ra) {
        try {
            Transferencia t = transferenciaService.obtener(id, p.getCodCliente());
            TransferenciaEditForm form = new TransferenciaEditForm();
            form.setId(t.getId());
            form.setMonto(t.getMonto());
            form.setFecha(t.getFecha());

            model.addAttribute("transferenciaForm", form);
            model.addAttribute("editando", true);
            model.addAttribute("idTransferencia", id);
            return "transferencia-form";
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/transferencias";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("transferenciaForm") TransferenciaEditForm form,
                             BindingResult result, @AuthenticationPrincipal UsuarioPrincipal p,
                             Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                transferenciaService.actualizar(id, form, p.getCodCliente());
                ra.addFlashAttribute("mensaje", "Transferencia actualizada ✅");
                return "redirect:/transferencias";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());
            }
        }
        model.addAttribute("editando", true);
        model.addAttribute("idTransferencia", id);        
        return "transferencia-form";
    }
    
    public void cargarCuentas(Model model, Long codCliente){
        model.addAttribute("cuentas", cuentaRepo.listarPorCliente(codCliente));
    }
}
