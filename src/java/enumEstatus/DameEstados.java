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
public class DameEstados {

    public static String dameEstado(int i) {
        String estado = "";
        for (EnumEstatus e : EnumEstatus.values()) {
            if (e.getValorEstado() == i) {
                estado = e.toString();
            }
        }
        return estado;
    }

}
