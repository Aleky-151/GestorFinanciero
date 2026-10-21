/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;

import com.finanzas.gestorFinanciero.service.NegocioException;
import com.finanzas.gestorFinanciero.dto.RegistroForm;
import com.finanzas.gestorFinanciero.service.UsuarioService;
import jakarta.validation.Valid;
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
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login(){ 
        return "login"; 
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroForm form,
                            BindingResult result, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                usuarioService.registrar(form);
                ra.addFlashAttribute("mensaje", "Cuenta creada, ya puedes ingresar ✅");
                return "redirect:/login";
            } catch (NegocioException e) {
                result.reject("negocio", e.getMessage());
            }
        }
        return "registro";
    }
}
