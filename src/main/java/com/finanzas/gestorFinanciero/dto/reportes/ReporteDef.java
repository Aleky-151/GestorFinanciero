/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.finanzas.gestorFinanciero.dto.reportes;

/**
 *
 * @author adris
 */
public record ReporteDef(Long id, String categoria, String nombre, String descripcion,
                         String paquete, String procedimiento,
                         boolean usaFechas, boolean soloAdmin, String formato) {}