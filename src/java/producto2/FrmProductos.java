package producto2;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import producto2.dao.DAOProductos;
import producto2.dominio.Empaque;
import producto2.dominio.Producto;
import producto2.dominio.SubProducto;
import producto2.dominio.Upc;

/**
 *
 * @author jesc
 */
@Named(value = "frmProductos")
@SessionScoped
public class FrmProductos implements Serializable {
    @ManagedProperty(value = "#{mbArticulos}")
    private MbArticulos mbArticulos;
    @ManagedProperty(value = "#{mbEmpaques}")
    private MbEmpaques mbEmpaques;
    @ManagedProperty(value = "#{mbSubProductos}")
    private MbSubProductos mbSubProductos;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    @ManagedProperty(value = "#{mbCombo}")
    private MbCombo mbCombo;
    
    private Producto producto;
//    private Producto comboProducto;
//    private ArrayList<Producto> combo;
    private DAOProductos dao;
    
    public FrmProductos() {
        this.mbArticulos=new MbArticulos();
        this.mbEmpaques=new MbEmpaques();
        this.mbBuscar=new MbProductosBuscar();
        this.mbSubProductos=new MbSubProductos();
        this.mbCombo=new MbCombo();
        this.inicializaLocales();
    }
    
    public void onIdle() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, 
                                        "No activity.", "What are you doing over there?"));
    }
 
    public void onActive() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                                        "Welcome Back", "Well, that's a long coffee break!"));
    }

    
    public void inicializar() {
        this.mbArticulos.inicializar();
        this.mbEmpaques.inicializar();
        this.mbBuscar.inicializar();
        this.mbCombo.inicializar();
        this.inicializaLocales();
    }
    
    private void inicializaLocales() {
        this.producto=new Producto();
    }
    
//    public void grabarParte() {
//        boolean ok=false;
//        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
//        try {
//            if(this.mbArticulos.getMbParte().grabar()) {
//                ok=true;
//            }
//        } catch (NamingException ex) {
//            fMsg.setDetail(ex.getMessage());
//        } catch (SQLException ex) {
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
//        }
//        if(!ok) {
//            FacesContext.getCurrentInstance().addMessage(null, fMsg);
//        }
//    }
    
    public void eliminarParte() {
        this.mbArticulos.getMbParte().setParte(this.mbArticulos.getArticulo().getParte());
        if(this.mbArticulos.getMbParte().eliminar(this.mbArticulos.getArticulo().getIdArticulo())) {
            this.mbArticulos.getArticulo().setParte(this.mbArticulos.getMbParte().getParte());
        }
    }
    
    public void mttoSubProductos() {
        this.mbSubProductos.setSubProducto(this.producto.getSubProducto());
    }
    
