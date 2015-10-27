package proveedores;

import empresas.MbMiniEmpresas;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TimeZone;
import javax.faces.bean.ManagedProperty;
import org.primefaces.context.RequestContext;
import proveedores.dominio.MiniProveedor;
import proveedores.dominio.ProveedorProducto;
import proveedores.dominio.ProveedorProductoLista;
import proveedores.dominio.ProveedorProductoOferta;
import proveedores.dominio.ProveedorProductoPrecio;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jsolis
 */
@Named(value = "mbListas")
@SessionScoped
public class MbProveedoresListas implements Serializable {
    private boolean modoEdicion;
    private ArrayList<ProveedorProducto> listaProductos;
    private ArrayList<ProveedorProducto> listaFiltrados;
    private ProveedorProductoLista productoLista;
    private ProveedorProducto producto;
    private ProveedorProductoPrecio precio;
    private ProveedorProductoOferta oferta;
    private TimeZone zonaHoraria=TimeZone.getDefault();
    
    private ArrayList<Accion> acciones;
    @ManagedProperty(value="#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbMiniEmpresas}")
    private MbMiniEmpresas mbEmpresas;
    @ManagedProperty(value="#{mbMiniProveedor}")
    private MbMiniProveedor mbProveedor;
    @ManagedProperty(value="#{mbProveedorProducto}")
    private MbProveedorProducto mbProducto;
    @ManagedProperty(value="#{mbProveedorProductoOfertas}")
    private MbProveedorProductoOfertas mbOfertas;
    @ManagedProperty(value="#{mbProveedorProductoPrecios}")
    private MbProveedorProductoPrecios mbPrecios;
//    @ManagedProperty(value="#{mbProductosBuscar}")
//    private MbProductosBuscar mbBuscar;
    
    public MbProveedoresListas() {
        this.modoEdicion=false;
        this.productoLista=new ProveedorProductoLista();
        this.mbEmpresas=new MbMiniEmpresas();
        this.mbProveedor=new MbMiniProveedor();
        this.mbAcciones=new MbAcciones();
        this.mbProducto=new MbProveedorProducto(0, 0);
        this.mbOfertas=new MbProveedorProductoOfertas();
        this.mbPrecios=new MbProveedorProductoPrecios();
    }
    
    public void actualizaProductoSeleccionado() {
        boolean ok = false;
        this.mbProducto.getProducto().setEquivalencia(this.mbProducto.getMbBuscar().getProducto());
        ok=true;
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okBuscar", ok);
    }
    
        public void buscar() {
         this.mbProducto.getMbBuscar().buscarLista();
         if(this.mbProducto.getMbBuscar().getProducto()!=null) {
             this.actualizaProductoSeleccionado();
         } else {
             RequestContext context = RequestContext.getCurrentInstance();
            context.addCallbackParam("okBuscar", false);
         }
    }
    
    public void eliminarOferta() {
        if(this.mbOfertas.eliminar()) {
            this.oferta=null;
            this.mbOfertas.setOfertas(null);
        }
    }
    
    public void grabarOferta() {
        if(this.mbOfertas.grabar()) {
            this.oferta=null;
            this.mbOfertas.setOfertas(null);
        }
    }
    
    public void mantenimientoOferta(boolean nueva) {
        if(nueva) {
            this.mbOfertas.setOferta(new ProveedorProductoOferta());
        } else {
            this.mbOfertas.copia(this.oferta);
        }
    }
    
    public void eliminarPrecio() {
        if(this.mbPrecios.eliminar()) {
            this.precio=null;
            this.mbPrecios.setPrecios(null);
        }
    }
    
    public void grabarPrecio() {
        if(this.mbPrecios.grabar()) {
            this.precio=null;
            this.mbPrecios.setPrecios(null);
        }
    }
    
    public void mantenimientoPrecio(boolean nueva) {
        if(nueva) {
            this.mbPrecios.setPrecio(new ProveedorProductoPrecio());
        } else {
            this.mbPrecios.copia(this.precio);
        }
    }
    
    public void grabarProducto() {
        if (this.mbProducto.grabar()) {
            this.productoLista.setProducto(this.mbProducto.getProducto());
        }
    }
    
    public void mantenimientoProducto() {
        this.mbProducto.mantenimiento(this.productoLista.getProducto());
    }
    
    public void modificarProductoLista() {
        this.modoEdicion=true;
        this.mbOfertas.setIdProducto(this.producto.getIdProducto());
        this.mbPrecios.setIdProducto(this.producto.getIdProducto());
        this.productoLista.setProducto(this.producto);
        this.mbPrecios.setPrecios(null);
        this.mbOfertas.setOfertas(null);
    }
    
