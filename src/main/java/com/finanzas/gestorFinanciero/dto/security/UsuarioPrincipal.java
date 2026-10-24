/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.dto.security;

import com.finanzas.gestorFinanciero.model.Usuario;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


/**
 *
 * @author adris
 */
public class UsuarioPrincipal extends org.springframework.security.core.userdetails.User {

    private final Long codCliente;
    private final String nombreCompleto;

    public UsuarioPrincipal(Usuario u) {
        super(u.getUsuario(), u.getClave(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.codCliente = u.getCliente().getCodCliente();   // ajusta si lo renombraste a getId()
        this.nombreCompleto = u.getCliente().getNombres() + " " + u.getCliente().getAp_paterno();
    }

    public Long getCodCliente() { return codCliente; }
    public String getNombreCompleto() { return nombreCompleto; }

    public String getIniciales() {
        return nombreCompleto.isBlank() ? "?" : nombreCompleto.substring(0, 1).toUpperCase();
    }
}
