package ventas;

import Message.Mensajes;
import almacenes.MbMiniAlmacenes;
import clientes.MbMiniClientes;
import ventas.dominio.Venta;
import ventas.dominio.VentaProducto;
import entradas.dao.DAOMovimientos;
import movimientos.to.TOMovimiento;
import movimientos.to.TOMovimientoProducto;
import formatos.MbFormatos;
import impuestos.dominio.ImpuestosProducto;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import mbMenuClientesGrupos.MbClientesGrupos;
import movimientos.to.TOLote;
import movimientos.to.TOMovimientoAlmacenProducto;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;
import tiendas.MbMiniTiendas;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;
import ventas.dominio.VentaAlmacenProducto;

/**
 *
 * @author jesc
 */
@Named(value = "mbVentas")
@SessionScoped
public class MbVentas implements Serializable {

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
    private DAOMovimientos dao;
    private ArrayList<Venta> pedidos;
    private Venta venta;
    private boolean ventaAsegurada;
    private ArrayList<VentaProducto> detalle, similares;
    private VentaProducto producto, respaldo, similar;
    private ArrayList<ImpuestosProducto> impuestosTotales;
    private ArrayList<VentaAlmacenProducto> almacenDetalle, empaqueLotes;
    private VentaAlmacenProducto loteOrigen, loteDestino;
    private boolean todos;
    private Date fechaInicial;
//    private boolean modoEdicion;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private double cantTraspasar;

    public MbVentas() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbMiniAlmacenes();

        this.mbGrupos = new MbClientesGrupos();
        this.mbClientes = new MbMiniClientes();
        this.mbFormatos = new MbFormatos();
        this.mbTiendas = new MbMiniTiendas();
        this.mbBuscar = new MbProductosBuscar();

