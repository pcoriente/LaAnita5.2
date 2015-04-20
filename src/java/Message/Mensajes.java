/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.primefaces.context.RequestContext;

/**
 *
 * @author Usuario
 */
public class Mensajes {

    public static void mensajeSucces(String valor) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito:", valor);
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
        context.addCallbackParam("ok", true);
    }

    public static void mensajeError(String valor) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", valor);
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
        context.addCallbackParam("ok", false);
    }

    public static void mensajeAlert(String valor) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", valor);
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
        context.addCallbackParam("ok", false);
    }
}
