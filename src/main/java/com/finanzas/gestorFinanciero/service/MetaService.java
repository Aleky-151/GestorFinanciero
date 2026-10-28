/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.service;

import com.finanzas.gestorFinanciero.dto.MetaFila;
import com.finanzas.gestorFinanciero.dto.MetaForm;
import com.finanzas.gestorFinanciero.model.AporteMeta;
import com.finanzas.gestorFinanciero.model.Categorias.TipoMeta;
import com.finanzas.gestorFinanciero.model.Cliente;
import com.finanzas.gestorFinanciero.model.Cuenta;
import com.finanzas.gestorFinanciero.model.Meta;
import com.finanzas.gestorFinanciero.repository.AporteMetaRepository;
import com.finanzas.gestorFinanciero.repository.ClienteRepository;
import com.finanzas.gestorFinanciero.repository.CuentaRepository;
import com.finanzas.gestorFinanciero.repository.EstadoMetaRepository;
import com.finanzas.gestorFinanciero.repository.MetaRepository;
import com.finanzas.gestorFinanciero.repository.TipoMetaRepository;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author adris
 */
@Service
public class MetaService {
    private final MetaRepository metaRepo;
    private final TipoMetaRepository tipoMetaRepo;
    private final EstadoMetaRepository estadoMetaRepo;
    private final ClienteRepository clienteRepo;
    private final AporteMetaRepository aporteRepo;
    private final CuentaRepository cuentaRepo;
    private final CuentaService cuentaService;
    private static final Long ESTADO_PROGRESO = 1L;
    private static final Long ESTADO_CANCELADA = 4L;

    public MetaService(MetaRepository metaRepo, TipoMetaRepository tipoMetaRepo, EstadoMetaRepository estadoMetaRepo, ClienteRepository clienteRepo, AporteMetaRepository aporteRepo, CuentaRepository cuentaRepo, CuentaService cuentaService) {
        this.metaRepo = metaRepo;
        this.tipoMetaRepo = tipoMetaRepo;
        this.estadoMetaRepo = estadoMetaRepo;
        this.clienteRepo = clienteRepo;
        this.aporteRepo = aporteRepo;
        this.cuentaRepo = cuentaRepo;
        this.cuentaService = cuentaService;
    }
    
