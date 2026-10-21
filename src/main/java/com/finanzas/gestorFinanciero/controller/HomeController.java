/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;
import com.finanzas.gestorFinanciero.model.*;
import com.finanzas.gestorFinanciero.repository.*;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author adris
 */
@Controller
public class HomeController {
    
    private final CategoriaGastoRepository categoriaRepo;
    
    public HomeController(CategoriaGastoRepository categoriaRepo){
        this.categoriaRepo = categoriaRepo;
    }
    
    @GetMapping("/")
    public String inicio(Model model){
        model.addAttribute("categorias", categoriaRepo.findAll());
        return "inicio";
    }
}
