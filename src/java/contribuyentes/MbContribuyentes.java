package contribuyentes;

import Message.Mensajes;
import clientes.MbClientes;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import utilerias.Utilerias;

/**
 *
 * @author jsolis
 */
@Named(value = "mbContribuyentes")
@SessionScoped
public class MbContribuyentes implements Serializable {

    private Contribuyente contribuyente;
    private Contribuyente respaldo;
    private ArrayList<Contribuyente> contribuyentes;
    private ArrayList<SelectItem> listaContribuyentes;
    private DAOContribuyentes dao;

    private boolean personaFisica;

    public MbContribuyentes() {
        this.contribuyente = new Contribuyente();
        this.contribuyentes = new ArrayList<>();
    }

    public void cancelar() {
        boolean ok = true;
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okContribuyente", ok);
    }

    public void validaSalir() {
        boolean ok = false;
        if (this.contribuyente.getIdContribuyente() == 0) {
            ok = true;
        } else if (this.contribuyente.getDireccion().getIdDireccion() != 0) {
            ok = true;
        } else {
            Mensajes.mensajeError("Capture la direccion del contribuyente !!!");
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okContribuyente", ok);
    }

    private void actualizaContribuyente() {
        if(this.respaldo==null) {
            this.respaldo=new Contribuyente();
        }
        this.respaldo.setIdContribuyente(this.contribuyente.getIdContribuyente());
        this.respaldo.setContribuyente(this.contribuyente.getContribuyente());
        this.respaldo.setIdRfc(this.contribuyente.getIdRfc());
        this.respaldo.setRfc(this.contribuyente.getRfc());
        this.respaldo.setCurp(this.contribuyente.getCurp());
        this.respaldo.setDireccion(this.contribuyente.getDireccion());
    }

    public boolean grabar() {
//        FALTA AGREGAR LA ALTA DE CONTRIBUYENTE
        
        boolean ok = false;
        if (this.valida()) {
            try {
                this.dao = new DAOContribuyentes();
                if (this.contribuyente.getIdContribuyente() == 0) {
                    this.contribuyente.setIdContribuyente(this.dao.agregar(this.contribuyente));
                } else {
                    this.dao.modificar(this.contribuyente);
                }
                this.actualizaContribuyente();
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
//        context.addCallbackParam("okContribuyente", ok);
        return ok;
    }

    public void dameStatusRfc() {
        this.personaFisica = false;
        if (this.contribuyente.getRfc().length() == 13) {
            this.personaFisica = true;
        }
    }

    private void copiaContribuyente(Contribuyente contribuyente) {
        this.contribuyente = new Contribuyente();
        this.contribuyente.setIdContribuyente(contribuyente.getIdContribuyente());
        this.contribuyente.setContribuyente(contribuyente.getContribuyente());
        this.contribuyente.setIdRfc(contribuyente.getIdRfc());
        this.contribuyente.setRfc(contribuyente.getRfc());
        this.contribuyente.setCurp(contribuyente.getCurp());
        this.contribuyente.setDireccion(contribuyente.getDireccion());
        System.out.println("va el contribuyente "+this.contribuyente.getContribuyente());
    }

    public void mttoContribuyente(Contribuyente contribuyente) {
        this.copiaContribuyente(contribuyente);
        this.respaldo = contribuyente;
        this.dameStatusRfc();
    }
    
    public Contribuyente copia(Contribuyente contribuyente) {
        Contribuyente c = new Contribuyente();
        c.setIdContribuyente(contribuyente.getIdContribuyente());
        c.setContribuyente(contribuyente.getContribuyente());
        c.setIdRfc(contribuyente.getIdRfc());
        c.setRfc(contribuyente.getRfc());
        c.setCurp(contribuyente.getCurp());
        c.setDireccion(contribuyente.getDireccion());
        return c;
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

    public ArrayList<Contribuyente> obtenerContribuyentesCliente() {
        ArrayList<Contribuyente> lstContribuyentes = new ArrayList<>();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOContribuyentes();
            lstContribuyentes = this.dao.obtenerContribuyentesCliente();
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
        return lstContribuyentes;
    }

    public Contribuyente obtenerContribuyenteRfc(String rfc) {
        boolean ok = false;
        Contribuyente c = null;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerContribuyenteRfc");
        try {
            this.dao = new DAOContribuyentes();
            c = this.dao.obtenerContribuyenteRfc(rfc);
            ok = true;
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return c;
    }

    public Contribuyente obtenerContribuyente(int idContribuyente) {
        Contribuyente c = null;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao = new DAOContribuyentes();
            c = this.dao.obtenerContribuyente(idContribuyente);
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
        return c;
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

    public void limpiarContribuyente() {
        contribuyente = new Contribuyente();
    }

//    public void obtenerContribuyentesRFC() {
//        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
//        try {
//            this.dao = new DAOContribuyentes();
//            int idRfc = this.dao.obtenerRfc(this.contribuyente.getRfc());
//            if (idRfc == 0) {
//                idRfc = this.dao.grabarRFC(this.contribuyente.getRfc());
//                this.contribuyente.setIdRfc(idRfc);
////                this.contribuyentes = new ArrayList<>();
//            } else {
////                this.contribuyentes = this.dao.obtenerContribuyentesRFC(this.contribuyente.getRfc());
//            }
//        } catch (SQLException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
//        } catch (NamingException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getMessage());
//        }
//    }

    public Contribuyente buscarContribuyente(String rfc) {
        String mensaje = "";
        try {
            Utilerias utilerias = new Utilerias();
            mensaje = utilerias.verificarRfc(rfc);
            if ("".equals(mensaje)) {
                DAOContribuyentes dao1 = new DAOContribuyentes();
                contribuyente = dao1.buscarContribuyente(rfc);
                Mensajes.mensajeSucces("RFC valido!");
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbContribuyentes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
            Logger.getLogger(MbContribuyentes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return contribuyente;
//        return mensaje;
    }

    public List<String> completarClientes(String rfc) {
        List<String> lst = new ArrayList<>();
        try {
            DAOContribuyentes dao1 = new DAOContribuyentes();
            for (Contribuyente con : dao1.dameRfcContribuyente(rfc)) {
                lst.add(con.getRfc());
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
            Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lst;
    }

//    public boolean verificar(String rfc) {
//        boolean ok = false;
//
//        return ok;
//    }
    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
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
                Logger.getLogger(MbContribuyentes.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Message.Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
                Logger.getLogger(MbContribuyentes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return listaContribuyentes;
    }

    public void setListaContribuyentes(ArrayList<SelectItem> listaContribuyentes) {
        this.listaContribuyentes = listaContribuyentes;
    }

    public boolean isPersonaFisica() {
        return personaFisica;
    }

    public void setPersonaFisica(boolean personaFisica) {
        this.personaFisica = personaFisica;
    }
}
