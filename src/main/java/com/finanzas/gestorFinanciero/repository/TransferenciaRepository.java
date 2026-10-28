/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finanzas.gestorFinanciero.repository;

import com.finanzas.gestorFinanciero.model.Transferencia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author adris
 */
public interface TransferenciaRepository extends JpaRepository<Transferencia, Long>{
    @Query("SELECT t FROM Transferencia t JOIN FETCH t.cuentaOg c WHERE c.cliente.codCliente = :cod ORDER BY t.fecha DESC")
    List<Transferencia> listarPorCliente(@Param("cod") Long cod);    
}
