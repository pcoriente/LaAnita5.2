package almacenes;

import Message.Mensajes;
import almacenes.dao.DAOAlmacenesJS;
//import almacenes.dominio.AlmacenJS;
import almacenes.to.TOAlmacenJS;
//import direccion.dominio.Direccion;
import empresas.MbMiniEmpresas;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;

/**
 *
 * @author jesc
 */
@Named(value = "mbAlmacenesJS")
@SessionScoped
public class MbAlmacenesJS implements Serializable {
    private TOAlmacenJS toAlmacen;
//    private ArrayList<AlmacenJS> almacenes;
    private ArrayList<SelectItem> listaAlmacenes;
    private DAOAlmacenesJS dao;
    
//    @ManagedProperty(value = "#{mbMiniCedis}")
//    private MbMiniCedis mbCedis;
    @ManagedProperty(value = "#{mbMiniEmpresas}")
    private MbMiniEmpresas mbEmpresas;
    
    public MbAlmacenesJS() throws NamingException {
        this.mbEmpresas=new MbMiniEmpresas();
//        this.mbCedis=new MbMiniCedis();
        this.inicializaLocales();
    }
    
    public void inicializar() {
        this.mbEmpresas.inicializar();
//        this.mbCedis.inicializar();
        this.inicializaLocales();
    }
    
    private void inicializaLocales() {
        this.toAlmacen=new TOAlmacenJS();
//        this.setAlmacenes(null);
        this.setListaAlmacenes(null);
    }
    
    public void inicializaConAlmacen(TOAlmacenJS toAlmacen) {
//        this.mbCedis.cargaMiniCedisTodos();
//        this.mbCedis.setCedis(this.mbCedis.obtenerCedis(toAlmacen.getIdCedis()));
//        this.mbCedis.obtenerDefaultCedis();
        this.cargaAlmacenesEmpresa(toAlmacen.getIdEmpresa(), 0);
        this.setToAlmacen(toAlmacen);
    }
    
    public void inicializaAlmacen() {
//        this.mbCedis.cargaMiniCedisZona();
//        this.mbCedis.obtenerDefaultCedis();
        this.cargaAlmacenes();
        this.toAlmacen=(TOAlmacenJS)this.listaAlmacenes.get(0).getValue();
    }
    
    public void cargaAlmacenesEmpresa(int idEmpresa, int noSelect) {
        try {
            if(idEmpresa!=0) {
                this.dao=new DAOAlmacenesJS();
                this.cargaListaAlmacenes(this.dao.obtenerAlmacenesEmpresa(idEmpresa), noSelect);
            } else {
                Mensajes.mensajeAlert("Debe seleccionar un cedis y un almacen");
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }
    
    public void cargaAlmacenes() {
        try {
            this.dao=new DAOAlmacenesJS();
            this.cargaListaAlmacenes(this.dao.obtenerAlmacenes(), 0);
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }
    
    private void cargaListaAlmacenes(ArrayList<TOAlmacenJS> lstAlmacenes, int noSelect) {
        boolean disabled;
        this.listaAlmacenes=new ArrayList<>();
        this.toAlmacen=new TOAlmacenJS();
        this.toAlmacen.setIdAlmacen(0);
        this.toAlmacen.setAlmacen("Seleccione un almacen");
        this.listaAlmacenes.add(new SelectItem(this.toAlmacen, this.toAlmacen.toString()));
        for(TOAlmacenJS a: lstAlmacenes) {
            disabled=false;
            if(a.getIdAlmacen()==noSelect) {
                disabled=true;
            }
            listaAlmacenes.add(new SelectItem(a, a.toString(),"",disabled));
        }
    }
    
//    private AlmacenJS convertir(TOAlmacenJS to) {
//        AlmacenJS a=new AlmacenJS();
//        a.setIdAlmacen(to.getIdAlmacen());
//        a.setAlmacen(to.getAlmacen());
//        a.setEmpresa(this.mbEmpresas.obtenerEmpresa(to.getIdEmpresa()));
////        a.setCedis(this.mbCedis.obtenerCedis(to.getIdCedis()));
//        a.setIdCedis(to.getIdCedis());
//        a.setCedis(to.getCedis());
//        a.setDireccion(new Direccion());
//        a.getDireccion().setIdDireccion(to.getIdDireccion());
//        return a;
//    }
    
    public TOAlmacenJS obtenerAlmacen(int idAlmacen) {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerAlmacen");
        TOAlmacenJS a=null;
        try {
            this.dao=new DAOAlmacenesJS();
//            a=convertir(this.dao.obtenerAlmacen(idAlmacen));
            a=this.dao.obtenerAlmacen(idAlmacen);
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
        return a;
    }
    
    public TOAlmacenJS obtenerTOAlmacen(int idAlmacen) {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerAlmacen");
        TOAlmacenJS a=null;
        try {
            this.dao=new DAOAlmacenesJS();
            a=this.dao.obtenerAlmacen(idAlmacen);
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
        return a;
    }

    public ArrayList<SelectItem> getListaAlmacenes() {
        if(this.listaAlmacenes==null) {
            this.cargaAlmacenes();
        }
        return listaAlmacenes;
    }

    public void setListaAlmacenes(ArrayList<SelectItem> listaAlmacenes) {
        this.listaAlmacenes = listaAlmacenes;
    }

//    public MbMiniCedis getMbCedis() {
//        return mbCedis;
//    }
//
//    public void setMbCedis(MbMiniCedis mbCedis) {
//        this.mbCedis = mbCedis;
//    }

    public MbMiniEmpresas getMbEmpresas() {
        return mbEmpresas;
    }

    public void setMbEmpresas(MbMiniEmpresas mbEmpresas) {
        this.mbEmpresas = mbEmpresas;
    }

//    public ArrayList<AlmacenJS> getAlmacenes() {
//        return almacenes;
//    }
//
//    public void setAlmacenes(ArrayList<AlmacenJS> almacenes) {
//        this.almacenes = almacenes;
//    }

    public TOAlmacenJS getToAlmacen() {
        return toAlmacen;
    }

    public void setToAlmacen(TOAlmacenJS toAlmacen) {
        this.toAlmacen = toAlmacen;
    }
}
