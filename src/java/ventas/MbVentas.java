package ventas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import clientes.MbMiniClientes;
import entradas.dao.DAOMovimientos1;
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
import movimientos.dao.DAOMovimientosOld;
import movimientos.to.TOLote;
import movimientos.to.TOMovimientoAlmacenProducto;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;
import tiendas.MbMiniTiendas;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;
import ventas.dao.DAOVentas;
import ventas.dominio.Venta;
import ventas.dominio.VentaAlmacenProducto;
import ventas.dominio.VentaProducto;
import ventas.to.TOVenta;
import ventas.to.TOVentaProducto;

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
    private DAOMovimientos1 dao1;
    private DAOMovimientosOld daoMv;
    private DAOVentas dao;
    private ArrayList<Venta> ventas;
    private Venta venta;
    private boolean ventaAsegurada;
    private ArrayList<VentaProducto> detalle, similares;
    private VentaProducto producto, respaldo, similar;
    private ArrayList<ImpuestosProducto> impuestosTotales;
    private ArrayList<VentaAlmacenProducto> almacenDetalle, empaqueLotes;
    private VentaAlmacenProducto loteOrigen, loteDestino;
    private boolean pendientes;
    private Date fechaInicial;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private double cantTraspasar;

    public MbVentas() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();

        this.mbGrupos = new MbClientesGrupos();
        this.mbClientes = new MbMiniClientes();
        this.mbFormatos = new MbFormatos();
        this.mbTiendas = new MbMiniTiendas();
        this.mbBuscar = new MbProductosBuscar();

        this.inicializa();
    }

    public void surtirFincado() {
        VentaProducto prod;
        boolean completo = true;
        TOVenta toMov = this.convertir(this.venta);
        try {
            this.dao = new DAOVentas();
            for (TOVentaProducto to : this.dao.surtirFincado(toMov)) {
                prod = this.convertir(to);
                if (prod.getCantOrdenada() + prod.getCantOrdenadaSinCargo() != prod.getCantFacturada() + prod.getCantSinCargo()) {
                    completo = false;
                }
                this.detalle.add(prod);
            }
            this.actualizaTotales();
            if (!completo) {
                Mensajes.mensajeAlert("El pedido no se surtio completo !!!");
            } else {
                Mensajes.mensajeSucces("El pedido se surtio correctamente");
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void cerrarPedidoAlmacenRelacionado() {
        boolean ok = false;
        try {
            this.dao1 = new DAOMovimientos1();
            int remision = this.dao1.cerrarMovtoAlmacenSalidaRelacionado(this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen());
//            this.venta.setRemision(Integer.toString(remision));
            this.venta.setEstatus(2);
//            this.venta.setStatusAlmacen(2);
            Mensajes.mensajeSucces("El pedido se remisiono correctamente !!!");
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    public void actualizaTraspasoLote() {
        boolean ok = false;
        try {
            int idx;
            this.dao1 = new DAOMovimientos1();
            this.dao1.traspasarLote(this.venta.getAlmacen().getIdAlmacen(), this.convertirAlmacenProducto(this.loteOrigen), this.loteDestino.getLote(), this.cantTraspasar);
            idx = this.almacenDetalle.indexOf(this.loteOrigen);
            this.loteOrigen = this.almacenDetalle.get(idx);
            this.loteOrigen.setCantidad(this.loteOrigen.getCantidad() - this.cantTraspasar);
            if ((idx = this.almacenDetalle.indexOf(this.loteDestino)) != -1) {
                this.loteDestino = this.almacenDetalle.get(idx);
                this.loteDestino.setCantidad(this.loteDestino.getCantidad() + this.cantTraspasar);
                this.almacenDetalle.set(idx, this.loteDestino);
            } else {
                this.loteDestino.setCantidad(this.cantTraspasar);
                this.almacenDetalle.add(this.loteDestino);
            }
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLote", ok);
    }

    public void inicializaTraspasoLote() {
        boolean ok = false;
        this.cantTraspasar = 0;
        this.loteDestino = null;
        VentaAlmacenProducto prod;
        this.empaqueLotes = new ArrayList<>();
        try {
            this.dao1 = new DAOMovimientos1();
            for (TOLote to : this.dao1.obtenerEmpaqueLotesDisponibles(this.venta.getAlmacen().getIdAlmacen(), this.loteOrigen.getProducto().getIdProducto())) {
                if (!this.loteOrigen.getLote().equals(to.getLote())) {
                    prod = new VentaAlmacenProducto(this.loteOrigen.getProducto(), to.getLote());
                    prod.setIdMovtoAlmacen(this.loteOrigen.getIdMovtoAlmacen());
                    prod.setCantidad(to.getCantidad());
                    this.empaqueLotes.add(prod);
                }
            }
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLote", ok);
    }

    private TOMovimientoAlmacenProducto convertirAlmacenProducto(VentaAlmacenProducto prod) throws SQLException {
        TOMovimientoAlmacenProducto to = new TOMovimientoAlmacenProducto();
        to.setIdMovtoAlmacen(prod.getIdMovtoAlmacen());
        to.setIdProducto(prod.getProducto().getIdProducto());
        to.setCantidad(prod.getCantidad());
        return to;
    }

    private VentaAlmacenProducto convertirAlmacenProducto(TOMovimientoAlmacenProducto to) throws SQLException {
        VentaAlmacenProducto prod = new VentaAlmacenProducto();
        prod.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        prod.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        prod.setCantidad(to.getCantidad());
        return prod;
    }

    public void obtenerAlmacenDetalle(SelectEvent event) {
        boolean ok = false;
        this.loteOrigen = null;
        this.venta = (Venta) event.getObject();
        this.almacenDetalle = new ArrayList<>();
        try {
            this.dao1 = new DAOMovimientos1();
            try {
                this.ventaAsegurada = this.dao1.asegurarMovtoRelacionado(this.venta.getIdMovto());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
            for (TOMovimientoAlmacenProducto to : this.dao1.obtenerMovimientoAlmacenDetalle(this.venta.getIdMovtoAlmacen())) {
                this.almacenDetalle.add(this.convertirAlmacenProducto(to));
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

//    public void regresarAlmacenFechaActual() {
//        this.fechaInicial = new Date();
//        this.obtenerPedidosAlmacen1();
//    }
//    
//    public void obtenerPedidosAlmacen1() {
//        try {   // Segun fecha y status
//            this.ventas = new ArrayList<>();
//            this.dao1 = new DAOMovimientos1();
//            for (TOMovimientoOficina to : this.dao1.obtenerMovimientosAlmacenRelacionados(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), 28, (this.pendientes ? 9999 : 1), this.fechaInicial)) {
//                this.ventas.add(this.convertir(to));
//            }
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//        } catch (NamingException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        }
//    }
    // *************************** Ventas Oficina ******************************* //
    public void eliminarPedidoRelacionado() {
        boolean ok = false;
        this.ventaAsegurada = false;
        try {
            this.dao1 = new DAOMovimientos1();
            this.dao1.eliminarMovtoSalidaRelacionado(this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen());
            this.ventas.remove(this.venta);
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

    public void liberarMovtoRelacionado() {
        boolean ok = false;
        if (this.venta == null) {
            ok = true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else if (this.ventaAsegurada) {
            try {
                this.ventaAsegurada = false;
                this.dao1 = new DAOMovimientos1();
                ok = this.dao1.liberarMovtoRelacionado(this.venta.getIdMovto());
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
        boolean ok = false;
        try {
            this.dao1 = new DAOMovimientos1();
            int folio = this.dao1.cerrarMovtoSalidaRelacionado(this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen(), 28);
            this.venta.setFolio(folio);
            this.venta.setEstatus(1);
//            this.venta.setStatusAlmacen(1);
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

    public void actualizaTraspasoSimilar() {
//        boolean ok = false;
//        TOVentaProducto toOrigen = this.convertir(this.producto);
//        TOVentaProducto toDestino = new TOVentaProducto();
//        toDestino.setIdMovto(this.similar.getIdMovto());
//        toDestino.setIdProducto(this.similar.getProducto().getIdProducto());
//        toDestino.setIdImpuestoGrupo(this.similar.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
//        toDestino.setCantSinCargo(this.similar.getCantSinCargo());
//        try {
//            this.daoMv = new DAOMovimientosOld();
//            this.daoMv.tranferirSinCargo(this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen(), toOrigen, toDestino, this.cantTraspasar, this.venta.getTienda().getIdImpuestoZona());
//            this.producto.setCantSinCargo(toOrigen.getCantSinCargo());
//            this.producto.setSeparados(this.producto.getCantFacturada() + this.producto.getCantSinCargo());
//            if (this.similar.getIdMovto() == 0) {
//                this.detalle.add(this.convertir(toDestino));
//            } else {
//                int idx = this.detalle.indexOf(this.similar);
//                this.similar = this.detalle.get(idx);
//                this.similar.setCantSinCargo(toDestino.getCantSinCargo());
//                this.similar.setSeparados(this.similar.getCantFacturada() + this.similar.getCantSinCargo());
//            }
//            this.actualizaTotales();
//            ok = true;
//        } catch (NamingException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//        }
//        RequestContext context = RequestContext.getCurrentInstance();
//        context.addCallbackParam("okSimilares", ok);
    }

    public void traspasoSimilar() {
        int idx;
        boolean ok = false;
        this.similar = null;
        VentaProducto prod;
        this.cantTraspasar = 0;
        this.similares = new ArrayList<>();
        try {
            for (Producto p : this.mbBuscar.obtenerSimilares(this.producto.getProducto().getIdProducto())) {
                prod = new VentaProducto(p);
                if ((idx = this.detalle.indexOf(prod)) != -1) {
                    prod.setIdMovto(this.detalle.get(idx).getIdMovto());
                    prod.setCantSinCargo(this.detalle.get(idx).getCantSinCargo());
                }
                this.similares.add(prod);
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

    public void capturar() {
//        if (this.mbClientes.getCliente().getIdContribuyente() == 0) {
//            Mensajes.mensajeAlert("Se requiere seleccionar un cliente");
//        } else if (this.mbTiendas.getTienda().getIdTienda() == 0) {
//            Mensajes.mensajeAlert("Se requiere seleccionar una tienda");
//        } else {
//            this.venta = new Venta();
//            try {
//                TOMovimientoOficina to = this.convertir(this.venta);
//                this.dao1 = new DAOMovimientos1();
//                this.dao1.agregarMovimientoRelacionado(to);
//
//                this.dao = new DAOVentas();
//                this.venta = this.convertir(this.dao.obtenerVenta(to.getIdMovto()));
//                this.detalle = new ArrayList<>();
//                this.producto = new VentaProducto();
//            } catch (SQLException ex) {
//                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//            } catch (NamingException ex) {
//                Mensajes.mensajeError(ex.getMessage());
//            }
//        }
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
        this.mbAlmacenes.setListaAlmacenes(null);

        this.mbGrupos.inicializar();
        this.mbClientes.inicializar();
        this.mbFormatos.inicializar();
        this.mbBuscar.inicializar();

        this.pendientes = true;
        this.fechaInicial = new Date();
        this.ventas = new ArrayList<>();
        this.venta = new Venta();
        this.detalle = new ArrayList<>();
    }

    private void totalResta(VentaProducto prod) {
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

    public void actualizarProductoSinCargo() {
        boolean ok = false;
        if (this.producto.getCantSinCargo() < 0) {
            Mensajes.mensajeAlert("La cantidad sin cargo no debe ser menor que cero !!!");
        } else if (this.producto.getCantSinCargo() > this.producto.getCantOrdenadaSinCargo()) {
            Mensajes.mensajeAlert("La cantidad sin cargo no debe ser mayor a la cantidad ordenada sin cargo !!!");
        } else {
            TOVenta toMov = this.convertir(this.venta);
            TOVentaProducto toProd = this.convertir(this.producto);
            this.producto.setCantSinCargo(this.producto.getSeparados() - this.producto.getCantFacturada());
            try {
                this.dao = new DAOVentas();
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

    public void actualizarProductoCantidad() {
        boolean ok = false;
        if (this.producto.getCantFacturada() < 0) {
            Mensajes.mensajeAlert("La cantidad facturada no debe ser menor que cero !!!");
        } else if (this.venta.getIdPedido() != 0 && this.producto.getCantFacturada() > this.producto.getCantOrdenada()) {
            Mensajes.mensajeAlert("La cantidad facturada no debe ser mayor a la cantidad ordenada !!!");
        } else {
            TOVenta toMov = this.convertir(this.venta);
            TOVentaProducto toProd = this.convertir(this.producto);
            this.producto.setCantFacturada(this.producto.getSeparados() - this.producto.getCantSinCargo());
            try {
                this.dao = new DAOVentas();
                this.dao.actualizarProductoCantidad(toMov, toProd, this.producto.getSeparados());
                this.totalResta(this.producto);

                this.producto.setCantFacturada(toProd.getCantFacturada());
                this.producto.setCantSinCargo(toProd.getCantSinCargo());
                this.producto.setSeparados(toProd.getCantFacturada() + toProd.getCantSinCargo());
                this.totalSuma(this.producto);
                ok = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }

//    private void respalda() {
//        this.respaldo = new VentaProducto();
//        this.respaldo.setProducto(this.producto.getProducto());
//        this.respaldo.setCantOrdenada(this.producto.getCantOrdenada());
//        this.respaldo.setCantOrdenadaSinCargo(this.producto.getCantOrdenadaSinCargo());
//        this.respaldo.setCantFacturada(this.producto.getCantFacturada());
//        this.respaldo.setCantSinCargo(this.producto.getCantSinCargo());
//        this.respaldo.setSeparados(this.producto.getSeparados());
//        this.respaldo.setCosto(this.producto.getCosto());
//        this.respaldo.setDesctoProducto1(this.producto.getDesctoProducto1());
//        this.respaldo.setImpuestos(this.producto.getImpuestos());
//        this.respaldo.setNeto(this.producto.getNeto());
//    }
    private void actualizaTotales() {
        int index;
        double importe;
        ImpuestosProducto nuevo;
        this.venta.setSubTotal(0);
        this.venta.setDescuento(0);
        this.impuestosTotales = new ArrayList<>();
        this.venta.setTotal(0);
        for (VentaProducto prod : this.detalle) {
            this.venta.setSubTotal(this.venta.getSubTotal() + prod.getCosto() * prod.getCantFacturada());
            this.venta.setDescuento(this.venta.getDescuento() + (prod.getCosto() - prod.getUnitario()) * prod.getCantFacturada());
            for (ImpuestosProducto impuesto : prod.getImpuestos()) {
                importe = impuesto.getImporte() * prod.getCantFacturada();
                if ((index = this.impuestosTotales.indexOf(impuesto)) == -1) {
                    nuevo = new ImpuestosProducto();
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
                    this.impuestosTotales.get(index).setImporte(this.impuestosTotales.get(index).getImporte() + importe);
                }
            }
            this.venta.setTotal(this.venta.getTotal() + (prod.getNeto() * prod.getCantFacturada()));
        }
    }

    public void modificarProducto(SelectEvent event) {
        if (this.venta.getEstatus() >= 7) {
            Mensajes.mensajeAlert("La venta ya esta cerrada, no se puede modificar !!!");
        } else if (this.ventaAsegurada) {
            this.producto = (VentaProducto) event.getObject();
//            this.respalda();
        } else {
            Mensajes.mensajeAlert("La venta esta en modo lectura, no se puede modificar !!!");
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", this.ventaAsegurada);
    }

    private void totalSuma(VentaProducto prod) {
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

    public void obtenerDetalle(SelectEvent event) {
        this.venta = (Venta) event.getObject();

        boolean ok = false;
        this.detalle = new ArrayList<>();
        this.impuestosTotales = new ArrayList<>();
        try {
            this.venta.setSubTotal(0);
            this.venta.setDescuento(0);
            this.venta.setImpuesto(0);
            this.venta.setTotal(0);
            TOVenta toMov = this.convertir(this.venta);

            VentaProducto prod;
            this.dao = new DAOVentas();
            for (TOVentaProducto to : (toMov.getReferencia() != 0 ? this.dao.obtenerDetalleVenta(toMov) : this.dao.obtenerDetalleFincado(toMov))) {
                prod = this.convertir(to);
                this.totalSuma(prod);
                this.detalle.add(prod);
            }
            this.venta.setIdUsuario(toMov.getIdUsuario());
            this.venta.setPropietario(toMov.getPropietario());
            this.ventaAsegurada = (this.venta.getIdUsuario() == this.venta.getPropietario());
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    private VentaProducto convertir(TOVentaProducto toProd) throws SQLException {
        VentaProducto prod = new VentaProducto();
        prod.setIdPedido(toProd.getIdPedido());
        prod.setCantOrdenada(toProd.getCantOrdenada());
        prod.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setNeto(prod.getUnitario() + this.dao.obtenerImpuestosProducto(toProd.getIdMovto(), toProd.getIdProducto(), prod.getImpuestos()));
        return prod;
    }

    private TOVentaProducto convertir(VentaProducto prod) {
        TOVentaProducto toProd = new TOVentaProducto();
        toProd.setCantOrdenada(prod.getCantOrdenada());
        toProd.setCantOrdenadaSinCargo(prod.getCantOrdenadaSinCargo());
        movimientos.Movimientos.convertir(prod, toProd);
        return toProd;
    }

    public void actualizaProductoSeleccionado() {
        int idx;
        boolean ok = false;
//        ArrayList<TOMovimientoProducto> agregados;
        this.producto = new VentaProducto(this.mbBuscar.getProducto());
        if ((idx = this.detalle.indexOf(this.producto)) != -1) {
            this.producto = this.detalle.get(idx);
        } else {
            try {
                TOVenta toMov = this.convertir(this.venta);
                TOVentaProducto toProd = new TOVentaProducto();
                toProd.setIdMovto(this.venta.getIdMovto());
                toProd.setIdPedido(this.venta.getIdPedido());
                toProd.setIdProducto(this.producto.getProducto().getIdProducto());
                toProd.setIdImpuestoGrupo(this.producto.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());

                this.dao = new DAOVentas();
                this.dao.agregarProducto(toMov, toProd);
                this.producto.setIdMovto(toProd.getIdMovto());
                this.producto.setIdPedido(toProd.getIdPedido());
                this.detalle.add(this.producto);
//                this.daoMv = new DAOMovimientosOld();
//                agregados=this.daoMv.grabarMovimientoDetalle(true, this.venta.getAlmacen().getIdAlmacen(), this.venta.getIdMovto(), this.venta.getIdMovtoAlmacen(), to, this.producto.getSeparados(), this.venta.getTienda().getIdImpuestoZona());
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

    private TOVenta convertir(Venta mov) {
        TOVenta toMov = new TOVenta();
        movimientos.Movimientos.convertir(mov, toMov);
        toMov.setIdComprobante(mov.getComprobante().getIdComprobante());
        toMov.setIdImpuestoZona(mov.getTienda().getIdImpuestoZona());
        toMov.setIdReferencia(mov.getTienda().getIdTienda());
        toMov.setReferencia(mov.getIdPedido());
        return toMov;
    }

    public void crearVenta() {
        boolean ok = false;
        this.mbClientes.setCliente(this.mbClientes.obtenerCliente(this.mbTiendas.getTienda().getIdCliente()));
        this.venta = new Venta(this.mbAlmacenes.getToAlmacen(), this.mbTiendas.getTienda(), this.mbFormatos.getFormatoSeleccion(), this.mbClientes.getCliente());
        TOVenta to = this.convertir(this.venta);
        try {
            this.dao = new DAOVentas();
            this.dao.agregarVenta(to);
            this.venta.setIdMovto(to.getIdMovto());
            this.venta.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
            this.ventas.add(this.venta);
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        this.detalle = new ArrayList<>();
        this.impuestosTotales = new ArrayList<>();
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    public void nuevaVenta() {
        this.mbGrupos.inicializar();
        this.cambioDeGrupo();
    }

    public void regresarFechaActual() {
        this.fechaInicial = new Date();
        this.obtenerVentas();
    }

    private Venta convertir(TOVenta toMov) {
        Venta mov = new Venta(this.mbAlmacenes.obtenerAlmacen(toMov.getIdAlmacen()), this.mbTiendas.obtenerTienda(toMov.getIdReferencia()));
        mov.setFormato(this.mbFormatos.obtenerFormato(mov.getTienda().getIdFormato()));
        mov.setCliente(this.mbClientes.obtenerCliente(mov.getTienda().getIdCliente()));
        mov.setIdPedidoOC(toMov.getIdPedidoOC());
        mov.setOrdenDeCompra(toMov.getOrdenDeCompra());
        mov.setOrdenDeCompraFecha(toMov.getOrdenDeCompraFecha());
        mov.setCanceladoMotivo(toMov.getCanceladoMotivo());
        mov.setCanceladoFecha(toMov.getCanceladoFecha());
        if (toMov.getIdComprobante() != 0) {
            // Se buscará el comprobante
        }
        movimientos.Movimientos.convertir(toMov, mov);
        mov.setIdPedido(toMov.getReferencia());
        return mov;
    }

    public void obtenerVentas() {
        try {   // Segun fecha y status
            this.ventas = new ArrayList<>();
            this.dao = new DAOVentas();
            for (TOVenta to : this.dao.obtenerVentas(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), (this.pendientes ? 0 : 7), this.fechaInicial)) {
                this.ventas.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cambioDeFormato() {
        this.mbTiendas.obtenerTiendasFormato(this.mbFormatos.getFormatoSeleccion().getIdFormato());
        this.mbTiendas.nuevaTienda();
    }

    public void cambioDeGrupo() {
        this.mbFormatos.cargarFormatosCliente(this.mbGrupos.getClienteGrupoSeleccionado().getIdGrupoCte());
        this.mbTiendas.inicializar();
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

    public ArrayList<Venta> getVentas() {
        return ventas;
    }

    public void setVentas(ArrayList<Venta> ventas) {
        this.ventas = ventas;
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

    public MbAlmacenesJS getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbAlmacenesJS mbAlmacenes) {
        this.mbAlmacenes = mbAlmacenes;
    }
}
