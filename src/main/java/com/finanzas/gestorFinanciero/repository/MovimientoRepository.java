/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finanzas.gestorFinanciero.repository;
import com.finanzas.gestorFinanciero.model.Movimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author adris
 */
public interface MovimientoRepository extends JpaRepository<Movimiento,Long>{
    @Query("SELECT COUNT(m) > 0 FROM Movimiento m WHERE m.cuenta.id= :idCuenta")
    boolean existenMovimientosDeCuenta(@Param("idCuenta") Long idCuenta);
    @Query("SELECT m FROM Movimiento m JOIN FETCH m.cuenta c WHERE m.cuenta.cliente.codCliente = :cod ORDER BY c.nombreCuenta")
    List<Movimiento> listarPorCuenta(@Param("cod") Long cod);
}
