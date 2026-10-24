/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.controller;

import com.finanzas.gestorFinanciero.service.NegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author adris
 */
@ControllerAdvice
public class ErrorAdvice {

    @ExceptionHandler(NegocioException.class)
    public String negocio(NegocioException e, RedirectAttributes ra, HttpServletRequest req) {
        ra.addFlashAttribute("error", e.getMessage());
        String origen = req.getHeader("Referer");
        return "redirect:" + (origen != null ? origen : "/cuentas");
    }
}
