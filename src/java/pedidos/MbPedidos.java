package pedidos;

import Message.Mensajes;
import almacenes.MbMiniAlmacenes;
import clientes.MbMiniClientes;
import entradas.dao.DAOMovimientos;
import formatos.MbFormatos;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import mbMenuClientesGrupos.MbClientesGrupos;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import pedidos.DAO.DAOPedidos;
import pedidos.dominio.Pedido;
import pedidos.dominio.PedidoProducto;
import pedidos.to.TOPedido;
import pedidos.to.TOPedidoProducto;
import producto2.MbProductosBuscar;
import tiendas.MbMiniTiendas;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbPedidos")
@SessionScoped
public class MbPedidos implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbMiniAlmacenes}")
    private MbMiniAlmacenes mbAlmacenes;
    @ManagedProperty(value = "#{mbMenuClientesGrupos}")
    private MbClientesGrupos mbGrupos;
    @ManagedProperty(value = "#{mbMiniClientes}")
    private MbMiniClientes mbClientes;
    @ManagedProperty(value = "#{mbFormatos}")
    private MbFormatos mbFormatos;
    @ManagedProperty(value = "#{mbMiniTiendas}")
    private MbMiniTiendas mbTiendas;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    
    private Pedido pedido;
    private ArrayList<Pedido> pedidos;
    private ArrayList<PedidoProducto> detalle;
    private PedidoProducto producto;
    private double cantRespaldo;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private boolean asegurado;
    private DAOPedidos dao;
    
    private boolean todos;
    private Date fechaInicial;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    
    public MbPedidos() {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbMiniAlmacenes();
        
        this.mbGrupos = new MbClientesGrupos();
        this.mbClientes = new MbMiniClientes();
        this.mbFormatos = new MbFormatos();
        this.mbTiendas = new MbMiniTiendas();
        this.mbBuscar = new MbProductosBuscar();
        
        this.inicializa();
    }
    
    public void eliminarPedido() {
        boolean ok=false;
        this.asegurado=false;
        try {
            this.dao=new DAOPedidos();
            this.dao.eliminarPedido(this.pedido.getIdPedido());
            this.pedidos.remove(this.pedido);
            this.pedido=null;
            ok=true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    public void cerrarPedido() {
        boolean ok=false;
        try {
            DAOMovimientos daoMv=new DAOMovimientos();
            daoMv.cerrarPedido(this.pedido.getIdPedido());
            this.pedido.setStatus(1);
            Mensajes.mensajeSucces("El pedido se cerro correctamente !!!");
            ok=true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    public void modificarProducto(SelectEvent event) {
        boolean ok=false;
        if(this.pedido.getStatus()!=0) {
            Mensajes.mensajeAlert("El pedido ya esta cerrado, no se puede modificar !!!");
        } else if(this.asegurado) {
            this.producto = (PedidoProducto) event.getObject();
            this.cantRespaldo=this.producto.getCantFacturada();
            ok=true;
        } else {
            Mensajes.mensajeAlert("El pedido esta en modo lectura, no se puede modificar !!!");
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }
    
    public void actualizaProductoSeleccionado() {
        int idx;
        boolean ok=false;
        this.producto = new PedidoProducto(this.mbBuscar.getProducto());
        if ((idx=this.detalle.indexOf(this.producto)) != -1) {
            this.producto=this.detalle.get(idx);
        } else {
            this.producto.setIdPedido(this.pedido.getIdPedido());
            try {
                this.dao=new DAOPedidos();
                this.dao.agregarProducto(this.convertir(this.producto));
                this.detalle.add(this.producto);
                ok=true;
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okBuscar", ok);
    }
    
    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        } else {
            RequestContext context = RequestContext.getCurrentInstance();
            context.addCallbackParam("okBuscar", false);
        }
    }

    public void buscarProductos(String update) {
        this.mbBuscar.inicializar();
        this.mbBuscar.setUpdate(update);
    }
    
    public void liberarPedido() {
        boolean ok=false;
        if(this.pedido==null) {
            ok=true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else if(this.asegurado) {
            try {
                this.asegurado=false;
                this.dao=new DAOPedidos();
                ok=this.dao.liberarPedido(this.pedido.getIdPedido());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    private TOPedidoProducto convertir(PedidoProducto p) {
        TOPedidoProducto to=new TOPedidoProducto();
        to.setIdPedido(p.getIdPedido());
        to.setIdEmpaque(p.getProducto().getIdProducto());
        to.setCantFacturada(p.getCantFacturada());
        to.setCantSinCargo(p.getCantSinCargo());
        to.setUnitario(p.getUnitario());
        to.setIdImpuestoGrupo(p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
        return to;
    }
    
    public void actualizaProducto() {
        boolean ok=false;
        try {
            this.dao=new DAOPedidos();
            this.dao.grabarMovimiento(this.convertir(this.producto));
            this.pedido.setCantArticulos(this.pedido.getCantArticulos()-this.cantRespaldo+this.producto.getCantFacturada());
            ok=true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }
    
    private PedidoProducto convertir(TOPedidoProducto to) {
        PedidoProducto p=new PedidoProducto();
        p.setIdPedido(to.getIdPedido());
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdEmpaque()));
        p.setCantFacturada(to.getCantFacturada());
        p.setCantSinCargo(to.getCantSinCargo());
        p.setPrecio(to.getUnitario());
        p.setDescuento(0);
        p.setUnitario(to.getUnitario());
        return p;
    }
    
    public void obtenerDetalle(SelectEvent event) {
        boolean ok=false;
        int n=0;
        this.pedido = (Pedido) event.getObject();
        this.detalle=new ArrayList<PedidoProducto>();
        try {
            this.dao=new DAOPedidos();
            try {
                this.asegurado=this.dao.asegurarPedido(this.pedido.getIdPedido());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
//            PedidoProducto p;
//            DAOMovimientos daoMv=new DAOMovimientos();
            for(TOPedidoProducto to: this.dao.obtenerPedidoDetalle(this.pedido.getIdPedido())) {
//                p=this.convertir(to);
//                p.setImpuestos(daoMv.obtenerImpuestosProducto(idMovto, idEmpaque));
                this.detalle.add(this.convertir(to));
                n+=to.getCantFacturada();
//                this.detalle.add(p);
            }
            this.pedido.setCantArticulos(n);
//            this.actualizaTotales();
            ok=true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    private TOPedido convertir(Pedido p) {
        TOPedido to=new TOPedido();
        to.setIdPedido(p.getIdPedido());
        to.setIdAlmacen(p.getIdAlmacen());
        to.setIdTienda(p.getTienda().getIdTienda());
        to.setFecha(p.getFecha());
        to.setStatus(p.getStatus());
        to.setOrdenDeCompra(p.getOrdenDeCompra());
        to.setOrdenDeCompraFecha(p.getOrdenDeCompraFecha());
        to.setCancelacionMotivo(p.getCancelacionMotivo());
        to.setCancelacionFecha(p.getCancelacionFecha());
        return to;
    }
    
    public void crearPedido() {
        boolean ok=false;
        this.pedido = new Pedido(this.mbAlmacenes.getAlmacen().getIdAlmacen(), this.mbTiendas.getTienda(), this.mbFormatos.getFormatoSeleccion(), this.mbClientes.getCliente());
        this.pedido.setOrdenDeCompra(this.ordenDeCompra);
        this.pedido.setOrdenDeCompraFecha(this.ordenDeCompraFecha);
        TOPedido to=this.convertir(this.pedido);
        try {
            this.dao=new DAOPedidos();
            this.pedido.setIdPedido(this.dao.agregarPedido(to));
            this.pedidos.add(this.pedido);
            this.asegurado=true;
            ok=true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        }
        this.detalle = new ArrayList<PedidoProducto>();
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    public void cambioDeFormato() {
        this.mbTiendas.cargaTiendasFormato(this.mbFormatos.getFormatoSeleccion().getIdFormato());
        this.mbTiendas.nuevaTienda();
    }

    public void cambioDeCliente() {
        this.mbFormatos.cargarFormatosCliente(this.mbClientes.getCliente().getIdCliente());
        this.mbFormatos.nuevoFormato();
        this.cambioDeFormato();
    }
    
    public void cambioDeGrupo() {
        this.mbClientes.cargarClientesGrupo(this.mbGrupos.getClienteGrupoSeleccionado().getIdGrupoCte());
        this.mbClientes.nuevoCliente();
        this.cambioDeCliente();
    }
    
    public void nuevoPedido() {
        this.mbGrupos.inicializar();
        this.cambioDeGrupo();
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
    }

    public void regresarFechaActual() {
        this.fechaInicial = new Date();
        this.obtenerPedidos();
        this.ordenDeCompra="";
        this.ordenDeCompraFecha=new Date();
    }
    
    private Pedido convertir(TOPedido to) {
        Pedido p=new Pedido();
        p.setIdPedido(to.getIdPedido());
        p.setIdAlmacen(to.getIdAlmacen());
        p.setTienda(this.mbTiendas.obtenerTienda(to.getIdTienda()));
        p.setFormato(this.mbFormatos.obtenerFormato(p.getTienda().getIdFormato()));
        p.setCliente(this.mbClientes.obtenerCliente(p.getFormato().getIdCliente()));
        p.setOrdenDeCompra(to.getOrdenDeCompra());
        p.setOrdenDeCompraFecha(to.getOrdenDeCompraFecha());
        p.setFecha(to.getFecha());
        p.setStatus(to.getStatus());
        p.setCancelacionFecha(to.getCancelacionFecha());
        p.setCancelacionMotivo(to.getCancelacionMotivo());
        return p;
    }
    
    public void obtenerPedidos() {
        try {   // Segun fecha y status
            this.pedidos = new ArrayList<Pedido>();
            this.dao = new DAOPedidos();
            for (TOPedido to : this.dao.obtenerPedidos(this.mbAlmacenes.getAlmacen().getIdAlmacen(), (this.todos ? 9999 : 1), this.fechaInicial)) {
                this.pedidos.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public String terminar() {
        this.acciones = null;
        this.inicializa();
        return "index.xhtml";
    }

    private void inicializa() {
        this.inicializar();
    }

    public void inicializar() {
        this.mbAlmacenes.nuevoAlmacen();
        this.mbAlmacenes.setSinEmpresa(true);
        this.mbAlmacenes.setListaAlmacenes(null);
        
        this.mbGrupos.inicializar();
        this.mbClientes.inicializar();
        this.mbFormatos.inicializar();
        this.mbBuscar.inicializar();
        
        this.todos = false;
        this.fechaInicial = new Date();
        this.pedidos = new ArrayList<Pedido>();
        this.pedido = new Pedido();
        this.detalle = new ArrayList<PedidoProducto>();
    }

    public String getOrdenDeCompra() {
        return ordenDeCompra;
    }

    public void setOrdenDeCompra(String ordenDeCompra) {
        this.ordenDeCompra = ordenDeCompra;
    }

    public Date getOrdenDeCompraFecha() {
        return ordenDeCompraFecha;
    }

    public void setOrdenDeCompraFecha(Date ordenDeCompraFecha) {
        this.ordenDeCompraFecha = ordenDeCompraFecha;
    }

    public boolean isAsegurado() {
        return asegurado;
    }

    public void setAsegurado(boolean asegurado) {
        this.asegurado = asegurado;
    }

    public ArrayList<PedidoProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<PedidoProducto> detalle) {
        this.detalle = detalle;
    }

    public PedidoProducto getProducto() {
        return producto;
    }

    public void setProducto(PedidoProducto producto) {
        this.producto = producto;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public ArrayList<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(ArrayList<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    public boolean isTodos() {
        return todos;
    }

    public void setTodos(boolean todos) {
        this.todos = todos;
    }

    public Date getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public TimeZone getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(TimeZone zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public MbClientesGrupos getMbGrupos() {
        return mbGrupos;
    }

    public void setMbGrupos(MbClientesGrupos mbGrupos) {
        this.mbGrupos = mbGrupos;
    }

    public MbMiniClientes getMbClientes() {
        return mbClientes;
    }

    public void setMbClientes(MbMiniClientes mbClientes) {
        this.mbClientes = mbClientes;
    }

    public MbFormatos getMbFormatos() {
        return mbFormatos;
    }

    public void setMbFormatos(MbFormatos mbFormatos) {
        this.mbFormatos = mbFormatos;
    }

    public MbMiniTiendas getMbTiendas() {
        return mbTiendas;
    }

    public void setMbTiendas(MbMiniTiendas mbTiendas) {
        this.mbTiendas = mbTiendas;
    }

    public MbMiniAlmacenes getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbMiniAlmacenes mbAlmacenes) {
        this.mbAlmacenes = mbAlmacenes;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(31);
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }
}
