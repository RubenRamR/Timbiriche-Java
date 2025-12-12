/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.servidor;

import java.util.ArrayList;
import java.util.List;

public class Blackboard {

    private List<Evento> espacioConocimiento;
    private List<IFuenteConocimiento> suscriptores;

    public Blackboard() {
        this.espacioConocimiento = new ArrayList<>();
        this.suscriptores = new ArrayList<>();
    }

    public void agregarEvento(Evento evento) {
        this.espacioConocimiento.add(evento);
    }

    public List<Evento> obtenerEventos() {
        return new ArrayList<>(espacioConocimiento);
    }

    public void suscribir(IFuenteConocimiento experto) {
        this.suscriptores.add(experto);
        experto.setBlackboard(this);
    }

    public void publicarEvento(Evento evento) {
        agregarEvento(evento);
        notificarSuscriptores(evento);
    }

    private void notificarSuscriptores(Evento evento) {
        System.out.println("[Blackboard] Notificando evento '" + evento.getTipo() + "' a " + suscriptores.size() + " suscriptores.");
        for (IFuenteConocimiento experto : suscriptores)
        {
            experto.procesarEvento(evento);
        }
    }

    public Evento obtenerUltimoEvento(String tipo) {
        for (int i = espacioConocimiento.size() - 1; i >= 0; i--)
        {
            Evento e = espacioConocimiento.get(i);
            if (e.getTipo().equals(tipo))
            {
                return e;
            }
        }
        return null;
    }
}
