/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.model;

import com.finanzas.gestorFinanciero.model.Categorias.EstadoMeta;
import com.finanzas.gestorFinanciero.model.Categorias.TipoMeta;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.annotations.DynamicUpdate;

/**
 *
 * @author adris
 */
@Entity
@Table(name="metas")
@DynamicUpdate
public class Meta {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_meta")
    private Long id;
    
    @Column(name="nombre_meta",nullable=false)
    private String nombreMeta;
    
    @Column(name="monto_objetivo", nullable=false)
    private BigDecimal montoObjetivo;
    
    @Column(name="saldo", nullable=false)
    private BigDecimal saldo;
    
    @Column(name="fecha_limite", nullable=false)
    private LocalDate fechaLimite;
    
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="cod_cliente", nullable=false)
    private Cliente cliente;
    
    @OneToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="id_tipo", nullable=false)
    private TipoMeta tipo;
    
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="id_estado", nullable=false)
    private EstadoMeta estado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreMeta() {
        return nombreMeta;
    }

    public void setNombreMeta(String nombreMeta) {
        this.nombreMeta = nombreMeta;
    }

    public BigDecimal getMontoObjetivo() {
        return montoObjetivo;
    }

    public void setMontoObjetivo(BigDecimal montoObjetivo) {
        this.montoObjetivo = montoObjetivo;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public TipoMeta getTipo() {
        return tipo;
    }

    public void setTipo(TipoMeta tipo) {
        this.tipo = tipo;
    }

    public EstadoMeta getEstado() {
        return estado;
    }

    public void setEstado(EstadoMeta estado) {
        this.estado = estado;
    }
}
