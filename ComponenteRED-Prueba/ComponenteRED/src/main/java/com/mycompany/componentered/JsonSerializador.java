/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.componentered;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 *
 * @author rramirez Implementación concreta usando la librería Jackson.
 */
public class JsonSerializador implements ISerializador {

    private final ObjectMapper mapper;

    public JsonSerializador() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String serializar(Object obj) {
        try
        {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e)
        {
            throw new RuntimeException("Error crítico serializando JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deserializar(String json, Class<?> clase) {
        try
        {
            Object resultado = mapper.readValue(json, clase);
            return resultado;
        } catch (Throwable e)
        {
            System.err.println("[ERROR-JACKSON] Falló Jackson: " + e.getClass().getName() + " -> " + e.getMessage());
            e.printStackTrace(); // ¡Muestra el stacktrace!
            throw new RuntimeException("Error deserializando", e);
        }
    }
}
