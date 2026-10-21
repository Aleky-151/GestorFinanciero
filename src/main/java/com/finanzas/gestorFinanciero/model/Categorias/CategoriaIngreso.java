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
@Table(name="categoria_ingresos")
public class CategoriaIngreso {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name= "id_categoria")
    private Long id;
    
    @Column(name= "categoria", unique= true)
    private String categoria;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    
}
