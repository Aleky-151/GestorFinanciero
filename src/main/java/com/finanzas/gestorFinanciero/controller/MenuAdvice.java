/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;

import com.finanzas.gestorFinanciero.dto.MenuItem;
import com.finanzas.gestorFinanciero.dto.security.UsuarioPrincipal;
import com.finanzas.gestorFinanciero.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 *
 * @author adris
 */
@ControllerAdvice
public class MenuAdvice {

    private final MenuService menuService;

    public MenuAdvice(MenuService menuService) {
        this.menuService = menuService;
    }

    @ModelAttribute("menu")
    public List<MenuItem> menu() {
        return menuService.arbol();
    }

    @ModelAttribute("rutaActual")
    public String rutaActual(HttpServletRequest request) {
        return request.getRequestURI();
    }
    
    @ModelAttribute("usuarioActual")
    public UsuarioPrincipal usuarioActual(@AuthenticationPrincipal UsuarioPrincipal p) {
        return p;  
    }
}