package contribuyentes;

import Message.Mensajes;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import utilerias.Utilerias;

/**
 *
 * @author jesc
 */
@Named(value = "mbContribuyentesJS")
@SessionScoped
public class MbContribuyentesJS implements Serializable {
    private boolean personaFisica;
    private Contribuyente contribuyente;
    private Contribuyente respaldo;
    private ArrayList<Contribuyente> contribuyentes;
    private ArrayList<SelectItem> listaContribuyentes;
    private DAOContribuyentes dao;
    
    public MbContribuyentesJS() {
        this.contribuyente = new Contribuyente();
        this.contribuyentes = new ArrayList<>();
    }
    
    public boolean grabar() {
        boolean ok = false;
        if (this.valida()) {
            try {
                this.dao = new DAOContribuyentes();
                if (this.contribuyente.getIdContribuyente() == 0) {
                    this.contribuyente.setIdContribuyente(this.dao.agregar(this.contribuyente));
                } else {
                    this.dao.modificar(this.contribuyente);
                }
                Mensajes.mensajeSucces("El contribuyente se grabo correctamente !!!");
                ok = true;
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okContribuyente", ok);
        return ok;
    }
    
    public boolean valida(Contribuyente c) {
        return validar(c);
    }
    
    public boolean valida() {
        return validar(this.contribuyente);
    }

    private boolean validar(Contribuyente c) {
        boolean ok = false;
        if (c.getContribuyente().equals("")) {
            Mensajes.mensajeAlert("Se requiere un contribuyente");
        } else if (c.getRfc().isEmpty()) {
            Mensajes.mensajeAlert("Se requiere el RFC !!");
        } else {
            c.setRfc(c.getRfc().trim().toUpperCase());
            c.setCurp(c.getCurp().trim().toUpperCase());
            Utilerias utilerias = new Utilerias();
            String mensaje = utilerias.verificarRfc(c.getRfc());
            if (!mensaje.equals("")) {
                Mensajes.mensajeAlert(mensaje);
            } else if (c.getRfc().length() == 12 || c.getCurp().equals("") || utilerias.validarCurp(c.getCurp())) {
                ok = true;
            } else {
                Mensajes.mensajeAlert("Error! Curp no valido");
            }
        }
        return ok;
    }
    
    public void copiaContribuyente(Contribuyente contribuyente) {
        this.contribuyente = new Contribuyente();
        this.contribuyente.setIdContribuyente(contribuyente.getIdContribuyente());
        this.contribuyente.setContribuyente(contribuyente.getContribuyente());
        this.contribuyente.setIdRfc(contribuyente.getIdRfc());
        this.contribuyente.setRfc(contribuyente.getRfc());
        this.contribuyente.setCurp(contribuyente.getCurp());
        this.contribuyente.setDireccion(contribuyente.getDireccion());
    }
    
    public void obtenerIdRfc() {
        try {
            if(this.contribuyente.getIdRfc()==0) {
                this.dao = new DAOContribuyentes();
                this.contribuyente.setIdRfc(this.dao.obtenerRfc(this.contribuyente.getRfc()));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
    public void nuevoContribuyente() {
        this.contribuyente=new Contribuyente();
    }
    
    public ArrayList<Contribuyente> obtenerContribuyentesRfc(String rfc) {
        ArrayList<Contribuyente> lst = new ArrayList<>();
        try {
            this.dao = new DAOContribuyentes();
            lst=this.dao.obtenerContribuyentesRFC(rfc);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        return lst;
    }

    public boolean isPersonaFisica() {
        return personaFisica;
    }

    public void setPersonaFisica(boolean personaFisica) {
        this.personaFisica = personaFisica;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    public Contribuyente getRespaldo() {
        return respaldo;
    }

    public void setRespaldo(Contribuyente respaldo) {
        this.respaldo = respaldo;
    }

    public ArrayList<Contribuyente> getContribuyentes() {
        return contribuyentes;
    }

    public void setContribuyentes(ArrayList<Contribuyente> contribuyentes) {
        this.contribuyentes = contribuyentes;
    }

    public ArrayList<SelectItem> getListaContribuyentes() {
        if (listaContribuyentes == null) {
            try {
                listaContribuyentes = new ArrayList<>();
                DAOContribuyentes dao1 = new DAOContribuyentes();
                Contribuyente c = new Contribuyente();
                c.setIdContribuyente(0);
                c.setContribuyente("Seleccione Contribuyente");
                listaContribuyentes.add(new SelectItem(c, c.getContribuyente()));
                for (Contribuyente contr : dao1.dameContribuyentes()) {
                    listaContribuyentes.add(new SelectItem(contr, contr.getContribuyente()));
                }
            } catch (NamingException ex) {
                Message.Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Message.Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
            }
        }
        return listaContribuyentes;
    }

    public void setListaContribuyentes(ArrayList<SelectItem> listaContribuyentes) {
        this.listaContribuyentes = listaContribuyentes;
    }
}
