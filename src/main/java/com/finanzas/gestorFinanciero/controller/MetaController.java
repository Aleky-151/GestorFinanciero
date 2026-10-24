/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;

import com.finanzas.gestorFinanciero.dto.AporteForm;
import com.finanzas.gestorFinanciero.dto.MetaForm;
import com.finanzas.gestorFinanciero.dto.security.UsuarioPrincipal;
import com.finanzas.gestorFinanciero.model.Meta;
import com.finanzas.gestorFinanciero.repository.CuentaRepository;
import com.finanzas.gestorFinanciero.repository.EstadoMetaRepository;
import com.finanzas.gestorFinanciero.repository.MetaRepository;
import com.finanzas.gestorFinanciero.repository.TipoMetaRepository;
import com.finanzas.gestorFinanciero.service.AporteCommand;
import com.finanzas.gestorFinanciero.service.MetaService;
import com.finanzas.gestorFinanciero.service.NegocioException;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author adris
 */
@Controller
@RequestMapping("/metas")
public class MetaController {
    private final MetaRepository metaRepo;
    private final TipoMetaRepository tipoMetaRepo;
    private final EstadoMetaRepository estadoMetaRepo;
    private final MetaService metaService;
    private final CuentaRepository cuentaRepo;

    public MetaController(MetaRepository metaRepo, TipoMetaRepository tipoMetaRepo, EstadoMetaRepository estadoMetaRepo, MetaService metaService, CuentaRepository cuentaRepo) {
        this.metaRepo = metaRepo;
        this.tipoMetaRepo = tipoMetaRepo;
        this.estadoMetaRepo = estadoMetaRepo;
        this.metaService = metaService;
        this.cuentaRepo = cuentaRepo;
    }
    
    @GetMapping
    private String listar(@AuthenticationPrincipal UsuarioPrincipal p, Model model) {
        model.addAttribute("metas", metaService.listar(p.getCodCliente()));
        return "metas";
    }
    
    @GetMapping("/nuevo")
    public String nuevo(Model model){
        MetaForm form = new MetaForm();
        model.addAttribute("metaForm", form);
        cargarTipos(model);
        return "meta-form";
    }
    
    @PostMapping
    public String guardar(@AuthenticationPrincipal UsuarioPrincipal p,
                          @Valid @ModelAttribute("metaForm") MetaForm form,
                          BindingResult result, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                metaService.crear(form, p.getCodCliente());
                ra.addFlashAttribute("mensaje", "Meta registrada ✅");
                return "redirect:/metas";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());   // error general del formulario
            }
        }
        cargarTipos(model);
        return "meta-form";
    }
    
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal p,
                           RedirectAttributes ra) {
        try {
            metaService.eliminar(id, p.getCodCliente());
            ra.addFlashAttribute("mensaje", "Meta eliminada ✅");
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/metas";
    }
    
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal p,
                           RedirectAttributes ra) {
        try {
            metaService.cancelar(id, p.getCodCliente());
            ra.addFlashAttribute("mensaje", "Meta cancelada ✅");
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/metas";
    }
    
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal p,
                         Model model, RedirectAttributes ra) {
        try {
            Meta m = metaService.obtener(id, p.getCodCliente());
            MetaForm form = new MetaForm();
            form.setIdMeta(m.getId());
            form.setNombreMeta(m.getNombreMeta());
            form.setMontoObjetivo(m.getMontoObjetivo());
            form.setFechaLimite (m.getFechaLimite());
            form.setIdTipo(m.getTipo().getId());
            
            model.addAttribute("metaForm", form);
            cargarTipos(model);
            return "meta-form";
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/metas";
        }
    }
    
    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("metaForm") MetaForm form,
                             BindingResult result, @AuthenticationPrincipal UsuarioPrincipal p,
                             Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                metaService.actualizar(id, form, p.getCodCliente());
                ra.addFlashAttribute("mensaje", "Meta actualizada ✅");
                return "redirect:/metas";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());
            }
        }
        Meta m = metaService.obtener(id, p.getCodCliente());
        cargarTipos(model);
        return "meta-form";
    }
    
    @GetMapping("/aportar")
    public String formAporte(@AuthenticationPrincipal UsuarioPrincipal p, Model model) {
        AporteForm form = new AporteForm();
        form.setFecha(LocalDate.now());
        model.addAttribute("aporteForm", form);
        cargarListas(model, p);              
        return "aporte-form";
    }
    
    @PostMapping("/aportar")
    public String aportar(@Valid @ModelAttribute("aporteForm") AporteForm form,
                          BindingResult result, @AuthenticationPrincipal UsuarioPrincipal p,
                          Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                metaService.aportar(
                    new AporteCommand(form.getIdMeta(), form.getIdCuenta(), form.getMonto(), form.getFecha()),
                    p.getCodCliente());
                ra.addFlashAttribute("mensaje", "Aporte registrado ✅");
                return "redirect:/metas";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());
            }
        }
        cargarListas(model, p);
        return "aporte-form";
    }
    
    private void cargarTipos(Model model) {
        model.addAttribute("tipos", tipoMetaRepo.findAll());
    }
    
    private void cargarListas(Model model, UsuarioPrincipal p){
        model.addAttribute("cuentas", cuentaRepo.listarPorCliente(p.getCodCliente()));
        model.addAttribute("metas", metaRepo.listarPorCliente(p.getCodCliente()));
        model.addAttribute("aportables", metaService.listarAportables(p.getCodCliente()));
    }
}
