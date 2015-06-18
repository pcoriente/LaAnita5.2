package entradas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import compras.dominio.CompraOficina;
import entradas.dominio.MovimientoProducto;
import entradas.dominio.MovimientoProductoReporte;
import movimientos.to.TOMovimiento;
import movimientos.to.TOMovimientoProducto;
import impuestos.dominio.ImpuestosProducto;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import monedas.MbMonedas;
import movimientos.dao.DAOMovimientos;
import movimientos.to.TOMovimientoAlmacen;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import ordenesDeCompra.MbOrdenCompra;
import org.primefaces.context.RequestContext;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;
import proveedores.MbMiniProveedor;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jsolis
 */
@Named(value = "mbEntradas")
@SessionScoped
public class MbEntradas implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniProveedor}")
    private MbMiniProveedor mbProveedores;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;
    @ManagedProperty(value = "#{mbOrdenCompra}")
    private MbOrdenCompra mbOrdenCompra;
    @ManagedProperty(value = "#{mbMonedas}")
    private MbMonedas mbMonedas;
    private boolean modoEdicion;
//    private OrdenCompraEncabezado ordenCompra;
    private CompraOficina entrada;
    private CompraOficina selEntrada;
    private ArrayList<CompraOficina> entradas;
    private MovimientoProducto entradaProducto;
    private MovimientoProducto resEntradaProducto;
    private ArrayList<MovimientoProducto> entradaDetalle;
    private Date fechaIniPeriodo = new Date();
    private Date fechaFinPeriodo = new Date();
//    private boolean sinOrden;
    private double tipoDeCambio;  // Solo sirve para cuando hay cambio en el valor del tipo de cambio
    private int idModulo;
    private boolean grabar;
    private DAOMovimientos dao;
