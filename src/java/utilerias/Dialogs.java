/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilerias;

import org.primefaces.context.RequestContext;

/**
 *
 * @author Torres
 */
public class Dialogs {

    public static void abrirDialogo(String nombreDelDialog) {
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('" + nombreDelDialog + "').show();");
    }

    public static void ocultarDialogo(String nombreDelDialog) {
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('" + nombreDelDialog + "').hide();");
    }

}
