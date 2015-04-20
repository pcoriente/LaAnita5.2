package entradas;

import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
import entradas.dao.DAOComprobantes;
import entradas.dominio.Comprobante;
import entradas.to.TOComprobante;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import proveedores.MbMiniProveedor;

/**
 *
 * @author jesc
 */
@Named(value = "mbComprobantes")
@SessionScoped
public class MbComprobantes implements Serializable {
    private int tipoComprobante;
    private Comprobante comprobante;    // Se usa en traspasos: envios
    private TOComprobante toComprobante;
    private TOComprobante edicion;
    private ArrayList<Comprobante> comprobantes;
    private ArrayList<SelectItem> listaComprobantes;
    private DAOComprobantes dao;
    
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniProveedor}")
    private MbMiniProveedor mbProveedores;
    
    public MbComprobantes() throws NamingException {
        this.mbAlmacenes=new MbAlmacenesJS();
        this.mbProveedores=new MbMiniProveedor();
        this.inicializaLocales();
    }
    
    public void cancelar() {
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "cancelar");
        try {
            this.dao=new DAOComprobantes();
            if(this.dao.cancelar(this.toComprobante.getIdComprobante())) {
                ok=true;
            }
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okComprobante", ok);
    }
    
    public void inicializaConAlmacen(TOAlmacenJS toAlmacen) {
        this.mbAlmacenes.inicializaConAlmacen(toAlmacen);
        this.mbProveedores.inicializar();
        this.inicializaLocales();
    }
    
    public void inicializar() {
        this.mbAlmacenes.inicializar();
        this.mbProveedores.inicializar();
        this.inicializaLocales();
    }
    
    private void inicializaLocales() {
        this.tipoComprobante=1;
        this.edicion=new TOComprobante();
        this.toComprobante=new TOComprobante();
        this.toComprobante.setIdAlmacen(this.mbAlmacenes.getToAlmacen().getIdAlmacen());
        this.toComprobante.setIdProveedor(this.mbProveedores.getMiniProveedor().getIdProveedor());
        this.toComprobante.setTipoComprobante(this.tipoComprobante);
        this.comprobante=new Comprobante();
        this.setComprobante(null);
        this.setListaComprobantes(null);
    }
    
    private void nuevo() {
        this.edicion=new TOComprobante();
        this.edicion.setIdComprobante(0);
        this.edicion.setIdAlmacen(this.mbAlmacenes.getToAlmacen().getIdAlmacen());
        this.edicion.setIdProveedor(this.mbProveedores.getMiniProveedor().getIdProveedor());
        this.edicion.setTipoComprobante(this.tipoComprobante);
        this.edicion.setRemision("");
        this.edicion.setSerie("");
        this.edicion.setNumero("");
        this.edicion.setFecha(new Date());
        this.edicion.setStatusOficina((byte)0);
        this.edicion.setStatusAlmacen((byte)0);
        this.toComprobante=new TOComprobante();
        this.comprobante=new Comprobante();
        this.setComprobantes(null);
        this.setListaComprobantes(null);
    }
    
    public boolean aseguraComprobante(int idComprobante, boolean oficina) {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "aseguraComprobante");
        try {
            this.dao=new DAOComprobantes();
            if(this.dao.asegurarComprobante(this.toComprobante.getIdComprobante(), oficina)) {
                ok=true;
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
    
    public void mttoComprobante(boolean oficina) {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        if(this.validaComprobante()) {
//            this.setTipoComprobante(1);
            if(this.toComprobante.getIdComprobante()==0) {
                this.nuevo();
                ok=true;
            } else if(this.aseguraComprobante(this.toComprobante.getIdComprobante(), oficina)) {
                this.respalda();
                ok=true;
            }
        }
        context.addCallbackParam("okComprobante", ok);
    }
    
    public boolean validaComprobante() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "validaComprobante");
        if(this.mbAlmacenes.getToAlmacen().getIdAlmacen()==0) {
            fMsg.setDetail("Se requiere tener seleccionado un almacen");
        } else if(this.mbProveedores.getMiniProveedor().getIdProveedor()==0) {
            fMsg.setDetail("Se requiere tener seleccionado un proveedor");
        } else {
            ok=true;
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return ok;
    }
    
    public void cargaAlmacenes() {
        this.mbAlmacenes.cargaAlmacenes();
        this.cargaListaComprobantes();
    }
    
    private void respalda() {
        this.edicion.setIdComprobante(this.toComprobante.getIdComprobante());
        this.edicion.setIdAlmacen(this.toComprobante.getIdAlmacen());
        this.edicion.setIdProveedor(this.toComprobante.getIdProveedor());
        this.edicion.setTipoComprobante(this.toComprobante.getTipoComprobante());
        this.edicion.setRemision(this.toComprobante.getRemision());
        this.edicion.setSerie(this.toComprobante.getSerie());
        this.edicion.setNumero(this.toComprobante.getNumero());
        this.edicion.setFecha(this.toComprobante.getFecha());
        this.edicion.setStatusOficina(this.toComprobante.getStatusOficina());
        this.edicion.setStatusAlmacen(this.toComprobante.getStatusAlmacen());
    }
    
    private void restaura() {
        this.toComprobante.setIdComprobante(this.edicion.getIdComprobante());
        this.toComprobante.setIdAlmacen(this.edicion.getIdAlmacen());
        this.toComprobante.setIdProveedor(this.edicion.getIdProveedor());
        this.toComprobante.setTipoComprobante(this.edicion.getTipoComprobante());
        this.toComprobante.setRemision(this.edicion.getRemision());
        this.toComprobante.setSerie(this.edicion.getSerie());
        this.toComprobante.setNumero(this.edicion.getNumero());
        this.toComprobante.setFecha(this.edicion.getFecha());
        this.toComprobante.setStatusOficina(this.edicion.getStatusOficina());
        this.toComprobante.setStatusAlmacen(this.edicion.getStatusAlmacen());
    }
    
//    public void copiaX(Comprobante comprobante) {
//        this.comprobante=new Comprobante();
//        this.comprobante.setFecha(comprobante.getFecha());
//        this.comprobante.getAlmacen().setIdAlmacen(comprobante.getAlmacen().getIdAlmacen());
//        this.comprobante.setIdComprobante(comprobante.getIdComprobante());
//        this.comprobante.getProveedor().setIdProveedor(comprobante.getProveedor().getIdProveedor());
//        this.comprobante.setTipoComprobante(comprobante.getTipoComprobante());
//        this.comprobante.setNumero(comprobante.getNumero());
//        this.comprobante.setSerie(comprobante.getSerie());
//        this.comprobante.setCerradaOficina(comprobante.isCerradaOficina());
//        this.comprobante.setCerradaAlmacen(comprobante.isCerradaAlmacen());
//    }
//    
//    public boolean cerradaAlmacenX() {
//        boolean ok = false;
//        boolean cerrada=false;
//        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cerradaAlmacen");
//        try {
//            this.dao=new DAOComprobantes();
//            cerrada=this.dao.obtenerEstadoAlmacen(this.comprobante.getIdComprobante());
//            ok=true;
//        } catch (NamingException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getMessage());
//        } catch (SQLException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
//        }
//        if (!ok) {
//            FacesContext.getCurrentInstance().addMessage(null, fMsg);
//        }
//        return cerrada;
//    }
//    
//    public boolean cerradaOficina() {
//        boolean ok = false;
//        boolean cerrada=false;
//        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cerradaOficina");
//        try {
//            this.dao=new DAOComprobantes();
//            cerrada=this.dao.obtenerEstadoOficina(this.comprobante.getIdComprobante());
//            ok=true;
//        } catch (NamingException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getMessage());
//        } catch (SQLException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
//        }
//        if (!ok) {
//            FacesContext.getCurrentInstance().addMessage(null, fMsg);
//        }
//        return cerrada;
//    }
    
    private Comprobante convertir(TOComprobante to) {
        Comprobante c=new Comprobante();
        c.setIdComprobante(to.getIdComprobante());
        c.setAlmacen(this.mbAlmacenes.obtenerAlmacen(to.getIdAlmacen()));
        c.setProveedor(this.mbProveedores.obtenerProveedor(to.getIdProveedor()));
        c.setTipoComprobante(to.getTipoComprobante());
        c.setRemision(to.getRemision());
        c.setSerie(to.getSerie());
        c.setNumero(to.getNumero());
        c.setFecha(to.getFecha());
        c.setStatusOficina(to.getStatusOficina());
        c.setStatusAlmacen(to.getStatusAlmacen());
        return c;
    }
    
    public TOComprobante convertirTO(Comprobante c) {
        TOComprobante to=new TOComprobante();
        to.setIdComprobante(c.getIdComprobante());
        to.setIdAlmacen(c.getAlmacen().getIdAlmacen());
        to.setIdProveedor(c.getProveedor().getIdProveedor());
        to.setRemision(c.getRemision());
        to.setSerie(c.getSerie());
        to.setNumero(c.getNumero());
        to.setFecha(c.getFecha());
        to.setStatusOficina(c.getStatusOficina());
        to.setStatusAlmacen(c.getStatusAlmacen());
        return to;
    }
    
    public void convertirComprobante() {
        this.comprobante=this.convertir(this.toComprobante);
    }
    
    public boolean grabar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "grabar");
        try {
            if(this.edicion.getNumero().equals("")) {
                fMsg.setDetail("Se requiere el numero del comprobante");
            } else {
                this.dao=new DAOComprobantes();
                if (this.edicion.getIdComprobante() == 0) {
                    this.edicion.setIdComprobante(this.dao.agregar(this.edicion));
                } else {
                    this.dao.modificar(this.edicion);
                }
                this.obtenerListaComprobantes();
                this.restaura();
                ok=true;
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
    
    public void obtenerSolicitudes() throws NamingException, SQLException {
        this.comprobantes = new ArrayList<Comprobante>();
        this.dao = new DAOComprobantes();
        for (TOComprobante to : this.dao.obtenerSolicitudes(this.mbAlmacenes.getToAlmacen().getIdAlmacen())) {
            this.comprobantes.add(this.convertir(to));
        }
    }
    
    private void obtenerListaComprobantes() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerListaComprobantes");
        try {
            this.listaComprobantes = new ArrayList<SelectItem>();
            TOComprobante c0 = new TOComprobante(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.mbProveedores.getMiniProveedor().getIdProveedor(), this.tipoComprobante);
            c0.setNumero("Seleccione");
            this.listaComprobantes.add(new SelectItem(c0, c0.toString()));

            this.dao=new DAOComprobantes();
            for (TOComprobante c : this.dao.obtenerComprobantes(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.mbProveedores.getMiniProveedor().getIdProveedor(), this.tipoComprobante)) {
                listaComprobantes.add(new SelectItem(c, c.toString()));
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
        context.addCallbackParam("okComprobante", ok);
    }
    
    public void cargaListaComprobantes() {
        if(this.validaComprobante()) {
//            this.setTipoComprobante(1);
            this.obtenerListaComprobantes();
            this.toComprobante=(TOComprobante)this.listaComprobantes.get(0).getValue();
        } else {
            if(this.listaComprobantes==null) {
                this.listaComprobantes=new ArrayList<SelectItem>();
            } else {
                this.listaComprobantes.clear();
            }
            this.toComprobante = new TOComprobante(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.mbProveedores.getMiniProveedor().getIdProveedor(), this.tipoComprobante);
            this.toComprobante.setNumero("Seleccione");
            this.listaComprobantes.add(new SelectItem(this.toComprobante, this.toComprobante.toString()));
        }
    }
    
    public Comprobante obtenerComprobante(int idComprobante) {
        Comprobante c=null;
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerComprobante");
        try {
            this.dao=new DAOComprobantes();
            TOComprobante to=this.dao.obtenerComprobante(idComprobante);
            if(to==null) {
                fMsg.setDetail("No se encontro el Comprobante solicitado");
            } else {
                c=convertir(to);
                ok=true;
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
        return c;
    }
    
    public ArrayList<SelectItem> getListaComprobantes() {
        return listaComprobantes;
    }

    public void setListaComprobantes(ArrayList<SelectItem> listaComprobantes) {
        this.listaComprobantes = listaComprobantes;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }

    public ArrayList<Comprobante> getComprobantes() {
        return comprobantes;
    }

    public void setComprobantes(ArrayList<Comprobante> comprobantes) {
        this.comprobantes = comprobantes;
    }

    public MbAlmacenesJS getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbAlmacenesJS mbAlmacenes) {
        this.mbAlmacenes = mbAlmacenes;
    }

    public MbMiniProveedor getMbProveedores() {
        return mbProveedores;
    }

    public void setMbProveedores(MbMiniProveedor mbProveedores) {
        this.mbProveedores = mbProveedores;
    }

    public int getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(int tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public TOComprobante getToComprobante() {
        return toComprobante;
    }

    public void setToComprobante(TOComprobante toComprobante) {
        this.toComprobante = toComprobante;
    }

    public TOComprobante getEdicion() {
        return edicion;
    }

    public void setEdicion(TOComprobante edicion) {
        this.edicion = edicion;
    }
}
