package compras;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import compras.dominio.CompraAlmacen;
import entradas.MbComprobantes;
import entradas.dominio.MovimientoAlmacenProducto;
import entradas.dominio.MovimientoAlmacenProductoReporte;
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
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import movimientos.dao.DAOMovimientos;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOMovimientoAlmacenProducto;
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
    private MovimientoAlmacenProducto producto;
    private MovimientoAlmacenProducto resProducto;
    private ArrayList<MovimientoAlmacenProducto> detalle;
    private int idModulo;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAOMovimientos dao;

    public MbComprasAlmacen() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbProveedores = new MbMiniProveedor();
        this.mbBuscar = new MbProductosBuscar();
        this.mbComprobantes = new MbComprobantes();
        this.mbOrdenCompra = new MbOrdenCompra();
        this.inicializaLocales();
    }

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

    public void cancelarCompraAlmacen() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cancelarCompraAlmacen(this.compra.getIdCompra(), this.compra.getAlmacen().getIdAlmacen(), this.compra.getIdOrdenCompra());
            this.compra.setEstatus(3);
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
            this.detalle.remove(this.producto);
            this.producto = null;
        } else {
            Mensajes.mensajeAlert("No hay producto seleccionado !!!");
        }
    }
    
    private MovimientoAlmacenProductoReporte convertirProductoReporte(MovimientoAlmacenProducto prod) {
        MovimientoAlmacenProductoReporte rep = new MovimientoAlmacenProductoReporte();
        rep.setCantidad(prod.getCantidad());
//        rep.setCantOrdenada(prod.getCantOrdenada());
//        rep.setCantRecibida(prod.getCantRecibida());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setSku(prod.getProducto().getCod_pro());
        return rep;
    }

    public void imprimirCompraPdf() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        
        ArrayList<MovimientoAlmacenProductoReporte> detalleReporte = new ArrayList<>();
        for (MovimientoAlmacenProducto p : this.detalle) {
            if (p.getCantidad() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
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
        parameters.put("comprobanteFecha", this.compra.getComprobante().getFecha());
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
    
    private TOMovimientoAlmacenProducto convertir(MovimientoAlmacenProducto prod) {
        TOMovimientoAlmacenProducto to=new TOMovimientoAlmacenProducto();
        to.setIdMovtoAlmacen(prod.getIdMovtoAlmacen());
        to.setIdProducto(prod.getProducto().getIdProducto());
        to.setCantidad(prod.getCantidad());
        return to;
    }
    
    public TOMovimientoAlmacen convertir(CompraAlmacen compra) {
        TOMovimientoAlmacen to = new TOMovimientoAlmacen();
        to.setIdMovto(compra.getIdCompra());
        to.setIdTipo(1);
        to.setIdCedis(compra.getAlmacen().getIdCedis());
        to.setIdEmpresa(compra.getAlmacen().getIdEmpresa());
        to.setIdAlmacen(compra.getAlmacen().getIdAlmacen());
        to.setFolio(compra.getFolio());
        to.setIdComprobante(compra.getComprobante().getIdComprobante());
        to.setFecha(compra.getFecha());
        to.setIdUsuario(compra.getIdUsuario());
        to.setIdReferencia(compra.getProveedor().getIdProveedor());
        to.setReferencia(compra.getIdOrdenCompra());
        return to;
    }
    
    private double sumaPiezas() {
        double piezas = 0;
        for (MovimientoAlmacenProducto p : this.detalle) {
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
                this.dao = new DAOMovimientos();
                TOMovimientoAlmacen toEntrada = convertir(this.compra);
//                ArrayList<TOMovimientoAlmacenProducto> det=new ArrayList<>();
//                for(MovimientoAlmacenProducto to: this.detalle) {
//                    det.add(this.convertir(to));
//                }
//                this.dao.grabarCompraAlmacen(toEntrada, det);
                this.dao.grabarCompraAlmacen(toEntrada, this.detalle);
                this.compra.setFolio(toEntrada.getFolio());
                this.compra.setEstatus(1);
                Mensajes.mensajeSucces("La entrada se grabo correctamente !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private MovimientoAlmacenProducto convertir(TOMovimientoAlmacenProducto to) {
        MovimientoAlmacenProducto prod = new MovimientoAlmacenProducto();
        prod.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        prod.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        prod.setCantidad(to.getCantidad());
        return prod;
    }

    public void editaCompra() {
        try {
            this.dao = new DAOMovimientos();
            this.detalle = new ArrayList<>();
            for (TOMovimientoAlmacenProducto to : this.dao.obtenerDetalleAlmacen(this.compra.getIdCompra())) {
                this.detalle.add(this.convertir(to));
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
        c.setIdCompra(to.getIdMovto());
        c.setIdOrdenCompra(to.getReferencia());
        c.setFolio(to.getFolio());
        c.setFecha(to.getFecha());
        c.setIdUsuario(to.getIdUsuario());
        c.setEstatus(to.getEstatus());
        return c;
    }

    public void mttoCompra() {
        if (validaCompra()) {
            try {
                this.dao = new DAOMovimientos();
                this.compras = new ArrayList<>();
                for (TOMovimientoAlmacen to : this.dao.obtenerMovimientosAlmacen(this.mbComprobantes.getComprobante().getIdComprobante())) {
                    this.compras.add(this.convertir(to));
                }
                if (this.compras.isEmpty()) {
                    this.crearCompra();
                } else if (this.compras.size() == 1) {
                    this.compra = this.compras.get(0);
                    this.editaCompra();
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    public void cargaDetalleOrdenCompra() {
        try {
            this.detalle=new ArrayList<>();
            this.compra.setIdOrdenCompra(this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
            
            this.dao = new DAOMovimientos();
            for (TOMovimientoAlmacenProducto d : this.dao.obtenerOrdenDeCompraDetalleAlmacen(this.compra.getIdOrdenCompra())) {
                this.producto = new MovimientoAlmacenProducto();
                this.producto.setIdMovtoAlmacen(0);
                this.producto.setProducto(this.mbBuscar.obtenerProducto(d.getIdProducto()));
                this.producto.setCantOrdenada(d.getCantOrdenada());
                this.producto.setCantRecibida(d.getCantRecibida());
                this.producto.setCantidad(0);
                this.detalle.add(this.producto);
            }
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
            if (!this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("El movimiento ya tiene productos cargados !!!");
            } else {
                this.mbOrdenCompra.cargaOrdenesEncabezadoAlmacen(this.compra.getProveedor().getIdProveedor(), 2);
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

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        MovimientoAlmacenProducto prod = new MovimientoAlmacenProducto();
        prod.setProducto(this.mbBuscar.getProducto());
        for (MovimientoAlmacenProducto p : this.detalle) {
            if (p.equals(prod)) {
                this.producto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            this.detalle.add(prod);
            this.producto = prod;
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
        this.detalle = new ArrayList<>();
        this.modoEdicion = true;
        this.producto = null;
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
            this.crearCompra();
        }
    }
    
    public void inicializaListaCompras() {
        this.compras = new ArrayList<>();
    }

    public void actualizaComprobanteProveedor() {
        this.mbComprobantes.setIdProveedor(this.mbProveedores.getMiniProveedor().getIdProveedor());
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
        this.resProducto = new MovimientoAlmacenProducto();
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

    public MovimientoAlmacenProducto getProducto() {
        return producto;
    }

    public void setProducto(MovimientoAlmacenProducto producto) {
        this.producto = producto;
    }

    public MovimientoAlmacenProducto getResProducto() {
        return resProducto;
    }

    public void setResProducto(MovimientoAlmacenProducto resProducto) {
        this.resProducto = resProducto;
    }

    public ArrayList<MovimientoAlmacenProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<MovimientoAlmacenProducto> detalle) {
        this.detalle = detalle;
    }

    public int getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(int idModulo) {
        this.idModulo = idModulo;
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
