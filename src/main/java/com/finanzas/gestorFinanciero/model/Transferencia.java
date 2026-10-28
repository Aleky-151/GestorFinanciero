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
@Table(name= "transferencias")
public class Transferencia {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name= "id_transferencia")
    private Long id;

    @Column(name="monto", nullable= false)
    private BigDecimal monto;
    
    @Column(name="fecha", nullable= false)
    private LocalDate fecha;
    
    @ManyToOne(fetch= FetchType.LAZY, optional= false)
    @JoinColumn(name="id_cuentaog", nullable= false)
    private Cuenta cuentaOg;

    @ManyToOne(fetch= FetchType.LAZY, optional= false)
    @JoinColumn(name="id_cuentades", nullable= false)
    private Cuenta cuentaDes;

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

    public Cuenta getCuentaOg() {
        return cuentaOg;
    }

    public void setCuentaOg(Cuenta cuentaOg) {
        this.cuentaOg = cuentaOg;
    }

    public Cuenta getCuentaDes() {
        return cuentaDes;
    }

    public void setCuentaDes(Cuenta cuentaDes) {
        this.cuentaDes = cuentaDes;
    }
}
