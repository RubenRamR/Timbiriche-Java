
package com.mycompany.red;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializador implements ISerializador {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializar(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando a JSON", e);
        }
    }

    @Override
    public <T> T deserializar(String data, Class<T> tipoClase) {
        try {
            return objectMapper.readValue(data, tipoClase);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializando JSON", e);
        }
    }
}