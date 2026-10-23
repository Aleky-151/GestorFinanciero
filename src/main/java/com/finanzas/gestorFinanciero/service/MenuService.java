/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.service;

import com.finanzas.gestorFinanciero.dto.MenuItem;
import com.finanzas.gestorFinanciero.model.Opcion;
import com.finanzas.gestorFinanciero.repository.OpcionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author adris
 */
@Service
public class MenuService {

    private final OpcionRepository opcionRepo;

    public MenuService(OpcionRepository opcionRepo) {
        this.opcionRepo = opcionRepo;
    }

    @Transactional(readOnly = true)
    public List<MenuItem> arbol() {
        List<Opcion> todas = opcionRepo.findByActivoOrderByOrdenAsc("S");
        return todas.stream()
                .filter(o -> o.getIdOpcionRef() == null)
                .map(o -> convertir(o, todas))
                .toList();
    }

    private MenuItem convertir(Opcion o, List<Opcion> todas) {
        List<MenuItem> hijos = todas.stream()
                .filter(h -> o.getId().equals(h.getIdOpcionRef()))
                .map(h -> convertir(h, todas))
                .toList();
        return new MenuItem(o.getNombreOpcion(), o.getUrlOpcion(), o.getIcono(), hijos);
    }
}