/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.service;

import com.finanzas.gestorFinanciero.dto.TransferenciaEditForm;
import com.finanzas.gestorFinanciero.model.Cuenta;
import com.finanzas.gestorFinanciero.model.Transferencia;
import com.finanzas.gestorFinanciero.repository.CuentaRepository;
import com.finanzas.gestorFinanciero.repository.TransferenciaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author adris
 */
@Service
public class TransferenciaService {
    private final TransferenciaRepository transferenciaRepo;
    private final CuentaService cuentaServ;
    private final CuentaRepository cuentaRepo;

    public TransferenciaService(TransferenciaRepository transferenciaRepo, CuentaService cuentaServ, CuentaRepository cuentaRepo) {
        this.transferenciaRepo = transferenciaRepo;
        this.cuentaServ = cuentaServ;
        this.cuentaRepo = cuentaRepo;
    }
    
    @Transactional
    public Transferencia transferir(Long codCliente, Long idCuentaOg,
                                Long idCuentaDes, BigDecimal monto, LocalDate fecha) {
        validarMonto(monto);

        Cuenta cuentaOg = cuentaServ.cuentaDelCliente(idCuentaOg,codCliente);
        Cuenta cuentaDes = cuentaServ.cuentaDelCliente(idCuentaDes,codCliente);

        if (cuentaOg.getSaldo().compareTo(monto) < 0) {
            throw new NegocioException("Saldo insuficiente en " + cuentaOg.getNombreCuenta());
        }

        Transferencia transferencia = new Transferencia();
        transferencia.setMonto(monto);
        transferencia.setFecha(fecha);
        transferencia.setCuentaOg(cuentaOg);
        transferencia.setCuentaDes(cuentaDes);

        cuentaOg.setSaldo(cuentaOg.getSaldo().subtract(monto));
        cuentaDes.setSaldo(cuentaDes.getSaldo().add(monto));
        cuentaRepo.save(cuentaOg);
        cuentaRepo.save(cuentaDes);
        return transferenciaRepo.save(transferencia);
    }

    @Transactional
    public void deshacer(Long idTransferencia, Long codCliente){
        Transferencia t = transferenciaDelCliente(idTransferencia, codCliente);
        Cuenta cuentaDes = t.getCuentaOg();
        Cuenta cuentaOg = t.getCuentaDes();
        
        BigDecimal nuevoSaldo = cuentaOg.getSaldo().subtract(t.getMonto());   
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegocioException("No puedes eliminar esta transferencia: el saldo quedaría negativo en " 
                    + cuentaOg.getNombreCuenta());
        }
        
        transferir(codCliente, cuentaOg.getId(), cuentaDes.getId(), t.getMonto(), LocalDate.now());
    }
    
    @Transactional (readOnly= true)
    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El monto debe ser mayor a 0");
        }
    }
    
    @Transactional (readOnly= true)
    private Transferencia transferenciaDelCliente(Long id, Long codCliente) {
        Transferencia t = transferenciaRepo.findById(id)
                .orElseThrow(() -> new NegocioException("La transferencia no existe"));
        cuentaServ.cuentaDelCliente(t.getCuentaOg().getId(), codCliente);
        cuentaServ.cuentaDelCliente(t.getCuentaDes().getId(), codCliente);
        return t;
    }

    @Transactional
    public void actualizar(Long id, TransferenciaEditForm f, Long codCliente) {
        Transferencia t = transferenciaDelCliente(id, codCliente);
        validarMonto(f.getMonto());
        Cuenta cuentaOg = t.getCuentaOg();
        Cuenta cuentaDes = t.getCuentaDes();

        BigDecimal efectoViejo = t.getMonto();
        
        t.setMonto(f.getMonto());
        t.setFecha(f.getFecha());

        BigDecimal nuevoSaldoOrigen = cuentaOg.getSaldo().add(efectoViejo).subtract(t.getMonto());
        BigDecimal nuevoSaldoDestino = cuentaDes.getSaldo().subtract(efectoViejo).add(t.getMonto());
        if (nuevoSaldoOrigen.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegocioException("Saldo insuficiente para ese monto en " + cuentaOg.getNombreCuenta());           
        }
        if (nuevoSaldoDestino.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegocioException("Saldo insuficiente para ese monto en " + cuentaDes.getNombreCuenta());           
        }
        cuentaOg.setSaldo(nuevoSaldoOrigen);
        cuentaDes.setSaldo(nuevoSaldoDestino);
        cuentaRepo.save(cuentaOg);
        cuentaRepo.save(cuentaDes);
    }      
    
    @Transactional(readOnly = true)
    public Transferencia obtener(Long id, Long codCliente) {
        return transferenciaDelCliente(id, codCliente);
    }
}
