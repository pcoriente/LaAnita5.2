package proveedores;

import monedas.Moneda;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;
import proveedores.dao.DAOMiniProveedores;
import proveedores.dominio.MiniProveedor;

@Named(value = "mbMiniProveedor")
@SessionScoped
public class MbMiniProveedor implements Serializable {

    private ArrayList<SelectItem> listaMiniProveedores = new ArrayList<SelectItem>();
    private MiniProveedor miniProveedor = new MiniProveedor();
//    private ArrayList<SelectItem> listaMonedas = new ArrayList<SelectItem>();
//    private Moneda moneda = new Moneda();
    private DAOMiniProveedores dao;

    public MbMiniProveedor() {
        this.inicializaLocales();
    }
    
    public void inicializar() {
        this.inicializaLocales();
    }
    
    private void inicializaLocales() {
        this.miniProveedor=new MiniProveedor();
        this.setListaMiniProveedores(null);
    }
    
    public MiniProveedor obtenerProveedor(int idProveedor) {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "obtenerProveedor");
        MiniProveedor p=null;
        try {
            this.dao=new DAOMiniProveedores();
            p=this.dao.obtenerProveedor(idProveedor);
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
        return p;
    }

    //////////////////////////////M E T O D O S
    public void inicializaProveedor() {
        this.miniProveedor=(MiniProveedor)this.listaMiniProveedores.get(0).getValue();
    }
    
    public void cargaListaProveedores() {
        try {
            this.listaMiniProveedores=new ArrayList<SelectItem>();
            
            MiniProveedor p0 = new MiniProveedor();
            p0.setIdProveedor(0);
            p0.setProveedor("Proveedor....");
            listaMiniProveedores.add(new SelectItem(p0, p0.toString()));
            
            this.dao = new DAOMiniProveedores();
            ArrayList<MiniProveedor> proveedores = this.dao.obtenerProveedores();
            for (MiniProveedor e : proveedores) {
                listaMiniProveedores.add(new SelectItem(e, e.toString()));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbMiniProveedor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(MbMiniProveedor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<SelectItem> obtenerListaMiniProveedor() throws NamingException {
        try {
            this.listaMiniProveedores=new ArrayList<SelectItem>();
            
            MiniProveedor p0 = new MiniProveedor();
            p0.setIdProveedor(0);
            p0.setProveedor("Proveedor....");
            listaMiniProveedores.add(new SelectItem(p0, p0.toString()));
            
            this.dao = new DAOMiniProveedores();
            ArrayList<MiniProveedor> proveedores = this.dao.obtenerProveedores();
            for (MiniProveedor e : proveedores) {
                listaMiniProveedores.add(new SelectItem(e, e.toString()));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbMiniProveedor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaMiniProveedores;
    }

    ///////////////////////////////// GETS Y SETS
    public ArrayList<SelectItem> getListaMiniProveedores() throws SQLException, NamingException {

        listaMiniProveedores = this.obtenerListaMiniProveedor();
        return listaMiniProveedores;
    }

    public void setListaMiniProveedores(ArrayList<SelectItem> listaMiniProveedores) {
        this.listaMiniProveedores = listaMiniProveedores;
    }

    public MiniProveedor getMiniProveedor() {
        return miniProveedor;
    }

    public void setMiniProveedor(MiniProveedor miniProveedor) {
        this.miniProveedor = miniProveedor;
    }
}