//    public void grabarSubEmpaque() {
//        this.mbSubProductos.grabar();
//    }
    
    public void grabarCombo() {
        this.mbCombo.grabarCombo(this.producto.getIdProducto());
    }
    
    public void obtenerCombo() {
        this.mbCombo.obtenerCombo(this.producto.getIdProducto());
    }
    
    public void eliminarUpc() {
        if (this.mbBuscar.getMbUpc().eliminar()) {
            this.producto.setUpc(this.mbBuscar.getMbUpc().nuevoLista(this.producto.getIdProducto()));
            this.mbBuscar.getMbUpc().cargaListaUpcs();
        }
    }
    
    public void grabarUpc() {
        boolean ok;
        if(this.mbBuscar.getMbUpc().isNueva()) {
            ok=this.mbBuscar.getMbUpc().agregar();
        } else {
            ok=this.mbBuscar.getMbUpc().modificar();
        }
        RequestContext context = RequestContext.getCurrentInstance();
        if(ok) {
            this.producto.setUpc(this.mbBuscar.getMbUpc().obtenerUpc(this.mbBuscar.getMbUpc().getUpc().getUpc()));
            this.mbBuscar.getMbUpc().cargaListaUpcs();
        }
        context.addCallbackParam("okUpc", ok);
    }
    
    public void mttoUpc() {
        if (this.producto.getUpc().getUpc().equals("SELECCIONE")) {
            this.mbBuscar.getMbUpc().setNueva(true);
            this.mbBuscar.getMbUpc().nuevo(this.producto.getIdProducto());
        } else {
            this.mbBuscar.getMbUpc().setNueva(false);
            this.mbBuscar.getMbUpc().copia(this.producto.getUpc());
        }
    }
    
    public void eliminarEmpaque() {
        if (this.mbEmpaques.eliminar()) {
            this.producto.getEmpaque().setIdEmpaque(0);
            this.mbEmpaques.setListaEmpaques(null);
        }
    }
    
    public void grabarEmpaque() {
        if (this.mbEmpaques.grabar()) {
            this.producto.getEmpaque().setIdEmpaque(this.mbEmpaques.getEmpaque().getIdEmpaque());
            this.mbEmpaques.setListaEmpaques(null);
        }
    }
    
    public void mttoEmpaque() {
        if (this.producto.getEmpaque().getIdEmpaque()==0) {
            this.mbEmpaques.nuevo();
        } else {
            this.mbEmpaques.copia(this.producto.getEmpaque());
        }
    }
    
    public void obtenerProductos() {
//        this.mbBuscar.obtenerProductos(this.producto.getArticulo().getIdArticulo());
        this.mbBuscar.obtenerProductos(this.mbArticulos.getArticulo().getIdArticulo());
    }
    
    public void actualizaProductoSeleccionado() {
        this.setProducto(this.mbBuscar.getProducto());
        this.mbSubProductos.cargaListaSubProductos(this.producto.getArticulo().getIdArticulo(), this.producto.getIdProducto());
        this.mbBuscar.getMbUpc().getUpc().setIdProducto(this.producto.getIdProducto());
        this.mbBuscar.getMbUpc().cargaListaUpcs();
        if(this.mbBuscar.getMbUpc().getListaUpcs().size()>1) {
            this.producto.setUpc((Upc)this.mbBuscar.getMbUpc().getListaUpcs().get(1).getValue());
        }
    }
    
    public void eliminar() {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        try {
            this.dao=new DAOProductos();
            this.dao.eliminar(this.producto.getIdProducto());
            this.mbBuscar.getProductos().remove(this.producto);
            this.producto.setIdProducto(0);
            this.producto.setCod_pro("");
            this.producto.setEmpaque(new Empaque());
            this.producto.setPiezas(0);
            this.producto.setSubProducto(new SubProducto());
            this.producto.setPeso(0);
            this.producto.setVolumen(0);
            this.producto.setDun14("");
            this.producto.setSufijo("");
            this.producto.setDiasCaducidad(0);
            this.mbBuscar.getMbUpc().nuevo(0);
            this.mbBuscar.getMbUpc().cargaListaUpcs();
            fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
            fMsg.setDetail("El empaque se ha eliminado con exito");
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }
    
    public void grabar() {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if (this.producto.getCod_pro().equals("")) {
            fMsg.setDetail("Se requiere el SKU del producto !!");
        } else if (this.producto.getEmpaque().getIdEmpaque()==0) {
            fMsg.setDetail("Se requiere la unidad de empaque !!");
        } else if (this.producto.getPiezas() < 0) {
            fMsg.setDetail("Las piezas deben ser mayor o igual que cero !!");
        } else {
            try {
                this.dao = new DAOProductos();
                if (this.producto.getIdProducto() == 0) {
                    this.producto.setIdProducto(this.dao.agregar(this.producto));
                } else {
                    this.dao.modificar(this.producto);
                }
                this.mbSubProductos.cargaListaSubProductos(this.producto.getArticulo().getIdArticulo(), this.producto.getIdProducto());
                fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                fMsg.setDetail("El producto se grabó correctamente !!");
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }
    
    public void actualizaContenido() {
        if(this.producto.getEmpaque().getIdEmpaque()==1) {
            this.producto.setPiezas(1);
        } else {
            this.producto.setPiezas(0);
        }
        this.producto.setSubProducto(new SubProducto());
    }
    
    public void nuevoProducto() {
        this.producto=new Producto(this.mbArticulos.getArticulo(), this.mbBuscar.getMbUpc().nuevoLista(0));
        this.mbBuscar.getMbUpc().nuevo(0);
        this.mbBuscar.getMbUpc().cargaListaUpcs();
        this.mbSubProductos.setIdArticulo(this.mbArticulos.getArticulo().getIdArticulo());
        this.mbSubProductos.cargaListaSubProductos(this.mbArticulos.getArticulo().getIdArticulo(), 0);
    }
    
    public String terminar() {
        this.mbArticulos=new MbArticulos();
        return "productos.terminar";
    }
    
    public MbArticulos getMbArticulos() {
        return mbArticulos;
    }

    public void setMbArticulos(MbArticulos mbArticulos) {
        this.mbArticulos = mbArticulos;
    }

    public MbEmpaques getMbEmpaques() {
        return mbEmpaques;
    }

    public void setMbEmpaques(MbEmpaques mbEmpaques) {
        this.mbEmpaques = mbEmpaques;
    }
    
    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public MbSubProductos getMbSubProductos() {
        return mbSubProductos;
    }

    public void setMbSubProductos(MbSubProductos mbSubProductos) {
        this.mbSubProductos = mbSubProductos;
    }
    
    public MbCombo getMbCombo() {
        return mbCombo;
    }

    public void setMbCombo(MbCombo mbCombo) {
        this.mbCombo = mbCombo;
    }
}
