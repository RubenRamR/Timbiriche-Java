/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package timbiriche.presentacion;

/**
 *
 * @author rramirez
 */
public interface Subjet{

    void agregarObservador(Observer o);

    void removerObservador(Observer o);

    void notificarObservadores();
}
