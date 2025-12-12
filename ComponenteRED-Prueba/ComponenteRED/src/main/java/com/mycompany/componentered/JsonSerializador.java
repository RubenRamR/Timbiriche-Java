/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.componentered;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import java.io.IOException;

/**
 *
 * @author rramirez Implementación concreta usando la librería Jackson.
 */
public class JsonSerializador implements ISerializador {

    private final ObjectMapper mapper;

    public JsonSerializador() {
        this.mapper = new ObjectMapper();

        // Configuración básica
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // ELIMINADO: activateDefaultTyping (PolymorphicTypeValidator)
        // Ya NO queremos que agregue "@class": "com.mycompany..."
        // Queremos JSON puro: {"x": 10, "y": 20}
    }

    @Override
    public String serializar(Object obj) {
        try
        {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e)
        {
            throw new RuntimeException("Error serializando JSON", e);
        }
    }

    @Override
    public Object deserializar(String json, Class<?> clase) {
        try
        {
            return mapper.readValue(json, clase);
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * NUEVO MÉTODO ÚTIL: Convierte un Mapa genérico a un Objeto concreto. El
     * Servidor nos devolverá un Mapa, y el Cliente necesitará convertirlo a
     * Linea.
     */
    public <T> T convertirDesdeMapa(Object mapa, Class<T> claseDestino) {
        return mapper.convertValue(mapa, claseDestino);
    }
}