    @Transactional(readOnly = true)
    public List<MetaFila> listar(Long codCliente) {
        Set<Long> conAportes = aporteRepo.idsMetasConAportes(codCliente);
        return metaRepo.listarPorCliente(codCliente).stream()
                .map(m -> new MetaFila(m, conAportes.contains(m.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Meta> listarAportables(Long codCliente) {
        LocalDate hoy = LocalDate.now();
        return metaRepo.listarPorClienteYEstado(codCliente, ESTADO_PROGRESO).stream()
                .filter(m -> !m.getFechaLimite().isBefore(hoy))   
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<Meta> listarRetirables(Long codCliente) {
        return metaRepo.listarPorCliente(codCliente).stream()
                .filter(m -> m.getEstado().getId()!= ESTADO_CANCELADA)
                .toList();
    }
        
    @Transactional
    public Meta crear(MetaForm form, Long codCliente) {
        TipoMeta tipoMeta= tipoMetaRepo.findById(form.getIdTipo())
                .orElseThrow(()->new NegocioException("El tipo no existe"));
        
        Cliente cliente= clienteRepo.findById(codCliente)
                .orElseThrow(()-> new NegocioException("El cliente no existe"));
        
        try{        
            Meta meta= new Meta();
            meta.setNombreMeta(form.getNombreMeta());
            meta.setMontoObjetivo(form.getMontoObjetivo());
            meta.setCliente(cliente);
            meta.setFechaLimite(form.getFechaLimite());
            meta.setTipo(tipoMeta);
            meta.setSaldo(BigDecimal.ZERO);
            meta.setEstado(estadoMetaRepo.findByEstado("EN_PROGRESO"));
            return metaRepo.save(meta);        
        } catch (DataAccessException ex) {
            throw traducir(ex);              
        }
    }
    
    @Transactional
    public void eliminar(Long id, Long codCliente) {
        Meta meta = metaDelCliente(id, codCliente);

        if (aporteRepo.existsByMetaId(id)) {
            if (meta.getEstado().getId() != ESTADO_CANCELADA) {  
                throw new NegocioException("No se puede eliminar una meta con aportes. Opte por cancelarla.");
            }
            if (meta.getSaldo().signum() != 0) {
                throw new NegocioException("La meta todavía tiene dinero reservado.");
            }
            try {
                aporteRepo.borrarPorMetaYTipo(id, 'R');   
                aporteRepo.borrarPorMetaYTipo(id, 'A');   
            } catch (DataAccessException ex) {
                throw traducir(ex);
            }
        }
        metaRepo.deleteById(id);
    }

    @Transactional
    public Meta cancelar(Long id, Long codCliente) {
        Meta meta = metaDelCliente(id, codCliente);
        if (!"EN_PROGRESO".equals(meta.getEstado().getEstado())) {
            throw new NegocioException("Solo se pueden cancelar metas en progreso");
        }
        
        // neto por cuenta: aportes (A) menos devoluciones previas (R)
        Map<Cuenta, BigDecimal> porCuenta = new HashMap<>();
        for (AporteMeta a : aporteRepo.findByMetaId(id)) {
            BigDecimal signo = a.getTipo() == 'A' ? a.getMonto() : a.getMonto().negate();
            porCuenta.merge(a.getCuenta(), signo, BigDecimal::add);
        }

        porCuenta.forEach((cuenta, monto) -> {
            if (monto.signum() > 0) {
                cuenta.setSaldo(cuenta.getSaldo().add(monto));
                cuentaRepo.save(cuenta);

                AporteMeta r = new AporteMeta();
                r.setMeta(meta);
                r.setCuenta(cuenta);
                r.setMonto(monto);
                r.setTipo('R');
                r.setFecha(LocalDate.now());
                aporteRepo.save(r);
            }
        });

        meta.setEstado(estadoMetaRepo.findByEstado("CANCELADA"));
        return metaRepo.save(meta);
    }

    public Meta obtener(Long id, Long codCliente) {
        return metaDelCliente(id, codCliente);
    }

    @Transactional
    public Meta actualizar(Long id, MetaForm form, Long codCliente) {
        TipoMeta tipoMeta= tipoMetaRepo.findById(form.getIdTipo())
                .orElseThrow(()->new NegocioException("El tipo no existe"));
        
        try{
            Meta m= metaDelCliente(id,codCliente);
            m.setFechaLimite(form.getFechaLimite());
            m.setMontoObjetivo(form.getMontoObjetivo());
            m.setNombreMeta(form.getNombreMeta());
            m.setTipo(tipoMeta);    
            return metaRepo.save(m);
        } catch (DataAccessException ex){
            throw traducir(ex);
        }
    }

    @Transactional
    public void aportar(AporteCommand cmd, Long codCliente) {
        validarMonto(cmd.monto());

        Meta meta = metaDelCliente(cmd.idMeta(), codCliente);
        if (meta.getEstado().getId() != 1) {
            throw new NegocioException("Solo puedes aportar a metas en progreso");
        }

        Cuenta cuenta = cuentaService.cuentaDelCliente(cmd.idCuenta(), codCliente);
        if (cuenta.getSaldo().compareTo(cmd.monto()) < 0) {
            throw new NegocioException("Saldo insuficiente en " + cuenta.getNombreCuenta());
        }
        
        if(meta.getFechaLimite().isBefore(cmd.fecha())){
            throw new NegocioException("No se puede aportar a una meta vencida");
        }
        
        try {
            cuenta.setSaldo(cuenta.getSaldo().subtract(cmd.monto()));
            cuentaRepo.save(cuenta);

            AporteMeta aporte = new AporteMeta();
            aporte.setMeta(meta);
            aporte.setCuenta(cuenta);
            aporte.setMonto(cmd.monto());
            aporte.setFecha(cmd.fecha());
            aporte.setTipo('A');
            aporteRepo.saveAndFlush(aporte);   
        } catch (DataAccessException ex) {
            throw traducir(ex);              
        }
    }
   
    @Transactional
    public void retirar(AporteCommand cmd, Long codCliente) {
        validarMonto(cmd.monto());

        Meta meta = metaDelCliente(cmd.idMeta(), codCliente);
        
        if (Objects.equals(meta.getEstado().getId(), ESTADO_CANCELADA)) {
            throw new NegocioException("No se puede retirar de una meta cancelada");
        }
        if (meta.getSaldo()== BigDecimal.ZERO) {
            throw new NegocioException("No hay saldo por retirar en la meta");
        }
        
        if (meta.getSaldo().compareTo(cmd.monto()) < 0) {
            throw new NegocioException("La meta solo tiene S/ " + meta.getSaldo() + " disponible");
        }
        
        Cuenta cuenta = cuentaService.cuentaDelCliente(cmd.idCuenta(), codCliente);
        try {
            cuenta.setSaldo(cuenta.getSaldo().add(cmd.monto()));
            cuentaRepo.save(cuenta);

            AporteMeta retiro = new AporteMeta();
            retiro.setMeta(meta);
            retiro.setCuenta(cuenta);
            retiro.setMonto(cmd.monto());
            retiro.setFecha(cmd.fecha());
            retiro.setTipo('R');
            aporteRepo.saveAndFlush(retiro);   
        } catch (DataAccessException ex) {
            throw traducir(ex);              
        }
    }
    
    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El monto debe ser mayor a 0");
        }
    }

    private NegocioException traducir(DataAccessException ex) {
        Throwable t = ex.getMostSpecificCause();
        if (t instanceof SQLException sql && sql.getErrorCode() >= 20000 && sql.getErrorCode() <= 20999) {
            String msg = sql.getMessage().split("\n")[0].replaceFirst("^ORA-\\d+:\\s*", "");
            return new NegocioException(msg);
        }
        throw ex;
    }

    private Meta metaDelCliente(Long id, Long codCliente) {
        Meta m= metaRepo.findById(id)
                .orElseThrow(()-> new NegocioException("La meta no existe"));
        if(!m.getCliente().getCodCliente().equals(codCliente)){
            throw new NegocioException("La meta no existe");
        }
        return m;
    }
}
