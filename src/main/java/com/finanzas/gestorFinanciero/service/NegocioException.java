/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.service;

/**
 *
 * @author adris
 */
public class NegocioException extends RuntimeException{
    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
