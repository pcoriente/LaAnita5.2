package entradas;

import entradas.dao.DAOFacturas;
import entradas.dominio.Factura;
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
 * @author jsolis
 */
@Named(value = "mbFacturas")
@SessionScoped
public class MbFacturas implements Serializable {

    private Factura factura;
    private ArrayList<Factura> facturas;
    private ArrayList<SelectItem> listaFacturas;
    private DAOFacturas dao;

    public MbFacturas() {
        this.factura = new Factura();
    }

    public void buscarFacturas(int idProveedor, String serie, String numero) {
    }
    
    public void copia(Factura factura) {
        this.factura=new Factura();
        this.factura.setFecha(factura.getFecha());
        this.factura.setIdFactura(factura.getIdFactura());
        this.factura.setIdProveedor(factura.getIdProveedor());
        this.factura.setNumero(factura.getNumero());
        this.factura.setSerie(factura.getSerie());
        this.factura.setCerradaOficina(factura.isCerradaOficina());
        this.factura.setCerradaAlmacen(factura.isCerradaAlmacen());
    }
    
    public boolean cerradaAlmacen() {
        boolean ok = false;
        boolean cerrada=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOFacturas();
            cerrada=this.dao.obtenerEstadoAlmacen(this.factura.getIdFactura());
            ok=true;
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
        return cerrada;
    }
    
    public boolean cerradaOficina() {
        boolean ok = false;
        boolean cerrada=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOFacturas();
            cerrada=this.dao.obtenerEstadoOficina(this.factura.getIdFactura());
            ok=true;
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
        return cerrada;
    }

    public boolean grabarFactura() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOFacturas();
            if (this.factura.getIdFactura() == 0) {
                this.factura.setIdFactura(this.dao.agregar(this.factura));
            } else {
                this.dao.modificar(this.factura);
            }
            this.obtenerListaFacturas(this.factura.getIdProveedor());
            ok=true;
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
        context.addCallbackParam("okFactura", ok);
        return ok;
    }

    public void obtenerListaFacturas(int idProveedor) {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        this.listaFacturas = new ArrayList<SelectItem>();
        try {
            Factura f0 = new Factura(idProveedor);
            f0.setIdFactura(0);
            f0.setSerie("");
            f0.setNumero("Seleccione");
            SelectItem cero = new SelectItem(f0, f0.toString());
            listaFacturas.add(cero);

            this.dao = new DAOFacturas();
            for (Factura a : this.dao.obtenerFacturas(idProveedor)) {
                listaFacturas.add(new SelectItem(a, a.toString()));
            }
            ok = true;
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
        context.addCallbackParam("okFactura", ok);
    }

    public Factura obtenerFactura(int idProveedor, String serie, String numero) {
        boolean ok = false;
        Factura f = null;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        this.facturas = new ArrayList<Factura>();
        try {
            this.dao = new DAOFacturas();
            f = this.dao.obtenerFactura(idProveedor, serie, numero);
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
        return f;
    }

    public void obtenerFacturas(int idProveedor) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        this.facturas = new ArrayList<Factura>();
        try {
            this.dao = new DAOFacturas();
            this.facturas = this.dao.obtenerFacturas(idProveedor);
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
    }

    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public ArrayList<Factura> getFacturas() {
        return facturas;
    }

    public void setFacturas(ArrayList<Factura> facturas) {
        this.facturas = facturas;
    }

    public ArrayList<SelectItem> getListaFacturas() {
        return listaFacturas;
    }

    public void setListaFacturas(ArrayList<SelectItem> listaFacturas) {
        this.listaFacturas = listaFacturas;
    }
}
