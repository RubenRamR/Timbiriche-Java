package timbiriche.presentacion;

import com.mycompany.dominio.Linea;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
/**
 *
 * @author rramirez
 */
public interface IModelViewModificable {

    void actualizarJugadaLocal(Linea linea);

    void actualizarEstadoDesdeMotor();
}
