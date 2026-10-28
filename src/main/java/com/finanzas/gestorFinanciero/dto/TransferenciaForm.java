/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 *
 * @author adris
 */
public class TransferenciaForm {
    @NotNull(message = "Elige una cuenta origen")
    private Long idCuentaOg;

    @NotNull(message = "Elige una cuenta destino")
    private Long idCuentaDes;
    
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Máximo 2 decimales")
    private BigDecimal monto;

    @NotNull(message = "La fecha es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    public Long getIdCuentaOg() {
        return idCuentaOg;
    }

    public void setIdCuentaOg(Long idCuentaOg) {
        this.idCuentaOg = idCuentaOg;
    }

    public Long getIdCuentaDes() {
        return idCuentaDes;
    }

    public void setIdCuentaDes(Long idCuentaDes) {
        this.idCuentaDes = idCuentaDes;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    
}
