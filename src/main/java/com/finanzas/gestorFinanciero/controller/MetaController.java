/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;

import com.finanzas.gestorFinanciero.dto.AporteForm;
import com.finanzas.gestorFinanciero.dto.MetaFila;
import com.finanzas.gestorFinanciero.dto.MetaForm;
import com.finanzas.gestorFinanciero.dto.security.UsuarioPrincipal;
import com.finanzas.gestorFinanciero.model.Categorias.EstadoMeta;
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
import java.util.List;
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
    private final TipoMetaRepository tipoRepo;
    private final EstadoMetaRepository estadoRepo;
    private final MetaService metaService;
    private final CuentaRepository cuentaRepo;

    public MetaController(MetaRepository metaRepo, TipoMetaRepository tipoRepo, EstadoMetaRepository estadoRepo, MetaService metaService, CuentaRepository cuentaRepo) {
        this.metaRepo = metaRepo;
        this.tipoRepo = tipoRepo;
        this.estadoRepo = estadoRepo;
        this.metaService = metaService;
        this.cuentaRepo = cuentaRepo;
    }
    
    @GetMapping
    private String listar(@RequestParam(required=false) Long estado,
                            @AuthenticationPrincipal UsuarioPrincipal p, Model model) {
        
        EstadoMeta seleccionado = (estado == null)
            ? null
            : estadoRepo.findById(estado).orElse(null);

        List<MetaFila> metas = (seleccionado == null)
            ? metaService.listar(p.getCodCliente())
            : metaService.listarPorEstado(p.getCodCliente(), estado);

        model.addAttribute("estados", estadoRepo.findAll());
        model.addAttribute("estadoSel", seleccionado);        
        model.addAttribute("metas", metas);
        model.addAttribute("cuentas", cuentaRepo.listarPorCliente(p.getCodCliente()));
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
                ra.addFlashAttribute("mensaje", "Se registró la meta exitosamente");
                return "redirect:/metas";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());   
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
            ra.addFlashAttribute("mensaje", "Se eliminó la meta exitosamente");
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/metas";
    }
    
    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, @RequestParam(required = false) Long idCuenta, 
                           @AuthenticationPrincipal UsuarioPrincipal p,
                           RedirectAttributes ra) {
        try {
            metaService.anular(id, idCuenta, p.getCodCliente());
            ra.addFlashAttribute("mensaje", "Se anuló la meta exitosamente. Revise la devolución del saldo en sus cuentas");
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
                ra.addFlashAttribute("mensaje", "Se actualizó la meta exitosamente");
                return "redirect:/metas";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());
            }
        }
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
    
    @GetMapping("/retirar")
    public String formRetiro(@AuthenticationPrincipal UsuarioPrincipal p, Model model) {
        AporteForm form = new AporteForm();
        form.setFecha(LocalDate.now());
        model.addAttribute("retiroForm", form);
        cargarListas(model, p);              
        return "retiro-form";
    }
    
    @PostMapping("/retirar")
    public String retirar(@Valid @ModelAttribute("retiroForm") AporteForm form,
                          BindingResult result, @AuthenticationPrincipal UsuarioPrincipal p,
                          Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                metaService.retirar(
                    new AporteCommand(form.getIdMeta(), form.getIdCuenta(), form.getMonto(), form.getFecha()),
                    p.getCodCliente());
                ra.addFlashAttribute("mensaje", "Retiro exitoso ✅");
                return "redirect:/metas";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());
            }
        }
        cargarListas(model, p);
        return "retiro-form";
    }
    
    private void cargarTipos(Model model) {
        model.addAttribute("tipos", tipoRepo.findAll());
    }
    
    private void cargarListas(Model model, UsuarioPrincipal p){
        model.addAttribute("cuentas", cuentaRepo.listarPorCliente(p.getCodCliente()));
        model.addAttribute("metas", metaRepo.listarPorCliente(p.getCodCliente()));
        model.addAttribute("aportables", metaService.listarAportables(p.getCodCliente()));
        model.addAttribute("retirables", metaService.listarRetirables(p.getCodCliente()));
    }
}