    public void nuevoProductoLista() {
        this.modoEdicion=true;
        this.mbOfertas.setIdProducto(0);
        this.mbPrecios.setIdProducto(0);
        this.productoLista=new ProveedorProductoLista();
        this.mbPrecios.setPrecios(null);
        this.mbOfertas.setOfertas(null);
    }
    
    public String salir() {
        this.modoEdicion=false;
        this.listaProductos=null;
        this.listaFiltrados=null;
        this.producto=null;
        this.mbOfertas.setOfertas(null);
        this.mbPrecios.setPrecios(null);
        return "proveedoresListas.xhtml";
    }
    
    public String terminar() {
        this.mbEmpresas.inicializar();
        this.mbProveedor.setListaMiniProveedores(null);
        this.mbProveedor.setMiniProveedor(new MiniProveedor());
        this.listaProductos=null;
        this.listaFiltrados=null;
        this.producto=null;
        this.acciones=null;
        return "index.xhtml";
    }
    
    public void cargaProductos() {
        int idEmpresa=this.mbEmpresas.getEmpresa().getIdEmpresa();
        this.mbProducto.setIdEmpresa(idEmpresa);
        int idProveedor=this.mbProveedor.getMiniProveedor().getIdProveedor();
        this.mbProducto.setIdProveedor(idProveedor);
        this.mbOfertas.setIdProveedor(idProveedor);
        this.mbPrecios.setIdProveedor(idProveedor);
        this.listaProductos=this.mbProducto.obtenerProductos(idEmpresa, idProveedor);
        this.listaFiltrados=null;
    }

    public ProveedorProductoLista getProductoLista() {
        return productoLista;
    }

    public void setProductoLista(ProveedorProductoLista productoLista) {
        this.productoLista = productoLista;
    }

    public MbMiniProveedor getMbProveedor() {
        return mbProveedor;
    }

    public void setMbProveedor(MbMiniProveedor mbProveedor) {
        this.mbProveedor = mbProveedor;
    }

    public MbMiniEmpresas getMbEmpresas() {
        return mbEmpresas;
    }

    public void setMbEmpresas(MbMiniEmpresas mbEmpresas) {
        this.mbEmpresas = mbEmpresas;
    }

    public ArrayList<Accion> getAcciones() {
        if(this.acciones==null) {
            this.acciones=this.mbAcciones.obtenerAcciones(9);
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public ArrayList<ProveedorProducto> getListaProductos() {
        if(this.listaProductos==null) {
            this.cargaProductos();
        }
        return listaProductos;
    }

    public void setListaProductos(ArrayList<ProveedorProducto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    public ArrayList<ProveedorProducto> getListaFiltrados() {
        return listaFiltrados;
    }

    public void setListaFiltrados(ArrayList<ProveedorProducto> listaFiltrados) {
        this.listaFiltrados = listaFiltrados;
    }

    public ProveedorProducto getProducto() {
        return producto;
    }

    public void setProducto(ProveedorProducto producto) {
        this.producto = producto;
    }

    public MbProveedorProducto getMbProducto() {
        return mbProducto;
    }

    public void setMbProducto(MbProveedorProducto mbProducto) {
        this.mbProducto = mbProducto;
    }

    public ProveedorProductoOferta getOferta() {
        return oferta;
    }

    public void setOferta(ProveedorProductoOferta oferta) {
        this.oferta = oferta;
    }

    public MbProveedorProductoOfertas getMbOfertas() {
        return mbOfertas;
    }

    public void setMbOfertas(MbProveedorProductoOfertas mbOfertas) {
        this.mbOfertas = mbOfertas;
    }

    public ProveedorProductoPrecio getPrecio() {
        return precio;
    }

    public void setPrecio(ProveedorProductoPrecio precio) {
        this.precio = precio;
    }

    public MbProveedorProductoPrecios getMbPrecios() {
        return mbPrecios;
    }

    public void setMbPrecios(MbProveedorProductoPrecios mbPrecios) {
        this.mbPrecios = mbPrecios;
    }

    //    public MbProductosBuscar getMbBuscar() {
    //        return mbBuscar;
    //    }
    //
    //    public void setMbBuscar(MbProductosBuscar mbBuscar) {
    //        this.mbBuscar = mbBuscar;
    //    }
    public TimeZone getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(TimeZone zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }
}
