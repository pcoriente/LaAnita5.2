package tiendas;

import Message.Mensajes;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import tiendas.dao.DAOTiendas;
import tiendas.to.TOTienda;

/**
 *
 * @author jesc
 */
@Named(value = "mbMiniTiendas")
@SessionScoped
public class MbMiniTiendas implements Serializable {
    private TOTienda tienda;
    private ArrayList<SelectItem> listaTiendas;
    private ArrayList<TOTienda> tiendas;
    private DAOTiendas dao;
    
    public MbMiniTiendas() {
        this.inicializa();
    }
    
    private void inicializa() {
        this.tiendas=new ArrayList<TOTienda>();
        this.tienda=new TOTienda();
    }
    
    public void inicializar() {
        this.inicializa();
    }
    
    public void nuevaTienda() {
        this.tienda=new TOTienda();
    }
    
    public TOTienda obtenerTienda(int idTienda) {
        TOTienda to=null;
        try {
            this.dao=new DAOTiendas();
            to=this.dao.obtenerTienda(idTienda);
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        }
        return to;
    }
    
    public void obtenerTiendasFormato(int idFormato) {
        this.tiendas=new ArrayList<TOTienda>();
        try {
            this.dao=new DAOTiendas();
            for(TOTienda to:this.dao.obtenerTiendasFormato(idFormato)) {
                this.tiendas.add(to);
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        }
    }
    
    public void cargaTiendasFormato(int idFormato) {
        this.listaTiendas=new ArrayList<SelectItem>();
        this.listaTiendas.add(new SelectItem(new TOTienda(), "Seleccione una tienda"));
        try {
            this.dao=new DAOTiendas();
            for(TOTienda to: this.dao.obtenerTiendasFormato(idFormato)) {
                this.listaTiendas.add(new SelectItem(to, to.toString()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        }
    }

    public TOTienda getTienda() {
        return tienda;
    }

    public void setTienda(TOTienda tienda) {
        this.tienda = tienda;
    }

    public ArrayList<SelectItem> getListaTiendas() {
        return listaTiendas;
    }

    public void setListaTiendas(ArrayList<SelectItem> listaTiendas) {
        this.listaTiendas = listaTiendas;
    }

    public ArrayList<TOTienda> getTiendas() {
        return tiendas;
    }

    public void setTiendas(ArrayList<TOTienda> tiendas) {
        this.tiendas = tiendas;
    }
}
