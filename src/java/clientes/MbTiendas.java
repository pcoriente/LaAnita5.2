package clientes;

import clientes.dao.DAOTiendas;
import clientes.dominio.MiniTienda;
import clientes.dominio.Tienda;
import clientes.to.TOTienda;
import direccion.MbDireccion;
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
@Named(value = "mbTiendasOld")
@SessionScoped
public class MbTiendas implements Serializable {
    @ManagedProperty(value = "#{mbDireccion}")
    private MbDireccion mbDireccion = new MbDireccion();
    
    private ArrayList<SelectItem> listaTiendas;
    private Tienda tienda;
    private MiniTienda miniTienda;
    private DAOTiendas dao;
    
    public MbTiendas() {
        this.inicializa();
    }
    
    public void cargaTiendas(int idCliente) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "cargaTiendas");
        this.listaTiendas=new ArrayList<SelectItem>();
        try {
            MiniTienda t0=new MiniTienda();
            t0.setTienda("Seleccione");
            this.listaTiendas.add(new SelectItem(t0, t0.toString()));
            
            this.dao=new DAOTiendas();
            for(MiniTienda t: this.dao.obtenerMiniTiendas(idCliente)) {
                //t0=convertirMini(t);
                this.listaTiendas.add(new SelectItem(t, t.toString()));
            }
            ok=true;
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    private MiniTienda convertirMini(TOTienda to) {
        MiniTienda mini=new MiniTienda();
        mini.setIdTienda(to.getIdTienda());
        mini.setCodigoTienda(to.getCodigoTienda());
        mini.setTienda(to.getTienda());
        return mini;
    }
    /*
    private Tienda convertir(TOTienda to) {
        Tienda tda=new Tienda();
        tda.setIdTienda(to.getIdTienda());
        tda.setCodigoTienda(to.getCodigoTienda());
        tda.setTienda(to.getTienda());
        tda.setDireccion(this.mbDireccion.obtener(to.getIdDireccion()));
        return tda;
    }
    */
    public void inicializar() {
        this.inicializa();
    }
    
    private void inicializa() {
        this.mbDireccion=new MbDireccion();
        this.inicializaLocales();
    }
    
    private void inicializaLocales() {
        this.cargaTiendas(0);
        this.tienda=new Tienda();
        this.miniTienda=new MiniTienda();
    }

    public MbDireccion getMbDireccion() {
        return mbDireccion;
    }

    public void setMbDireccion(MbDireccion mbDireccion) {
        this.mbDireccion = mbDireccion;
    }

    public ArrayList<SelectItem> getListaTiendas() {
        return listaTiendas;
    }

    public void setListaTiendas(ArrayList<SelectItem> listaTiendas) {
        this.listaTiendas = listaTiendas;
    }

    public Tienda getTienda() {
        return tienda;
    }

    public void setTienda(Tienda tienda) {
        this.tienda = tienda;
    }

    public MiniTienda getMiniTienda() {
        return miniTienda;
    }

    public void setMiniTienda(MiniTienda miniTienda) {
        this.miniTienda = miniTienda;
    }
}
