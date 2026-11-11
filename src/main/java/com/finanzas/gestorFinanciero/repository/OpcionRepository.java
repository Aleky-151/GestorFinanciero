/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finanzas.gestorFinanciero.repository;

import com.finanzas.gestorFinanciero.model.Opcion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author adris
 */
public interface OpcionRepository extends JpaRepository<Opcion, Long> {
    @Query("SELECT o FROM Opcion o WHERE o.activo = 'S' " +
       "AND (o.rolRequerido IS NULL OR o.rolRequerido = :rol) ORDER BY o.orden")
    List<Opcion> findVisibles(@Param("rol") String rol);
}