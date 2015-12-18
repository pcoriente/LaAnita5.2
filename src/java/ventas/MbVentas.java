package ventas;

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
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import mbMenuClientesGrupos.MbClientesGrupos;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import tiendas.MbMiniTiendas;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;
import ventas.dao.DAOVentas;
import ventas.dominio.Venta;
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
    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;
    private DAOVentas dao;
    private ArrayList<Venta> ventas, pedidos;
    private Venta venta;
    private boolean ventaAsegurada;
    private ArrayList<VentaProducto> detalle, similares;
    private VentaProducto producto, similar;
    private ArrayList<ImpuestosProducto> impuestosTotales;
//    private ArrayList<VentaProductoAlmacen> almacenDetalle, empaqueLotes;
//    private VentaProductoAlmacen loteOrigen, loteDestino;
    private double cantTraspasar;
    private boolean pendientes;
    private Date fechaInicial;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private String listaTitulo;

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
    
    public void actualizaTraspasoSimilar() {
//        int idx;
//        VentaProducto prod;
//        VentaProducto prodOld;
        boolean okSimilares = false;
        try {
            if (this.cantTraspasar > 0 && this.cantTraspasar <= this.producto.getCantSinCargo()) {
                TOVenta toVta = this.convertir(this.venta);
                TOVentaProducto toProd = this.convertir(this.producto);
                TOVentaProducto toSimilar = this.convertir(this.similar);

                this.dao = new DAOVentas();
//                Si el movimiento es solo de prod a similar, proque transferirSinCargo devuelve un arreglo con todos los similares ????;

//                for (TOVentaProducto to : this.dao.tranferirSinCargo(toVta, toProd, toSimilar, this.venta.getTienda().getIdImpuestoZona(), this.cantTraspasar)) {
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
                this.dao.tranferirSinCargo(toVta, toProd, toSimilar, this.venta.getTienda().getIdTienda(), this.cantTraspasar);
                this.producto.setCantSinCargo(this.producto.getCantSinCargo() - this.cantTraspasar);
                this.detalle.add(this.convertir(toSimilar));
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
            this.dao = new DAOVentas();
            for (TOVentaProducto to : this.dao.obtenerSimilares(this.producto.getIdMovto(), this.producto.getProducto().getIdProducto())) {
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

    private void actualizaTotales() {
        this.venta.setSubTotal(0);
        this.venta.setDescuento(0);
        this.venta.setImpuesto(0);
        this.venta.setTotal(0);
        this.impuestosTotales = new ArrayList<>();
        for (VentaProducto prod : this.detalle) {
            this.totalSuma(prod);
        }
    }

    public void surtirFincado() {
        VentaProducto prod;
        boolean completo = true;
        TOVenta toMov = this.convertir(this.venta);
        this.detalle = new ArrayList<>();
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
        } catch (Exception ex) {
            Mensajes.mensajeAlert(ex.getMessage());
        }
    }

    public void eliminarVenta() {
        boolean ok = false;
        this.ventaAsegurada = false;
        try {
            TOVenta toMov = this.convertir(this.venta);

            this.dao = new DAOVentas();
            this.dao.eliminarVenta(toMov);
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

    public void cerrarVenta() {
        boolean ok = false;
        try {
            TOVenta toMov = this.convertir(this.venta);

            this.dao = new DAOVentas();
            this.dao.cerrarVenta(toMov);
            this.venta.setFolio(toMov.getFolio());
            this.venta.setEstatus(toMov.getEstatus());
            this.venta.getComprobante().setTipo("2");
            this.venta.getComprobante().setNumero(String.valueOf(toMov.getFolio()));
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

    public void generarPedidoVenta() {
        boolean ok = false;
        VentaProducto prod;
        try {
            TOVenta toMov = this.convertir(this.venta);

            this.dao = new DAOVentas();
            ArrayList<TOVentaProducto> det = this.dao.generarPedidoVenta(toMov);
            this.venta = this.convertir(toMov);
            this.venta.setSubTotal(0);
            this.venta.setDescuento(0);
            this.venta.setImpuesto(0);
            this.venta.setTotal(0);
            this.impuestosTotales = new ArrayList<>();
            this.detalle = new ArrayList<>();
            for (TOVentaProducto to : det) {
                prod = this.convertir(to);
                this.totalSuma(prod);
                this.detalle.add(prod);
            }
            this.venta.setIdUsuario(toMov.getIdUsuario());
            this.venta.setPropietario(toMov.getPropietario());
            this.ventaAsegurada = (this.venta.getIdUsuario() == this.venta.getPropietario());
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

    private TOVentaProducto convertir(VentaProducto prod) {
        TOVentaProducto toProd = new TOVentaProducto();
        toProd.setCantOrdenada(prod.getCantOrdenada());
        toProd.setCantOrdenadaSinCargo(prod.getCantOrdenadaSinCargo());
        movimientos.Movimientos.convertir(prod, toProd);
        return toProd;
    }

    public void actualizarProductoCantidad() {
        boolean ok = false;
        if (this.producto.getCantFacturada() < 0) {
            Mensajes.mensajeAlert("La cantidad facturada no debe ser menor que cero !!!");
        } else if (this.venta.getIdPedido() != 0 && this.producto.getCantFacturada() > this.producto.getCantOrdenada()) {
            Mensajes.mensajeAlert("La cantidad facturada no debe ser mayor a la cantidad ordenada !!!");
        } else if (this.producto.getCantFacturada() + this.producto.getCantSinCargo() != this.producto.getSeparados()) {
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
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }

    public void modificarProducto(SelectEvent event) {
        if (this.venta.getEstatus() >= 7) {
            Mensajes.mensajeAlert("La venta ya esta cerrada, no se puede modificar !!!");
        } else if (this.ventaAsegurada) {
            this.producto = (VentaProducto) event.getObject();
        } else {
            Mensajes.mensajeAlert("La venta esta en modo lectura, no se puede modificar !!!");
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", this.ventaAsegurada);
    }

    public void liberarVenta() {
        boolean ok = false;
        if (this.venta == null) {
            ok = true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else if (this.ventaAsegurada) {
            try {

                this.dao = new DAOVentas();
                this.dao.liberarVenta(this.venta.getIdMovto());
                this.ventaAsegurada = false;
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

    public void obtenerDetalle(SelectEvent event) {
        this.venta = (Venta) event.getObject();

        boolean ok = false;
        VentaProducto prod;
        this.detalle = new ArrayList<>();
        this.impuestosTotales = new ArrayList<>();
        try {
            this.venta.setSubTotal(0);
            this.venta.setDescuento(0);
            this.venta.setImpuesto(0);
            this.venta.setTotal(0);
            TOVenta toMov = this.convertir(this.venta);

            this.dao = new DAOVentas();
            for (TOVentaProducto to : this.dao.obtenerDetalle(toMov)) {
                prod = this.convertir(to);
                this.totalSuma(prod);
                this.detalle.add(prod);
            }
            this.venta.setEstatus(toMov.getEstatus());
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

    private TOVenta convertir(Venta venta) {
        TOVenta toMov = new TOVenta();
        toMov.setIdPedidoOC(venta.getIdPedidoOC());
        toMov.setOrdenDeCompra(venta.getOrdenDeCompra());
        toMov.setOrdenDeCompraFecha(venta.getOrdenDeCompraFecha());
        toMov.setCanceladoMotivo(venta.getCanceladoMotivo());
        toMov.setCanceladoFecha(venta.getCanceladoFecha());
        movimientos.Movimientos.convertir(venta, toMov);
        toMov.setIdComprobante(venta.getComprobante() == null ? 0 : venta.getComprobante().getIdComprobante());
        toMov.setIdImpuestoZona(venta.getTienda().getIdImpuestoZona());
        toMov.setIdReferencia(venta.getTienda().getIdTienda());
        toMov.setReferencia(venta.getIdPedido());
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

    public void crearVenta() {
        boolean ok = false;
        if (this.validar()) {
            this.mbClientes.setCliente(this.mbClientes.obtenerCliente(this.mbTiendas.getTienda().getIdCliente()));
            this.venta = new Venta(this.mbAlmacenes.getToAlmacen(), this.mbTiendas.getTienda(), this.mbComprobantes.getMbMonedas().getSeleccionMoneda());
            this.venta.setDesctoComercial(this.mbClientes.getCliente().getDesctoComercial());
            TOVenta to = this.convertir(this.venta);
            try {
                this.dao = new DAOVentas();
                this.dao.agregarVenta(to, 1);
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
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

//    public void regresarFechaActual() {
//        this.fechaInicial = new Date();
//        this.obtenerVentas();
//    }
    public void obtenerPedidos() {
        try {   // Segun fecha y status
            this.venta = null;
            this.pedidos = new ArrayList<>();
            this.dao = new DAOVentas();
            for (TOVenta to : this.dao.obtenerPedidos(this.mbAlmacenes.getToAlmacen().getIdAlmacen())) {
                this.pedidos.add(this.convertir(to));
            }
            this.listaTitulo = "Lista de pedidos pendientes";
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private Venta convertir(TOVenta toVta) {
        Venta vta = new Venta(this.mbAlmacenes.obtenerAlmacen(toVta.getIdAlmacen()), this.mbTiendas.obtenerTienda(toVta.getIdReferencia()), this.mbComprobantes.obtenerComprobante(toVta.getIdComprobante()));
        Ventas.convertir(toVta, vta);
        this.mbClientes.setCliente(this.mbClientes.obtenerCliente(vta.getTienda().getIdCliente()));
        if (toVta.getEstatus() == 0) {
            // Si la venta todavia esta pendiente, se actualiza con datos del cliente
            // Si ya esta cerrada, se queda con lo que se leyo de la base
            vta.setDesctoComercial(this.mbClientes.getCliente().getDesctoComercial());
            vta.setDesctoProntoPago(0);
        }
        return vta;
    }

    public void obtenerVentas() {
        try {   // Segun fecha y status
            this.ventas = new ArrayList<>();
            this.dao = new DAOVentas();
            for (TOVenta to : this.dao.obtenerVentas(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), (this.pendientes ? 0 : 7), this.fechaInicial)) {
                this.ventas.add(this.convertir(to));
            }
            this.listaTitulo = this.pendientes ? "Lista de ventas pendientes" : "Lista de ventas";
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

    public void cambioDeCliente() {
        this.mbTiendas.obtenerTiendasCliente(this.mbClientes.getCliente().getIdCliente());
        this.mbTiendas.nuevaTienda();
    }

    public void nuevaVenta() {
//        this.mbGrupos.inicializar();
//        this.cambioDeGrupo();
        this.mbClientes.obtenerClientesCedis();
        this.mbClientes.nuevoCliente();
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
        this.venta = new Venta();
        this.detalle = new ArrayList<>();
        this.listaTitulo = "Lista de Ventas pendientes";
    }

    private void inicializa() {
        this.inicializar();
    }

    public ArrayList<Venta> getPedidos() {
        return pedidos;
    }

    public void setPedidos(ArrayList<Venta> pedidos) {
        this.pedidos = pedidos;
    }

    public String getListaTitulo() {
        return listaTitulo;
    }

    public void setListaTitulo(String listaTitulo) {
        this.listaTitulo = listaTitulo;
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
//
//    public ArrayList<VentaProductoAlmacen> getAlmacenDetalle() {
//        return almacenDetalle;
//    }
//
//    public void setAlmacenDetalle(ArrayList<VentaProductoAlmacen> almacenDetalle) {
//        this.almacenDetalle = almacenDetalle;
//    }
//
//    public ArrayList<VentaProductoAlmacen> getEmpaqueLotes() {
//        return empaqueLotes;
//    }
//
//    public void setEmpaqueLotes(ArrayList<VentaProductoAlmacen> empaqueLotes) {
//        this.empaqueLotes = empaqueLotes;
//    }
//
//    public VentaProductoAlmacen getLoteOrigen() {
//        return loteOrigen;
//    }
//
//    public void setLoteOrigen(VentaProductoAlmacen loteOrigen) {
//        this.loteOrigen = loteOrigen;
//    }
//
//    public VentaProductoAlmacen getLoteDestino() {
//        return loteDestino;
//    }
//
//    public void setLoteDestino(VentaProductoAlmacen loteDestino) {
//        this.loteDestino = loteDestino;
//    }

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
