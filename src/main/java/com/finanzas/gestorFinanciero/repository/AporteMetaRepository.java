/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finanzas.gestorFinanciero.repository;

import com.finanzas.gestorFinanciero.model.AporteMeta;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author adris
 */
public interface AporteMetaRepository extends JpaRepository<AporteMeta, Long>{
    @Query("SELECT COUNT(a) > 0 FROM AporteMeta a WHERE a.meta.id= :idMeta")
    boolean existenAportesDeMeta(@Param("idMeta") Long id);
    
    public Iterable<AporteMeta> findByMetaId(Long id);    
    
    @Query("SELECT DISTINCT a.meta.id FROM AporteMeta a WHERE a.meta.cliente.codCliente = :cod")
    Set<Long> idsMetasConAportes(@Param("cod") Long cod);
}
