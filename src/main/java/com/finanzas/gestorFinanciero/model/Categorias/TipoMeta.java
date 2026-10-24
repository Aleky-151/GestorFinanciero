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
@Table(name= "tipo_metas")
public class TipoMeta {
     @Id
     @GeneratedValue(strategy= GenerationType.IDENTITY)
     @Column(name="id_tipo")
     private Long id;
     
     @Column(name="tipo", unique=true, nullable=false)
     private String tipo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
