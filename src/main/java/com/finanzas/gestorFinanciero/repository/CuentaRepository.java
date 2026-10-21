/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finanzas.gestorFinanciero.repository;
import com.finanzas.gestorFinanciero.model.Cuenta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
/**
 *
 * @author adris
 */
public interface CuentaRepository extends JpaRepository<Cuenta, Long>{
    @Query("SELECT c FROM Cuenta c JOIN FETCH c.categoria WHERE c.cliente.codCliente = :cod ORDER BY c.nombreCuenta")
    List<Cuenta> listarPorCliente(@Param("cod") Long cod);
}
