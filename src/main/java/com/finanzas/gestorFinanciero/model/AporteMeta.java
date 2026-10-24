/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author adris
 */
@Entity
@Table(name="aportes_meta")
public class AporteMeta {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_aporte")
    private Long id;
    
    @Column(name="monto", nullable=false)
    private BigDecimal monto;
    
    @Column(name="fecha", nullable=false)
    private LocalDate fecha;
    
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="id_meta", nullable=false)
    private Meta meta;
    
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="id_cuenta", nullable=false)
    private Cuenta cuenta;

    @Column(name="tipo", nullable=false)
    private char tipo;

    public char getTipo() {
        return tipo;
    }

    public void setTipo(char tipo) {
        this.tipo = tipo;
    }
    
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }
    
    
}
