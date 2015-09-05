/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enumEstatus;

/**
 *
 * @author Pjgt
 */
public enum EnumEstatus {

    Ninguno(0),
    Solicitado(1),
    Rechazado(2),
    Aprobado(3),
    Proceso(4),
    Ordenado(5),
    Cancelado(6),
    Cerrada(7),
    Devolucion(8);
    private final int valorEstado;

    private EnumEstatus(int valor) {
        this.valorEstado = valor;
    }

    public int getValorEstado() {
        return valorEstado;
    }

}
