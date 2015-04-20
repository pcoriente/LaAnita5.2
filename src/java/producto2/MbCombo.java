package producto2;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import producto2.dao.DAOCombos;
import producto2.dominio.Producto;
import producto2.dominio.ProductoCombo;
import producto2.to.TOProductoCombo;

/**
 *
 * @author jesc
 */
@Named(value = "mbCombo")
@SessionScoped
public class MbCombo implements Serializable {
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    
    private ProductoCombo producto;
    private ArrayList<ProductoCombo> productos;
    private DAOCombos dao;
    
    public MbCombo() {
        this.mbBuscar=new MbProductosBuscar();
        this.inicializaLocales();
    }
    
    public void inicializar() {
        this.mbBuscar.inicializar();
        this.inicializaLocales();
    }
    
    private void inicializaLocales() {
        this.productos=new ArrayList<ProductoCombo>();
        this.producto=new ProductoCombo();
    }
    
    public void eliminar() {
        this.productos.remove(this.producto);
        if(this.productos.isEmpty()) {
            this.setProducto(null);
        } else {
            this.setProducto(this.productos.get(0));
        }
    }
    
    public void grabarCombo(int idProducto) {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        RequestContext context = RequestContext.getCurrentInstance();
        ArrayList<TOProductoCombo> tos=new ArrayList<TOProductoCombo>();
        for(ProductoCombo p:this.productos) {
            tos.add(convertir(p));
        }
        try {
            this.dao=new DAOCombos();
            this.dao.grabarCombo(tos, idProducto);
            ok=true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okCombo", ok);
    }
    
    private TOProductoCombo convertir(ProductoCombo p) {
        return new TOProductoCombo(p.getProducto().getIdProducto(), p.getPiezas());
    }
    
    public void agregarProductosCombo() {
        for(Producto p:this.mbBuscar.getSeleccionados()) {
            this.mbBuscar.setProducto(p);
            this.agregarProductoCombo();
        }
    }
    
    public void buscar() {
        this.mbBuscar.buscarLista();
        if(this.mbBuscar.getProducto()!=null) {
            this.agregarProductoCombo();
        }
    }
    
    public void agregarProductoCombo() {
        boolean nuevo=true;
        for(ProductoCombo p:this.productos) {
            if(p.getProducto().equals(this.mbBuscar.getProducto())) {
                nuevo=false;
                break;
            }
        }
        if(nuevo) {
            this.actualizaProductoSeleccionado();
            this.productos.add(this.producto);
        }
    }
    
    public void actualizaProductoSeleccionado() {
        this.producto=new ProductoCombo(this.mbBuscar.getProducto(), 1);
    }
    
    public void obtenerCombo(int idProducto) {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        this.productos=new ArrayList<ProductoCombo>();
        try {
            this.dao=new DAOCombos();
            for(TOProductoCombo to:this.dao.obtenerCombo(idProducto)) {
                this.productos.add(this.convertir(to));
            }
            this.setProducto(null);
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
    
    private ProductoCombo convertir(TOProductoCombo to) {
        return new ProductoCombo(this.mbBuscar.obtenerProducto(to.getIdSubProducto()), to.getPiezas());
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public ProductoCombo getProducto() {
        return producto;
    }

    public void setProducto(ProductoCombo producto) {
        this.producto = producto;
    }

    public ArrayList<ProductoCombo> getProductos() {
        return productos;
    }

    public void setProductos(ArrayList<ProductoCombo> productos) {
        this.productos = productos;
    }
}