        this.inicializa();
    }
    
    public void surtirOrdenDeCompra() {
        try {
            this.dao=new DAOMovimientos();
            this.dao.surtirOrdenDeCompra(this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen(), this.venta.getTienda().getIdImpuestoZona());
            
            this.detalle=new ArrayList<VentaProducto>();
            for(TOMovimientoProducto to: this.dao.obtenerMovimientoDetalle(this.venta.getIdMovto())) {
                this.detalle.add(this.convertirProducto(to));
            }
            this.actualizaTotales();
            Mensajes.mensajeSucces("La orden de compra se surtio correctamente");
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }
    
    public void cerrarPedidoAlmacenRelacionado() {
        boolean ok=false;
        try {
            this.dao=new DAOMovimientos();
            int remision=this.dao.cerrarMovtoAlmacenSalidaRelacionado(this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen());
            this.venta.setRemision(Integer.toString(remision));
            this.venta.setStatus(2);
            this.venta.setStatusAlmacen(2);
            Mensajes.mensajeSucces("El pedido se remisiono correctamente !!!");
            ok=true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    public void actualizaTraspasoLote() {
        boolean ok=false;
        try {
            int idx;
            this.dao=new DAOMovimientos();
            this.dao.traspasarLote(this.venta.getAlmacen().getIdAlmacen(), this.convertirAlmacenProducto(this.loteOrigen), this.loteDestino.getLote(), this.cantTraspasar);
            idx=this.almacenDetalle.indexOf(this.loteOrigen);
            this.loteOrigen=this.almacenDetalle.get(idx);
            this.loteOrigen.setCantidad(this.loteOrigen.getCantidad()-this.cantTraspasar);
            if((idx=this.almacenDetalle.indexOf(this.loteDestino))!=-1) {
                this.loteDestino=this.almacenDetalle.get(idx);
                this.loteDestino.setCantidad(this.loteDestino.getCantidad()+this.cantTraspasar);
                this.almacenDetalle.set(idx, this.loteDestino);
            } else {
                this.loteDestino.setCantidad(this.cantTraspasar);
                this.almacenDetalle.add(this.loteDestino);
            }
            ok=true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLote", ok);
    }
    
    public void inicializaTraspasoLote() {
        boolean ok=false;
        this.cantTraspasar=0;
        this.loteDestino=null;
        VentaAlmacenProducto prod;
        this.empaqueLotes=new ArrayList<VentaAlmacenProducto>();
        try {
            this.dao=new DAOMovimientos();
            for(TOLote to: this.dao.obtenerEmpaqueLotesDisponibles(this.venta.getAlmacen().getIdAlmacen(), this.loteOrigen.getProducto().getIdProducto())) {
                if(!this.loteOrigen.getLote().equals(to.getLote())) {
                    prod=new VentaAlmacenProducto(this.loteOrigen.getProducto(), to.getLote());
                    prod.setIdMovtoAlmacen(this.loteOrigen.getIdMovtoAlmacen());
                    prod.setCantidad(to.getCantidad());
                    this.empaqueLotes.add(prod);
                }
            }
            ok=true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLote", ok);
    }
    
    private TOMovimientoAlmacenProducto convertirAlmacenProducto(VentaAlmacenProducto prod) throws SQLException {
        TOMovimientoAlmacenProducto to=new TOMovimientoAlmacenProducto();
        to.setIdMovtoAlmacen(prod.getIdMovtoAlmacen());
        to.setIdProducto(prod.getProducto().getIdProducto());
        to.setLote(prod.getLote());
        to.setCantidad(prod.getCantidad());
        return to;
    }
    
    private VentaAlmacenProducto convertirAlmacenProducto(TOMovimientoAlmacenProducto to) throws SQLException {
        VentaAlmacenProducto prod=new VentaAlmacenProducto();
        prod.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        prod.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        prod.setLote(to.getLote());
        prod.setCantidad(to.getCantidad());
        return prod;
    }
    
    public void obtenerAlmacenDetalle(SelectEvent event) {
        boolean ok=false;
        this.loteOrigen=null;
        this.venta = (Venta) event.getObject();
        this.almacenDetalle=new ArrayList<VentaAlmacenProducto>();
        try {
            this.dao=new DAOMovimientos();
            try {
                this.ventaAsegurada=this.dao.asegurarMovtoRelacionado(this.venta.getIdMovto());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
            for(TOMovimientoAlmacenProducto to: this.dao.obtenerMovimientoAlmacenDetalle(this.venta.getIdMovtoAlmacen())) {
                this.almacenDetalle.add(this.convertirAlmacenProducto(to));
            }
            ok=true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    // *************************** Ventas Oficina ******************************* //
    
    public void eliminarPedidoRelacionado() {
        boolean ok=false;
        this.ventaAsegurada=false;
        try {
            this.dao=new DAOMovimientos();
            this.dao.eliminarMovtoSalidaRelacionado(this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen());
            this.pedidos.remove(this.venta);
            this.venta=null;
            ok=true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    public void liberarMovtoRelacionado() {
        boolean ok=false;
        if(this.venta==null) {
            ok=true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else if(this.ventaAsegurada) {
            try {
                this.ventaAsegurada=false;
                this.dao=new DAOMovimientos();
                ok=this.dao.liberarMovtoRelacionado(this.venta.getIdMovto());
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
    
    public void cerrarPedidoRelacionado() {
        boolean ok=false;
        try {
            this.dao=new DAOMovimientos();
            int folio=this.dao.cerrarMovtoSalidaRelacionado(this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen(), 28);
            this.venta.setFolio(folio);
            this.venta.setStatus(1);
            this.venta.setStatusAlmacen(1);
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
    
    public void actualizaTraspasoSimilar() {
        boolean ok=false;
        TOMovimientoProducto toOrigen=this.convertirProducto(this.producto);
        TOMovimientoProducto toDestino=new TOMovimientoProducto();
        toDestino.setIdMovto(this.similar.getIdMovto());
        toDestino.setIdProducto(this.similar.getProducto().getIdProducto());
        toDestino.setIdImpuestoGrupo(this.similar.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
        toDestino.setCantSinCargo(this.similar.getCantSinCargo());
        try {
            this.dao=new DAOMovimientos();
            this.dao.tranferirSinCargo(this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen(), toOrigen, toDestino, this.cantTraspasar, this.venta.getTienda().getIdImpuestoZona());
            this.producto.setCantSinCargo(toOrigen.getCantSinCargo());
            this.producto.setSeparados(this.producto.getCantidadFacturada()+this.producto.getCantSinCargo());
            if(this.similar.getIdMovto()==0) {
                this.detalle.add(this.convertirProducto(toDestino));
            } else {
                int idx=this.detalle.indexOf(this.similar);
                this.similar=this.detalle.get(idx);
                this.similar.setCantSinCargo(toDestino.getCantSinCargo());
                this.similar.setSeparados(this.similar.getCantidadFacturada()+this.similar.getCantSinCargo());
            }
            this.actualizaTotales();
            ok=true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okSimilares", ok);
    }
    
    public void traspasoSimilar() {
        int idx;
        boolean ok=false;
        this.similar=null;
        VentaProducto prod;
        this.cantTraspasar=0;
        this.similares=new ArrayList<VentaProducto>();
        try {
            for(Producto p: this.mbBuscar.obtenerSimilares(this.producto.getProducto().getIdProducto())) {
                prod=new VentaProducto(p);
                if((idx=this.detalle.indexOf(prod)) != -1) {
                    prod.setIdMovto(this.detalle.get(idx).getIdMovto());
                    prod.setCantSinCargo(this.detalle.get(idx).getCantSinCargo());
                }
                this.similares.add(prod);
            }
            ok=true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okSimilares", ok);
    }

    public void capturar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "capturar");
        if (this.mbClientes.getCliente().getIdContribuyente() == 0) {
            fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
            fMsg.setDetail("Se requiere seleccionar un cliente");
        } else if (this.mbTiendas.getTienda().getIdTienda() == 0) {
            fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
            fMsg.setDetail("Se requiere seleccionar un cliente");
        } else {
            this.venta = new Venta();
            try {
                this.dao = new DAOMovimientos();
                TOMovimiento to=this.convertirTO();
                this.dao.agregarMovimientoRelacionado(to);
                this.venta = this.convertir(this.dao.obtenerMovimientoRelacionado(to.getIdMovto()));
                this.detalle = new ArrayList<VentaProducto>();
                this.producto = new VentaProducto();
                ok = true;
            } catch (SQLException ex) {
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setDetail(ex.getMessage());
            }
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    private Venta convertir(TOMovimiento to) {
        Venta vta = new Venta();
        vta.setIdMovto(to.getIdMovto());
        vta.setIdTipo(to.getIdTipo());
        vta.setAlmacen(this.mbAlmacenes.obtenerAlmacen(to.getIdAlmacen()));
        vta.setFolio(to.getFolio());
//        vta.setIdImpuestoZona(to.getIdImpuestoZona());
        vta.setDesctoComercial(to.getDesctoComercial());
        vta.setFecha(to.getFecha());
        vta.setIdUsuario(to.getIdUsuario());
        vta.setMoneda(null);
        vta.setTipoCambio(to.getTipoCambio());
        vta.setTienda(this.mbTiendas.obtenerTienda(to.getIdReferencia()));
        vta.setFormato(this.mbFormatos.obtenerFormato(vta.getTienda().getIdFormato()));
        vta.setCliente(this.mbClientes.obtenerCliente(vta.getFormato().getIdCliente()));
        vta.setIdOrdenCompra(to.getReferencia());
        vta.setStatus(to.getStatusOficina());
        //////////////////////////////////////////////////
        vta.setRemision(to.getRemision());
        vta.setFechaComprobante(to.getFechaComprobante());
        //////////////////////////////////////////////////
        vta.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        vta.setFechaAlmacen(to.getFechaAlmacen());
        vta.setIdUsuarioAlmacen(to.getIdUsuarioAlmacen());
        vta.setStatusAlmacen(to.getStatusAlmacen());
        return vta;
    }

    private TOMovimiento convertirTO() {
        TOMovimiento to = new TOMovimiento();
        to.setIdMovto(this.venta.getIdMovto());
        to.setIdTipo(this.venta.getIdTipo());
        to.setIdEmpresa(this.venta.getAlmacen().getIdEmpresa());
        to.setIdAlmacen(this.venta.getAlmacen().getIdAlmacen());
        to.setFolio(this.venta.getFolio());
        to.setIdImpuestoZona(this.venta.getTienda().getIdImpuestoZona());
        to.setDesctoComercial(this.venta.getDesctoComercial());
        to.setFecha(this.venta.getFecha());
        to.setIdUsuario(this.venta.getIdUsuario());
        to.setIdMoneda(this.venta.getMoneda().getIdMoneda());
        to.setTipoCambio(this.venta.getTipoCambio());
        to.setIdReferencia(this.venta.getTienda().getIdTienda());
        to.setReferencia(this.venta.getIdOrdenCompra());
        to.setStatusOficina(this.venta.getStatus());
        ////////////////////////////////////////////////////////////
        to.setRemision(this.venta.getRemision());
        to.setFechaComprobante(this.venta.getFechaComprobante());
        ////////////////////////////////////////////////////////////
        to.setIdMovtoAlmacen(this.venta.getIdMovtoAlmacen());
        to.setFechaAlmacen(this.venta.getFechaAlmacen());
        to.setIdUsuarioAlmacen(this.venta.getIdUsuarioAlmacen());
        to.setStatusAlmacen(this.venta.getStatusAlmacen());
        return to;
    }

    public String terminar() {
        this.acciones = null;
        this.inicializar();
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
        this.pedidos = new ArrayList<Venta>();
        this.venta = new Venta();
        this.detalle = new ArrayList<VentaProducto>();
//        this.modoEdicion=false;
    }
    
    public void actualizaProducto() {
        int idx, idx1;
        boolean ok=false;
        ArrayList<TOMovimientoProducto> agregados;
        try {
            this.dao = new DAOMovimientos();
            idx=this.detalle.indexOf(this.producto);
            TOMovimientoProducto to=this.convertirProducto(this.producto);
            agregados=this.dao.grabarMovimientoDetalle(this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen(), to, this.producto.getSeparados(), this.venta.getTienda().getIdImpuestoZona());
            this.producto=this.detalle.get(idx);
            this.producto.setCantidadFacturada(to.getCantFacturada());
            this.producto.setCantSinCargo(to.getCantSinCargo());
            this.producto.setSeparados(to.getCantFacturada()+to.getCantSinCargo());
            
            for(TOMovimientoProducto p: agregados) {
                if(p.getIdMovto()==0) {
                    p.setIdMovto(to.getIdMovto());
                    this.detalle.add(this.convertirProducto(p));
                } else {
                    this.producto=new VentaProducto(this.mbBuscar.obtenerProducto(p.getIdProducto()));
                    if((idx1=this.detalle.indexOf(this.producto)) != -1) {
                        this.producto=this.detalle.get(idx1);
                        this.producto.setCantSinCargo(this.producto.getCantSinCargo()+p.getCantSinCargo());
                        this.producto.setSeparados(this.producto.getCantidadFacturada()+this.producto.getCantSinCargo());
                    } else {
                        Mensajes.mensajeError("Inconsistencia de lotes, notifique a informatica !!!");
                    }
                }
            }
            this.actualizaTotales();
            ok=true;
        } catch (SQLException ex) {
            producto.setCantidadFacturada(respaldo.getCantidadFacturada());
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            producto.setCantidadFacturada(respaldo.getCantidadFacturada());
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }
    
    private void respalda() {
        this.respaldo=new VentaProducto();
        this.respaldo.setProducto(this.producto.getProducto());
        this.respaldo.setCantidadOrdenada(this.producto.getCantidadOrdenada());
        this.respaldo.setCantidadFacturada(this.producto.getCantidadFacturada());
        this.respaldo.setCantSinCargo(this.producto.getCantSinCargo());
        this.respaldo.setSeparados(this.producto.getSeparados());
        this.respaldo.setPrecio(this.producto.getPrecio());
        this.respaldo.setDescuento(this.producto.getDescuento());
        this.respaldo.setImpuestos(this.producto.getImpuestos());
        this.respaldo.setNeto(this.producto.getNeto());
    }

    public void modificarProducto(SelectEvent event) {
        if(this.venta.getStatus()==2) {
            Mensajes.mensajeAlert("El pedido ya esta cerrado, no se puede modificar !!!");
        } else if(this.ventaAsegurada) {
            this.producto = (VentaProducto) event.getObject();
            this.respalda();
        } else {
            Mensajes.mensajeAlert("El pedido esta en modo lectura, no se puede modificar !!!");
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", this.ventaAsegurada);
    }

    public void obtenerDetalle(SelectEvent event) {
        boolean ok=false;
        this.venta = (Venta) event.getObject();
        this.detalle=new ArrayList<VentaProducto>();
        try {
            this.dao=new DAOMovimientos();
            try {
                this.ventaAsegurada=this.dao.asegurarMovtoRelacionado(this.venta.getIdMovto());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
            for(TOMovimientoProducto to: this.dao.obtenerMovimientoDetalle(this.venta.getIdMovto())) {
                this.detalle.add(this.convertirProducto(to));
            }
            this.actualizaTotales();
            ok=true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    private void actualizaTotales() {
        int index;
        double importe;
        ImpuestosProducto nuevo;
        this.venta.setSubTotal(0);
        this.venta.setDescuento(0);
        this.impuestosTotales=new ArrayList<ImpuestosProducto>();
        this.venta.setTotal(0);
        for(VentaProducto prod: this.detalle) {
            this.venta.setSubTotal(this.venta.getSubTotal()+prod.getPrecio()*prod.getCantidadFacturada());
            this.venta.setDescuento(this.venta.getDescuento()+(prod.getPrecio()-prod.getUnitario())*prod.getCantidadFacturada());
            for(ImpuestosProducto impuesto: prod.getImpuestos()) {
                importe=impuesto.getImporte()*prod.getCantidadFacturada();
                if((index=this.impuestosTotales.indexOf(impuesto))==-1) {
                    nuevo=new ImpuestosProducto();
                    nuevo.setAcreditable(impuesto.isAcreditable());
                    nuevo.setAcumulable(impuesto.isAcumulable());
                    nuevo.setAplicable(impuesto.isAplicable());
                    nuevo.setIdImpuesto(impuesto.getIdImpuesto());
                    nuevo.setImporte(importe);
                    nuevo.setImpuesto(impuesto.getImpuesto());
                    nuevo.setModo(impuesto.getModo());
                    nuevo.setValor(impuesto.getValor());
                    this.impuestosTotales.add(nuevo);
                } else {
                    this.impuestosTotales.get(index).setImporte(this.impuestosTotales.get(index).getImporte()+importe);
                }
            }
            this.venta.setTotal(this.venta.getTotal()+(prod.getNeto()*prod.getCantidadFacturada()));
        }
    }
    
    private VentaProducto convertirProducto(TOMovimientoProducto to) throws SQLException {
        VentaProducto prod=new VentaProducto();
        prod.setIdMovto(to.getIdMovto());
        prod.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        prod.setCantidadOrdenada(to.getCantOrdenada());
        prod.setCantidadFacturada(to.getCantFacturada());
        prod.setCantSinCargo(to.getCantSinCargo());
        prod.setSeparados(to.getCantFacturada()+to.getCantSinCargo());
        prod.setPrecio(to.getCosto());
        prod.setDescuento(to.getDesctoProducto1());
        prod.setUnitario(to.getUnitario());
        prod.setImpuestos(this.dao.obtenerImpuestosProducto(to.getIdMovto(), to.getIdProducto()));
        prod.setNeto(prod.getUnitario());
        for(ImpuestosProducto imp: prod.getImpuestos()) {
            prod.setNeto(prod.getNeto()+imp.getImporte());
        }
        return prod;
    }
    
    private TOMovimientoProducto convertirProducto(VentaProducto producto) {
        TOMovimientoProducto to=new TOMovimientoProducto();
        to.setIdMovto(producto.getIdMovto());
        to.setIdProducto(producto.getProducto().getIdProducto());
        to.setCostoOrdenado(0);
        to.setCantOrdenada(producto.getCantidadOrdenada());
        to.setCantFacturada(producto.getCantidadFacturada());
        to.setCantSinCargo(producto.getCantSinCargo());
        to.setCantRecibida(0);
        to.setCosto(producto.getPrecio());
        to.setDesctoProducto1(producto.getDescuento());
        to.setDesctoProducto2(0);
        to.setDesctoConfidencial(0);
        to.setUnitario(producto.getUnitario());
        to.setIdImpuestoGrupo(producto.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
        to.setNeto(producto.getNeto());
        return to;
    }

    public void actualizaProductoSeleccionado() {
        int idx;
        boolean ok=false;
        VentaProducto tmp;
        ArrayList<TOMovimientoProducto> agregados;
        this.producto = new VentaProducto(this.mbBuscar.getProducto());
        if ((idx=this.detalle.indexOf(this.producto)) != -1) {
            this.producto=this.detalle.get(idx);
        } else {
            try {
                this.dao = new DAOMovimientos();
                TOMovimientoProducto to=this.convertirProducto(this.producto);
                agregados=this.dao.grabarMovimientoDetalle(this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen(), to, this.producto.getSeparados(), this.venta.getTienda().getIdImpuestoZona());
                this.producto=this.convertirProducto(to);
                this.detalle.add(this.producto);
                
                for(TOMovimientoProducto p: agregados) {
                    if(p.getIdMovto()==0) {
                        p.setIdMovto(to.getIdMovto());
                        this.detalle.add(this.convertirProducto(p));
                    } else {
                        tmp=new VentaProducto(this.mbBuscar.obtenerProducto(p.getIdProducto()));
                        if((idx=this.detalle.indexOf(tmp)) != -1) {
                            this.detalle.get(idx).setCantSinCargo(p.getCantSinCargo());
                        } else {
                            Mensajes.mensajeAlert("Error de proceso no corresponden tablas con vista !!!");
                        }
                    }
//                    tmp=new VentaProducto(this.mbBuscar.obtenerProducto(p.getIdProducto()));
//                    if((idx=this.detalle.indexOf(tmp)) != -1) {
//                        this.detalle.get(idx).setCantSinCargo(p.getCantSinCargo());
//                    } else {
//                        this.detalle.add(this.convertirProducto(p));
//                    }
                }
                this.actualizaTotales();
                ok=true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okBuscar", ok);
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void buscarProductos(String update) {
        this.mbBuscar.inicializar();
        this.mbBuscar.setUpdate(update);
    }

    public void crearPedido() {
        boolean ok=false;
        this.venta = new Venta(this.mbAlmacenes.getAlmacen(), this.mbTiendas.getTienda(), this.mbFormatos.getFormatoSeleccion(), this.mbClientes.getCliente());
//        this.venta.setIdImpuestoZona(this.mbTiendas.getTienda().getIdImpuestoZona());
        TOMovimiento to=this.convertirTO();
        try {
            this.dao=new DAOMovimientos();
            this.dao.agregarMovimientoRelacionado(to);
            this.venta.setIdMovto(to.getIdMovto());
            this.venta.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
            this.pedidos.add(this.venta);
            ok=true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        }
        this.detalle = new ArrayList<VentaProducto>();
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okNuevoPedido", ok);
    }

    public void nuevoPedido() {
//        this.modoEdicion=true;
        this.mbGrupos.inicializar();
        this.cambioDeGrupo();
    }
    
    public void regresarAlmacenFechaActual() {
        this.fechaInicial = new Date();
//        if (!this.todos) {
            this.obtenerPedidosAlmacen();
//        }
    }

    public void regresarFechaActual() {
        this.fechaInicial = new Date();
//        if (!this.todos) {
            this.obtenerPedidos();
//        }
    }
    
    public void obtenerPedidosAlmacen() {
        try {   // Segun fecha y status
            this.pedidos=new ArrayList<Venta>();
            this.dao=new DAOMovimientos();
            for(TOMovimiento to: this.dao.obtenerMovimientosAlmacenRelacionados(this.mbAlmacenes.getAlmacen().getIdAlmacen(), 28, (this.todos?9999:1), this.fechaInicial)) {
                this.pedidos.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void obtenerPedidos() {
        try {   // Segun fecha y status
            this.pedidos=new ArrayList<Venta>();
            this.dao=new DAOMovimientos();
            for(TOMovimiento to: this.dao.obtenerMovimientosRelacionados(this.mbAlmacenes.getAlmacen().getIdAlmacen(), 28, (this.todos?9999:1), this.fechaInicial)) {
                this.pedidos.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cambioDeFormato() {
        this.mbTiendas.cargaTiendasFormato(this.mbFormatos.getFormatoSeleccion().getIdFormato());
        this.mbTiendas.nuevaTienda();
    }

    public void cambioDeCliente() {
        this.mbFormatos.cargarFormatosCliente(this.mbClientes.getCliente().getIdCliente());
        this.mbFormatos.nuevoFormato();
//        this.mbTiendas.cargaTiendasFormato(this.mbFormatos.getFormatoSeleccion().getIdFormato());
//        this.mbTiendas.nuevaTienda();
        this.cambioDeFormato();
    }

    public void cambioDeGrupo() {
        this.mbClientes.cargarClientesGrupo(this.mbGrupos.getClienteGrupoSeleccionado().getIdGrupoCte());
        this.mbClientes.nuevoCliente();
//        this.mbFormatos.cargarFormatosCliente(0);
//        this.mbFormatos.nuevoFormato();
//        this.mbTiendas.cargaTiendasFormato(0);
//        this.mbTiendas.nuevaTienda();
        this.cambioDeCliente();
    }

    //    public boolean isModoEdicion() {
    //        return modoEdicion;
    //    }
    //
    //    public void setModoEdicion(boolean modoEdicion) {
    //        this.modoEdicion = modoEdicion;
    //    }
    
    public ArrayList<ImpuestosProducto> getImpuestosTotales() {
        return impuestosTotales;
    }

    public void setImpuestosTotales(ArrayList<ImpuestosProducto> impuestosTotales) {
        this.impuestosTotales = impuestosTotales;
    }

    public boolean isTodos() {
        return todos;
    }

    public void setTodos(boolean todos) {
        this.todos = todos;
    }

    public TimeZone getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(TimeZone zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public Date getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public ArrayList<Venta> getPedidos() {
        return pedidos;
    }

    public void setPedidos(ArrayList<Venta> pedidos) {
        this.pedidos = pedidos;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public ArrayList<VentaProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<VentaProducto> detalle) {
        this.detalle = detalle;
    }

    public VentaProducto getProducto() {
        return producto;
    }

    public void setProducto(VentaProducto producto) {
        this.producto = producto;
    }

    public boolean isVentaAsegurada() {
        return ventaAsegurada;
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

    public ArrayList<VentaProducto> getSimilares() {
        return similares;
    }

    public void setSimilares(ArrayList<VentaProducto> similares) {
        this.similares = similares;
    }

    public VentaProducto getSimilar() {
        return similar;
    }

    public void setSimilar(VentaProducto similar) {
        this.similar = similar;
    }

    public double getCantTraspasar() {
        return cantTraspasar;
    }

    public void setCantTraspasar(double cantTraspasar) {
        this.cantTraspasar = cantTraspasar;
    }

    public ArrayList<VentaAlmacenProducto> getAlmacenDetalle() {
        return almacenDetalle;
    }

    public void setAlmacenDetalle(ArrayList<VentaAlmacenProducto> almacenDetalle) {
        this.almacenDetalle = almacenDetalle;
    }

    public ArrayList<VentaAlmacenProducto> getEmpaqueLotes() {
        return empaqueLotes;
    }

    public void setEmpaqueLotes(ArrayList<VentaAlmacenProducto> empaqueLotes) {
        this.empaqueLotes = empaqueLotes;
    }

    public VentaAlmacenProducto getLoteOrigen() {
        return loteOrigen;
    }

    public void setLoteOrigen(VentaAlmacenProducto loteOrigen) {
        this.loteOrigen = loteOrigen;
    }

    public VentaAlmacenProducto getLoteDestino() {
        return loteDestino;
    }

    public void setLoteDestino(VentaAlmacenProducto loteDestino) {
        this.loteDestino = loteDestino;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
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

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public MbMiniAlmacenes getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbMiniAlmacenes mbAlmacenes) {
        this.mbAlmacenes = mbAlmacenes;
    }
}
