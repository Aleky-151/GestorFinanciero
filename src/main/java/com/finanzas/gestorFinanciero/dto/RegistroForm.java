/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.dto;

import jakarta.validation.constraints.*;

/**
 *
 * @author adris
 */
public class RegistroForm {
    @NotBlank 
    @Size(max = 100) 
    private String nombres;
    
    @NotBlank 
    @Size(max = 50)  
    private String apPaterno;
    
    @Size(max = 50)            
    private String apMaterno;
    
    @NotBlank 
    @Size(min = 4, max = 50, message = "Minimo 4 caracteres") 
    private String usuario;
    
    @NotBlank 
    @Size(min = 8, max = 60, message = "Mínimo 8 caracteres") 
    private String clave;

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApPaterno() {
        return apPaterno;
    }

    public void setApPaterno(String apPaterno) {
        this.apPaterno = apPaterno;
    }

    public String getApMaterno() {
        return apMaterno;
    }

    public void setApMaterno(String apMaterno) {
        this.apMaterno = apMaterno;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}
