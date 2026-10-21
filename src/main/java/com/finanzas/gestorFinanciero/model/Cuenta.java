/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.model;
import com.finanzas.gestorFinanciero.model.Categorias.CategoriaCuenta;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 *
 * @author adris
 */
@Entity
@Table(name="cuentas")
public class Cuenta {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id_cuenta")
    private Long id;
    
    @Column(name="nombre_cuenta", nullable = false)
    private String nombreCuenta;
    
    @Column(name="saldo", nullable= false)
    private BigDecimal saldo;
    
    @ManyToOne(fetch= FetchType.LAZY, optional= false)
    @JoinColumn(name="cod_cliente", nullable = false)
    private Cliente cliente;
    
    @ManyToOne(fetch= FetchType.LAZY, optional= false)
    @JoinColumn(name="id_categoria", nullable= false)
    private CategoriaCuenta categoria;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public CategoriaCuenta getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaCuenta categoria) {
        this.categoria = categoria;
    }
}
