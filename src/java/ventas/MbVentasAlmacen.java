package ventas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import clientes.MbMiniClientes;
import comprobantes.MbComprobantes;
import direccion.MbDireccion;
import direccion.dominio.Direccion;
import formatos.MbFormatos;
import impuestos.dominio.ImpuestosProducto;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import mbMenuClientesGrupos.MbClientesGrupos;
import movimientos.to.TOMovimientoProductoAlmacen;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import pedidos.Pedidos;
import pedidos.dominio.Pedido;
import pedidos.to.TOPedido;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;
import rechazos.to.TORechazoProductoAlmacen;
import tiendas.MbMiniTiendas;
import traspasos.dominio.TraspasoProductoReporte;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;
import utilerias.Numero_a_Letra;
import ventas.dao.DAOVentas;
//import ventas.dominio.Venta;
import ventas.dominio.VentaProducto;
import ventas.dominio.VentaProductoAlmacen;
//import ventas.to.TOVenta;
import ventas.to.TOVentaProducto;
import ventas.to.TOVentaProductoAlmacen;

/**
 *
 * @author jesc
 */
@Named(value = "mbVentasAlmacen")
@SessionScoped
public class MbVentasAlmacen implements Serializable {

    @ManagedProperty(value = "#{mbDireccion}")
    private MbDireccion mbDireccion;
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
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    private ArrayList<Accion> acciones;
    private Pedido venta;
    private ArrayList<Pedido> ventas;
    private VentaProductoAlmacen loteOrigen, loteDestino;
    private ArrayList<VentaProductoAlmacen> detalle, empaqueLotes;
    private ArrayList<ImpuestosProducto> impuestosTotales;
    private double cantTraspasar;
    private boolean pendientes;
    private Date fechaInicial;
    private boolean locked;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAOVentas dao;

    public MbVentasAlmacen() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbDireccion = new MbDireccion();

        this.mbGrupos = new MbClientesGrupos();
        this.mbClientes = new MbMiniClientes();
        this.mbFormatos = new MbFormatos();
        this.mbTiendas = new MbMiniTiendas();
        this.mbBuscar = new MbProductosBuscar();
        this.mbComprobantes = new MbComprobantes();

