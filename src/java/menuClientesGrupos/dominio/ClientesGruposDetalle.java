/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package menuClientesGrupos.dominio;

import contactos.dominio.Contacto;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.model.SelectItem;

/**
 *
 * @author PJGT
 */
public class ClientesGruposDetalle implements Serializable{
    private Contacto contacto = new Contacto();
    private ArrayList<SelectItem> listaContactos= new ArrayList<SelectItem>();

 
    public ArrayList<SelectItem> getListaContactos() {
        return listaContactos;
    }

    public void setListaContactos(ArrayList<SelectItem> listaContactos) {
        this.listaContactos = listaContactos;
    }

    public Contacto getContacto() {
        return contacto;
    }

    public void setContacto(Contacto contacto) {
        this.contacto = contacto;
    }
    
    
    
}
