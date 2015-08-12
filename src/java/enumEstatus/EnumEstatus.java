/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enumEstatus;

import java.util.ArrayList;

/**
 *
 * @author Pjgt
 */
public  enum EnumEstatus {

    Ninguno(0),
    Rechazado(1),
    Aprobado(2),
    Ordenado(3),
    Cancelado(4),
    Terminado(5);
    private final int valorEstado;

    private EnumEstatus(int valor) {
        this.valorEstado = valor;
    }

    public int getValorEstado() {
        return valorEstado;
    }

   

}
