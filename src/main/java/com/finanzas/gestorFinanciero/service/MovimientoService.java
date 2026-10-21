/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.service;

import com.finanzas.gestorFinanciero.dto.security.UsuarioPrincipal;
import com.finanzas.gestorFinanciero.model.*;
import com.finanzas.gestorFinanciero.model.Categorias.*;
import com.finanzas.gestorFinanciero.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimientoService {

    private final CuentaRepository cuentaRepo;
    private final GastoRepository gastoRepo;
    private final IngresoRepository ingresoRepo;
    private final CategoriaGastoRepository catGastoRepo;
    private final CategoriaIngresoRepository catIngresoRepo;
    private final MovimientoRepository movimientoRepo;

    public MovimientoService(CuentaRepository cuentaRepo, GastoRepository gastoRepo, IngresoRepository ingresoRepo, CategoriaGastoRepository catGastoRepo, CategoriaIngresoRepository catIngresoRepo, MovimientoRepository movimientoRepo) {
        this.cuentaRepo = cuentaRepo;
        this.gastoRepo = gastoRepo;
        this.ingresoRepo = ingresoRepo;
        this.catGastoRepo = catGastoRepo;
        this.catIngresoRepo = catIngresoRepo;
        this.movimientoRepo = movimientoRepo;
    }

    @Transactional
    public Gasto registrarGasto(Long idCliente,
                                Long idCuenta, Long idCategoria, String descripcion,
                                BigDecimal monto, LocalDate fecha) {
        validarMonto(monto);

        Cuenta cuenta = cuentaDelCliente(idCuenta,idCliente);
        CategoriaGasto categoria = catGastoRepo.findById(idCategoria)
                .orElseThrow(() -> new NegocioException("La categoría no existe"));

        if (cuenta.getSaldo().compareTo(monto) < 0) {
            throw new NegocioException("Saldo insuficiente en " + cuenta.getNombreCuenta());
        }

        Gasto gasto = new Gasto();
        gasto.setDescripcion(descripcion);
        gasto.setMonto(monto);
        gasto.setFecha(fecha);
        gasto.setCuenta(cuenta);
        gasto.setCategoria(categoria);

        cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
        cuentaRepo.save(cuenta);
        return gastoRepo.save(gasto);
    }

    @Transactional
    public void EliminarMovimiento(Long idMovimiento){
        Optional<Movimiento> movimiento= movimientoRepo.findById(idMovimiento);
        
    }
    
    @Transactional
    public Ingreso registrarIngreso(Long idCliente,
                                    Long idCuenta, Long idCategoria, String descripcion,
                                    BigDecimal monto, LocalDate fecha) {
        validarMonto(monto);

        Cuenta cuenta = cuentaDelCliente(idCuenta, idCliente);
        CategoriaIngreso categoria = catIngresoRepo.findById(idCategoria)
                .orElseThrow(() -> new NegocioException("La categoría no existe"));

        Ingreso ingreso = new Ingreso();
        ingreso.setDescripcion(descripcion);
        ingreso.setMonto(monto);
        ingreso.setFecha(fecha);
        ingreso.setCuenta(cuenta);
        ingreso.setCategoria(categoria);

        cuenta.setSaldo(cuenta.getSaldo().add(monto));
        cuentaRepo.save(cuenta);
        return ingresoRepo.save(ingreso);
    }

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El monto debe ser mayor a 0");
        }
    }
    
    private Cuenta cuentaDelCliente(Long idCuenta, Long codCliente) {
        Cuenta c = cuentaRepo.findById(idCuenta)
                .orElseThrow(() -> new NegocioException("La cuenta no existe"));
        if (!c.getCliente().getCodCliente().equals(codCliente)) {
            throw new NegocioException("La cuenta no existe");   
        }
        return c;
    }
}