        this.inicializa();
    }

    private TORechazoProductoAlmacen convertir(TOMovimientoProductoAlmacen to) {
        TORechazoProductoAlmacen toProd = new TORechazoProductoAlmacen();
        toProd.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        toProd.setIdProducto(to.getIdProducto());
        toProd.setLote(to.getLote());
        toProd.setCantidad(to.getCantidad());
        toProd.setFechaCaducidad(to.getFechaCaducidad());
        return toProd;
    }

    private TraspasoProductoReporte convertirProductoReporte(TOVentaProducto toProd) throws SQLException {
        boolean ya = false;
        Producto producto = this.mbBuscar.obtenerProducto(toProd.getIdProducto());
        TraspasoProductoReporte rep = new TraspasoProductoReporte();
        rep.setSku(producto.getCod_pro());
        rep.setEmpaque(producto.toString());
        rep.setCantFacturada(toProd.getCantFacturada());
        rep.setCantSinCargo(toProd.getCantSinCargo());
        rep.setUnitario(toProd.getUnitario());
        for (TOMovimientoProductoAlmacen l : this.dao.obtenerProductoDetalle(this.venta.getIdMovtoAlmacen(), toProd.getIdProducto())) {
            if (l.getCantidad() != 0) {
                if (ya) {
                    rep.getLotes().add(this.convertir(l));
                } else {
                    rep.setLote(l.getLote());
                    rep.setLoteCantidad(l.getCantidad());
                    ya = true;
                }
            }
        }
        return rep;
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

    public void imprimir() {
        Direccion dir;
//        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
//        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        try {
            TOPedido toPed = this.convertir(this.venta);

            dir = this.mbDireccion.obtener(this.venta.getAlmacen().getIdDireccion());
            String cedisDir = dir.toString2();
            String cedisLoc = dir.toString3();

            this.mbClientes.setCliente(this.mbClientes.obtenerCliente(this.venta.getTienda().getIdCliente()));
            dir = this.mbDireccion.obtener(this.mbClientes.getCliente().getIdDireccionFiscal());
            String clienteDir = dir.toString2();
            String clienteLoc = dir.toString3();

            dir = this.mbDireccion.obtener(this.venta.getTienda().getIdDireccion());
            String tiendaDir = dir.toString2();
            String tiendaLoc = dir.toString3();

            double peso = 0;
            double volumen = 0;
            this.venta.setSubTotal(0);
            this.venta.setDescuento(0);
            this.venta.setImpuesto(0);
            this.venta.setTotal(0);
            VentaProducto prod;
            this.impuestosTotales = new ArrayList<>();
            ArrayList<TraspasoProductoReporte> detalleReporte = new ArrayList<>();
            this.dao = new DAOVentas();
            for (TOVentaProducto to : this.dao.obtenerDetalleOficina(toPed, "")) {
                prod = this.convertir(to);
                peso += prod.getProducto().getPeso() * (prod.getCantFacturada() + prod.getCantSinCargo());
                volumen += prod.getProducto().getVolumen() * (prod.getCantFacturada() + prod.getCantSinCargo());
                this.totalSuma(prod);
                if (to.getCantFacturada() + to.getCantSinCargo() != 0) {
                    detalleReporte.add(this.convertirProductoReporte(to));
                }
            }
            String sourceFileName = "C:\\Carlos Pat\\Reportes\\remision.jasper";
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
            Map parameters = new HashMap();
            parameters.put("empresa", this.venta.getAlmacen().getEmpresa());
            parameters.put("cedis", this.venta.getAlmacen().getCedis());
            parameters.put("almacen", this.venta.getAlmacen().getAlmacen());
            parameters.put("cedisDir", cedisDir);
            parameters.put("cedisLoc", cedisLoc);

            parameters.put("clienteRFC", this.mbClientes.getCliente().getRfc());
            parameters.put("cliente", this.mbClientes.getCliente().getContribuyente());
            parameters.put("clienteDir", clienteDir);
            parameters.put("clienteLoc", clienteLoc);
            parameters.put("formaPago", this.mbClientes.getCliente().getDiasCredito() == 0 ? "Contado" : "Crédito " + this.mbClientes.getCliente().getDiasCredito() + " días");

            parameters.put("tienda", "(" + this.venta.getTienda().getCodigoTienda() + ") " + this.venta.getTienda().getTienda());
            parameters.put("tiendaDir", tiendaDir);
            parameters.put("tiendaLoc", tiendaLoc);

            parameters.put("pedido", this.venta.getIdPedido());
            parameters.put("pedidoFecha", this.venta.getPedidoFecha());
            parameters.put("remision", this.venta.getFolio());
            parameters.put("remisionFecha", this.venta.getFecha());
            parameters.put("peso", peso);
            parameters.put("volumen", volumen);

            parameters.put("subTotal", this.venta.getSubTotal());
            parameters.put("descuento", this.venta.getDescuento());
            parameters.put("impuestos", this.impuestosTotales);
            parameters.put("total", this.venta.getTotal());
//            parameters.put("letras", utilerias.NumerosALetrasConvertidor.convertNumberToLetter(this.venta.getTotal()));
            Numero_a_Letra numeroALetra = new Numero_a_Letra();
            parameters.put("letras", numeroALetra.Convertir(String.valueOf((double) Math.round(this.venta.getTotal() * 100) / 100), true, this.venta.getComprobante().getMoneda()));

            parameters.put("idUsuario", this.venta.getIdUsuario());

            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=remision_" + this.venta.getFolio() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void salir() {
        try {
            this.dao = new DAOVentas();
            if (this.venta != null && this.isLocked()) {
                TOPedido toPed = this.convertir(this.venta);
                this.dao.liberarVentaAlmacen(toPed);
                this.venta.setPropietario(0);
                this.setLocked(false);
            }
            this.ventas = new ArrayList<>();
            for (TOPedido to : this.dao.obtenerVentasAlmacen(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), (this.pendientes ? 5 : 7), this.fechaInicial)) {
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
                this.dao = new DAOVentas();
                this.dao.liberarVentaAlmacen(toPed);
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

    public void cerrarVentaAlmacen() {
        boolean ok = false;
        try {
            TOPedido toPed = this.convertir(this.venta);

            this.dao = new DAOVentas();
            this.dao.cerrarVentaAlmacen(toPed);
            this.venta.setEstatus(toPed.getEstatus());
            this.venta.setIdUsuario(toPed.getIdUsuario());
            this.venta.setPropietario(toPed.getPropietario());
            this.setLocked(this.venta.getIdUsuario() == this.venta.getPropietario());
            Mensajes.mensajeSucces("El pedido se cerró correctamente !!!");
            this.obtenerVentas();
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    public void surtirVentaAlmacen() {
        TOPedido toPed = this.convertir(this.venta);
        try {
            this.dao = new DAOVentas();
            for (TOVentaProductoAlmacen toProd : this.dao.sutirVentaAlmacen(toPed)) {
                this.detalle.add(this.convertirAlmacenProducto(toProd));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void actualizaTraspasoLote() {
        boolean ok = false;
        try {
            this.dao = new DAOVentas();
            this.dao.traspasarLote(this.venta.getAlmacen().getIdAlmacen(), this.convertirAlmacenProducto(this.loteOrigen), this.convertirAlmacenProducto(this.loteDestino), this.cantTraspasar);
            this.loteOrigen.setCantidad(this.loteOrigen.getCantidad() - this.cantTraspasar);
            this.loteOrigen.setSeparados(this.loteOrigen.getCantidad());
            if (this.loteDestino.getIdMovtoAlmacen() != 0) {
                int idx = this.detalle.indexOf(this.loteDestino);
                this.setLoteDestino(this.detalle.get(idx));
            } else {
                this.loteDestino.setIdMovtoAlmacen(this.venta.getIdMovtoAlmacen());
                this.detalle.add(this.loteDestino);
                this.loteDestino.setDisponibles(0);
            }
            this.loteDestino.setCantidad(this.loteDestino.getCantidad() + this.cantTraspasar);
            this.loteDestino.setSeparados(this.loteDestino.getCantidad());
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLote", ok);
    }

    private TOVentaProductoAlmacen convertirAlmacenProducto(VentaProductoAlmacen prod) {
        TOVentaProductoAlmacen toProd = new TOVentaProductoAlmacen();
        movimientos.Movimientos.convertir(prod, toProd);
        toProd.setDisponibles(prod.getDisponibles());
        toProd.setFechaCaducidad(prod.getFechaCaducidad());
        return toProd;
    }

    public void inicializaTraspasoLote() {
        boolean ok = false;
        try {
            if (this.venta.getEstatus() != 5) {
                Mensajes.mensajeAlert("La venta ya ha sido cerrada !!!");
            } else {
                this.empaqueLotes = new ArrayList<>();
                this.dao = new DAOVentas();
                for (TOVentaProductoAlmacen to : this.dao.obtenerLotesDisponibles(this.venta.getAlmacen().getIdAlmacen(), this.convertirAlmacenProducto(this.loteOrigen))) {
                    this.empaqueLotes.add(this.convertirAlmacenProducto(to));
                }
                if (this.empaqueLotes.isEmpty()) {
                    Mensajes.mensajeAlert("No hay lotes con existencia disponible para traspasar !!");
                } else {
                    this.cantTraspasar = 0;
                    this.loteDestino = null;
                    ok = true;
                }
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLote", ok);
    }

    private VentaProductoAlmacen convertirAlmacenProducto(TOVentaProductoAlmacen toProd) throws SQLException {
        VentaProductoAlmacen prod = new VentaProductoAlmacen();
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setSeparados(toProd.getCantidad());
        prod.setDisponibles(toProd.getDisponibles());
        prod.setFechaCaducidad(toProd.getFechaCaducidad());
        return prod;
    }

    private TOPedido convertir(Pedido pedido) {
        TOPedido toPed = new TOPedido();
        Pedidos.convertirPedido(pedido, toPed);
        return toPed;
    }

    public void obtenerDetalleAlmacen(SelectEvent event) {
        boolean ok = false;
        this.venta = (Pedido) event.getObject();
        TOPedido toPed = this.convertir(this.venta);
        try {
            this.detalle = new ArrayList<>();
            this.dao = new DAOVentas();
            for (TOVentaProductoAlmacen to : this.dao.obtenerDetalleAlmacen(toPed)) {
                this.detalle.add(this.convertirAlmacenProducto(to));
            }
            this.venta.setIdUsuario(toPed.getIdUsuario());
            this.venta.setPropietario(toPed.getPropietario());
            this.venta.setEstatus(toPed.getEstatus());
            this.setLocked(this.venta.getIdUsuario() == this.venta.getPropietario());
            this.loteOrigen = null;
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

    private Pedido convertir(TOPedido toVta) {
        Pedido vta = new Pedido(this.mbAlmacenes.obtenerAlmacen(toVta.getIdAlmacen()), this.mbTiendas.obtenerTienda(toVta.getIdReferencia()), this.mbComprobantes.obtenerComprobante(toVta.getIdComprobante()));
        Pedidos.convertirPedido(toVta, vta);
        this.mbClientes.setCliente(this.mbClientes.obtenerCliente(vta.getTienda().getIdCliente()));
        return vta;
    }

    private void obtenVentasAlmacen() throws NamingException, SQLException {
        this.ventas = new ArrayList<>();
        this.dao = new DAOVentas();
        for (TOPedido to : this.dao.obtenerVentasAlmacen(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), (this.pendientes ? 5 : 7), this.fechaInicial)) {
            this.ventas.add(this.convertir(to));
        }
    }

    public void obtenerVentas() {
        try {   // Segun fecha y status
            this.obtenVentasAlmacen();
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
        this.venta = new Pedido();
    }

    private void inicializa() {
        this.inicializar();
    }

    public MbDireccion getMbDireccion() {
        return mbDireccion;
    }

    public void setMbDireccion(MbDireccion mbDireccion) {
        this.mbDireccion = mbDireccion;
    }

    public MbAlmacenesJS getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbAlmacenesJS mbAlmacenes) {
        this.mbAlmacenes = mbAlmacenes;
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

    public MbComprobantes getMbComprobantes() {
        return mbComprobantes;
    }

    public void setMbComprobantes(MbComprobantes mbComprobantes) {
        this.mbComprobantes = mbComprobantes;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
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

    public Pedido getVenta() {
        return venta;
    }

    public void setVenta(Pedido venta) {
        this.venta = venta;
    }

    public ArrayList<Pedido> getVentas() {
        return ventas;
    }

    public void setVentas(ArrayList<Pedido> ventas) {
        this.ventas = ventas;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public VentaProductoAlmacen getLoteOrigen() {
        return loteOrigen;
    }

    public void setLoteOrigen(VentaProductoAlmacen loteOrigen) {
        this.loteOrigen = loteOrigen;
    }

    public VentaProductoAlmacen getLoteDestino() {
        return loteDestino;
    }

    public void setLoteDestino(VentaProductoAlmacen loteDestino) {
        this.loteDestino = loteDestino;
    }

    public ArrayList<VentaProductoAlmacen> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<VentaProductoAlmacen> detalle) {
        this.detalle = detalle;
    }

    public ArrayList<VentaProductoAlmacen> getEmpaqueLotes() {
        return empaqueLotes;
    }

    public void setEmpaqueLotes(ArrayList<VentaProductoAlmacen> empaqueLotes) {
        this.empaqueLotes = empaqueLotes;
    }

    public double getCantTraspasar() {
        return cantTraspasar;
    }

    public void setCantTraspasar(double cantTraspasar) {
        this.cantTraspasar = cantTraspasar;
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
}
