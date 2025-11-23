
package com.mycompany.red;

public class DataDTO {
    private String tipo;
    private String payload;
    private String proyectoOrigen;
    private String proyectoDestino;
    private long timestamp;

    public DataDTO() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public DataDTO(String tipo) { 
        this();
        this.tipo = tipo; 
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    
    public String getProyectoOrigen() { return proyectoOrigen; }
    public void setProyectoOrigen(String proyectoOrigen) { 
        this.proyectoOrigen = proyectoOrigen; 
    }
    
    public String getProyectoDestino() { return proyectoDestino; }
    public void setProyectoDestino(String proyectoDestino) { 
        this.proyectoDestino = proyectoDestino; 
    }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "DataDTO{tipo='" + tipo + "', payload='" + payload + 
               "', origen='" + proyectoOrigen + "', destino='" + proyectoDestino + "'}";
    }
}