package entradas;

import Message.Mensajes;
import almacenes.to.TOAlmacenJS;
import entradas.dao.DAOComprobantes;
import entradas.to.TOComprobante;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;

/**
 *
 * @author jesc
 */
@Named(value = "mbComprobantes")
@SessionScoped
public class MbComprobantes implements Serializable {
    private String tipo;
    private int idProveedor;
    private TOComprobante comprobante;    // Se usa en traspasos: envios
    private TOComprobante edicion;
    private ArrayList<SelectItem> listaComprobantes;
    private DAOComprobantes dao;

    public MbComprobantes() throws NamingException {
        this.inicializaLocales();
    }

    public void inicializaConAlmacen(TOAlmacenJS toAlmacen) {
        this.inicializaLocales();
    }

    public void inicializar() {
        this.inicializaLocales();
    }

    private void inicializaLocales() {
//        this.comprobante = new TOComprobante();
        this.edicion=new TOComprobante();
    }

    public void cancelar() {
        boolean ok = false;
        try {
            this.dao = new DAOComprobantes();
            if(this.edicion.getIdComprobante()!=0) {
                this.dao.liberaComprobante(this.edicion.getIdComprobante());
            }
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okComprobante", ok);
    }
    
    public void eliminar() {
        boolean ok = false;
        try {
            this.dao = new DAOComprobantes();
            this.dao.eliminar(this.edicion.getIdComprobante());
//            this.comprobante=new TOComprobante(this.idProveedor);
            this.comprobante=null;
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okComprobante", ok);
    }
    
    private void restaura() {
        this.comprobante.setIdComprobante(this.edicion.getIdComprobante());
        this.comprobante.setIdReferencia(this.edicion.getIdReferencia());
        this.comprobante.setTipo(this.edicion.getTipo());
        this.comprobante.setSerie(this.edicion.getSerie());
        this.comprobante.setNumero(this.edicion.getNumero());
        this.comprobante.setFecha(this.edicion.getFecha());
        this.comprobante.setIdMovto(this.edicion.getIdMovto());
    }

    public boolean grabar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "grabar");
        try {
            if (this.edicion.getNumero().equals("")) {
                fMsg.setDetail("Se requiere el numero del comprobante");
            } else {
                this.dao = new DAOComprobantes();
                this.edicion.setTipo(Integer.parseInt(this.tipo));
                if (this.edicion.getIdComprobante() == 0) {
                    this.edicion.setIdComprobante(this.dao.agregar(this.edicion));
                } else {
                    this.dao.modificar(this.edicion);
                }
                this.restaura();
                ok = true;
            }
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okComprobante", ok);
        return ok;
    }
    
    private void respalda() {
        this.edicion.setIdComprobante(this.comprobante.getIdComprobante());
        this.edicion.setIdReferencia(this.comprobante.getIdReferencia());
        this.edicion.setTipo(this.comprobante.getTipo());
        this.edicion.setSerie(this.comprobante.getSerie());
        this.edicion.setNumero(this.comprobante.getNumero());
        this.edicion.setFecha(this.comprobante.getFecha());
        this.edicion.setIdMovto(this.comprobante.getIdMovto());
    }
    
    public boolean aseguraComprobante(int idComprobante) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "aseguraComprobante");
        try {
            this.dao = new DAOComprobantes();
            if (this.dao.asegurarComprobante(this.comprobante.getIdComprobante())) {
                ok = true;
            } else {
                fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
                fMsg.setDetail("El comprobante esta en uso o ya ha sido cerrado");
            }
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return ok;
    }
    
    public void mttoComprobante() {
        boolean ok = true;
        if(this.comprobante==null) {
            this.comprobante=new TOComprobante(this.idProveedor);
        }
        if (this.comprobante.getIdComprobante()==0) {
            this.tipo="2";
            this.edicion = new TOComprobante(this.idProveedor);
        } else if (this.aseguraComprobante(this.comprobante.getIdComprobante())) {
            this.respalda();
            this.tipo=Integer.toString(this.edicion.getTipo());
        } else {
            ok=false;
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okComprobante", ok);
    }
    
    public ArrayList<TOComprobante> completeComprobantes(String query) {
        ArrayList<TOComprobante> lstComprobantes = null;
        try {
            this.dao = new DAOComprobantes();
            lstComprobantes=this.dao.completeComprobantes(this.idProveedor, query);
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        return lstComprobantes;
    }

    public void obtenerSolicitudes() throws NamingException, SQLException {
    }

    public TOComprobante obtenerComprobante(int idComprobante) {
        TOComprobante c = null;
        try {
            this.dao = new DAOComprobantes();
            TOComprobante to = this.dao.obtenerComprobante(idComprobante);
            if (to == null) {
                Mensajes.mensajeAlert("No se encontro el Comprobante solicitado");
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        return c;
    }

    public ArrayList<SelectItem> getListaComprobantes() {
        return listaComprobantes;
    }

    public void setListaComprobantes(ArrayList<SelectItem> listaComprobantes) {
        this.listaComprobantes = listaComprobantes;
    }

    public TOComprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(TOComprobante comprobante) {
        this.comprobante = comprobante;
    }

    public TOComprobante getEdicion() {
        return edicion;
    }

    public void setEdicion(TOComprobante edicion) {
        this.edicion = edicion;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
