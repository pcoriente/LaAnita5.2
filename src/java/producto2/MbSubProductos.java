package producto2;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import producto2.dao.DAOSubProductos;
import producto2.dominio.Empaque;
import producto2.dominio.SubProducto;

/**
 *
 * @author jesc
 */
@Named(value = "mbSubProductos")
@SessionScoped
public class MbSubProductos implements Serializable {
    private int idArticulo;
    private SubProducto subProducto;
    private ArrayList<SelectItem> listaSubProductos;
    private DAOSubProductos dao;
    
    
    public MbSubProductos() {
        this.subProducto=new SubProducto();
    }
    
    public void eliminar() {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        try {
            this.dao=new DAOSubProductos();
            this.dao.eliminar(this.subProducto.getIdProducto());
            this.cargaListaSubProductos(this.idArticulo, 0);
            this.subProducto=new SubProducto();
            fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
            fMsg.setDetail("El producto se eliminó correctamente !!");
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }
    
    public void grabar() {
//        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if(this.subProducto.getEmpaque()==null) {
            fMsg.setDetail("El empaque es nulo !!");
        } else if(this.subProducto.getEmpaque().getIdEmpaque()==0) {
            fMsg.setDetail("Se requiere un empaque !!");
        } else if (this.subProducto.getPiezas() < 0) {
            fMsg.setDetail("Las piezas deben ser mayor o igual que cero !!");
        } else {
            try {
                this.dao=new DAOSubProductos();
                this.subProducto.setIdProducto(this.dao.agregar(this.idArticulo, this.subProducto));
                this.cargaListaSubProductos(this.idArticulo, this.subProducto.getIdProducto());
                fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                fMsg.setDetail("El producto se grabó correctamente !!");
//                ok=true;
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
//        return ok;
    }
    
    public void mttoSubProductos() {
        if(this.subProducto==null) {
            this.subProducto=new SubProducto();
        }
    }
    
    public void nuevo() {
        this.subProducto=new SubProducto();
        this.habilitaTodos();
    }
    
    public void cargaListaSubProductos() {
        this.listaSubProductos=new ArrayList<SelectItem>();
        SubProducto sp0=new SubProducto(0, 0, new Empaque(0, "SELECCIONE", ""));
        this.listaSubProductos.add(new SelectItem(sp0, sp0.toString(),"",false));
    }
    
    public void habilitaTodos() {
        for(SelectItem s: this.listaSubProductos) {
            s.setDisabled(false);
        }
    }
    
    public void cargaListaSubProductos(int idArticulo, int idProducto) {
        boolean ok = false;
        this.listaSubProductos=new ArrayList<SelectItem>();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        try {
            this.dao=new DAOSubProductos();
            SubProducto sp0=new SubProducto(0, 0, new Empaque(0, "SELECCIONE", ""));
            this.listaSubProductos.add(new SelectItem(sp0, sp0.toString(),"",false));
            for(SubProducto sp: dao.obtenerSubProductos(idArticulo)) {
                this.listaSubProductos.add(new SelectItem(sp, sp.toString(),"",(sp.getIdProducto()==idProducto?true:false)));
            }
            ok=true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public ArrayList<SelectItem> getListaSubProductos() {
        return listaSubProductos;
    }

    public void setListaSubProductos(ArrayList<SelectItem> listaSubProductos) {
        this.listaSubProductos = listaSubProductos;
    }

    public SubProducto getSubProducto() {
        return subProducto;
    }

    public void setSubProducto(SubProducto subProducto) {
        this.subProducto = subProducto;
    }

    public void setIdArticulo(int idArticulo) {
        this.idArticulo = idArticulo;
    }
}
