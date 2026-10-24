/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.finanzas.gestorFinanciero.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author adris
 */
public record AporteCommand(Long idMeta, Long idCuenta, BigDecimal monto, LocalDate fecha) {
}