//    private DAOImpuestosProducto daoImps;
    private TimeZone zonaHoraria = TimeZone.getDefault();

    public MbEntradas() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbProveedores = new MbMiniProveedor();

        this.mbBuscar = new MbProductosBuscar();
        this.mbComprobantes = new MbComprobantes();
        this.mbOrdenCompra = new MbOrdenCompra();
        this.mbMonedas = new MbMonedas();
        this.inicializaLocales();
    }

    public boolean prueba() {
        boolean disabled = false;
        if (this.entrada.getEstatus() != 0) {
            disabled = true;
        } else if (this.entrada.getIdOrdenCompra() != 0) {
            disabled = true;
        } else if (this.entradaProducto == null) {
            disabled = true;
        }
        return disabled;
    }

    public void eliminarProducto() {
        if (this.entradaProducto != null) {
            this.entradaDetalle.remove(this.entradaProducto);
            this.resEntradaProducto = this.entradaProducto;
            this.restaTotales();
            this.entradaProducto = null;
        } else {
            Mensajes.mensajeAlert("No hay producto seleccionado !!!");
        }
    }

    private MovimientoProductoReporte convertir(MovimientoProducto prod) {
        MovimientoProductoReporte rep = new MovimientoProductoReporte();
        rep.setCantFacturada(prod.getCantFacturada());
        rep.setCantOrdenada(prod.getCantOrdenada());
        rep.setCantOrdenadaSinCargo(prod.getCantOrdenadaSinCargo());
        rep.setCantRecibida(prod.getCantRecibida());
        rep.setCantSinCargo(prod.getCantSinCargo());
        rep.setCosto(prod.getCosto());
        rep.setCostoOrdenado(prod.getCostoOrdenado());
        rep.setCostoPromedio(prod.getCostoPromedio());
        rep.setDesctoConfidencial(prod.getDesctoConfidencial());
        rep.setDesctoProducto1(prod.getDesctoProducto1());
        rep.setDesctoProducto2(prod.getDesctoProducto2());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setImporte(prod.getImporte());
        rep.setNeto(prod.getNeto());
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setUnitario(prod.getUnitario());
        return rep;
    }

    public void imprimirCompraOficinaPdf() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        ArrayList<MovimientoProductoReporte> detalleReporte = new ArrayList<>();
        for (MovimientoProducto p : this.entradaDetalle) {
            if (p.getCantFacturada() + p.getCantSinCargo() != 0) {
                detalleReporte.add(this.convertir(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\CompraOficina.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.entrada.getAlmacen().getEmpresa());

        parameters.put("cedis", this.entrada.getAlmacen().getCedis());
        parameters.put("almacen", this.entrada.getAlmacen().getAlmacen());

        parameters.put("proveedor", this.entrada.getProveedor().getProveedor());

        parameters.put("comprobante", this.entrada.getComprobante().toString());
        parameters.put("comprobanteFecha", this.entrada.getComprobante().getFecha());
        parameters.put("capturaFolio", this.entrada.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.entrada.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.entrada.getFecha()));

        parameters.put("moneda", this.entrada.getMoneda().getCodigoIso());
        parameters.put("tipoDeCambio", this.entrada.getTipoDeCambio());
        parameters.put("desctoComercial", this.entrada.getDesctoComercial());
        parameters.put("desctoProntoPago", this.entrada.getDesctoProntoPago());

        parameters.put("idUsuario", this.entrada.getIdUsuario());
        parameters.put("idOrdenDeCompra", this.entrada.getIdOrdenCompra());

        parameters.put("subTotal", this.entrada.getSubTotal());
        parameters.put("descuento", this.entrada.getDescuento());
        parameters.put("impuestos", this.entrada.getImpuesto());
        parameters.put("total", this.entrada.getTotal());
        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=CompraOficina " + this.entrada.getFolio() + " " + this.entrada.getComprobante().toString() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void inicializaListaCompras() {
        this.entradas = new ArrayList<>();
    }

//    public void editaCompraAlmacenSeleccionada() {
//        this.modoEdicion = true;
//        this.entradaProducto = null;
//        this.entradaDetalle = new ArrayList<>();
//        try {
//            this.dao = new DAOMovimientos();
//            for (TOMovimientoProducto to : this.dao.obtenerDetalleAlmacen(this.entrada.getIdEntrada())) {
//                this.entradaProducto = this.convertir(this.entrada.getIdEntrada(), to);
//                this.entradaDetalle.add(this.entradaProducto);
////                this.sumaTotales();
//            }
//        } catch (NamingException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//        }
//    }

    public void editaCompraSeleccionada() {
        this.modoEdicion = true;
        this.entradaDetalle = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for (TOMovimientoProducto to : this.dao.obtenerDetalle(this.entrada.getIdEntrada())) {
                this.entradaProducto = this.convertir(this.entrada.getIdEntrada(), to);
                this.entradaDetalle.add(this.entradaProducto);
                this.sumaTotales();
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public CompraOficina convertir(TOMovimiento to) throws SQLException {
        CompraOficina e = new CompraOficina(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
        e.setIdEntrada(to.getIdMovto());
        e.setIdOrdenCompra(to.getReferencia());
        e.setFolio(to.getFolio());
        if(this.idModulo==13) {
            if (to.getIdMoneda() != 0) {
                e.setMoneda(this.mbMonedas.obtenerMoneda(to.getIdMoneda()));
            }
            e.setTipoDeCambio(to.getTipoDeCambio());
            e.setDesctoComercial(to.getDesctoComercial());
            e.setDesctoProntoPago(to.getDesctoProntoPago());
        }
        e.setFecha(to.getFecha());
        e.setIdUsuario(to.getIdUsuario());
        e.setEstatus(to.getEstatus());
        return e;
    }

    public void mttoCompraAlmacen() {
        if (validaCompra()) {
            this.modoEdicion = true;
            this.entradaProducto = null;
            this.entradas = new ArrayList<>();
            try {
                this.dao = new DAOMovimientos();
                for (TOMovimientoAlmacen to : this.dao.obtenerMovimientosAlmacen(this.mbComprobantes.getComprobante().getIdComprobante())) {
//                    this.entradas.add(this.convertir(to));
                }
                if (entradas.isEmpty()) {
                    this.entrada = new CompraOficina(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
                    this.entradaDetalle = new ArrayList<>();
                } else if (this.entradas.size() == 1) {
                    this.entrada = this.entradas.get(0);

                    this.entradaDetalle = new ArrayList<>();
                    for (TOMovimientoProducto to : this.dao.obtenerDetalle(this.entrada.getIdEntrada())) {
                        this.entradaProducto = this.convertir(this.entrada.getIdEntrada(), to);
                        this.entradaDetalle.add(this.entradaProducto);
//                        this.sumaTotales();
                    }
                } else {
                    this.modoEdicion = false;
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    public void mttoCompra() {
        if (validaCompra()) {
            this.modoEdicion = true;
            this.entradas = new ArrayList<>();
            try {
                this.dao = new DAOMovimientos();
                for (TOMovimiento to : this.dao.obtenerMovimientosRelacionados(this.mbComprobantes.getComprobante().getIdComprobante())) {
                    this.entradas.add(this.convertir(to));
                }
                if (this.entradas.isEmpty()) {
                    this.entrada = new CompraOficina(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
                    this.entradaDetalle = new ArrayList<>();
                } else if (this.entradas.size() == 1) {
                    this.entrada = this.entradas.get(0);

                    this.entradaDetalle = new ArrayList<>();
                    for (TOMovimientoProducto to : this.dao.obtenerDetalle(this.entrada.getIdEntrada())) {
                        this.entradaProducto = this.convertir(this.entrada.getIdEntrada(), to);
                        this.entradaDetalle.add(this.entradaProducto);
                        this.sumaTotales();
                    }
                } else {
                    this.modoEdicion = false;
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    private boolean validaCompra() {
        boolean ok = false;
        if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere un almacen");
        } else if (this.mbProveedores.getMiniProveedor().getIdProveedor() == 0) {
            Mensajes.mensajeAlert("Se requiere un proveedor");
        } else if (this.mbComprobantes.getComprobante() == null) {
            Mensajes.mensajeAlert("Se requiere un comprobante");
        } else {
            ok = true;
        }
        return ok;
    }

    public void nuevaCompra() {
        if (validaCompra()) {
            this.modoEdicion = true;
            this.entradas = new ArrayList<>();
            this.entrada = new CompraOficina(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
            this.entrada.setMoneda(this.mbMonedas.obtenerMoneda(1));
            this.entradaDetalle = new ArrayList<>();
            this.entradaProducto = null;
        }
    }

    public void actualizaComprobanteProveedor() {
        this.mbComprobantes.setIdProveedor(this.mbProveedores.getMiniProveedor().getIdProveedor());
        this.mbComprobantes.setComprobante(null);
    }
//
//    public void cargaListaComprobantes() {
////        if(this.mbComprobantes.validaComprobante()) {
////            
////        }
//    }

    public void mostrarSeleccion() {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cargaOrdenes");
        fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
        fMsg.setDetail("Mensaje de seleccionado");
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }

    public void inicializar() {
        this.mbBuscar.inicializar();
        this.inicializaLocales();
    }

    private void inicializaLocales() {
        this.modoEdicion = false;
        this.entrada = new CompraOficina();
        this.resEntradaProducto = new MovimientoProducto();
        this.entradas = new ArrayList<>();
    }

//    public void cargaAlmacenesEmpresa() {
//        this.mbComprobantes.getMbAlmacenes().cargaAlmacenesEmpresa(this.mbAlmacenes.getToAlmacen().getIdEmpresa());
//    }
    public void mttoComprobante() {
        this.mbComprobantes.inicializaConAlmacen(this.mbAlmacenes.getToAlmacen());
    }

//    private boolean validaEntradaOficina1x() {
//        boolean ok = false;
//        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "validaComprobante");
//        if(this.mbComprobantes.getMbAlmacenes().getToAlmacen().getIdAlmacen()==0) {
//            fMsg.setDetail("Se requiere tener seleccionado un almacen");
//        } else if(this.mbComprobantes.getMbProveedores().getMiniProveedor().getIdProveedor()==0) {
//            fMsg.setDetail("Se requiere tener seleccionado un proveedor");
//        } else {
//            this.mbComprobantes.setTipoComprobante(1);
//            ok=true;
//        }
//        if(!ok) {
//            FacesContext.getCurrentInstance().addMessage(null, fMsg);
//        }
//        return ok;
//    }
//    public void cargaListaComprobantes() {
//        if(this.mbComprobantes.validaComprobante()) {
//            this.mbComprobantes.setTipoComprobante(1);
//            this.mbComprobantes.cargaListaComprobantes();
//        }
//    }
//    public void mttoComprobanteOficina() {
//        boolean ok=false;
//        RequestContext context = RequestContext.getCurrentInstance();
//        if(this.mbComprobantes.validaComprobante()) {
//            this.mbComprobantes.setTipoComprobante(1);
//            this.mbComprobantes.mttoComprobante();
//            ok=true;
//        }
//        context.addCallbackParam("okComprobante", ok);
//    }
    public void cargaOrdenes() {
        boolean ok = false;
        try {
            if (!this.entradaDetalle.isEmpty()) {
                Mensajes.mensajeAlert("El movimiento ya tiene productos cargados !!!");
            } else {
                if (this.idModulo == 13) {
                    this.mbOrdenCompra.cargaOrdenesEncabezado(this.entrada.getProveedor().getIdProveedor(), 2);
                } else {
                    this.mbOrdenCompra.cargaOrdenesEncabezadoAlmacen(this.entrada.getProveedor().getIdProveedor(), 2);
                }
                if (this.mbOrdenCompra.getListaOrdenesEncabezado().isEmpty()) {
                    Mensajes.mensajeAlert("No se encontraron ordenes de compra pendientes !!!");
                } else {
                    this.mbOrdenCompra.setOrdenElegida(null);
                    ok = true;
                }
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okOrdenDeCompra", ok);
    }

    public boolean grabar() {
        this.grabar = false;
        return this.grabar;
    }

//    public void grabarCompraAlmacen() {
//        try {
//            if (this.entradaDetalle.isEmpty()) {
//                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
//            } else if (this.sumaPiezas() == 0) {
//                Mensajes.mensajeAlert("No hay piezas capturadas !!!");
//            } else {
//                this.dao = new DAOMovimientos();
//                TOMovimiento toEntrada = convertirTO(this.entrada);
//                this.dao.grabarCompraAlmacen(toEntrada, this.entradaDetalle);
//                this.entrada.setFolio(toEntrada.getFolio());
//                this.entrada.setEstatus(1);
//                Mensajes.mensajeSucces("La entrada se grabo correctamente !!!");
//            }
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//        } catch (NamingException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        }
//    }

    private double sumaPiezas() {
        double piezas = 0;
        for (MovimientoProducto p : this.entradaDetalle) {
            piezas += (p.getCantFacturada() + p.getCantSinCargo());
        }
        return piezas;
    }

    public void grabarCompraOficina() {
        try {
            if (this.entradaDetalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else if (this.sumaPiezas() == 0) {
                Mensajes.mensajeAlert("No hay piezas capturadas !!!");
            } else {
                this.dao = new DAOMovimientos();
                TOMovimiento toEntrada = convertirTO(this.entrada);
                this.dao.grabarCompraOficina(toEntrada, this.entradaDetalle);
                this.entrada.setFolio(toEntrada.getFolio());
                this.entrada.setEstatus(1);
                Mensajes.mensajeSucces("La entrada se grabo correctamente !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

//    private Producto convertir(TOProducto to, Articulo a) throws SQLException {
//        Producto p=new Producto();
//        p.setIdProducto(to.getIdProducto());
//        p.setCod_pro(to.getCod_pro());
//        p.setArticulo(a);
//        p.setPiezas(to.getPiezas());
//        p.setEmpaque(to.getEmpaque());
//        p.setSubProducto(to.getSubProducto());
//        p.setDun14(to.getDun14());
//        p.setPeso(to.getPeso());
//        p.setVolumen(to.getVolumen());
//        return p;
//    }
    public void cerrarOrdenDeCompraAlmacen() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cerrarOrdenDeCompra(false, this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
            this.mbOrdenCompra.getListaOrdenesEncabezado().remove(this.mbOrdenCompra.getOrdenElegida());
            this.mbOrdenCompra.setOrdenElegida(null);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cerrarOrdenDeCompra() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cerrarOrdenDeCompra(true, this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
            this.mbOrdenCompra.getListaOrdenesEncabezado().remove(this.mbOrdenCompra.getOrdenElegida());
            this.mbOrdenCompra.setOrdenElegida(null);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cargaDetalleOrdenCompra() {
//    public void cargaDetalleOrdenCompra(SelectEvent event) {
//        this.mbOrdenCompra.setOrdenElegida((OrdenCompraEncabezado) event.getObject());
        try {
            this.dao = new DAOMovimientos();

            this.entrada.setIdOrdenCompra(this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
            this.entrada.setDesctoComercial(this.mbOrdenCompra.getOrdenElegida().getDesctoComercial());
            this.entrada.setDesctoProntoPago(this.mbOrdenCompra.getOrdenElegida().getDesctoProntoPago());
            this.entrada.setMoneda(this.mbOrdenCompra.getOrdenElegida().getMoneda());
            this.tipoDeCambio = 1;

//            this.mbOrdenCompra.obtenerDetalleOrdenCompra();
//            for (OrdenCompraDetalle d : this.mbOrdenCompra.getListaOrdenDetalle()) {
            for (TOMovimientoProducto d : this.dao.obtenerOrdenDeCompraDetalle(this.entrada.getIdOrdenCompra())) {
                this.entradaProducto = new MovimientoProducto();
                this.entradaProducto.setProducto(this.mbBuscar.obtenerProducto(d.getIdProducto()));
                this.entradaProducto.setCantOrdenada(d.getCantOrdenada());
                this.entradaProducto.setCantOrdenadaSinCargo(d.getCantOrdenadaSinCargo());
                this.entradaProducto.setCantOrdenadaTotal(d.getCantOrdenada() + (d.getCantOrdenadaSinCargo() == 0 ? "" : " + " + d.getCantOrdenadaSinCargo()));
                if (this.idModulo == 13) {
                    this.entradaProducto.setCostoOrdenado(d.getCosto());
                    this.entradaProducto.setCantRecibida(d.getCantRecibida());
                    this.entradaProducto.setCantRecibidaSinCargo(d.getCantRecibidaSinCargo());
                    this.entradaProducto.setCantFacturada(d.getCantOrdenada() - d.getCantRecibida());
                    this.entradaProducto.setCantSinCargo(d.getCantOrdenadaSinCargo() - d.getCantRecibidaSinCargo());
                    this.entradaProducto.setCosto(d.getCosto());
                    this.entradaProducto.setDesctoProducto1(d.getDesctoProducto1());
                    this.entradaProducto.setDesctoProducto2(d.getDesctoProducto2());
                    this.entradaProducto.setDesctoConfidencial(d.getDesctoConfidencial());
                    this.entradaProducto.setImpuestos(this.dao.generarImpuestosProducto(this.entradaProducto.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo(), this.entrada.getProveedor().getIdImpuestoZona()));
//                    this.calculaProducto();
                } else { // 14
                    this.entradaProducto.setCantOrdenada(d.getCantOrdenada() + d.getCantOrdenadaSinCargo());
                    this.entradaProducto.setCantRecibida(d.getCantRecibida() + d.getCantRecibidaSinCargo());
                    this.entradaProducto.setCantFacturada(this.entradaProducto.getCantOrdenada() - this.entradaProducto.getCantRecibida());
                }
                this.entradaDetalle.add(this.entradaProducto);
            }
            if (this.idModulo == 13) {
                this.cambiaPrecios();
            }
            this.entradaProducto = null;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private void cargaDatosFactura(int idEntrada) throws NamingException, SQLException {
//        TOEmpaque to;
//        DAOProductos daoProds=new DAOProductos();
//        this.entradaDetalle = this.dao.obtenerDetalleMovimiento(idEntrada);
        this.entradaDetalle = new ArrayList<>();
//        this.dao.obtenerMovimientoDetalle(idEntrada);
        for (TOMovimientoProducto to : this.dao.obtenerDetalle(idEntrada)) {
            this.convertir(idEntrada, to);
//            to=this.daoEmpaques.obtenerEmpaque(p.getEmpaque().getIdEmpaque());
//            p.setEmpaque(convertir(to, daoProds.obtenerProducto(to.getIdProducto())));
//            p.setImpuestos(this.daoImps.obtenerImpuestosProducto(idEntrada, p.getEmpaque().getIdEmpaque()));
        }
    }

    private MovimientoProducto convertir(int idEntrada, TOMovimientoProducto to) throws SQLException {
        MovimientoProducto p = new MovimientoProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantOrdenada(to.getCantOrdenada());
        p.setCantRecibida(to.getCantRecibida());
        p.setCantFacturada(to.getCantFacturada());
        p.setCantSinCargo(to.getCantSinCargo());
        p.setCosto(to.getCosto());
        p.setDesctoProducto1(to.getDesctoProducto1());
        p.setDesctoProducto2(to.getDesctoProducto2());
        p.setDesctoConfidencial(to.getDesctoConfidencial());
        p.setUnitario(to.getUnitario());
        p.setImporte(to.getUnitario() * (to.getCantFacturada() + to.getCantSinCargo()));
        p.setImpuestos(new ArrayList<ImpuestosProducto>());
        p.setNeto(to.getUnitario() + this.dao.obtenerImpuestosProducto(idEntrada, to.getIdProducto(), p.getImpuestos()));
        return p;
    }

    public void cancelarCompraAlmacen() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cancelarCompraAlmacen(this.entrada.getIdEntrada(), this.entrada.getAlmacen().getIdAlmacen(), this.entrada.getIdOrdenCompra());
            this.entrada.setEstatus(3);
            this.modoEdicion = false;
            Mensajes.mensajeSucces("La compra de Almacen se cancelo correctamente !!!");
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void cancelarCompra() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cancelarCompra(this.entrada.getIdEntrada(), this.entrada.getAlmacen().getIdAlmacen(), this.entrada.getIdOrdenCompra());
            this.entrada.setEstatus(3);
            this.modoEdicion = false;
            Mensajes.mensajeSucces("La compra se cancelo correctamente !!!");
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cargaFactura() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cargaFactura");
        try {
            this.dao = new DAOMovimientos();
//            this.daoImps = new DAOImpuestosProducto();
//            this.daoEmpaques = new DAOEmpaques();
            this.cargaDatosFactura(this.selEntrada.getIdEntrada());
            ok = true;
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okBuscar", ok);
    }

    public void obtenerEntradas() {
        boolean ok = false;
        this.entradas = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
//            TOMovimiento m=this.dao.obtenerMovimientoComprobante(this.mbComprobantes.getSeleccion().getIdComprobante());
//            this.entradas.add(this.convertir(m));
////            for (TOMovimiento m : this.dao.obtenerMovimientoComprobante(this.mbComprobantes.getToComprobante().getIdComprobante())) {
////                this.entradas.add(convertir(m));
//            }
            this.selEntrada = null;
            ok = true;
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okBuscar", ok);
    }

    public TOMovimiento convertirTO(CompraOficina entrada) {
        TOMovimiento to = new TOMovimiento();
        to.setIdMovto(entrada.getIdEntrada());
        to.setIdTipo(1);
        to.setIdCedis(entrada.getAlmacen().getIdCedis());
        to.setIdEmpresa(entrada.getAlmacen().getIdEmpresa());
        to.setIdAlmacen(entrada.getAlmacen().getIdAlmacen());
        to.setFolio(entrada.getFolio());
        to.setIdComprobante(entrada.getComprobante().getIdComprobante());
        to.setIdImpuestoZona(entrada.getProveedor().getIdImpuestoZona());
        to.setIdMoneda(entrada.getMoneda().getIdMoneda());
        to.setTipoDeCambio(entrada.getTipoDeCambio());
        to.setDesctoComercial(entrada.getDesctoComercial());
        to.setDesctoProntoPago(entrada.getDesctoProntoPago());
        to.setFecha(entrada.getFecha());
        to.setIdUsuario(entrada.getIdUsuario());
        to.setIdReferencia(entrada.getProveedor().getIdProveedor());
        to.setReferencia(entrada.getIdOrdenCompra());
        return to;
    }

    public void cancelarProductoOficina() {
//        this.entradaProducto.setProducto(this.resEntradaProducto.getProducto());
        this.entradaProducto.setCantOrdenada(this.resEntradaProducto.getCantOrdenada());
        this.entradaProducto.setCantFacturada(this.resEntradaProducto.getCantFacturada());
        this.entradaProducto.setCantSinCargo(this.resEntradaProducto.getCantSinCargo());
        this.entradaProducto.setCantRecibida(this.resEntradaProducto.getCantRecibida());
        this.entradaProducto.setCosto(this.resEntradaProducto.getCosto());
        this.entradaProducto.setDesctoProducto1(this.resEntradaProducto.getDesctoProducto1());
        this.entradaProducto.setDesctoProducto2(this.resEntradaProducto.getDesctoProducto2());
        this.entradaProducto.setDesctoConfidencial(this.resEntradaProducto.getDesctoConfidencial());
        this.entradaProducto.setUnitario(this.resEntradaProducto.getUnitario());
        this.entradaProducto.setNeto(this.resEntradaProducto.getNeto());
        this.entradaProducto.setImporte(this.resEntradaProducto.getImporte());
    }

    public void cambiaPrecios() {
        this.entrada.setSubTotal(0.00);
        this.entrada.setDescuento(0.00);
        this.entrada.setImpuesto(0.00);
        this.entrada.setTotal(0.00);

        MovimientoProducto ep = this.entradaProducto;
        for (MovimientoProducto p : this.entradaDetalle) {
            this.entradaProducto = p;
            this.entradaProducto.setCosto(this.entradaProducto.getCosto() / this.tipoDeCambio);
            this.entradaProducto.setCosto(this.entradaProducto.getCosto() * this.entrada.getTipoDeCambio());
            calculaProducto();
            sumaTotales();
        }
        this.tipoDeCambio = this.entrada.getTipoDeCambio();

        for (MovimientoProducto p : this.entradaDetalle) {
            if (p.equals(ep)) {
                this.entradaProducto = p;
                this.respaldaFila();
            }
        }
    }

    public double calculaImpuestos() {
        double impuestos = 0.00;
        double precioConImpuestos = this.entradaProducto.getUnitario();
        for (ImpuestosProducto i : this.entradaProducto.getImpuestos()) {
            if (i.isAcumulable()) {
                if (i.isAplicable()) {
                    if (i.getModo() == 1) {
                        i.setImporte(precioConImpuestos * i.getValor() / 100.00);
                    } else {
                        i.setImporte(this.entradaProducto.getProducto().getPiezas() * i.getValor());
                    }
                    precioConImpuestos += i.getImporte();
                } else {
                    i.setImporte(0.00);
                }
                impuestos += i.getImporte();
            }
        }
        for (ImpuestosProducto i : this.entradaProducto.getImpuestos()) {
            if (!i.isAcumulable()) {
                if (i.isAplicable()) {
                    if (i.getModo() == 1) {
                        i.setImporte(precioConImpuestos * i.getValor() / 100.00);
                    } else {
                        i.setImporte(this.entradaProducto.getProducto().getPiezas() * i.getValor());
                    }
                } else {
                    i.setImporte(0.00);
                }
                impuestos += i.getImporte();
            }
        }
        return impuestos;
    }

    public void actualizaTotales() {
        this.restaTotales();
        this.sumaTotales();
    }

    private void sumaTotales() {
        double suma;
        suma = this.entradaProducto.getCosto() * this.entradaProducto.getCantFacturada();   // Calcula el subTotal
        this.entrada.setSubTotal(this.entrada.getSubTotal() + Math.round(suma * 100.00) / 100.00);    // Suma el importe el subtotal

        suma = this.entradaProducto.getCosto() - this.entradaProducto.getUnitario();   // Obtiene el descuento por diferencia.
        suma = suma * this.entradaProducto.getCantFacturada();                           // Calcula el importe de descuento
        this.entrada.setDescuento(this.entrada.getDescuento() + Math.round(suma * 100.00) / 100.00);  // Suma el descuento

        suma = this.entradaProducto.getNeto() - this.entradaProducto.getUnitario();     // Obtiene el impuesto por diferencia
        suma = suma * this.entradaProducto.getCantFacturada();                           // Calcula el importe de impuestos
        this.entrada.setImpuesto(this.entrada.getImpuesto() + Math.round(suma * 100.00) / 100.00);    // Suma los impuestos

        suma = this.entradaProducto.getNeto() * this.entradaProducto.getCantFacturada(); // Calcula el importe total
        this.entrada.setTotal(this.entrada.getTotal() + Math.round(suma * 100.00) / 100.00);          // Suma el importe al total
    }

    private void restaTotales() {
        double resta;
        resta = this.resEntradaProducto.getCosto() * this.resEntradaProducto.getCantFacturada();
        this.entrada.setSubTotal(this.entrada.getSubTotal() - Math.round(resta * 100.00) / 100.00);

        resta = this.resEntradaProducto.getCosto() - this.resEntradaProducto.getUnitario();
        resta = resta * this.resEntradaProducto.getCantFacturada();
        this.entrada.setDescuento(this.entrada.getDescuento() - Math.round(resta * 100.00) / 100.00);

        resta = this.resEntradaProducto.getNeto() - this.resEntradaProducto.getUnitario();
        resta = resta * this.resEntradaProducto.getCantFacturada();
        this.entrada.setImpuesto(this.entrada.getImpuesto() - Math.round(resta * 100.00) / 100.00);

        resta = this.resEntradaProducto.getNeto() * this.resEntradaProducto.getCantFacturada();
        this.entrada.setTotal(this.entrada.getTotal() - Math.round(resta * 100.00) / 100.00);
    }

    public void cambiaDescto() {
//        restaTotales();
        calculaProducto();
//        sumaTotales();
    }

    public void cambiaPrecio() {
//        restaTotales();
        this.entradaProducto.setCosto(this.entradaProducto.getCosto() * this.entrada.getTipoDeCambio());
        calculaProducto();
//        sumaTotales();
    }

    public void cambiaCantSinCargo() {
        boolean ok = true;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cambiaCantSinCargo");
        if (this.entradaProducto.getCantSinCargo() > this.entradaProducto.getCantFacturada()) {
            ok = false;
            fMsg.setDetail("La cantidad sin cargo no puede ser mayor a la cantidad facturada");
        } else {
            calculaProducto();
        }
        if (!ok) {
            this.entradaProducto.setCantSinCargo(0.00);
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public void cambiaCantFacturada() {
        boolean ok = true;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cambiaCantFacturada");
//        restaTotales();
        calculaProducto();
//        sumaTotales();
        if (!ok) {
            this.entradaProducto.setCantFacturada(0.00);
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    private void calculaProducto() {
        double unitario = this.entradaProducto.getCosto();
        unitario *= (1 - this.entrada.getDesctoComercial() / 100.00);
        unitario *= (1 - this.entrada.getDesctoProntoPago() / 100.00);
        unitario *= (1 - this.entradaProducto.getDesctoProducto1() / 100.00);
        unitario *= (1 - this.entradaProducto.getDesctoProducto2() / 100.00);
        unitario *= (1 - this.entradaProducto.getDesctoConfidencial() / 100.00);
        this.entradaProducto.setUnitario(unitario);
        double neto = unitario + calculaImpuestos();
        this.entradaProducto.setNeto(neto);
        if (this.entradaProducto.getCantSinCargo() != 0) {
            unitario = unitario * this.entradaProducto.getCantFacturada() / (this.entradaProducto.getCantFacturada() + this.entradaProducto.getCantSinCargo());
        }
        this.entradaProducto.setCostoPromedio(unitario);
        double subTotal = this.entradaProducto.getUnitario() * (this.entradaProducto.getCantFacturada());
        this.entradaProducto.setImporte(subTotal);
    }

//    private void calculaProducto() {
//        double unitario = this.entradaProducto.getCosto();
//        unitario *= (1 - this.entrada.getDesctoComercial() / 100.00);
//        unitario *= (1 - this.entrada.getDesctoProntoPago() / 100.00);
//        unitario *= (1 - this.entradaProducto.getDesctoProducto1() / 100.00);
//        unitario *= (1 - this.entradaProducto.getDesctoProducto2() / 100.00);
//        unitario *= (1 - this.entradaProducto.getDesctoConfidencial() / 100.00);
//        if (this.entradaProducto.getCantSinCargo() != 0) {
//            unitario = unitario * this.entradaProducto.getCantFacturada() / (this.entradaProducto.getCantFacturada() + this.entradaProducto.getCantSinCargo());
//        }
//        this.entradaProducto.setUnitario(unitario);
//        double neto = unitario + calculaImpuestos();
//        this.entradaProducto.setNeto(neto);
//        double subTotal = this.entradaProducto.getUnitario() * (this.entradaProducto.getCantFacturada() + this.entradaProducto.getCantSinCargo());
//        this.entradaProducto.setImporte(subTotal);
//    }
    public void respaldaFila() {
        this.resEntradaProducto.setProducto(this.entradaProducto.getProducto());
        this.resEntradaProducto.setCantOrdenada(this.entradaProducto.getCantOrdenada());
        this.resEntradaProducto.setCantFacturada(this.entradaProducto.getCantFacturada());
        this.resEntradaProducto.setCantSinCargo(this.entradaProducto.getCantSinCargo());
        this.resEntradaProducto.setCantRecibida(this.entradaProducto.getCantRecibida());
        this.resEntradaProducto.setCosto(this.entradaProducto.getCosto());
        this.resEntradaProducto.setDesctoProducto1(this.entradaProducto.getDesctoProducto1());
        this.resEntradaProducto.setDesctoProducto2(this.entradaProducto.getDesctoProducto2());
        this.resEntradaProducto.setDesctoConfidencial(this.entradaProducto.getDesctoConfidencial());
        this.resEntradaProducto.setUnitario(this.entradaProducto.getUnitario());
        this.resEntradaProducto.setNeto(this.entradaProducto.getNeto());
        this.resEntradaProducto.setImporte(this.entradaProducto.getImporte());
    }

    public void actualizaProductosSeleccionados() {
        for (Producto p : this.mbBuscar.getSeleccionados()) {
            this.mbBuscar.setProducto(p);
            this.actualizaProductoSeleccionado();
        }
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        MovimientoProducto producto = new MovimientoProducto();
        producto.setProducto(this.mbBuscar.getProducto());
        for (MovimientoProducto p : this.entradaDetalle) {
            if (p.equals(producto)) {
                this.entradaProducto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            try {
                this.dao = new DAOMovimientos();
                producto.setImpuestos(this.dao.generarImpuestosProducto(producto.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo(), this.entrada.getProveedor().getIdImpuestoZona()));
                producto.setCosto(this.dao.obtenerPrecioUltimaCompra(this.entrada.getAlmacen().getIdEmpresa(), producto.getProducto().getIdProducto()));
                this.entradaDetalle.add(producto);
                this.entradaProducto = producto;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    // Este metodo se ejecutaba al seleccionar un almacen de la lista
//    public void inicializarEntrada() {
//        this.modoEdicion=true;
//        this.entrada=new Entrada();
//        this.ordenCompra=new OrdenCompraEncabezado();
//        this.entradaDetalle=new ArrayList<MovimientoProducto>();
//    }
    public void salir() {
        this.modoEdicion = false;
        this.entradas = null;
    }

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.entrada = null;
        return "index.xhtml";
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.idModulo = idModulo;
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
            this.inicializar();
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(13);
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

//    public OrdenCompraEncabezado getOrdenCompra() {
//        return ordenCompra;
//    }
//
//    public void setOrdenCompra(OrdenCompraEncabezado ordenCompra) {
//        this.ordenCompra = ordenCompra;
//    }

    public CompraOficina getEntrada() {
        return entrada;
    }

    public void setEntrada(CompraOficina entrada) {
        this.entrada = entrada;
    }

    public MovimientoProducto getEntradaProducto() {
        return entradaProducto;
    }

    public void setEntradaProducto(MovimientoProducto entradaProducto) {
        this.entradaProducto = entradaProducto;
    }

    public ArrayList<MovimientoProducto> getEntradaDetalle() {
        return entradaDetalle;
    }

    public void setEntradaDetalle(ArrayList<MovimientoProducto> entradaDetalle) {
        this.entradaDetalle = entradaDetalle;
    }

    public MovimientoProducto getResEntradaProducto() {
        return resEntradaProducto;
    }

    public void setResEntradaProducto(MovimientoProducto resEntradaProducto) {
        this.resEntradaProducto = resEntradaProducto;
    }

    public MbComprobantes getMbComprobantes() {
        return mbComprobantes;
    }

    public void setMbComprobantes(MbComprobantes mbComprobantes) {
        this.mbComprobantes = mbComprobantes;
    }

    public Date getFechaIniPeriodo() {
        return fechaIniPeriodo;
    }

    public void setFechaIniPeriodo(Date fechaIniPeriodo) {
        this.fechaIniPeriodo = fechaIniPeriodo;
    }

    public Date getFechaFinPeriodo() {
        return fechaFinPeriodo;
    }

    public void setFechaFinPeriodo(Date fechaFinPeriodo) {
        this.fechaFinPeriodo = fechaFinPeriodo;
    }

    public MbOrdenCompra getMbOrdenCompra() {
        return mbOrdenCompra;
    }

    public void setMbOrdenCompra(MbOrdenCompra mbOrdenCompra) {
        this.mbOrdenCompra = mbOrdenCompra;
    }

    public ArrayList<CompraOficina> getEntradas() {
        return entradas;
    }

    public void setEntradas(ArrayList<CompraOficina> entradas) {
        this.entradas = entradas;
    }

//    public boolean isSinOrden() {
//        return sinOrden;
//    }
//
//    public void setSinOrden(boolean sinOrden) {
//        this.sinOrden = sinOrden;
//    }
    public MbMonedas getMbMonedas() {
        return mbMonedas;
    }

    public void setMbMonedas(MbMonedas mbMonedas) {
        this.mbMonedas = mbMonedas;
    }

    public CompraOficina getSelEntrada() {
        return selEntrada;
    }

    public void setSelEntrada(CompraOficina selEntrada) {
        this.selEntrada = selEntrada;
    }

    public boolean isGrabar() {
        return grabar;
    }

    public void setGrabar(boolean grabar) {
        this.grabar = grabar;
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

    public TimeZone getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(TimeZone zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public MbMiniProveedor getMbProveedores() {
        return mbProveedores;
    }

    public void setMbProveedores(MbMiniProveedor mbProveedores) {
        this.mbProveedores = mbProveedores;
    }
}
