/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.service;

import com.finanzas.gestorFinanciero.dto.RegistroForm;
import com.finanzas.gestorFinanciero.model.Cliente;
import com.finanzas.gestorFinanciero.model.Usuario;
import com.finanzas.gestorFinanciero.repository.ClienteRepository;
import com.finanzas.gestorFinanciero.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author adris
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final ClienteRepository clienteRepo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarioRepo, ClienteRepository clienteRepo,
                          PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.clienteRepo = clienteRepo;
        this.encoder = encoder;
    }

    @Transactional
    public void registrar(RegistroForm f) {
        if (usuarioRepo.existsByUsuario(f.getUsuario())) {
            throw new NegocioException("Ese usuario ya existe");
        }
        Cliente c = new Cliente();
        c.setNombres(f.getNombres());
        c.setAp_paterno(f.getApPaterno());
        c.setAp_materno(f.getApMaterno());
        c = clienteRepo.save(c);

        Usuario u = new Usuario();
        u.setUsuario(f.getUsuario());
        u.setClave(encoder.encode(f.getClave()));   // BCrypt, nunca texto plano
        u.setCliente(c);
        usuarioRepo.save(u);
    }
}
