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
import movimientos.dao.DAOMovimientos;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
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
    private Pedido pedido;
    private ArrayList<Pedido> pedidos;
    private ArrayList<ImpuestosProducto> impuestosTotales;
    private ArrayList<PedidoProducto> detalle, similares;
    private PedidoProducto producto, similar;
    private double cantTraspasar;
    private String ordenDeCompra;
    private Date ordenDeCompraFecha;
    private boolean asegurado;
    private DAOMovimientos daoMv;
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

        this.inicializa();
    }

    public void cancelarPedido() {
        boolean ok = false;
        if (this.pedido.getCancelacionMotivo().isEmpty()) {
            Mensajes.mensajeAlert("Se requiere el motivo de cancelacion !!!");
        } else {
            try {
                this.daoMv = new DAOMovimientos();
                TOPedido toPed = this.convertir(this.pedido);
                this.daoMv.cancelarPedido(toPed);
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
        this.asegurado = false;
        try {
            this.daoMv = new DAOMovimientos();
            TOPedido toPed = this.convertir(this.pedido);
            this.daoMv.eliminarPedido(toPed);
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

    public void cerrarPedido() {
        boolean ok = false;
        try {
            this.daoMv = new DAOMovimientos();
            TOPedido toPed = this.convertir(this.pedido);
            this.daoMv.cerrarPedido(toPed);
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

    public void traspasoSimilar() {
        boolean ok = false;
        this.similares = new ArrayList<>();
        try {
            this.daoMv = new DAOMovimientos();
            for (TOProductoPedido to : daoMv.obtenerSimilaresPedido(this.pedido.getIdPedido(), this.producto.getProducto().getIdProducto())) {
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

                this.daoMv = new DAOMovimientos();
                this.daoMv.trasferirSinCargo(this.pedido.getIdPedido(), this.producto.getProducto().getIdProducto(), toSimilar, this.pedido.getTienda().getIdImpuestoZona(), this.cantTraspasar);

                this.daoMv = new DAOMovimientos();
                for (TOProductoPedido to : this.daoMv.obtenerPedidoSimilares(this.pedido.getIdPedido(), toProd.getIdProducto())) {
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

    private void totalResta(PedidoProducto prod) {
        double resta;
        resta = prod.getPrecio() * prod.getCantOrdenada();
        this.pedido.setSubTotal(this.pedido.getSubTotal() - Math.round(resta * 1000000.00) / 1000000.00);

        resta = prod.getPrecio() - prod.getUnitario();
        resta = resta * prod.getCantOrdenada();
        this.pedido.setDescuento(this.pedido.getDescuento() - Math.round(resta * 1000000.00) / 1000000.00);

        resta = prod.getNeto() - prod.getUnitario();
        resta = resta * prod.getCantOrdenada();
        this.pedido.setImpuesto(this.pedido.getImpuesto() - Math.round(resta * 1000000.00) / 1000000.00);

        resta = prod.getNeto() * prod.getCantOrdenada();
        this.pedido.setTotal(this.pedido.getTotal() - Math.round(resta * 1000000.00) / 1000000.00);

        int index;
        double importe;
        ImpuestosProducto nuevo;
        for (ImpuestosProducto impuesto : prod.getImpuestos()) {
            importe = impuesto.getImporte() * prod.getCantOrdenada();
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
                this.impuestosTotales.get(index).setImporte(this.impuestosTotales.get(index).getImporte() - importe);
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
        to.setCosto(p.getPrecio());
        to.setDesctoProducto1(p.getDescuento());
        to.setUnitario(p.getUnitario());
        to.setIdImpuestoGrupo(p.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
        return to;
    }

    public void actualizaProducto() {
        boolean ok = false;
        double cantOrdenadaOld = this.producto.getCantOrdenadaTotal() - this.producto.getCantOrdenadaSinCargo();
        try {
            if (this.producto.getCantOrdenada() != cantOrdenadaOld) {
                TOPedido toPed = this.convertir(this.pedido);
                TOProductoPedido toProd = this.convertir(this.producto);
                this.producto.setCantOrdenada(cantOrdenadaOld);

                this.daoMv = new DAOMovimientos();
                daoMv.grabarPedidoDetalle(toPed, toProd);
                this.pedido.setCantArticulos(this.pedido.getCantArticulos() - this.producto.getCantOrdenadaTotal());
                this.totalResta(this.producto);

                this.producto.setCantOrdenada(toProd.getCantOrdenada());
                this.producto.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
                this.producto.setCantOrdenadaTotal(toProd.getCantOrdenada() + toProd.getCantOrdenadaSinCargo());
                this.pedido.setCantArticulos(this.pedido.getCantArticulos() + this.producto.getCantOrdenadaTotal());
                this.totalSuma(this.producto);
            }
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            this.producto.setCantFacturada(cantOrdenadaOld);
        }
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
                this.asegurado = false;
                this.daoMv = new DAOMovimientos();
                ok = this.daoMv.liberarPedido(this.pedido.getIdMovto());
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
            this.producto.setIdPedido(this.pedido.getIdPedido());
            try {
                TOPedido toPed = this.convertir(this.pedido);
                TOProductoPedido toProd = new TOProductoPedido();
                toProd.setIdMovto(this.pedido.getIdMovto());
                toProd.setIdPedido(this.pedido.getIdPedido());
                toProd.setIdProducto(this.producto.getProducto().getIdProducto());
                toProd.setIdImpuestoGrupo(this.producto.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());

                this.daoMv = new DAOMovimientos();
                this.daoMv.agregarProductoPedido(toPed, toProd);
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
        double suma;
        suma = prod.getPrecio() * prod.getCantOrdenada();   // Calcula el subTotal
        this.pedido.setSubTotal(this.pedido.getSubTotal() + Math.round(suma * 1000000.00) / 1000000.00);    // Suma el importe el subtotal

        suma = prod.getPrecio() - prod.getUnitario();       // Obtiene el descuento por diferencia.
        suma = suma * prod.getCantOrdenada();               // Calcula el importe de descuento
        this.pedido.setDescuento(this.pedido.getDescuento() + Math.round(suma * 1000000.00) / 1000000.00);  // Suma el descuento

        suma = prod.getNeto() - prod.getUnitario();         // Obtiene el impuesto por diferencia
        suma = suma * prod.getCantOrdenada();               // Calcula el importe de impuestos
        this.pedido.setImpuesto(this.pedido.getImpuesto() + Math.round(suma * 1000000.00) / 1000000.00);    // Suma los impuestos

        suma = prod.getNeto() * prod.getCantOrdenada();     // Calcula el importe total
        this.pedido.setTotal(this.pedido.getTotal() + Math.round(suma * 1000000.00) / 1000000.00);          // Suma el importe al total
        
        int index;
        double importe;
        ImpuestosProducto nuevo;
        for (ImpuestosProducto impuesto : prod.getImpuestos()) {
            importe = impuesto.getImporte() * prod.getCantOrdenada();
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
    }

    private PedidoProducto convertir(TOProductoPedido to) throws SQLException {
        PedidoProducto p = new PedidoProducto();
        p.setIdPedido(to.getIdPedido());
        p.setIdMovto(to.getIdMovto());
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantOrdenada(to.getCantOrdenada());
        p.setCantOrdenadaSinCargo(to.getCantOrdenadaSinCargo());
        p.setCantOrdenadaTotal(to.getCantOrdenada() + to.getCantOrdenadaSinCargo());
        p.setCantFacturada(to.getCantFacturada());
        p.setCantSinCargo(to.getCantSinCargo());
        p.setPrecio(to.getCosto());
        p.setDescuento(to.getDesctoProducto1());
        p.setUnitario(to.getUnitario());
        p.setNeto(p.getUnitario() + this.daoMv.obtenerImpuestosProducto(to.getIdMovto(), to.getIdProducto(), p.getImpuestos()));
        return p;
    }

    public void obtenerDetalle(SelectEvent event) {
        int n = 0;
        boolean ok = false;
        this.pedido = (Pedido) event.getObject();
        this.detalle = new ArrayList<>();
        this.impuestosTotales = new ArrayList<>();
        PedidoProducto prod;
        try {
            this.daoMv = new DAOMovimientos();
            try {
                this.asegurado = this.daoMv.asegurarPedido(this.pedido.getIdMovto());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
            ArrayList<TOProductoPedido> tos = this.daoMv.obtenerPedidoDetalle(this.pedido.getIdPedido());
            if (this.pedido.getEstatus() == 0) {
                TOPedido toPed = this.convertir(this.pedido);
                this.daoMv.actualizarPedido(toPed, tos);
            }
            this.pedido.setSubTotal(0);
            this.pedido.setDescuento(0);
            this.pedido.setImpuesto(0);
            this.pedido.setTotal(0);
            for (TOProductoPedido to : tos) {
                prod = this.convertir(to);
                this.totalSuma(prod);
                this.detalle.add(prod);
                n += to.getCantOrdenada() + to.getCantOrdenadaSinCargo();
            }
            this.pedido.setCantArticulos(n);
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
        to.setIdPedidoOC(p.getIdPedidoOC());
        to.setOrdenDeCompra(p.getOrdenDeCompra());
        to.setOrdenDeCompraFecha(p.getOrdenDeCompraFecha());
        to.setCanceladoMotivo(p.getCancelacionMotivo());
        to.setCanceladoFecha(p.getCancelacionFecha());

        to.setIdMovto(p.getIdMovto());
        to.setIdTipo(28);
        to.setIdEmpresa(this.mbAlmacenes.getToAlmacen().getIdEmpresa());
        to.setIdCedis(this.mbAlmacenes.getToAlmacen().getIdCedis());
        to.setIdAlmacen(this.mbAlmacenes.getToAlmacen().getIdAlmacen());
        to.setFolio(0);
        to.setIdComprobante(0);
        to.setDesctoComercial(p.getCliente().getDesctoComercial());
        to.setDesctoProntoPago(p.getDesctoProntoPago());
        to.setIdImpuestoZona(p.getTienda().getIdImpuestoZona());
        to.setIdMoneda(1);
        to.setTipoDeCambio(1);
        to.setFecha(p.getFecha());
        to.setIdReferencia(p.getTienda().getIdTienda());
        to.setReferencia(p.getIdPedido());
        to.setEstatus(p.getEstatus());
        to.setIdMovtoAlmacen(p.getIdMovtoAlmacen());
        return to;
    }

    public void crearPedido() {
        boolean ok = false;
        this.pedido = new Pedido(this.mbTiendas.getTienda(), this.mbFormatos.getFormatoSeleccion(), this.mbClientes.getCliente());
        this.pedido.setDesctoComercial(this.mbClientes.getCliente().getDesctoComercial());
        this.pedido.setDesctoProntoPago(0);
        this.pedido.setOrdenDeCompra(this.ordenDeCompra);
        this.pedido.setOrdenDeCompraFecha(this.ordenDeCompraFecha);
        TOPedido to = this.convertir(this.pedido);
        try {
            this.daoMv = new DAOMovimientos();
            this.daoMv.agregarPedido(to);
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
        p.setIdPedidoOC(to.getIdPedidoOC());
        p.setOrdenDeCompra(to.getOrdenDeCompra());
        p.setOrdenDeCompraFecha(to.getOrdenDeCompraFecha());
        p.setCancelacionFecha(to.getCanceladoFecha());
        p.setCancelacionMotivo(to.getCanceladoMotivo());

        p.setIdMovto(to.getIdMovto());
        p.setIdEmpresa(to.getIdEmpresa());
        p.setTienda(this.mbTiendas.obtenerTienda(to.getIdReferencia()));
        p.setFormato(this.mbFormatos.obtenerFormato(p.getTienda().getIdFormato()));
        p.setCliente(this.mbClientes.obtenerCliente(p.getTienda().getIdCliente()));
        if (to.getEstatus() == 0) { // Si el pedido esta abierto hay que actualizar
            p.setDesctoComercial(p.getCliente().getDesctoComercial());
            p.setDesctoProntoPago(0);
        } else {    // Si ya esta cerrado, leer los datos de la base
            p.setDesctoComercial(to.getDesctoComercial());
            p.setDesctoProntoPago(to.getDesctoProntoPago());
        }
        p.setFecha(to.getFecha());
        p.setEstatus(to.getEstatus());
        p.setIdPedido(to.getReferencia());
        p.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        return p;
    }

    public void obtenerPedidos() {
        try {   // Segun fecha y status
            this.pedidos = new ArrayList<>();
            this.daoMv = new DAOMovimientos();
            for (TOPedido to : this.daoMv.obtenerPedidos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), (this.pendientes ? 0 : 1), this.fechaInicial)) {
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

        this.pendientes = true;
        this.fechaInicial = new Date();
        this.pedidos = new ArrayList<>();
        this.pedido = new Pedido();
        this.detalle = new ArrayList<>();
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
