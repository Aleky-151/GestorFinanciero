/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.dto.security;

import com.finanzas.gestorFinanciero.model.Usuario;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


/**
 *
 * @author adris
 */
public class UsuarioPrincipal extends org.springframework.security.core.userdetails.User {

    private final Long codCliente;
    private final String nombreCompleto;

    public UsuarioPrincipal(Usuario u) {
        super(u.getUsuario(), u.getClave(), autoridades(u.getRol()));
        this.codCliente = u.getCliente().getCodCliente();   
        this.nombreCompleto = u.getCliente().getNombres() + " " + u.getCliente().getAp_paterno();
    }

    private static List<GrantedAuthority> autoridades(String rol) {
        List<GrantedAuthority> a = new ArrayList<>();
        a.add(new SimpleGrantedAuthority("ROLE_USER"));        
        if ("ADMIN".equals(rol)) a.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return a;
    }

    public boolean isAdmin() {
        return getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN"));
    }

    public Long getCodCliente() { return codCliente; }
    public String getNombreCompleto() { return nombreCompleto; }

    public String getIniciales() {
        return nombreCompleto.isBlank() ? "?" : nombreCompleto.substring(0, 1).toUpperCase();
    }
}
