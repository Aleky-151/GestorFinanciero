/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finanzas.gestorFinanciero.repository;

import com.finanzas.gestorFinanciero.model.Categorias.EstadoMeta;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author adris
 */
public interface EstadoMetaRepository extends JpaRepository<EstadoMeta, Long>{
    public EstadoMeta findByEstado(String estado);
}
