package compras;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import compras.dao.DAOComprasAlmacen;
import compras.dominio.CompraAlmacen;
import compras.dominio.ProductoCompraAlmacen;
import compras.to.TOProductoCompraAlmacen;
import comprobantes.MbComprobantes;
import entradas.dominio.ProductoReporteAlmacen;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import movimientos.dao.DAOMovimientosAlmacen;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOProductoAlmacen;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import ordenesDeCompra.MbOrdenCompra;
import ordenesDeCompra.dominio.OrdenCompraEncabezado;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import proveedores.MbMiniProveedor;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbComprasAlmacen")
@SessionScoped
public class MbComprasAlmacen implements Serializable {

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
    private boolean modoEdicion;
    private CompraAlmacen compra;
    private ArrayList<CompraAlmacen> compras;
    private ProductoCompraAlmacen producto;
    private ProductoCompraAlmacen resProducto;
    private ArrayList<ProductoCompraAlmacen> detalle;
    private int idModulo;
    private String btnOrdenCompraIcono;
    private String btnOrdenCompraTitle;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAOMovimientosAlmacen daoMv;
    private DAOComprasAlmacen dao;

    public MbComprasAlmacen() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbProveedores = new MbMiniProveedor();
        this.mbBuscar = new MbProductosBuscar();
        this.mbComprobantes = new MbComprobantes();
        this.mbOrdenCompra = new MbOrdenCompra();
        this.inicializaLocales();
    }

    public void imprimirComprobantePdf() {
//        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
//        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        try {
//            if(this.mbComprobantes.getSeleccion()==null) {
//                throw new Exception("Seleccione un comprobante");
//            }
            this.compra = this.compras.get(0);

            ProductoCompraAlmacen prod;
            this.dao = new DAOComprasAlmacen();
            ArrayList<ProductoReporteAlmacen> detalleReporte = new ArrayList<>();
            for (CompraAlmacen cmp : this.compras) {
                if (cmp.getEstatus() == 5) {
                    for (TOProductoCompraAlmacen p : this.dao.obtenerCompraDetalle(this.compra.getIdMovtoAlmacen())) {
                        prod = this.convertir(p);
                        if (p.getCantidad() != 0) {
                            detalleReporte.add(this.convertirRep(prod, cmp));
                        }
                    }
                }
            }
            String sourceFileName = "C:\\Carlos Pat\\Reportes\\CompraAlmacenComprobante.jasper";
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
            Map parameters = new HashMap();
            parameters.put("empresa", this.compra.getAlmacen().getEmpresa());

            parameters.put("cedis", this.compra.getAlmacen().getCedis());
            parameters.put("almacen", this.compra.getAlmacen().getAlmacen());

            parameters.put("proveedor", this.compra.getProveedor().getProveedor());

            parameters.put("comprobante", this.compra.getComprobante().toString());
            parameters.put("comprobanteFecha", this.compra.getComprobante().getFechaFactura());
//        parameters.put("capturaFolio", this.compra.getFolio());
//        parameters.put("capturaFecha", formatoFecha.format(this.compra.getFecha()));
//        parameters.put("capturaHora", formatoHora.format(this.compra.getFecha()));

//            parameters.put("idUsuario", this.compra.getIdUsuario());
//            parameters.put("idOrdenDeCompra", this.compra.getIdOrdenCompra());

            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=CompraAlmacenComprobante " + this.compra.getComprobante().toString() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (Exception ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.producto = this.detalle.get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
        } else {
            this.producto.setCantidad((Double) oldValue);
        }
    }

    public void cambiarCantidad() {
        if (this.producto == null) {
            Mensajes.mensajeAlert("Se requiere seleccionar un producto !!!");
        } else if (this.producto.getCantidad() < 0) {
            this.producto.setCantidad(this.producto.getSeparados());
            Mensajes.mensajeAlert("La cantidad NO debe ser MENOR QUE CERO !!!");
        } else if (this.producto.getCantidad() != this.producto.getSeparados()) {
            try {
                this.dao = new DAOComprasAlmacen();
                if (this.compra.getIdOrdenCompra() != 0) {
                    if (this.producto.getCantidad() > this.producto.getSeparados()) {
                        double cantSolicitada = this.producto.getCantidad() - this.producto.getSeparados();
                        double cantSeparada = this.dao.separar(this.producto.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto(), this.producto.getLote(), cantSolicitada, this.compra.getIdOrdenCompra());
                        if (cantSeparada < cantSolicitada) {
                            this.producto.setCantidad(this.producto.getSeparados() + cantSeparada);
                            Mensajes.mensajeAlert("Solo se pudieron separar " + cantSeparada + " unidades !!!");
                        }
                    } else {
                        double cantLiberar = this.producto.getSeparados() - this.producto.getCantidad();
                        this.dao.liberar(this.producto.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto(), this.producto.getLote(), cantLiberar, this.compra.getIdOrdenCompra());
                    }
                } else if (this.producto.getCantidad() > this.producto.getSeparados()) {
                    double cantSolicitada = this.producto.getCantidad() - this.producto.getSeparados();
                    this.dao.separar(this.producto.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto(), this.producto.getLote(), cantSolicitada, this.compra.getIdOrdenCompra());
                } else {
                    double cantLiberar = this.producto.getSeparados() - this.producto.getCantidad();
                    this.dao.liberar(this.producto.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto(), this.producto.getLote(), cantLiberar, this.compra.getIdOrdenCompra());
                }
                this.producto.setSeparados(this.producto.getCantidad());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void cerrarOrdenDeCompra() {
        try {
            this.dao = new DAOComprasAlmacen();
            this.dao.cerrarOrdenDeCompra(this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
            this.mbOrdenCompra.getListaOrdenesEncabezado().remove(this.mbOrdenCompra.getOrdenElegida());
            this.mbOrdenCompra.setOrdenElegida(null);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
    public void eliminarCompra() {
        try {
            this.dao = new DAOComprasAlmacen();
            this.dao.eliminarCompra(this.compra.getIdMovtoAlmacen());
            this.modoEdicion = false;
            Mensajes.mensajeSucces("El movimiento se elimino correctamente !!!");
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cancelarCompra() {
        try {
            this.dao = new DAOComprasAlmacen();
            this.dao.cancelarCompra(this.compra.getIdMovtoAlmacen(), this.compra.getAlmacen().getIdAlmacen(), this.compra.getIdOrdenCompra());
            this.compra.setEstatus(8);
            this.modoEdicion = false;
            Mensajes.mensajeSucces("La compra de Almacen se cancelo correctamente !!!");
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void eliminarProductoAlmacen() {
        if (this.producto != null) {
            try {
                this.dao = new DAOComprasAlmacen();
                this.dao.eliminarProducto(this.producto.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto());
                this.detalle.remove(this.producto);
                this.producto = null;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        } else {
            Mensajes.mensajeAlert("No hay producto seleccionado !!!");
        }
    }

    private ProductoReporteAlmacen convertirRep(ProductoCompraAlmacen prod, CompraAlmacen cmp) {
        ProductoReporteAlmacen rep = new ProductoReporteAlmacen();
        rep.setFolio(cmp.getFolio());
        rep.setIdOrdenCompra(cmp.getIdOrdenCompra());
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setCantidad(prod.getCantidad());
        return rep;
    }

    public void imprimirCompraPdf() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        ArrayList<ProductoReporteAlmacen> detalleReporte = new ArrayList<>();
        for (ProductoCompraAlmacen p : this.detalle) {
            if (p.getCantidad() != 0) {
                detalleReporte.add(this.convertirRep(p, this.compra));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\CompraAlmacen.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.compra.getAlmacen().getEmpresa());

        parameters.put("cedis", this.compra.getAlmacen().getCedis());
        parameters.put("almacen", this.compra.getAlmacen().getAlmacen());

        parameters.put("proveedor", this.compra.getProveedor().getProveedor());

        parameters.put("comprobante", this.compra.getComprobante().toString());
        parameters.put("comprobanteFecha", this.compra.getComprobante().getFechaFactura());
        parameters.put("capturaFolio", this.compra.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.compra.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.compra.getFecha()));

        parameters.put("idUsuario", this.compra.getIdUsuario());
        parameters.put("idOrdenDeCompra", this.compra.getIdOrdenCompra());

        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=CompraAlmacen " + this.compra.getFolio() + " " + this.compra.getComprobante().toString() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public TOMovimientoAlmacen convertir(CompraAlmacen mov) {
        TOMovimientoAlmacen toMov = new TOMovimientoAlmacen();
        movimientos.Movimientos.convertir(mov, toMov);
        toMov.setIdComprobante(mov.getComprobante().getIdComprobante());
        toMov.setIdReferencia(mov.getProveedor().getIdProveedor());
        toMov.setReferencia(mov.getIdOrdenCompra());
        return toMov;
    }

    private double sumaPiezas() {
        double piezas = 0;
        for (ProductoCompraAlmacen p : this.detalle) {
            piezas += p.getCantidad();
        }
        return piezas;
    }

    public void grabarCompra() {
        try {
            if (this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else if (this.sumaPiezas() == 0) {
                Mensajes.mensajeAlert("No hay piezas capturadas !!!");
            } else {
                this.dao = new DAOComprasAlmacen();
                TOMovimientoAlmacen toCompra = convertir(this.compra);
                this.dao.grabarCompra(toCompra);
                this.compra.setFolio(toCompra.getFolio());
                this.compra.setEstatus(1);
                Mensajes.mensajeSucces("La compra se grabo correctamente !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private ProductoCompraAlmacen convertir(TOProductoCompraAlmacen to) {
        ProductoCompraAlmacen prod = new ProductoCompraAlmacen();
        prod.setCantOrdenada(to.getCantOrdenada());
        prod.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        prod.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        prod.setLote(to.getLote());
        prod.setCantidad(to.getCantidad());
        prod.setSeparados(to.getCantidad());
        return prod;
    }

    public void editaCompra() {
        try {
            this.dao = new DAOComprasAlmacen();
            this.detalle = new ArrayList<>();
            for (TOProductoCompraAlmacen to : this.dao.obtenerCompraDetalle(this.compra.getIdMovtoAlmacen())) {
                this.detalle.add(this.convertir(to));
            }
            if (this.compra.getIdOrdenCompra() != 0) {
                this.btnOrdenCompraIcono = "ui-icon-cancel";
                this.btnOrdenCompraTitle = "Cambiar Orden de Compra";
            } else {
                this.btnOrdenCompraIcono = "ui-icon-search";
                this.btnOrdenCompraTitle = "Buscar Orden de Compra";
            }
            this.modoEdicion = true;
            this.producto = null;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    private CompraAlmacen convertir(TOMovimientoAlmacen to) {
        CompraAlmacen c = new CompraAlmacen(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
        c.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        c.setIdOrdenCompra(to.getReferencia());
        c.setFolio(to.getFolio());
        c.setFecha(to.getFecha());
        c.setIdUsuario(to.getIdUsuario());
        c.setEstatus(to.getEstatus());
        return c;
    }

    public void mttoCompra() {
        boolean ok = false;
        if (this.validaCompra()) {
            this.compras = new ArrayList<>();
            try {
                this.daoMv = new DAOMovimientosAlmacen();
                for (TOMovimientoAlmacen to : this.daoMv.obtenerMovimientosComprobante(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), 1, this.mbComprobantes.getComprobante().getIdComprobante())) {
                    this.compras.add(this.convertir(to));
                }
                if (this.compras.isEmpty()) {
                    Mensajes.mensajeAlert("No se encontraron compras !!!");
                } else {
                    ok = true;
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEntradas", ok);
    }

    public void crearNuevaCompra() {
        this.compra = new CompraAlmacen(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
        this.compra.setIdOrdenCompra(this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
        TOMovimientoAlmacen toCompra = this.convertir(this.compra);
        this.detalle = new ArrayList<>();
        try {
            this.dao = new DAOComprasAlmacen();
            this.daoMv = new DAOMovimientosAlmacen();
            for (TOProductoCompraAlmacen d : this.dao.crearOrdenDeCompraDetalle(toCompra, false)) {
                this.detalle.add(this.convertir(d));
            }
            this.compra.setIdMovtoAlmacen(toCompra.getIdMovtoAlmacen());
            this.compra.setFecha(toCompra.getFecha());
            this.compra.setIdUsuario(toCompra.getIdUsuario());
            this.compra.setPropietario(toCompra.getPropietario());
            this.compra.setEstatus(toCompra.getEstatus());
            this.btnOrdenCompraIcono = "ui-icon-cancel";
            this.btnOrdenCompraTitle = "Cambiar Orden de Compra";
            this.modoEdicion = true;
            this.producto = null;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
    public void cargarOrdenesDeCompra() {
        this.compra = new CompraAlmacen(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
        if (this.compra.getAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere un almacen !!!");
        } else if (this.compra.getProveedor().getIdProveedor() == 0) {
            Mensajes.mensajeAlert("Se requiere un proveedor !!!");
        } else {
            try {
                this.mbOrdenCompra.cargaOrdenesEncabezadoAlmacen(this.compra.getProveedor().getIdProveedor(), 5);
                if (this.mbOrdenCompra.getListaOrdenesEncabezado().isEmpty()) {
                    Mensajes.mensajeAlert("No se encontraron ordenes de compra pendientes !!!");
                } else {
                    this.mbOrdenCompra.setOrdenElegida(null);
                }
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void cargarOrdenDeCompraDetalle(SelectEvent event) {
        this.mbOrdenCompra.setOrdenElegida((OrdenCompraEncabezado) event.getObject());
        try {
            this.detalle = new ArrayList<>();
            this.dao = new DAOComprasAlmacen();
            for (TOProductoCompraAlmacen d : this.dao.cargarOrdenDeCompraDetalle(this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra(), this.compra.getIdMovtoAlmacen())) {
                this.detalle.add(this.convertir(d));
            }
            this.compra.setIdOrdenCompra(this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
            this.btnOrdenCompraIcono = "ui-icon-cancel";
            this.btnOrdenCompraTitle = "Cambiar Orden de Compra";
            this.producto = null;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cargaOrdenes() {
        boolean ok = false;
        try {
            if (this.compra.getIdOrdenCompra() != 0) {
                TOMovimientoAlmacen toMov = this.convertir(this.compra);
                this.dao = new DAOComprasAlmacen();
                this.dao.inicializarCompra(toMov);
                this.compra.setIdOrdenCompra(0);
                this.detalle = new ArrayList<>();
                this.btnOrdenCompraIcono = "ui-icon-search";
                this.btnOrdenCompraTitle = "Buscar Orden de Compra";
            } else if (!this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("El movimiento ya tiene productos cargados !!!");
            } else {
                this.mbOrdenCompra.cargaOrdenesEncabezadoAlmacen(this.compra.getProveedor().getIdProveedor(), 5);
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

    public boolean prueba() {
        boolean disabled = false;
        if (this.compra.getEstatus() != 0) {
            disabled = true;
        } else if (this.compra.getIdOrdenCompra() != 0) {
            disabled = true;
        } else if (this.producto == null) {
            disabled = true;
        }
        return disabled;
    }

    private TOProductoCompraAlmacen convertir(ProductoCompraAlmacen prod) {
        TOProductoCompraAlmacen to = new TOProductoCompraAlmacen();
        to.setCantOrdenada(prod.getCantOrdenada());
        to.setIdMovtoAlmacen(prod.getIdMovtoAlmacen());
        to.setIdProducto(prod.getProducto().getIdProducto());
        to.setLote(prod.getLote());
        to.setCantidad(prod.getCantidad());
        return to;
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        ProductoCompraAlmacen prod = new ProductoCompraAlmacen();
        prod.setProducto(this.mbBuscar.getProducto());
        for (ProductoCompraAlmacen p : this.detalle) {
            if (p.equals(prod)) {
                this.producto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            prod.setIdMovtoAlmacen(this.compra.getIdMovtoAlmacen());
            TOProductoAlmacen toProd = this.convertir(prod);
            try {
                this.dao = new DAOComprasAlmacen();
                this.dao.agregarProductoAlmacen(toProd);
                this.producto = prod;
                this.detalle.add(this.producto);
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

    private void crearCompra() {
        this.compra = new CompraAlmacen(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
        TOMovimientoAlmacen toMov = this.convertir(this.compra);
        try {
            this.daoMv = new DAOMovimientosAlmacen();
            this.daoMv.agregarMovimiento(toMov, false);
            this.compra.setIdMovtoAlmacen(toMov.getIdMovtoAlmacen());
            this.compra.setFecha(toMov.getFecha());
            this.compra.setIdUsuario(toMov.getIdUsuario());
            this.compra.setPropietario(toMov.getPropietario());
            this.compra.setEstatus(toMov.getEstatus());
            this.btnOrdenCompraIcono = "ui-icon-search";
            this.btnOrdenCompraTitle = "Buscar Orden de Compra";

            this.modoEdicion = true;
            this.detalle = new ArrayList<>();
            this.producto = null;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private boolean validaCompra() {
        boolean ok = false;
        if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere un almacen");
        } else if (this.mbProveedores.getMiniProveedor().getIdProveedor() == 0) {
            Mensajes.mensajeAlert("Se requiere un proveedor");
        } else if (this.mbComprobantes.getSeleccion() == null) {
            Mensajes.mensajeAlert("Se requiere un comprobante");
        } else {
            ok = true;
        }
        return ok;
    }

    public void nuevaCompra() {
        if (this.validaCompra()) {
            if(this.mbComprobantes.getSeleccion().isCerradoAlmacen()) {
                Mensajes.mensajeAlert("El comprobante ya está cerrado !!!");
            } else {
                this.crearCompra();
            }
        }
    }

    public void inicializaListaCompras() {
        this.compras = new ArrayList<>();
        this.mbComprobantes.convierteSeleccion();
    }

    public void actualizaComprobanteProveedor() {
        this.mbComprobantes.setIdTipoMovto(1);
        this.mbComprobantes.setIdReferencia(this.mbProveedores.getMiniProveedor().getIdProveedor());
        this.mbComprobantes.setComprobante(null);
    }

    public void salir() {
        this.modoEdicion = false;
        this.compras = null;
    }

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.compra = null;
        return "index.xhtml";
    }

    private void inicializaLocales() {
        this.modoEdicion = false;
        this.compra = new CompraAlmacen();
        this.resProducto = new ProductoCompraAlmacen();
        this.compras = new ArrayList<>();
    }

    public void inicializar() {
        this.mbBuscar.inicializar();
        this.inicializaLocales();
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
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public CompraAlmacen getCompra() {
        return compra;
    }

    public void setCompra(CompraAlmacen compra) {
        this.compra = compra;
    }

    public ArrayList<CompraAlmacen> getCompras() {
        return compras;
    }

    public void setCompras(ArrayList<CompraAlmacen> compras) {
        this.compras = compras;
    }

    public ProductoCompraAlmacen getProducto() {
        return producto;
    }

    public void setProducto(ProductoCompraAlmacen producto) {
        this.producto = producto;
    }

    public ProductoCompraAlmacen getResProducto() {
        return resProducto;
    }

    public void setResProducto(ProductoCompraAlmacen resProducto) {
        this.resProducto = resProducto;
    }

    public ArrayList<ProductoCompraAlmacen> getDetalle() {
        return detalle;
    }

    public int getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(int idModulo) {
        this.idModulo = idModulo;
    }

    public String getBtnOrdenCompraIcono() {
        return btnOrdenCompraIcono;
    }

    public void setBtnOrdenCompraIcono(String btnOrdenCompraIcono) {
        this.btnOrdenCompraIcono = btnOrdenCompraIcono;
    }

    public String getBtnOrdenCompraTitle() {
        return btnOrdenCompraTitle;
    }

    public void setBtnOrdenCompraTitle(String btnOrdenCompraTitle) {
        this.btnOrdenCompraTitle = btnOrdenCompraTitle;
    }

    public TimeZone getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(TimeZone zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public MbAlmacenesJS getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbAlmacenesJS mbAlmacenes) {
        this.mbAlmacenes = mbAlmacenes;
    }

    public MbMiniProveedor getMbProveedores() {
        return mbProveedores;
    }

    public void setMbProveedores(MbMiniProveedor mbProveedores) {
        this.mbProveedores = mbProveedores;
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

    public MbOrdenCompra getMbOrdenCompra() {
        return mbOrdenCompra;
    }

    public void setMbOrdenCompra(MbOrdenCompra mbOrdenCompra) {
        this.mbOrdenCompra = mbOrdenCompra;
    }
}
