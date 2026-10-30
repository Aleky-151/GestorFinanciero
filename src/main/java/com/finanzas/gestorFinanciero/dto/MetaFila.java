/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.finanzas.gestorFinanciero.dto;

import com.finanzas.gestorFinanciero.model.Meta;

/**
 *
 * @author adris
 */
public record MetaFila(Meta meta, boolean tieneAportes, boolean saldoRetirado) {}
