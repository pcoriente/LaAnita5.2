package pedidos;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import clientes.MbMiniClientes;
import comprobantes.MbComprobantes;
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
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import mbMenuClientesGrupos.MbClientesGrupos;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import pedidos.dao.DAOPedidos;
import pedidos.dominio.Pedido;
import pedidos.dominio.PedidoProducto;
import pedidos.to.TOPedido;
import pedidos.to.TOProductoPedido;
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
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
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
    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;
    private Pedido pedido;
    private ArrayList<Pedido> pedidos;
    private ArrayList<ImpuestosProducto> impuestosTotales;
    private PedidoProducto producto, similar;
    private ArrayList<PedidoProducto> detalle, similares;
    private double cantTraspasar;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private boolean pendientes;
    private Date fechaInicial;
    private boolean locked;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAOPedidos dao;

    public MbPedidos() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();

        this.mbGrupos = new MbClientesGrupos();
        this.mbClientes = new MbMiniClientes();
        this.mbFormatos = new MbFormatos();
        this.mbTiendas = new MbMiniTiendas();
        this.mbBuscar = new MbProductosBuscar();
        this.mbComprobantes = new MbComprobantes();

        this.inicializa();
    }

//    public void liberarPedido() {
//        boolean ok = false;
//        if (this.pedido == null) {
//            ok = true;    // Para que no haya problema al cerrar despues de eliminar un pedido
//        } else if (this.isLocked()) {
//            TOPedido toPed = this.convertir(this.pedido);
//            try {
//                this.dao = new DAOPedidos();
//                this.dao.liberarPedido(toPed);
//                this.pedido.setPropietario(0);
//                this.setLocked(false);
//            } catch (NamingException ex) {
//                Mensajes.mensajeError(ex.getMessage());
//            } catch (SQLException ex) {
//                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//            } catch (Exception ex) {
//                Mensajes.mensajeAlert(ex.getMessage());
//            }
//        }
//        RequestContext context = RequestContext.getCurrentInstance();
//        context.addCallbackParam("okPedido", ok);
//    }
    public void salir() {
        try {
            this.dao = new DAOPedidos();
            if (this.pedido != null && this.isLocked()) {
                TOPedido toPed = this.convertir(this.pedido);
                this.dao.liberarPedido(toPed);
                this.pedido.setPropietario(0);
                this.setLocked(false);
            }
            this.pedidos = new ArrayList<>();
            for (TOPedido to : this.dao.obtenerPedidos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.pendientes ? 0 : 5, this.fechaInicial)) {
                this.pedidos.add(this.convertir(to));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void cancelarPedido() {
        boolean ok = false;
        if (this.pedido.getCanceladoMotivo().isEmpty()) {
            Mensajes.mensajeAlert("Se requiere el motivo de cancelacion !!!");
        } else {
            TOPedido toPed = this.convertir(this.pedido);
            try {
                this.dao = new DAOPedidos();
                this.dao.cancelarPedido(toPed);
                this.pedido.setIdUsuario(toPed.getIdUsuario());
                this.pedido.setPropietario(toPed.getPropietario());
                this.pedido.setEstatus(toPed.getEstatus());
                this.setLocked(this.pedido.getIdUsuario() == this.pedido.getPropietario());
                ok = true;
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    public void cerrarPedido() {
        boolean ok = false;
        TOPedido toPed = this.convertir(this.pedido);
        try {
            this.dao = new DAOPedidos();
            this.dao.cerrarPedido(toPed);
            this.pedido.setIdUsuario(toPed.getIdUsuario());
            this.pedido.setPropietario(toPed.getPropietario());
            this.pedido.setEstatus(toPed.getEstatus());
            this.setLocked(this.pedido.getIdUsuario() == this.pedido.getPropietario());
            Mensajes.mensajeSucces("El pedido se cerro correctamente !!!");
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    public void eliminarPedido() {
        boolean ok = false;
        TOPedido toPed = this.convertir(this.pedido);
        try {
            this.dao = new DAOPedidos();
            this.dao.eliminarPedido(toPed);
            this.pedidos.remove(this.pedido);
            this.pedido = null;
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    public void actualizaTraspasoSimilar() {
//        int idx;
//        PedidoProducto prod;
//        PedidoProducto prodOld;
        boolean okSimilares = false;
        try {
            if (this.cantTraspasar > 0 && this.cantTraspasar <= this.producto.getCantSinCargo()) {
                TOPedido toPed = this.convertir(this.pedido);
                TOProductoPedido toProd = this.convertir(this.producto);
                TOProductoPedido toSimilar = this.convertir(this.similar);

                this.dao = new DAOPedidos();
//                for (TOProductoPedido to : this.dao.trasferirSinCargo(toPed, toProd, toSimilar, this.cantTraspasar)) {
//                    prod = this.convertir(to);
//                    if ((idx = this.detalle.indexOf(prod)) != -1) {
//                        prodOld = this.detalle.get(idx);
//                        this.totalResta(prodOld);
//                        this.detalle.set(idx, prod);
//                    } else {
//                        this.detalle.add(prod);
//                    }
//                    this.totalSuma(prod);
//                }
                this.procesaSimilares(this.dao.trasferirSinCargo(toPed, toProd, toSimilar, this.cantTraspasar));
                okSimilares = true;
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okSimilares", okSimilares);
    }

    public void traspasoSimilar() {
        boolean ok = false;
        this.similares = new ArrayList<>();
        try {
            this.similar = null;
            this.cantTraspasar = 0;
            this.dao = new DAOPedidos();
            for (TOProductoPedido to : this.dao.obtenerSimilares(this.producto.getIdMovto(), this.producto.getProducto().getIdProducto())) {
                this.similares.add(this.convertir(to));
            }
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okSimilares", ok);
    }

    private void totalResta(PedidoProducto prod) {
        int index;
        ImpuestosProducto nuevo;
        movimientos.Movimientos.restaTotales(prod, this.pedido);
        for (ImpuestosProducto impuesto : prod.getImpuestos()) {
            if ((index = this.impuestosTotales.indexOf(impuesto)) == -1) {
                nuevo = new ImpuestosProducto();
                nuevo.setAcreditable(impuesto.isAcreditable());
                nuevo.setAcumulable(impuesto.isAcumulable());
                nuevo.setAplicable(impuesto.isAplicable());
                nuevo.setIdImpuesto(impuesto.getIdImpuesto());
                nuevo.setImporte(impuesto.getImporte() * prod.getCantOrdenada());
                nuevo.setImpuesto(impuesto.getImpuesto());
                nuevo.setModo(impuesto.getModo());
                nuevo.setValor(impuesto.getValor());
                this.impuestosTotales.add(nuevo);
            } else {
                this.impuestosTotales.get(index).setImporte(this.impuestosTotales.get(index).getImporte() - impuesto.getImporte() * prod.getCantOrdenada());
            }
        }
    }

    public void actualizaProducto() {
        boolean ok = false;
        try {
            if (this.producto.getCantOrdenada() != this.producto.getCantFacturada()) {
                TOPedido toPed = this.convertir(this.pedido);
                TOProductoPedido toProd = this.convertir(this.producto);
                this.producto.setCantOrdenada(this.producto.getCantFacturada());

                this.dao = new DAOPedidos();
                ArrayList<TOProductoPedido> listaSimilares = this.dao.grabarProductoCantidad(toPed, toProd);
                if (listaSimilares.size() <= 1) {
                    this.totalResta(this.producto);
                    this.producto.setCantOrdenada(toProd.getCantOrdenada());
                    this.producto.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
                    this.producto.setCantFacturada(this.producto.getCantOrdenada());
                    this.producto.setCantSinCargo(this.producto.getCantOrdenadaSinCargo());
                    this.totalSuma(this.producto);
                } else {
//                    int idx;
//                    PedidoProducto prod;
//                    for (TOProductoPedido to : listaSimilaresZZ) {
//                        prod = this.convertir(to);
//                        if ((idx = this.detalle.indexOf(prod)) != -1) {
//                            if (prod.equals(this.producto)) {
//                                this.setProducto(prod);
//                            }
//                            this.totalResta(this.detalle.get(idx));
//                            this.detalle.set(idx, prod);
//                        } else {
//                            this.detalle.add(prod);
//                        }
//                        this.totalSuma(prod);
//                    }
                    this.procesaSimilares(listaSimilares);
                }
            }
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }

    public void modificarProducto(SelectEvent event) {
        boolean ok = false;
        if (this.pedido.getEstatus() != 0) {
            Mensajes.mensajeAlert("El pedido ya esta cerrado, no se puede modificar !!!");
        } else if (this.isLocked()) {
            this.producto = (PedidoProducto) event.getObject();
            ok = true;
        } else {
            Mensajes.mensajeAlert("El pedido esta en modo lectura, no se puede modificar !!!");
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }

    private TOProductoPedido convertir(PedidoProducto p) {
        TOProductoPedido to = new TOProductoPedido();
        to.setIdPedido(p.getIdPedido());
        to.setIdMovto(p.getIdMovto());
        to.setIdProducto(p.getProducto().getIdProducto());
        to.setCantOrdenada(p.getCantOrdenada());
        to.setCantOrdenadaSinCargo(p.getCantOrdenadaSinCargo());
//        to.setSimilar(p.isSimilar());
        to.setCantFacturada(p.getCantFacturada());
        to.setCantSinCargo(p.getCantSinCargo());
        to.setCosto(p.getCosto());
        to.setDesctoProducto1(p.getDesctoProducto1());
        to.setUnitario(p.getUnitario());
        to.setIdImpuestoGrupo(p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
        return to;
    }

    public void actualizaProductoSeleccionado() {
        int idx;
        boolean ok = false;
        this.producto = new PedidoProducto(this.mbBuscar.getProducto());
        if ((idx = this.detalle.indexOf(this.producto)) != -1) {
            this.producto = this.detalle.get(idx);
        } else {
            this.producto.setIdMovto(this.pedido.getIdMovto());
            this.producto.setIdPedido(this.pedido.getIdPedido());
            try {
                TOPedido toPed = this.convertir(this.pedido);
                TOProductoPedido toProd = this.convertir(this.producto);

                this.dao = new DAOPedidos();
                this.dao.agregarProductoPedido(toPed, toProd);
                this.producto = this.convertir(toProd);
                this.detalle.add(this.producto);
                ok = true;
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

    private void totalSuma(PedidoProducto prod) {
        int index;
        ImpuestosProducto nuevo;
        movimientos.Movimientos.sumaTotales(prod, this.pedido);
        for (ImpuestosProducto impuesto : prod.getImpuestos()) {
            if ((index = this.impuestosTotales.indexOf(impuesto)) == -1) {
                nuevo = new ImpuestosProducto();
                nuevo.setAcreditable(impuesto.isAcreditable());
                nuevo.setAcumulable(impuesto.isAcumulable());
                nuevo.setAplicable(impuesto.isAplicable());
                nuevo.setIdImpuesto(impuesto.getIdImpuesto());
                nuevo.setImporte(impuesto.getImporte() * prod.getCantOrdenada());
                nuevo.setImpuesto(impuesto.getImpuesto());
                nuevo.setModo(impuesto.getModo());
                nuevo.setValor(impuesto.getValor());
                this.impuestosTotales.add(nuevo);
            } else {
                this.impuestosTotales.get(index).setImporte(this.impuestosTotales.get(index).getImporte() + impuesto.getImporte() * prod.getCantOrdenada());
            }
        }
    }

    private PedidoProducto convertir(TOProductoPedido toProd) throws SQLException {
        PedidoProducto prod = new PedidoProducto();
        prod.setIdPedido(toProd.getIdPedido());
        prod.setCantOrdenada(toProd.getCantOrdenada());
        prod.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
//        prod.setSimilar(toProd.isSimilar());
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setCantFacturada(prod.getCantOrdenada());
        prod.setCantSinCargo(prod.getCantOrdenadaSinCargo());
        prod.setNeto(prod.getUnitario() + this.dao.obtenerImpuestosProducto(toProd.getIdMovto(), toProd.getIdProducto(), prod.getImpuestos()));
        return prod;
    }

    public void obtenerDetalle(SelectEvent event) {
        this.pedido = (Pedido) event.getObject();

        boolean ok = false;
        PedidoProducto prod;
        this.pedido.setSubTotal(0);
        this.pedido.setDescuento(0);
        this.pedido.setImpuesto(0);
        this.pedido.setTotal(0);
        this.detalle = new ArrayList<>();
        this.impuestosTotales = new ArrayList<>();
        TOPedido toPed = this.convertir(this.pedido);
        try {
            this.dao = new DAOPedidos();
            for (TOProductoPedido toProd : this.dao.obtenerDetalle(toPed)) {
                prod = this.convertir(toProd);
                this.totalSuma(prod);
                this.detalle.add(prod);
            }
            this.pedido.setIdUsuario(toPed.getIdUsuario());
            this.pedido.setPropietario(toPed.getPropietario());
            this.pedido.setEstatus(toPed.getEstatus());
            this.setLocked(this.pedido.getIdUsuario() == this.pedido.getPropietario());
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    private TOPedido convertir(Pedido pedido) {
        TOPedido toMov = new TOPedido();
        toMov.setIdPedidoOC(pedido.getIdPedidoOC());
        toMov.setIdMoneda(pedido.getComprobante().getMoneda().getIdMoneda());
        toMov.setOrdenDeCompra(pedido.getOrdenDeCompra());
        toMov.setOrdenDeCompraFecha(pedido.getOrdenDeCompraFecha());
        toMov.setCanceladoMotivo(pedido.getCanceladoMotivo());
        toMov.setCanceladoFecha(pedido.getCanceladoFecha());
        movimientos.Movimientos.convertir(pedido, toMov);
        toMov.setIdComprobante(pedido.getComprobante().getIdComprobante());
        toMov.setIdImpuestoZona(pedido.getTienda().getIdImpuestoZona());
        toMov.setIdReferencia(pedido.getTienda().getIdTienda());
        toMov.setReferencia(pedido.getIdPedido());
        return toMov;
    }

    private boolean validar() {
        boolean ok = false;
        if (this.mbAlmacenes.getToAlmacen() == null) {
            Mensajes.mensajeAlert("Debe seleccionar un almacen !!!");
        } else if (this.mbTiendas.getTienda() == null) {
            Mensajes.mensajeAlert("Debe seleccionar una tienda !!!");
        } else if (this.mbComprobantes.getMbMonedas().getSeleccionMoneda() == null) {
            Mensajes.mensajeAlert("Debe seleccionar una moneda !!!");
        } else {
            ok = true;
        }
        return ok;
    }

    public void crearPedido() {
        boolean ok = false;
        if (this.validar()) {
            this.pedido = new Pedido(this.mbAlmacenes.getToAlmacen(), this.mbTiendas.getTienda(), this.mbComprobantes.getMbMonedas().getSeleccionMoneda());
            this.pedido.setDesctoComercial(this.mbClientes.getCliente().getDesctoComercial());
            this.pedido.setOrdenDeCompra(this.ordenDeCompra);
            this.pedido.setOrdenDeCompraFecha(this.ordenDeCompraFecha);
            TOPedido toPed = this.convertir(this.pedido);
            try {
                this.dao = new DAOPedidos();
                this.dao.agregarPedido(toPed);
                this.pedido.setIdPedido(toPed.getReferencia());
                this.pedido.setIdMovto(toPed.getIdMovto());
                this.pedido.setIdMovtoAlmacen(toPed.getIdMovtoAlmacen());
                this.pedido.setIdPedidoOC(toPed.getIdPedidoOC());
                this.pedido.setFecha(toPed.getFecha());
                this.pedido.setIdUsuario(toPed.getIdUsuario());
                this.pedido.setPropietario(toPed.getPropietario());
                this.pedido.setEstatus(toPed.getEstatus());
                this.setLocked(this.pedido.getIdUsuario() == this.pedido.getPropietario());
                this.detalle = new ArrayList<>();
                this.setProducto(null);
//                this.pedidos.add(this.pedido);
                ok = true;
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
            this.detalle = new ArrayList<>();
            this.impuestosTotales = new ArrayList<>();
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    public void cambioDeFormato() {
        this.mbTiendas.obtenerTiendasFormato(this.mbFormatos.getFormatoSeleccion().getIdFormato());
        this.mbTiendas.nuevaTienda();
    }

    public void cambioDeGrupo() {
        this.mbFormatos.cargarFormatosCliente(this.mbGrupos.getClienteGrupoSeleccionado().getIdGrupoCte());
        this.mbTiendas.inicializar();
    }

    public void cambioDeCliente() {
        this.mbTiendas.obtenerTiendasCliente(this.mbClientes.getCliente().getIdCliente());
        this.mbTiendas.nuevaTienda();
    }

    public void nuevoPedido() {
        this.mbClientes.obtenerClientesCedis();
        this.mbClientes.nuevoCliente();
        this.mbTiendas.obtenerTiendasCliente(this.mbClientes.getCliente().getIdCliente());
        this.mbTiendas.nuevaTienda();
        this.ordenDeCompra = "";
        this.ordenDeCompraFecha = new Date();
    }

    private Pedido convertir(TOPedido to) {
        Pedido p = new Pedido(this.mbAlmacenes.obtenerAlmacen(to.getIdAlmacen()), this.mbTiendas.obtenerTienda(to.getIdReferencia()), this.mbComprobantes.obtenerComprobante(to.getIdComprobante()));
        p.setIdPedidoOC(to.getIdPedidoOC());
        p.setOrdenDeCompra(to.getOrdenDeCompra());
        p.setOrdenDeCompraFecha(to.getOrdenDeCompraFecha());
        p.setCanceladoMotivo(to.getCanceladoMotivo());
        p.setCanceladoFecha(to.getCanceladoFecha());
        movimientos.Movimientos.convertir(to, p);
        this.mbClientes.setCliente(this.mbClientes.obtenerCliente(p.getTienda().getIdCliente()));
        // Si el pedido todavia esta pendiente, se actualiza con datos del cliente
        // Si ya esta cerrada, se queda con lo que se leyo de la base
        if (to.getEstatus() == 0) {
            p.setDesctoComercial(this.mbClientes.getCliente().getDesctoComercial());
            p.setDesctoProntoPago(0);
        }
        p.setIdPedido(to.getReferencia());
        return p;
    }

    public void obtenerPedidos() {
        try {   // Segun fecha y status
            this.pedidos = new ArrayList<>();
            this.dao = new DAOPedidos();
            for (TOPedido to : this.dao.obtenerPedidos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.pendientes ? 0 : 5, this.fechaInicial)) {
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
        this.mbAlmacenes.inicializaAlmacen();

        this.mbGrupos.inicializar();
        this.mbClientes.inicializar();
        this.mbFormatos.inicializar();
        this.mbBuscar.inicializar();
        this.mbComprobantes.getMbMonedas().setListaMonedas(null);

        this.pendientes = true;
        this.fechaInicial = new Date();
        this.pedidos = new ArrayList<>();
        this.pedido = new Pedido();
        this.detalle = new ArrayList<>();
    }

    private void procesaSimilares(ArrayList<TOProductoPedido> listaSimilares) throws SQLException {
        int idx;
        PedidoProducto prod;
        for (TOProductoPedido to : listaSimilares) {
            prod = this.convertir(to);
            if ((idx = this.detalle.indexOf(prod)) != -1) {
                this.totalResta(this.detalle.get(idx));
                this.detalle.set(idx, prod);
            } else {
                this.detalle.add(prod);
            }
            this.totalSuma(prod);
        }
    }

    public double getCantTraspasar() {
        return cantTraspasar;
    }

    public void setCantTraspasar(double cantTraspasar) {
        this.cantTraspasar = cantTraspasar;
    }

    public ArrayList<PedidoProducto> getSimilares() {
        return similares;
    }

    public void setSimilares(ArrayList<PedidoProducto> similares) {
        this.similares = similares;
    }

    public PedidoProducto getSimilar() {
        return similar;
    }

    public void setSimilar(PedidoProducto similar) {
        this.similar = similar;
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

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
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

    public ArrayList<ImpuestosProducto> getImpuestosTotales() {
        return impuestosTotales;
    }

    public void setImpuestosTotales(ArrayList<ImpuestosProducto> impuestosTotales) {
        this.impuestosTotales = impuestosTotales;
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

    public boolean isPendientes() {
        return pendientes;
    }

    public void setPendientes(boolean pendientes) {
        this.pendientes = pendientes;
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

    public MbComprobantes getMbComprobantes() {
        return mbComprobantes;
    }

    public void setMbComprobantes(MbComprobantes mbComprobantes) {
        this.mbComprobantes = mbComprobantes;
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

    public MbAlmacenesJS getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbAlmacenesJS mbAlmacenes) {
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
