/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.componentered;

/**
 *
 * @author rramirez Interfaz interna para la conversión de objetos.
 */
public interface ISerializador {

    public String serializar(Object obj);

    public Object deserializar(String json, Class<?> clase);
}
