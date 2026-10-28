/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.service;

import com.finanzas.gestorFinanciero.dto.CuentaForm;
import com.finanzas.gestorFinanciero.model.Categorias.CategoriaCuenta;
import com.finanzas.gestorFinanciero.model.Cliente;
import com.finanzas.gestorFinanciero.model.Cuenta;
import com.finanzas.gestorFinanciero.repository.CategoriaCuentaRepository;
import com.finanzas.gestorFinanciero.repository.ClienteRepository;
import com.finanzas.gestorFinanciero.repository.CuentaRepository;
import com.finanzas.gestorFinanciero.repository.MovimientoRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author adris
 */
@Service
public class CuentaService {
    private final CuentaRepository cuentaRepo;
    private final CategoriaCuentaRepository categoriaCuentaRepo;
    private final ClienteRepository clienteRepo;
    private final MovimientoRepository movimientoRepo;

    public CuentaService(CuentaRepository cuentaRepo, CategoriaCuentaRepository categoriaCuentaRepo, ClienteRepository clienteRepo, MovimientoRepository movimientoRepo) {
        this.cuentaRepo = cuentaRepo;
        this.categoriaCuentaRepo = categoriaCuentaRepo;
        this.clienteRepo = clienteRepo;
        this.movimientoRepo = movimientoRepo;
    }
    
    @Transactional
    public Cuenta crear(CuentaForm f, Long codCliente) {
        CategoriaCuenta catCuenta= categoriaCuentaRepo.findById(f.getIdCategoria())
                .orElseThrow(()-> new NegocioException("La categoria no existe"));
        
        Cliente cliente= clienteRepo.findById(codCliente)
                .orElseThrow(()-> new NegocioException("El cliente no existe"));
        
        Cuenta cuenta= new Cuenta();
        cuenta.setNombreCuenta(f.getNombreCuenta());
        cuenta.setCategoria(catCuenta);
        BigDecimal saldo = f.getSaldo() == null ? BigDecimal.ZERO : f.getSaldo();
        cuenta.setSaldo(saldo);
        cuenta.setCliente(cliente);
        return cuentaRepo.save(cuenta);
    }
    
    @Transactional
    public Cuenta actualizar(Long id, Long codCliente, CuentaForm f) {
        Cuenta cuenta= cuentaDelCliente(id, codCliente);
        CategoriaCuenta catCuenta= categoriaCuentaRepo.findById(f.getIdCategoria())
                .orElseThrow(()-> new NegocioException("La categoria no existe"));
        
        cuenta.setNombreCuenta(f.getNombreCuenta());
        cuenta.setCategoria(catCuenta);
        return cuentaRepo.save(cuenta);
    }
    
    @Transactional
    public void eliminar(Long id, Long codCliente) {
        Cuenta cuenta= cuentaDelCliente(id, codCliente);
        if (movimientoRepo.existenMovimientosDeCuenta(id)) {
            throw new NegocioException("No puedes eliminar una cuenta que tiene movimientos");
}       
        cuentaRepo.delete(cuenta);
    }
    
    @Transactional(readOnly = true)
    public Cuenta cuentaDelCliente(Long idCuenta, Long codCliente) {
        return buscarDelCliente(idCuenta, codCliente)
            .orElseThrow(() -> new NegocioException("La cuenta no existe"));
    }
    
    @Transactional(readOnly = true)
    public Optional<Cuenta> buscarDelCliente(Long idCuenta, Long codCliente) {
        return cuentaRepo.findById(idCuenta)
                .filter(c -> c.getCliente().getCodCliente().equals(codCliente));
    }
}
