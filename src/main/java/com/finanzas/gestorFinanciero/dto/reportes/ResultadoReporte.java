/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.finanzas.gestorFinanciero.dto.reportes;

import java.util.List;

/**
 *
 * @author adris
 */
public record ResultadoReporte(String formato, List<String> columnas,
                               List<List<Object>> filas, String texto) {
    public static ResultadoReporte tabla(List<String> c, List<List<Object>> f) {
        return new ResultadoReporte("T", c, f, null);
    }
    public static ResultadoReporte texto(String t) {
        return new ResultadoReporte("X", List.of(), List.of(), t);
    }
}
