/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package menuClientesGrupos.dominio;

import java.io.Serializable;

/**
 *
 * @author Usuario
 */
public class ClientesGruposIconos implements Serializable{
    
    private String lblGuardarContactos ="Guardar Contacto";
    private String btnGuardarContactos = "ui-icon-disk";
    public String getLblGuardarContactos() {
        return lblGuardarContactos;
    }

    public void setLblGuardarContactos(String lblGuardarContactos) {
        this.lblGuardarContactos = lblGuardarContactos;
    }

    public String getBtnGuardarContactos() {
        return btnGuardarContactos;
    }

    public void setBtnGuardarContactos(String btnGuardarContactos) {
        this.btnGuardarContactos = btnGuardarContactos;
    }
    
    
    
    
    
}
