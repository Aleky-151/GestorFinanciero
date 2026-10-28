/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finanzas.gestorFinanciero.repository;

import com.finanzas.gestorFinanciero.model.Meta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author adris
 */
public interface MetaRepository extends JpaRepository<Meta,Long>{
    @Query("SELECT m FROM Meta m JOIN FETCH m.estado WHERE m.cliente.codCliente = :cod ORDER BY m.estado.id")
    List<Meta> listarPorCliente(@Param("cod") Long cod);
    
    @Query("SELECT m FROM Meta m WHERE m.cliente.codCliente = :cod AND m.estado.id = :estado ORDER BY m.nombreMeta")
    List<Meta> listarPorClienteYEstado(@Param("cod") Long cod, @Param("estado") Long estado);
}
