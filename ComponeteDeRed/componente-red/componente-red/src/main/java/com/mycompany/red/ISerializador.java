package com.mycompany.red;

public interface ISerializador {

    String serializar(Object obj);

    <T> T deserializar(String data, Class<T> tipoClase);
}
