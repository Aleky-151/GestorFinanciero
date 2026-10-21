/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.model;
import com.finanzas.gestorFinanciero.model.Categorias.CategoriaGasto;
import jakarta.persistence.*;
/**
 *
 * @author adris
 */
@Entity
@Table(name = "gastos")
@DiscriminatorValue("G")
public class Gasto extends Movimiento {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaGasto categoria;
    
    public CategoriaGasto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaGasto categoria) {
        this.categoria = categoria;
    }
}
