package pedidos;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import clientes.MbMiniClientes;
import comprobantes.MbComprobantes;
import comprobantes.dominio.Comprobante;
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
    private PedidoProducto producto, similar;
    private ArrayList<PedidoProducto> detalle, similares;
    private ArrayList<ImpuestosProducto> impuestosTotales;
    private double cantTraspasar;
//    private String ordenDeCompra;
//    private Date ordenDeCompraFecha;
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
            for (TOPedido to : this.dao.obtenerPedidos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.pendientes ? 0 : 5, this.fechaInicial, false)) {
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
            Mensajes.mensajeAlert("Se requiere el motivo de cancelación !!!");
        } else {
            TOPedido toPed = this.convertir(this.pedido);
            try {
                this.dao = new DAOPedidos();
                this.dao.cancelarPedido(toPed);
                this.pedido.setPedidoIdUsuario(toPed.getPedidoIdUsuario());
                this.pedido.setIdUsuario(toPed.getIdUsuario());
                this.pedido.setPropietario(toPed.getPropietario());
                this.pedido.setPedidoEstatus(toPed.getPedidoEstatus());
                this.pedido.setEstatus(toPed.getEstatus());
                this.setLocked(this.pedido.getIdUsuario() == this.pedido.getPropietario());
                Mensajes.mensajeSucces("El pedido se canceló correctamente !!!");
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

    public double sumaPiezas() {
        double piezas = 0;
        for (PedidoProducto p : this.detalle) {
            piezas += (p.getCantFacturada() + p.getCantSinCargo());
        }
        return piezas;
    }

    public void cerrarPedido() {
        boolean ok = false;
        try {
            if (this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else if (this.sumaPiezas() == 0) {
                Mensajes.mensajeAlert("No hay cantidades capturadas !!!");
            } else {
                TOPedido toPed = this.convertir(this.pedido);

                this.dao = new DAOPedidos();
                this.dao.cerrarPedido(toPed);
                this.pedido.setPedidoIdUsuario(toPed.getPedidoIdUsuario());
                this.pedido.setIdUsuario(toPed.getIdUsuario());
                this.pedido.setPropietario(toPed.getPropietario());
                this.pedido.setPedidoEstatus(toPed.getPedidoEstatus());
                this.pedido.setEstatus(toPed.getEstatus());
                this.setLocked(this.pedido.getIdUsuario() == this.pedido.getPropietario());
                Mensajes.mensajeSucces("El pedido se cerró correctamente !!!");
                ok = true;
            }
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

    public void eliminarProducto() {
        boolean ok = false;
        TOPedido toPed = this.convertir(this.pedido);
        TOPedidoProducto toProd = Pedidos.convertir(this.producto);
        try {
            this.dao = new DAOPedidos();
            ArrayList<TOPedidoProducto> detalleSimilares = this.dao.eliminarProductoPedido(toPed, toProd);
            this.detalle.remove(this.producto);
            this.totalResta(this.producto);
            if (!detalleSimilares.isEmpty()) {
                this.procesaSimilares(detalleSimilares);
            }
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okProductoEliminar", ok);
    }

    public void actualizaTraspasoSimilar() {
        boolean okSimilares = false;
        try {
            if (this.cantTraspasar < 0) {
                Mensajes.mensajeAlert("La cantidad a traspasar debe ser mayor que cero !!!");
            } else if(this.cantTraspasar > this.producto.getCajasSinCargo()) {
                Mensajes.mensajeAlert("La cantidad a traspasar no puede ser mayor que la cantidad sin cargo !!!");
            } else if(this.cantTraspasar != 0) {
                TOPedido toPed = this.convertir(this.pedido);
                TOPedidoProducto toProd = Pedidos.convertir(this.producto);
                TOPedidoProducto toSimilar = Pedidos.convertir(this.similar);

                this.dao = new DAOPedidos();
                this.procesaSimilares(this.dao.transferirSinCargo(toPed, toProd, toSimilar, this.cantTraspasar * this.producto.getProducto().getPiezas()));
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
            for (TOPedidoProducto to : this.dao.obtenerSimilares(this.producto.getIdMovto(), this.producto.getProducto().getIdProducto(), this.producto.getProducto().getPiezas())) {
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

    public void actualizaProductoSinCargo() {
        this.producto.setCantOrdenadaSinCargo(this.producto.getCajasSinCargo() * this.producto.getProducto().getPiezas());
        TOPedidoProducto toProd = Pedidos.convertir(this.producto);
        this.producto.setCantOrdenadaSinCargo(this.producto.getCantSinCargo());
        this.producto.setCajasSinCargo(this.producto.getCantSinCargo() / this.producto.getProducto().getPiezas());
        if (toProd.getCantOrdenadaSinCargo() < 0) {
            Mensajes.mensajeAlert("La cantidad sin cargo no debe ser menor que cero !!!");
        } else if (toProd.getCantOrdenadaSinCargo() != toProd.getCantSinCargo()) {
            try {
                TOPedido toPed = this.convertir(this.pedido);

                this.dao = new DAOPedidos();
                this.dao.grabarProductoCantidadSinCargo(toPed, toProd);

                this.producto.setCantSinCargo(toProd.getCantOrdenadaSinCargo());
                this.producto.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
                this.producto.setCajasSinCargo(toProd.getCantOrdenadaSinCargo() / this.producto.getProducto().getPiezas());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

//    public void actualizaProducto() {
//        boolean ok = false;
//        this.producto.setCantOrdenada(this.producto.getCajas() * this.producto.getProducto().getPiezas());
//        TOProductoPedido toProd = this.convertir(this.producto);
//        this.producto.setCantOrdenada(this.producto.getCantFacturada());
//        this.producto.setCajas(this.producto.getCantFacturada() / this.producto.getProducto().getPiezas());
//        try {
//            if (toProd.getCantOrdenada() < 0) {
//                Mensajes.mensajeAlert("La cantidad no debe ser menor que cero !!!");
//            } else if (toProd.getCantOrdenada() != toProd.getCantFacturada()) {
//                TOPedido toPed = this.convertir(this.pedido);
//
//                this.dao = new DAOPedidos();
//                ArrayList<TOProductoPedido> listaSimilares = this.dao.grabarProductoCantidad(toPed, toProd);
//                this.totalResta(this.producto);
//                this.producto.setCantOrdenada(toProd.getCantOrdenada());
//                this.producto.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
//                this.producto.setCantFacturada(toProd.getCantOrdenada());
//                this.producto.setCantSinCargo(toProd.getCantOrdenadaSinCargo());
//                this.producto.setCajas(this.producto.getCantOrdenada() / this.producto.getProducto().getPiezas());
//                this.producto.setCajasSinCargo(this.producto.getCantOrdenadaSinCargo() / this.producto.getProducto().getPiezas());
//                this.totalSuma(this.producto);
//                if (!listaSimilares.isEmpty()) {
//                    this.procesaSimilares(listaSimilares);
//                }
//                ok = true;
//            }
//        } catch (NamingException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//        }
//        RequestContext context = RequestContext.getCurrentInstance();
//        context.addCallbackParam("okEdicion", ok);
//    }
//
    public void actualizaProducto() {
        boolean ok = false;
        this.producto.setCantOrdenada(this.producto.getCajas() * this.producto.getProducto().getPiezas());
        TOPedidoProducto toProd = Pedidos.convertir(this.producto);
        this.producto.setCantOrdenada(this.producto.getCantFacturada());
        this.producto.setCajas(this.producto.getCantFacturada() / this.producto.getProducto().getPiezas());
        try {
            if (toProd.getCantOrdenada() < 0) {
                Mensajes.mensajeAlert("La cantidad no debe ser menor que cero !!!");
            } else if (toProd.getCantOrdenada() != toProd.getCantFacturada()) {
                TOPedido toPed = this.convertir(this.pedido);

                this.dao = new DAOPedidos();
                ArrayList<TOPedidoProducto> listaSimilares = this.dao.grabarProductoCantidad(toPed, toProd);
                this.totalResta(this.producto);
                this.producto.setCantOrdenada(toProd.getCantOrdenada());
                this.producto.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
                this.producto.setCantFacturada(toProd.getCantOrdenada());
                this.producto.setCantSinCargo(toProd.getCantOrdenadaSinCargo());
                this.producto.setCajas(this.producto.getCantOrdenada() / this.producto.getProducto().getPiezas());
                this.producto.setCajasSinCargo(this.producto.getCantOrdenadaSinCargo() / this.producto.getProducto().getPiezas());
                this.totalSuma(this.producto);
                if (!listaSimilares.isEmpty()) {
                    this.procesaSimilares(listaSimilares);
                }
                ok = true;
            }
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

//    private TOProductoPedido convertir(PedidoProducto p) {
//        TOProductoPedido to = new TOProductoPedido();
//        to.setIdPedido(p.getIdPedido());
//        to.setIdMovto(p.getIdMovto());
//        to.setIdProducto(p.getProducto().getIdProducto());
//        to.setCantOrdenada(p.getCantOrdenada());
//        to.setCantOrdenadaSinCargo(p.getCantOrdenadaSinCargo());
////        to.setSimilar(p.isSimilar());
//        to.setPiezas(p.getProducto().getPiezas());
//        to.setCantFacturada(p.getCantFacturada());
//        to.setCantSinCargo(p.getCantSinCargo());
//        to.setCosto(p.getCosto());
//        to.setDesctoProducto1(p.getDesctoProducto1());
//        to.setUnitario(p.getUnitario());
//        to.setIdImpuestoGrupo(p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
//        return to;
//    }

    public void actualizaProductoSeleccionado() {
        int idx;
        boolean ok = false;
        this.producto = new PedidoProducto(this.mbBuscar.getProducto());
        if ((idx = this.detalle.indexOf(this.producto)) != -1) {
            this.producto = this.detalle.get(idx);
        } else {
            this.producto.setIdMovto(this.pedido.getIdMovto());
            this.producto.setIdVenta(this.pedido.getIdVenta());
            try {
                TOPedido toPed = this.convertir(this.pedido);
                TOPedidoProducto toProd = Pedidos.convertir(this.producto);

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

    private PedidoProducto convertir(TOPedidoProducto toProd) throws SQLException {
        PedidoProducto prod = new PedidoProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        Pedidos.convertir(toProd, prod);
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
            for (TOPedidoProducto toProd : this.dao.obtenerDetallePedido(toPed)) {
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

    private TOPedido convertir(Pedido ped) {
        TOPedido toPed = new TOPedido();
        Pedidos.convertirPedido(ped, toPed);
        return toPed;
    }

    private boolean validar() {
        boolean ok = false;
        if (this.mbAlmacenes.getToAlmacen() == null) {
            Mensajes.mensajeAlert("Debe seleccionar un almacen !!!");
        } else if (this.mbTiendas.getTienda() == null) {
            Mensajes.mensajeAlert("Debe seleccionar una tienda !!!");
        } else if (this.mbComprobantes.getMbMonedas().getSeleccionMoneda() == null || this.mbComprobantes.getMbMonedas().getSeleccionMoneda().getIdMoneda() == 0) {
            Mensajes.mensajeAlert("Debe seleccionar una moneda !!!");
        } else {
            ok = true;
        }
        return ok;
    }

    public void crearPedido() {
        boolean ok = false;
        if (this.validar()) {
            this.pedido.setAlmacen(this.mbAlmacenes.getToAlmacen());
            this.pedido.setTienda(this.mbTiendas.getTienda());
            this.pedido.setComprobante(new Comprobante(this.pedido.getTipo().getIdTipo(), this.pedido.getAlmacen().getIdEmpresa(), this.pedido.getTienda().getIdTienda(), this.mbComprobantes.getMbMonedas().getSeleccionMoneda()));
            this.pedido.setDesctoComercial(this.mbClientes.getCliente().getDesctoComercial());
            this.pedido.setDiasCredito(this.mbClientes.getCliente().getDiasCredito());
            TOPedido toPed = this.convertir(this.pedido);
            try {
                this.dao = new DAOPedidos();
                this.dao.agregarPedido(toPed, this.pedido.getComprobante().getMoneda().getIdMoneda());
                this.pedido.setIdPedido(toPed.getIdPedido());
                this.pedido.setIdMovto(toPed.getIdMovto());
                this.pedido.setIdMovtoAlmacen(toPed.getIdMovtoAlmacen());
                this.pedido.getComprobante().setIdComprobante(toPed.getIdComprobante());
                this.pedido.setFecha(toPed.getFecha());
                this.pedido.setEstatus(toPed.getEstatus());
                this.pedido.setIdUsuario(toPed.getIdUsuario());
                this.pedido.setPropietario(toPed.getPropietario());
                this.pedido.setIdVenta(toPed.getReferencia());
                this.setLocked(this.pedido.getIdUsuario() == this.pedido.getPropietario());
                this.detalle = new ArrayList<>();
                this.setProducto(null);
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
        this.pedido = new Pedido();
//        this.ordenDeCompra = "";
//        this.ordenDeCompraFecha = new Date();
    }

    private Pedido convertir(TOPedido toPed) {
        Pedido ped = new Pedido(this.mbAlmacenes.obtenerAlmacen(toPed.getIdAlmacen()), this.mbTiendas.obtenerTienda(toPed.getIdReferencia()), this.mbComprobantes.obtenerComprobante(toPed.getIdComprobante()));
        Pedidos.convertirPedido(toPed, ped);
        this.mbClientes.setCliente(this.mbClientes.obtenerCliente(ped.getTienda().getIdCliente()));
        // Si el pedido todavia esta pendiente, se actualiza con datos del cliente
        // Si ya esta cerrada, se queda con lo que se trajo de la base
        if (toPed.getEstatus() == 0) {
            ped.setDesctoComercial(this.mbClientes.getCliente().getDesctoComercial());
            ped.setDesctoProntoPago(0);
            ped.setDiasCredito(this.mbClientes.getCliente().getDiasCredito());
        }
        return ped;
    }

    public void obtenerPedidos() {
        try {   // Segun fecha y status
            this.pedidos = new ArrayList<>();
            this.dao = new DAOPedidos();
            for (TOPedido to : this.dao.obtenerPedidos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.pendientes ? 0 : 1, this.fechaInicial, false)) {
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

    private void procesaSimilares(ArrayList<TOPedidoProducto> listaSimilares) throws SQLException {
        int idx;
        PedidoProducto prod;
        for (TOPedidoProducto to : listaSimilares) {
            prod = this.convertir(to);
            if ((idx = this.detalle.indexOf(prod)) != -1) {
                if (prod.equals(this.producto)) {
                    this.setProducto(prod);
                }
                this.detalle.set(idx, prod);
            } else {
                this.detalle.add(prod);
            }
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
//
//    public String getOrdenDeCompra() {
//        return ordenDeCompra;
//    }
//
//    public void setOrdenDeCompra(String ordenDeCompra) {
//        this.ordenDeCompra = ordenDeCompra;
//    }
//
//    public Date getOrdenDeCompraFecha() {
//        return ordenDeCompraFecha;
//    }
//
//    public void setOrdenDeCompraFecha(Date ordenDeCompraFecha) {
//        this.ordenDeCompraFecha = ordenDeCompraFecha;
//    }

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
            this.acciones = this.mbAcciones.obtenerAcciones(36);
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }
}
