/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 *
 * @author adris
 */
public class CuentaForm {
    private Long idCuenta;
    
    @NotBlank(message= "Nombre la cuenta a crear")
    @Size(max=80, message= "Máximo 80 caracteres")
    private String nombreCuenta;
    
    //@NotNull(message= "Ingrese su codigo de cliente")
    //private Long codCliente;
    
    @NotNull(message= "Elige una categoria")
    private Long idCategoria;
    
    @PositiveOrZero(message = "El monto debe ser igual o mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Máximo 2 decimales")
    private BigDecimal saldo;

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }
    /*
    public Long getCodCliente() {
        return codCliente;
    }

    public void setCodCliente(Long codCliente) {
        this.codCliente = codCliente;
    }
    */

    public Long getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(Long idCuenta) {
        this.idCuenta = idCuenta;
    }
    
    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}
