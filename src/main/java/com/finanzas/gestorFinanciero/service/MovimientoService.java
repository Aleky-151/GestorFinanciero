/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.service;

import com.finanzas.gestorFinanciero.dto.MovimientoEditForm;
import com.finanzas.gestorFinanciero.model.*;
import com.finanzas.gestorFinanciero.model.Categorias.*;
import com.finanzas.gestorFinanciero.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimientoService {

    private final CuentaService cuentaServ;
    private final CuentaRepository cuentaRepo;
    private final GastoRepository gastoRepo;
    private final IngresoRepository ingresoRepo;
    private final CategoriaGastoRepository catGastoRepo;
    private final CategoriaIngresoRepository catIngresoRepo;
    private final MovimientoRepository movimientoRepo;

    public MovimientoService(CuentaService cuentaServ, CuentaRepository cuentaRepo, GastoRepository gastoRepo, IngresoRepository ingresoRepo, CategoriaGastoRepository catGastoRepo, CategoriaIngresoRepository catIngresoRepo, MovimientoRepository movimientoRepo) {
        this.cuentaServ = cuentaServ;
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

        Cuenta cuenta = cuentaServ.cuentaDelCliente(idCuenta,idCliente);
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
    public Ingreso registrarIngreso(Long idCliente,
                                    Long idCuenta, Long idCategoria, String descripcion,
                                    BigDecimal monto, LocalDate fecha) {
        validarMonto(monto);

        Cuenta cuenta = cuentaServ.cuentaDelCliente(idCuenta, idCliente);
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
    
    @Transactional(readOnly = true)
    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El monto debe ser mayor a 0");
        }
    }
    
    // Efecto del movimiento sobre el saldo
    @Transactional(readOnly = true)
    private BigDecimal efecto(Movimiento m) {
        return (m instanceof Ingreso) ? m.getMonto() : m.getMonto().negate();
    }
    
    @Transactional(readOnly = true)
    private Movimiento movimientoDelCliente(Long id, Long codCliente) {
        Movimiento m = movimientoRepo.findById(id)
                .orElseThrow(() -> new NegocioException("El movimiento no existe"));
        cuentaServ.cuentaDelCliente(m.getCuenta().getId(), codCliente);
        return m;
    }
    
    @Transactional(readOnly = true)
    public Movimiento obtener(Long id, Long codCliente) {
        return movimientoDelCliente(id, codCliente);
    }

    @Transactional
    public void eliminar(Long id, Long codCliente) {
        Movimiento m = movimientoDelCliente(id, codCliente);
        Cuenta cuenta = m.getCuenta();

        BigDecimal nuevoSaldo = cuenta.getSaldo().subtract(efecto(m));   // deshacer
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegocioException("No puedes eliminar este ingreso: el saldo quedaría negativo");
        }
        cuenta.setSaldo(nuevoSaldo);
        cuentaRepo.save(cuenta);
        movimientoRepo.delete(m);
    }

    @Transactional
    public void actualizar(Long id, MovimientoEditForm f, Long codCliente) {
        Movimiento m = movimientoDelCliente(id, codCliente);
        validarMonto(f.getMonto());
        Cuenta cuenta = m.getCuenta();

        BigDecimal efectoViejo = efecto(m);

        m.setDescripcion(f.getDescripcion());
        m.setMonto(f.getMonto());
        m.setFecha(f.getFecha());
        if (m instanceof Gasto g) {
            g.setCategoria(catGastoRepo.findById(f.getIdCategoria())
                    .orElseThrow(() -> new NegocioException("La categoría no existe")));
        } else if (m instanceof Ingreso i) {
            i.setCategoria(catIngresoRepo.findById(f.getIdCategoria())
                    .orElseThrow(() -> new NegocioException("La categoría no existe")));
        }

        // saldo = saldo - efectoViejo + efectoNuevo
        BigDecimal nuevoSaldo = cuenta.getSaldo().subtract(efectoViejo).add(efecto(m));
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegocioException("Saldo insuficiente para ese monto");   // rollback automático
        }
        cuenta.setSaldo(nuevoSaldo);
        cuentaRepo.save(cuenta);
    }   
}
