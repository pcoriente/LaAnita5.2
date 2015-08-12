/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package impuestos;

import impuestos.dominio.ImpuestosProducto;
import java.util.ArrayList;
import producto2.dominio.Producto;

/**
 *
 * @author Pjgt
 */
public class CalculoDeImpuestos {
    
    
     public double calculaImpuestos(Double costoUnitario, Double cantidadSolicitada,Producto p,ArrayList<ImpuestosProducto>lst) {
        double impuestos = 0.00;
        double precioConImpuestos = costoUnitario;
        for (ImpuestosProducto i : lst) {
            if (i.isAcumulable()) {
                if (i.isAplicable()) {
                    if (i.getModo() == 1) {
                        i.setImporte(precioConImpuestos * i.getValor() / 100.00);
                    } else {
                        i.setImporte(p.getPiezas() * i.getValor());
                    }
                    precioConImpuestos += i.getImporte();
                } else {
                    i.setImporte(0.00);
                }
                impuestos += i.getImporte();
            }
        }
        for (ImpuestosProducto i : lst) {
            if (!i.isAcumulable()) {
                if (i.isAplicable()) {
                    if (i.getModo() == 1) {
                        i.setImporte(precioConImpuestos * i.getValor() / 100.00);
                    } else {
                        i.setImporte(p.getPiezas() * i.getValor());
                    }
                } else {
                    i.setImporte(0.00);
                }
                impuestos += i.getImporte();
            }
        }
        return impuestos;
    }
    
}
