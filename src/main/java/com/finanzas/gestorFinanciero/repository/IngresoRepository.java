/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finanzas.gestorFinanciero.repository;
import com.finanzas.gestorFinanciero.model.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author adris
 */
public interface IngresoRepository extends JpaRepository<Ingreso, Long>{
    
}
