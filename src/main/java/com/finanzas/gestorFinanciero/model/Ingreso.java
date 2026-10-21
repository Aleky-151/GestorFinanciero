/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.model;
import com.finanzas.gestorFinanciero.model.Categorias.CategoriaIngreso;
import jakarta.persistence.*;

/**
 *
 * @author adris
 */
@Entity
@Table(name="ingresos")
@DiscriminatorValue("I")
public class Ingreso extends Movimiento{
    @ManyToOne(fetch= FetchType.LAZY, optional= false)
    @JoinColumn(name="id_categoria", nullable= false)
    private CategoriaIngreso categoria;

    public CategoriaIngreso getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaIngreso categoria) {
        this.categoria = categoria;
    }
}
