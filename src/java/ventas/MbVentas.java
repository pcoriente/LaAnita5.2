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
    private Venta venta;
    private ArrayList<Venta> ventas, pedidos;
    private boolean locked;
    private ArrayList<VentaProducto> detalle, similares;
    private VentaProducto producto, similar;
    private ArrayList<ImpuestosProducto> impuestosTotales;
    private double cantTraspasar;
    private boolean pendientes;
    private Date fechaInicial;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAOVentas dao;

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
        VentaProducto prod;
        boolean completo = true;
        TOVenta toMov = this.convertir(this.venta);
        this.venta.setSubTotal(0);
        this.venta.setDescuento(0);
        this.venta.setImpuesto(0);
        this.venta.setTotal(0);
        this.detalle = new ArrayList<>();
        this.impuestosTotales = new ArrayList<>();
        try {
            this.dao = new DAOVentas();
            for (TOVentaProducto to : this.dao.surtirFincado(toMov)) {
                prod = this.convertir(to);
                if (prod.getCantOrdenada() + prod.getCantOrdenadaSinCargo() != prod.getCantFacturada() + prod.getCantSinCargo()) {
                    completo = false;
                }
                this.totalSuma(prod);
                this.detalle.add(prod);
            }
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
            this.dao = new DAOVentas();
            for (TOVenta to : this.dao.obtenerPedidos(this.mbAlmacenes.getToAlmacen().getIdAlmacen())) {
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
            this.dao = new DAOVentas();
            if (this.venta != null && this.isLocked()) {
                TOVenta toVta = this.convertir(this.venta);
                this.dao.liberarVentaOficina(toVta);
                this.venta.setPropietario(0);
                this.setLocked(false);
            }
            this.ventas = new ArrayList<>();
            for (TOVenta to : this.dao.obtenerVentasOficina(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.pendientes ? 0 : 7, this.fechaInicial)) {
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
            TOVenta toVta = this.convertir(this.venta);
            try {

                this.dao = new DAOVentas();
                this.dao.liberarVentaOficina(toVta);
                this.venta.setPropietario(toVta.getPropietario());
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
        this.locked = false;
        try {
            TOVenta toMov = this.convertir(this.venta);

            this.dao = new DAOVentas();
            this.dao.eliminarVentaOficina(toMov);
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
            TOVenta toVta = this.convertir(this.venta);

            this.dao = new DAOVentas();
            this.dao.cerrarVentaOficina(toVta);
            this.venta.setFolio(toVta.getFolio());
            this.venta.setEstatus(toVta.getEstatus());
            this.venta.getComprobante().setTipo("2");
            this.venta.getComprobante().setNumero(String.valueOf(toVta.getFolio()));
            this.setLocked(this.venta.getIdUsuario() == this.venta.getPropietario());
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
        boolean okSimilares = false;
        try {
            if (this.cantTraspasar > 0 && this.cantTraspasar <= this.producto.getCantSinCargo()) {
                TOVenta toVta = this.convertir(this.venta);
                TOVentaProducto toProd = this.convertir(this.producto);
                TOVentaProducto toSimilar = this.convertir(this.similar);

                this.dao = new DAOVentas();
//                Si el movimiento es solo de prod a similar, porque transferirSinCargo devuelve un arreglo con todos los similares ????;
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
//        boolean ok = false;
//        if (this.venta.getEstatus() >= 7) {
//            Mensajes.mensajeAlert("La venta ya esta cerrada, no se puede modificar !!!");
//        } else if (this.isLocked()) {
        this.producto = (VentaProducto) event.getObject();
//            ok = true;
//        } else {
//            Mensajes.mensajeAlert("La venta esta en modo lectura, no se puede modificar !!!");
//        }
        RequestContext context = RequestContext.getCurrentInstance();
//        context.addCallbackParam("okEdicion", ok);
        context.addCallbackParam("okEdicion", true);
    }

    public void actualizaProductoSeleccionado() {
        int idx;
        boolean ok = false;
        this.producto = new VentaProducto(this.mbBuscar.getProducto());
        if ((idx = this.detalle.indexOf(this.producto)) != -1) {
            this.producto = this.detalle.get(idx);
        } else {
            TOVenta toVta = this.convertir(this.venta);
            TOVentaProducto toProd = new TOVentaProducto();
            toProd.setIdMovto(this.venta.getIdMovto());
            toProd.setIdPedido(this.venta.getIdPedido());
            toProd.setIdProducto(this.producto.getProducto().getIdProducto());
            toProd.setIdImpuestoGrupo(this.producto.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
            try {
                this.dao = new DAOVentas();
                this.dao.agregarProducto(toVta, toProd);
                this.producto = this.convertir(toProd);
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
            for (TOVentaProducto to : this.dao.obtenerDetalleOficina(toMov)) {
                prod = this.convertir(to);
                this.totalSuma(prod);
                this.detalle.add(prod);
            }
            this.venta.setEstatus(toMov.getEstatus());
            this.venta.setIdUsuario(toMov.getIdUsuario());
            this.venta.setPropietario(toMov.getPropietario());
            this.setLocked(this.venta.getIdUsuario() == this.venta.getPropietario());
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    private TOVenta convertir(Venta venta) {
        return Ventas.convertir(venta);
    }

    private boolean validar() {
        boolean ok = false;
        if (this.mbAlmacenes.getToAlmacen() == null) {
            Mensajes.mensajeAlert("Debe seleccionar un almacen !!!");
        } else if (this.mbTiendas.getTienda() == null) {
            Mensajes.mensajeAlert("Debe seleccionar una tienda !!!");
        } else if (this.mbComprobantes.getMbMonedas().getSeleccionMoneda() == null || this.mbComprobantes.getMbMonedas().getSeleccionMoneda().getIdMoneda()==0) {
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
            TOVenta toVta = this.convertir(this.venta);
            try {
                this.dao = new DAOVentas();
                this.dao.agregarVenta(toVta, 1);
                this.venta.setIdMovto(toVta.getIdMovto());
                this.venta.setIdMovtoAlmacen(toVta.getIdMovtoAlmacen());
                this.venta.setFecha(toVta.getFecha());
                this.venta.setIdUsuario(toVta.getIdUsuario());
                this.venta.setPropietario(toVta.getPropietario());
                this.venta.setEstatus(toVta.getEstatus());
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
            for (TOVenta to : this.dao.obtenerVentasOficina(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.pendientes ? 0 : 7, this.fechaInicial)) {
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
        this.venta = new Venta();
        this.detalle = new ArrayList<>();
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
