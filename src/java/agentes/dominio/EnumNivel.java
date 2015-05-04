/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes.dominio;

/**
 *
 * @author PJGT
 */
public enum EnumNivel {

    Ninguno(0),
    Junior(1),
    Vendedor(2),
    Supervisor(3);
    private final int valorNivel;

    private EnumNivel(int valorNivel) {
        this.valorNivel = valorNivel;
    }

    public int getValorNivel() {
        return valorNivel;
    }
}