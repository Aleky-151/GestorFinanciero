/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.finanzas.gestorFinanciero.dto;

import java.util.List;

/**
 *
 * @author adris
 */
public record MenuItem(String nombre, String url, String icono, List<MenuItem> hijos) {

    public boolean tieneHijos() {
        return !hijos.isEmpty();
    }

    public boolean activo(String ruta) {
        if (url != null && ruta != null && ruta.startsWith(url)) return true;
        return hijos.stream().anyMatch(h -> h.activo(ruta));
    }
}
