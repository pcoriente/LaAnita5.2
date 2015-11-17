package pedidos;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import clientes.MbMiniClientes;
import formatos.MbFormatos;
import impuestos.dominio.ImpuestosProducto;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import mbMenuClientesGrupos.MbClientesGrupos;
import monedas.MbMonedas;
import movimientos.dominio.MovimientoTipo;
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
    @ManagedProperty(value = "#{mbMonedas}")
    private MbMonedas mbMonedas;
    private Pedido pedido;
    private ArrayList<Pedido> pedidos;
    private ArrayList<ImpuestosProducto> impuestosTotales;
    private ArrayList<PedidoProducto> detalle, similares;
    private PedidoProducto producto;
    private PedidoProducto similar;
    private double cantTraspasar;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private boolean asegurado;
//    private DAOMovimientosOld daoMv;
    private DAOPedidos dao;
    private boolean pendientes;
    private Date fechaInicial;
    private TimeZone zonaHoraria = TimeZone.getDefault();

    public MbPedidos() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();

        this.mbGrupos = new MbClientesGrupos();
        this.mbClientes = new MbMiniClientes();
        this.mbFormatos = new MbFormatos();
        this.mbTiendas = new MbMiniTiendas();
        this.mbBuscar = new MbProductosBuscar();
        this.mbMonedas = new MbMonedas();

        this.inicializa();
    }

    public void cancelarPedido() {
        boolean ok = false;
        if (this.pedido.getCancelacionMotivo().isEmpty()) {
            Mensajes.mensajeAlert("Se requiere el motivo de cancelacion !!!");
        } else {
            TOPedido toPed = this.convertir(this.pedido);
            try {
                this.dao = new DAOPedidos();
                this.dao.cancelarPedido(toPed);
                this.pedidos.remove(this.pedido);
                this.pedido = null;
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

    public void eliminarPedido() {
        boolean ok = false;
        TOPedido toPed = this.convertir(this.pedido);
        try {
            this.dao = new DAOPedidos();
            this.dao.eliminarPedido(toPed);
            this.pedidos.remove(this.pedido);
            this.asegurado = false;
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

    public void cerrarPedido() {
        boolean ok = false;
        TOPedido toPed = this.convertir(this.pedido);
        try {
            this.dao = new DAOPedidos();
            this.dao.cerrarPedido(toPed);
            this.pedidos.remove(this.pedido);
            this.pedido = null;
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

    private TOProductoPedido convertir(PedidoProducto p) {
        TOProductoPedido to = new TOProductoPedido();
        to.setIdPedido(p.getIdPedido());
        to.setIdMovto(p.getIdMovto());
        to.setIdProducto(p.getProducto().getIdProducto());
        to.setCantOrdenada(p.getCantOrdenada());
        to.setCantOrdenadaSinCargo(p.getCantOrdenadaSinCargo());
        to.setCantFacturada(p.getCantFacturada());
        to.setCantSinCargo(p.getCantSinCargo());
        to.setCosto(p.getCosto());
        to.setDesctoProducto1(p.getDesctoProducto1());
        to.setUnitario(p.getUnitario());
        to.setIdImpuestoGrupo(p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
        return to;
    }

    public void actualizaProducto() {
        boolean ok = false;
//        double cantOrdenadaOld = this.producto.getCantOrdenadaTotal() - this.producto.getCantOrdenadaSinCargo();
        try {
//            if (this.producto.getCantOrdenada() != cantOrdenadaOld) {
            if (this.producto.getCantOrdenada() != this.producto.getCantFacturada()) {
                TOPedido toPed = this.convertir(this.pedido);
                TOProductoPedido toProd = this.convertir(this.producto);
//                this.producto.setCantOrdenada(cantOrdenadaOld);
                this.producto.setCantOrdenada(this.producto.getCantFacturada());

                this.dao = new DAOPedidos();
                this.dao.grabarPedidoDetalle(toPed, toProd);
                this.totalResta(this.producto);

                this.producto.setCantOrdenada(toProd.getCantOrdenada());
                this.producto.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
//                this.producto.setCantOrdenadaTotal(toProd.getCantOrdenada() + toProd.getCantOrdenadaSinCargo());
                this.producto.setCantFacturada(this.producto.getCantOrdenada());
                this.producto.setCantSinCargo(this.producto.getCantOrdenadaSinCargo());
                this.totalSuma(this.producto);
            }
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
//        if (!ok) {
//            this.producto.setCantFacturada(cantOrdenadaOld);
//        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }

    public void modificarProducto(SelectEvent event) {
        boolean ok = false;
        if (this.pedido.getEstatus() != 0) {
            Mensajes.mensajeAlert("El pedido ya esta cerrado, no se puede modificar !!!");
        } else if (this.asegurado) {
            this.producto = (PedidoProducto) event.getObject();
            ok = true;
        } else {
            Mensajes.mensajeAlert("El pedido esta en modo lectura, no se puede modificar !!!");
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }

    public void liberarPedido() {
        boolean ok = false;
        if (this.pedido == null) {
            ok = true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else if (this.asegurado) {
            try {
                this.dao = new DAOPedidos();
                ok = this.dao.liberarPedido(this.pedido.getIdMovto());
                this.asegurado = false;
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
//                TOProductoPedido toProd = new TOProductoPedido();
//                toProd.setIdMovto(this.pedido.getIdMovto());
//                toProd.setIdPedido(this.pedido.getIdPedido());
//                toProd.setIdProducto(this.producto.getProducto().getIdProducto());
//                toProd.setIdImpuestoGrupo(this.producto.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
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
//        prod.setCantOrdenadaTotal(toProd.getCantOrdenada() + toProd.getCantOrdenadaSinCargo());
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
            for (TOProductoPedido toProd : this.dao.obtenerPedidoDetalle(toPed)) {
                prod = this.convertir(toProd);
                this.totalSuma(prod);
                this.detalle.add(prod);
            }
            this.pedido.setIdUsuario(toPed.getIdUsuario());
            this.pedido.setPropietario(toPed.getPropietario());
            this.pedido.setEstatus(toPed.getEstatus());
            this.asegurado=false;
            if (this.pedido.getIdUsuario() == this.pedido.getPropietario()) {
                this.asegurado = true;
            }
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    private TOPedido convertir(Pedido p) {
        TOPedido to = new TOPedido();
        to.setIdMoneda(p.getMoneda().getIdMoneda());
        to.setIdPedidoOC(p.getIdPedidoOC());
        to.setOrdenDeCompra(p.getOrdenDeCompra());
        to.setOrdenDeCompraFecha(p.getOrdenDeCompraFecha());
        to.setCanceladoMotivo(p.getCancelacionMotivo());
        to.setCanceladoFecha(p.getCancelacionFecha());
        movimientos.Movimientos.convertir(p, to);
        to.setIdImpuestoZona(p.getTienda().getIdImpuestoZona());
        to.setIdReferencia(p.getTienda().getIdTienda());
        to.setReferencia(p.getIdPedido());
        return to;
    }

    private boolean validar() {
        boolean ok = false;
        if (this.mbAlmacenes.getToAlmacen() == null) {
            Mensajes.mensajeAlert("Debe seleccionar un almacen !!!");
        } else if (this.mbTiendas.getTienda() == null) {
            Mensajes.mensajeAlert("Debe seleccionar una tienda !!!");
        } else if (this.mbMonedas.getSeleccionMoneda() == null) {
            Mensajes.mensajeAlert("Debe seleccionar una moneda !!!");
        } else {
            ok = true;
        }
        return ok;
    }

    public void crearPedido() {
        boolean ok = false;
        if (this. validar()) {
            this.pedido = new Pedido(new MovimientoTipo(28, "Pedido"), this.mbAlmacenes.getToAlmacen(), this.mbTiendas.getTienda());
            this.pedido.setMoneda(this.mbMonedas.getSeleccionMoneda());
            this.pedido.setDesctoComercial(this.mbClientes.getCliente().getDesctoComercial());
            this.pedido.setOrdenDeCompra(this.ordenDeCompra);
            this.pedido.setOrdenDeCompraFecha(this.ordenDeCompraFecha);
            TOPedido to = this.convertir(this.pedido);
            try {
                this.dao = new DAOPedidos();
                this.dao.agregarPedido(to);
                this.pedido.setIdPedido(to.getReferencia());
                this.pedido.setIdMovto(to.getIdMovto());
                this.pedido.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
                this.pedido.setIdPedidoOC(to.getIdPedidoOC());
                this.pedido.setFecha(to.getFecha());
                this.pedidos.add(this.pedido);
                this.asegurado = true;
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

    public void cambioDeCliente() {
    }

    public void cambioDeGrupo() {
        this.mbFormatos.cargarFormatosCliente(this.mbGrupos.getClienteGrupoSeleccionado().getIdGrupoCte());
        this.mbTiendas.inicializar();
    }

    public void nuevoPedido() {
        this.mbGrupos.inicializar();
        this.cambioDeGrupo();
        this.ordenDeCompra = "";
        this.ordenDeCompraFecha = new Date();
    }

    private Pedido convertir(TOPedido to) {
        Pedido p = new Pedido();
        p.setMoneda(this.mbMonedas.obtenerMoneda(to.getIdMoneda()));
        p.setIdPedidoOC(to.getIdPedidoOC());
        p.setOrdenDeCompra(to.getOrdenDeCompra());
        p.setOrdenDeCompraFecha(to.getOrdenDeCompraFecha());
        p.setCancelacionFecha(to.getCanceladoFecha());
        p.setCancelacionMotivo(to.getCanceladoMotivo());

        p.setAlmacen(this.mbAlmacenes.getToAlmacen());
        movimientos.Movimientos.convertir(to, p);
        p.setTienda(this.mbTiendas.obtenerTienda(to.getIdReferencia()));
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
            for (TOPedido to : this.dao.obtenerPedidos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), (this.pendientes ? 0 : 5), this.fechaInicial)) {
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
        this.mbBuscar.inicializar();
        this.mbMonedas.setListaMonedas(null);

        this.pendientes = true;
        this.fechaInicial = new Date();
        this.pedidos = new ArrayList<>();
        this.pedido = new Pedido();
        this.detalle = new ArrayList<>();
    }

//    vvvvvvvvvvvvvvvvvvvvvvvvvvvv NO SE USAN vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    public void traspasoSimilar() {
        boolean ok = false;
        this.similares = new ArrayList<>();
        try {
            this.dao = new DAOPedidos();
            for (TOProductoPedido to : dao.obtenerSimilaresPedido(this.producto.getIdPedido(), this.producto.getProducto().getIdProducto())) {
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

    public void actualizaTraspasoSimilar() {
        int idx;
        PedidoProducto prod;
        boolean okSimilares = false;
        try {
            if (this.cantTraspasar > 0 && this.cantTraspasar <= this.producto.getCantSinCargo()) {
                TOProductoPedido toProd = this.convertir(this.producto);
                TOProductoPedido toSimilar = this.convertir(this.similar);

                this.dao = new DAOPedidos();
                this.dao.trasferirSinCargo(toProd, toSimilar, this.pedido.getTienda().getIdImpuestoZona(), this.cantTraspasar);
                for (TOProductoPedido to : this.dao.obtenerPedidoSimilares(toProd.getIdPedido(), toProd.getIdProducto())) {
                    prod = this.convertir(to);
                    if ((idx = this.detalle.indexOf(prod)) != -1) {
                        this.detalle.set(idx, prod);
                    } else {
                        this.detalle.add(prod);
                    }
                }
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
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ NO SE USAN ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

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

    public MbMonedas getMbMonedas() {
        return mbMonedas;
    }

    public void setMbMonedas(MbMonedas mbMonedas) {
        this.mbMonedas = mbMonedas;
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
