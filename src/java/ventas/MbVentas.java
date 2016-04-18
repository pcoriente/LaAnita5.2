package ventas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import clientes.MbMiniClientes;
import comprobantes.MbComprobantes;
import formatos.MbFormatos;
import impuestos.dominio.ImpuestosProducto;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedProperty;
import javax.inject.Named;
import javax.naming.NamingException;
import mbMenuClientesGrupos.MbClientesGrupos;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import pedidos.Pedidos;
import pedidos.dao.DAOPedidos;
import pedidos.dominio.Pedido;
import pedidos.dominio.PedidoProducto;
import pedidos.to.TOPedido;
import pedidos.to.TOPedidoProducto;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;
import tiendas.MbMiniTiendas;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

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
    private Pedido venta;
    private ArrayList<Pedido> ventas, pedidos;
    private boolean locked;
    private ArrayList<PedidoProducto> detalle, similares;
    private PedidoProducto producto, similar;
    private ArrayList<ImpuestosProducto> impuestosTotales;
    private double cantTraspasar;
    private boolean pendientes;
    private Date fechaInicial;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAOPedidos dao;

    public MbVentas() throws NamingException {
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

    public void surtirFincado() {
        PedidoProducto prod;
        boolean completo = true;
        TOPedido toPed = this.convertir(this.venta);
        this.venta.setSubTotal(0);
        this.venta.setDescuento(0);
        this.venta.setImpuesto(0);
        this.venta.setTotal(0);
        this.detalle = new ArrayList<>();
        this.impuestosTotales = new ArrayList<>();
        try {
            this.dao = new DAOPedidos();
            if (this.venta.isDirecto()) {
                int idMovtoRecepcion = this.dao.obtenerIdMovtoRecepcion(this.venta.getIdSolicitud());
                for (TOPedidoProducto to : this.dao.cerrarFincadoDirecto(toPed, idMovtoRecepcion)) {
                    prod = this.convertir(to);
                    this.totalSuma(prod);
                    this.detalle.add(prod);
                }
                this.venta.setFolio(toPed.getFolio());
                this.venta.setEstatus(toPed.getEstatus());
                this.venta.setIdUsuario(toPed.getIdUsuario());
                this.venta.setPropietario(toPed.getPropietario());
                this.venta.setComprobante(this.mbComprobantes.obtenerComprobante(toPed.getIdComprobante()));
                this.setLocked(this.venta.getIdUsuario() == this.venta.getPropietario());
                Mensajes.mensajeSucces("El pedido fincado se surtió y cerró correctamente !!!");
            } else {
                for (TOPedidoProducto to : this.dao.surtirFincado(toPed)) {
                    prod = this.convertir(to);
                    if (prod.getCantOrdenada() + prod.getCantOrdenadaSinCargo() != prod.getCantFacturada() + prod.getCantSinCargo()) {
                        completo = false;
                    }
                    this.totalSuma(prod);
                    this.detalle.add(prod);
                }
                if (!completo) {
                    Mensajes.mensajeAlert("El pedido no se surtió completo !!!");
                } else {
                    Mensajes.mensajeSucces("El pedido se surtió correctamente");
                }
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (Exception ex) {
            Mensajes.mensajeAlert(ex.getMessage());
        }
    }

    public void generarPedidoVenta() {
        boolean ok = false;
        PedidoProducto prod;
        try {
            TOPedido toPed = this.convertir(this.venta);

            this.dao = new DAOPedidos();
            ArrayList<TOPedidoProducto> det = this.dao.generarPedidoVenta(toPed);
            this.venta = this.convertir(toPed);
            this.venta.setSubTotal(0);
            this.venta.setDescuento(0);
            this.venta.setImpuesto(0);
            this.venta.setTotal(0);
            this.impuestosTotales = new ArrayList<>();
            this.detalle = new ArrayList<>();
            for (TOPedidoProducto to : det) {
                prod = this.convertir(to);
                this.totalSuma(prod);
                this.detalle.add(prod);
            }
            this.venta.setIdUsuario(toPed.getIdUsuario());
            this.venta.setPropietario(toPed.getPropietario());
            this.locked = (this.venta.getIdUsuario() == this.venta.getPropietario());
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (Exception ex) {
            Mensajes.mensajeAlert(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    public void obtenerPedidos() {
        try {   // Segun fecha y status
            this.venta = null;
            this.pedidos = new ArrayList<>();
            this.dao = new DAOPedidos();
            for (TOPedido to : this.dao.obtenerPedidos(this.mbAlmacenes.getToAlmacen().getIdAlmacen())) {
                this.pedidos.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void salir() {
        try {
            this.dao = new DAOPedidos();
            if (this.venta != null && this.isLocked()) {
                TOPedido toPed = this.convertir(this.venta);
                this.dao.liberarPedido(toPed);
                this.venta.setPropietario(0);
                this.setLocked(false);
            }
            this.ventas = new ArrayList<>();
            for (TOPedido to : this.dao.obtenerPedidos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.pendientes ? 0 : 7, this.fechaInicial, true)) {
                this.ventas.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void liberarVenta() {
        boolean ok = false;
        if (this.venta == null) {
            ok = true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else if (this.isLocked()) {
            TOPedido toPed = this.convertir(this.venta);
            try {

                this.dao = new DAOPedidos();
                this.dao.liberarPedido(toPed);
                this.venta.setPropietario(toPed.getPropietario());
                this.setLocked(false);
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

    public void eliminarVenta() {
        boolean ok = false;
        try {
            TOPedido toPed = this.convertir(this.venta);

            this.dao = new DAOPedidos();
            this.dao.eliminarVenta(toPed);
            this.ventas.remove(this.venta);
            this.setLocked(false);
            this.venta = null;
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    public void cancelarVenta() {
        boolean ok = false;
        try {
            TOPedido toPed = this.convertir(this.venta);

            this.dao = new DAOPedidos();
            this.dao.cancelarPedido(toPed);
            for (PedidoProducto prod : this.detalle) {
                prod.setCantFacturada(0);
                prod.setCantSinCargo(0);
            }
            this.venta.setPedidoEstatus(toPed.getPedidoEstatus());
            this.venta.setPropietario(toPed.getPropietario());
            this.venta.setIdUsuario(toPed.getIdUsuario());
            this.venta.setEstatus(toPed.getEstatus());
            this.setLocked(this.venta.getIdUsuario() == this.venta.getPropietario());
            Mensajes.mensajeSucces("La venta se canceló correctamente !!!");
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okVenta", ok);
    }

    public double sumaPiezasOficina() {
        double piezas = 0;
        for (PedidoProducto p : this.detalle) {
            piezas += (p.getCantFacturada() + p.getCantSinCargo());
        }
        return piezas;
    }

    public void cerrarVenta() {
        boolean ok = false;
        try {
            if (this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en la venta !!!");
            } else if (this.sumaPiezasOficina() == 0) {
                Mensajes.mensajeAlert("No hay cantidades capturadas !!!");
            } else {
                TOPedido toPed = this.convertir(this.venta);

                this.dao = new DAOPedidos();
                this.dao.cerrarVenta(toPed);
                this.venta.setFolio(toPed.getFolio());
                this.venta.setEstatus(toPed.getEstatus());
                this.venta.setIdUsuario(toPed.getIdUsuario());
                this.venta.setPropietario(toPed.getPropietario());
                this.venta.setComprobante(this.mbComprobantes.obtenerComprobante(toPed.getIdComprobante()));
                this.setLocked(this.venta.getIdUsuario() == this.venta.getPropietario());
                Mensajes.mensajeSucces("La venta se cerró correctamente !!!");
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

    public void eliminarProducto() {
        boolean ok = false;
        TOPedido toPed = this.convertir(this.venta);
        TOPedidoProducto toProd = this.convertir(this.producto);
        toProd.setCantFacturada(0);
        try {
            this.dao = new DAOPedidos();
            ArrayList<TOPedidoProducto> detalleSimilares = this.dao.eliminarProducto(toPed, toProd, this.producto.getSeparados());
            this.totalResta(this.producto);
            if (toProd.getCantSinCargo() == 0) {
                this.detalle.remove(this.producto);
            } else {
                this.producto.setCantFacturada(toProd.getCantFacturada());
                this.producto.setCantSinCargo(toProd.getCantSinCargo());
                this.producto.setSeparados(toProd.getCantSinCargo());
            }
            if (!detalleSimilares.isEmpty()) {
                this.procesaSimilares(detalleSimilares);
            }
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (Exception ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okProductoEliminar", ok);
    }

    public void actualizaTraspasoSimilar() {
        boolean okSimilares = false;
        try {
            if (this.cantTraspasar > 0 && this.cantTraspasar <= this.producto.getCantSinCargo()) {
                TOPedido toPed = this.convertir(this.venta);
                TOPedidoProducto toProd = this.convertir(this.producto);
                TOPedidoProducto toSimilar = this.convertir(this.similar);

                this.dao = new DAOPedidos();
                this.dao.transferirSinCargo(toPed, toProd, toSimilar, this.cantTraspasar);

                this.producto.setCantSinCargo(this.producto.getCantSinCargo() - this.cantTraspasar);
                this.producto.setSeparados(this.producto.getCantFacturada() + this.producto.getCantSinCargo());
                if (this.similar.getIdMovto() == 0) {
                    this.detalle.add(this.convertir(toSimilar));
                } else {
                    int idx = this.detalle.indexOf(this.similar);
                    this.setSimilar(this.detalle.get(idx));
                    this.similar.setCantSinCargo(this.similar.getCantSinCargo() + this.cantTraspasar);
                    this.similar.setSeparados(this.similar.getCantFacturada() + this.similar.getCantSinCargo());
                }
                okSimilares = true;
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (Exception ex) {
            Mensajes.mensajeAlert(ex.getMessage());
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
            for (TOPedidoProducto to : this.dao.obtenerSimilares(this.producto.getIdMovto(), this.producto.getProducto().getIdProducto())) {
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

    public void actualizarProductoSinCargo() {
        boolean ok = false;
        TOPedidoProducto toProd = this.convertir(this.producto);
        this.producto.setCantSinCargo(this.producto.getSeparados() - this.producto.getCantFacturada());
        if (toProd.getCantSinCargo() < 0) {
            Mensajes.mensajeAlert("La cantidad sin cargo no debe ser menor que cero !!!");
        } else if (toProd.getCantSinCargo() > toProd.getCantOrdenadaSinCargo()) {
            Mensajes.mensajeAlert("La cantidad sin cargo no debe ser mayor a la cantidad ordenada sin cargo !!!");
        } else if (toProd.getCantSinCargo() != this.producto.getCantSinCargo()) {
            TOPedido toMov = this.convertir(this.venta);
            try {
                this.dao = new DAOPedidos();
                this.dao.actualizarProductoSinCargo(toMov, toProd, this.producto.getSeparados());

                this.producto.setCantSinCargo(toProd.getCantSinCargo());
                this.producto.setSeparados(toProd.getCantFacturada() + toProd.getCantSinCargo());
                ok = true;
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }

    private void totalResta(PedidoProducto prod) {
        int index;
        ImpuestosProducto nuevo;
        movimientos.Movimientos.restaTotales(prod, this.venta);
        for (ImpuestosProducto impuesto : prod.getImpuestos()) {
            if ((index = this.impuestosTotales.indexOf(impuesto)) == -1) {
                nuevo = new ImpuestosProducto();
                nuevo.setAcreditable(impuesto.isAcreditable());
                nuevo.setAcumulable(impuesto.isAcumulable());
                nuevo.setAplicable(impuesto.isAplicable());
                nuevo.setIdImpuesto(impuesto.getIdImpuesto());
                nuevo.setImporte(impuesto.getImporte() * prod.getCantFacturada());
                nuevo.setImpuesto(impuesto.getImpuesto());
                nuevo.setModo(impuesto.getModo());
                nuevo.setValor(impuesto.getValor());
                this.impuestosTotales.add(nuevo);
            } else {
                this.impuestosTotales.get(index).setImporte(this.impuestosTotales.get(index).getImporte() - impuesto.getImporte() * prod.getCantFacturada());
            }
        }
    }

    private TOPedidoProducto convertir(PedidoProducto prod) {
        TOPedidoProducto toProd = new TOPedidoProducto();
        toProd.setCantOrdenada(prod.getCantOrdenada());
        toProd.setCantOrdenadaSinCargo(prod.getCantOrdenadaSinCargo());
        movimientos.Movimientos.convertir(prod, toProd);
        return toProd;
    }

    private void procesaSimilares(ArrayList<TOPedidoProducto> detalleSimilares) throws SQLException {
        int idx;
        PedidoProducto prod;
        for (TOPedidoProducto to : detalleSimilares) {
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

    public void actualizarProductoCantidad() {
        boolean ok = false;
        TOPedidoProducto toProd = this.convertir(this.producto);
        this.producto.setCantFacturada(this.producto.getSeparados() - this.producto.getCantSinCargo());
        if (toProd.getCantFacturada() < 0) {
            Mensajes.mensajeAlert("La cantidad facturada no debe ser menor que cero !!!");
        } else if (this.venta.getIdPedido() != 0 && toProd.getCantFacturada() > toProd.getCantOrdenada()) {
            Mensajes.mensajeAlert("La cantidad facturada no debe ser mayor a la cantidad ordenada !!!");
        } else if (toProd.getCantFacturada() + toProd.getCantSinCargo() != this.producto.getSeparados()) {
            TOPedido toPed = this.convertir(this.venta);
            try {
                this.dao = new DAOPedidos();
                ArrayList<TOPedidoProducto> detalleSimilares = this.dao.actualizarProductoCantidad(toPed, toProd, this.producto.getSeparados());
                this.totalResta(this.producto);
                this.producto.setCantFacturada(toProd.getCantFacturada());
                this.producto.setCantSinCargo(toProd.getCantSinCargo());
                this.producto.setSeparados(toProd.getCantFacturada() + toProd.getCantSinCargo());
                this.totalSuma(this.producto);
                if (!detalleSimilares.isEmpty()) {
                    this.procesaSimilares(detalleSimilares);
                }
                ok = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }

    public void modificarProducto(SelectEvent event) {
        boolean ok = false;
        if (this.venta.isDirecto()) {
            Mensajes.mensajeAlert("La venta es directa y no se puede modificar, aplicar surtir fincado !!!");
            ok = false;
        } else {
            this.producto = (PedidoProducto) event.getObject();
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }

    public void actualizaProductoSeleccionado() {
        int idx;
        boolean ok = false;
        this.producto = new PedidoProducto(this.mbBuscar.getProducto());
        if ((idx = this.detalle.indexOf(this.producto)) != -1) {
            this.producto = this.detalle.get(idx);
        } else {
            TOPedido toPed = this.convertir(this.venta);
            TOPedidoProducto toProd = new TOPedidoProducto();
            toProd.setIdMovto(this.venta.getIdMovto());
            toProd.setIdVenta(this.venta.getIdVenta());
            toProd.setIdProducto(this.producto.getProducto().getIdProducto());
            toProd.setIdImpuestoGrupo(this.producto.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
            try {
                this.dao = new DAOPedidos();
                this.dao.agregarProductoPedido(toPed, toProd);
                this.producto = this.convertir(toProd, this.producto.getProducto());
//                this.producto.setIdMovto(toProd.getIdMovto());
//                this.producto.setIdPedido(toProd.getIdPedido());
                this.detalle.add(this.producto);
                ok = true;
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

    private void totalSuma(PedidoProducto prod) {
        int index;
        ImpuestosProducto nuevo;
        movimientos.Movimientos.sumaTotales(prod, this.venta);
        for (ImpuestosProducto impuesto : prod.getImpuestos()) {
            if ((index = this.impuestosTotales.indexOf(impuesto)) == -1) {
                nuevo = new ImpuestosProducto();
                nuevo.setAcreditable(impuesto.isAcreditable());
                nuevo.setAcumulable(impuesto.isAcumulable());
                nuevo.setAplicable(impuesto.isAplicable());
                nuevo.setIdImpuesto(impuesto.getIdImpuesto());
                nuevo.setImporte(impuesto.getImporte() * prod.getCantFacturada());
                nuevo.setImpuesto(impuesto.getImpuesto());
                nuevo.setModo(impuesto.getModo());
                nuevo.setValor(impuesto.getValor());
                this.impuestosTotales.add(nuevo);
            } else {
                this.impuestosTotales.get(index).setImporte(this.impuestosTotales.get(index).getImporte() + impuesto.getImporte() * prod.getCantFacturada());
            }
        }
    }

    private PedidoProducto convertir(TOPedidoProducto toProd, Producto producto) throws SQLException {
        PedidoProducto prod = new PedidoProducto(producto);
        prod.setIdVenta(toProd.getIdVenta());
        prod.setCantOrdenada(toProd.getCantOrdenada());
        prod.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setNeto(prod.getUnitario() + this.dao.obtenerImpuestosProducto(toProd.getIdMovto(), toProd.getIdProducto(), prod.getImpuestos()));
        return prod;
    }

    private PedidoProducto convertir(TOPedidoProducto toProd) throws SQLException {
        PedidoProducto prod = new PedidoProducto();
        prod.setIdVenta(toProd.getIdVenta());
        prod.setCantOrdenada(toProd.getCantOrdenada());
        prod.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setNeto(prod.getUnitario() + this.dao.obtenerImpuestosProducto(toProd.getIdMovto(), toProd.getIdProducto(), prod.getImpuestos()));
        return prod;
    }

    public void obtenerDetalle(SelectEvent event) {
        this.venta = (Pedido) event.getObject();

        boolean ok = false;
        int estatus = 7;
        String aviso = "";
        PedidoProducto prod;
        this.detalle = new ArrayList<>();
        this.impuestosTotales = new ArrayList<>();
        try {
            this.dao = new DAOPedidos();
            if (this.venta.isDirecto()) {
                estatus = this.dao.obtenerEstatusTraspasoDirecto(this.venta.getIdEnvio(), this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdSolicitud());
                if (estatus == 0) {
                    Mensajes.mensajeAlert("El envío para este almacén no se ha cerrado !!!");
                } else if (estatus == 1) {
                    Mensajes.mensajeError("El traspaso no se ha realizado !!!");
                    estatus = 0;
                } else if (estatus == 2) {
                    Mensajes.mensajeAlert("La recepción no se ha realizado !!!");
                    estatus = 0;
                } else {
                    estatus = 7;
                }
            }
            if (estatus != 0) {
                this.venta.setSubTotal(0);
                this.venta.setDescuento(0);
                this.venta.setImpuesto(0);
                this.venta.setTotal(0);
                TOPedido toPed = this.convertir(this.venta);

                for (TOPedidoProducto to : this.dao.obtenerDetalleOficina(toPed, aviso)) {
                    prod = this.convertir(to);
                    this.totalSuma(prod);
                    this.detalle.add(prod);
                }
                if (!aviso.isEmpty()) {
                    Mensajes.mensajeAlert("Los productos (" + aviso + ") No se surtieron completamente !!!");
                }
                this.venta.setEstatus(toPed.getEstatus());
                this.venta.setIdUsuario(toPed.getIdUsuario());
                this.venta.setPropietario(toPed.getPropietario());
                this.setLocked(this.venta.getIdUsuario() == this.venta.getPropietario());
                ok = true;
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    private TOPedido convertir(Pedido pedido) {
        TOPedido toPed = new TOPedido();
        Pedidos.convertirPedido(pedido, toPed);
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

    public void crearVenta() {
        boolean ok = false;
        if (this.validar()) {
            this.mbClientes.setCliente(this.mbClientes.obtenerCliente(this.mbTiendas.getTienda().getIdCliente()));
            this.venta = new Pedido(this.mbAlmacenes.getToAlmacen(), this.mbTiendas.getTienda(), this.mbComprobantes.getMbMonedas().getSeleccionMoneda());
            this.venta.setDesctoComercial(this.mbClientes.getCliente().getDesctoComercial());
            this.venta.setDiasCredito(this.mbClientes.getCliente().getDiasCredito());
            TOPedido toVta = this.convertir(this.venta);
            try {
                this.dao = new DAOPedidos();
                this.dao.agregarVenta(toVta, this.venta.getComprobante().getMoneda().getIdMoneda());
                this.venta.setIdMovto(toVta.getIdMovto());
                this.venta.setIdMovtoAlmacen(toVta.getIdMovtoAlmacen());
                this.venta.getComprobante().setIdComprobante(toVta.getIdComprobante());
                this.venta.setFecha(toVta.getFecha());
                this.venta.setEstatus(toVta.getEstatus());
                this.venta.setIdUsuario(toVta.getIdUsuario());
                this.venta.setPropietario(toVta.getPropietario());
                this.venta.setIdVenta(toVta.getReferencia());
                this.setLocked(this.venta.getIdUsuario() == this.venta.getPropietario());
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

    public void nuevaVenta() {
        this.mbClientes.obtenerClientesCedis();
        this.mbClientes.nuevoCliente();
        this.mbTiendas.obtenerTiendasCliente(this.mbClientes.getCliente().getIdCliente());
        this.mbTiendas.nuevaTienda();
    }

    private Pedido convertir(TOPedido toPed) {
        Pedido ped = new Pedido(this.mbAlmacenes.obtenerAlmacen(toPed.getIdAlmacen()), this.mbTiendas.obtenerTienda(toPed.getIdReferencia()), this.mbComprobantes.obtenerComprobante(toPed.getIdComprobante()));
        Pedidos.convertirPedido(toPed, ped);
        this.mbClientes.setCliente(this.mbClientes.obtenerCliente(ped.getTienda().getIdCliente()));
        if (toPed.getEstatus() == 0) {
            // Si la venta todavia esta pendiente, se actualiza con datos del cliente
            // Si ya esta cerrada, se queda con lo que se leyo de la base
            ped.setDesctoComercial(this.mbClientes.getCliente().getDesctoComercial());
            ped.setDesctoProntoPago(0);
            ped.setDiasCredito(this.mbClientes.getCliente().getDiasCredito());
        }
        return ped;
    }

    public void obtenerVentas() {
        try {   // Segun fecha y status
            this.ventas = new ArrayList<>();
            this.dao = new DAOPedidos();
            for (TOPedido to : this.dao.obtenerPedidos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.pendientes ? 0 : 7, this.fechaInicial, true)) {
                this.ventas.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public String terminar() {
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);

        this.mbGrupos.inicializar();
        this.mbClientes.inicializar();
        this.mbFormatos.inicializar();

        this.mbBuscar.inicializar();
        this.mbComprobantes.getMbMonedas().setListaMonedas(null);

        this.pendientes = true;
        this.fechaInicial = new Date();
        this.ventas = new ArrayList<>();
        this.pedidos = new ArrayList<>();
        this.venta = new Pedido();
        this.detalle = new ArrayList<>();
    }

    private void inicializa() {
        this.inicializar();
    }

    public ArrayList<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(ArrayList<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    public ArrayList<ImpuestosProducto> getImpuestosTotales() {
        return impuestosTotales;
    }

    public void setImpuestosTotales(ArrayList<ImpuestosProducto> impuestosTotales) {
        this.impuestosTotales = impuestosTotales;
    }

    public boolean isPendientes() {
        return pendientes;
    }

    public void setPendientes(boolean pendientes) {
        this.pendientes = pendientes;
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

    public ArrayList<Pedido> getVentas() {
        return ventas;
    }

    public void setVentas(ArrayList<Pedido> ventas) {
        this.ventas = ventas;
    }

    public Pedido getVenta() {
        return venta;
    }

    public void setVenta(Pedido venta) {
        this.venta = venta;
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

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
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

    public double getCantTraspasar() {
        return cantTraspasar;
    }

    public void setCantTraspasar(double cantTraspasar) {
        this.cantTraspasar = cantTraspasar;
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

    public MbAlmacenesJS getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbAlmacenesJS mbAlmacenes) {
        this.mbAlmacenes = mbAlmacenes;
    }
}
