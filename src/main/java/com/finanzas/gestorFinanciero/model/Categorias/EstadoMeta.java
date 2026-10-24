/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.model.Categorias;

import jakarta.persistence.*;

/**
 *
 * @author adris
 */
@Entity
@Table(name="estado_metas")
public class EstadoMeta {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_estado")
    private Long id;
    
    @Column(name="estado", unique=true, nullable=false)
    private String estado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
