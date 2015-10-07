package mvEntradas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import entradas.dominio.ProductoReporteAlmacen;
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
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import movimientos.dao.DAOMovimientosAlmacen;
import movimientos.dominio.ProductoAlmacen;
import movimientos.dominio.MovimientoAlmacen;
import movimientos.dominio.MovimientoTipo;
import movimientos.dominio.ProductoLotes;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOProductoAlmacen;
import movimientos.to.TOProductoLotes;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbEntradasAlmacen")
@SessionScoped
public class MbEntradasAlmacen implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private boolean modoEdicion;
    private ArrayList<SelectItem> listaMovimientosTipos;
    private MovimientoTipo tipo;
    private ArrayList<ProductoLotes> detalle;
    private ProductoLotes producto;
    private MovimientoAlmacen entrada;
    private ArrayList<MovimientoAlmacen> pendientes;
    private ProductoAlmacen lote;
    private DAOMovimientosAlmacen dao;

    public MbEntradasAlmacen() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }
    
    public void eliminarProducto() {
        try {
            this.dao = new DAOMovimientosAlmacen();
            this.dao.cancelarProducto(this.entrada.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto(), true);
            this.detalle.remove(this.producto);
            this.producto = new ProductoLotes();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private ProductoReporteAlmacen convertirProductoReporte(ProductoLotes prod) {
        boolean ya = false;
        ProductoReporteAlmacen rProd = new ProductoReporteAlmacen();
        rProd.setCantidad(prod.getCantidad());
        rProd.setEmpaque(prod.getProducto().toString());
        rProd.setSku(prod.getProducto().getCod_pro());
        for (ProductoAlmacen l : prod.getLotes()) {
            if (l.getCantidad() != 0) {
                if (ya) {
                    rProd.getLotes().add(l);
                } else {
                    rProd.setLote(l.getLote());
                    rProd.setLoteCantidad(l.getCantidad());
                    ya = true;
                }
            }
        }
        return rProd;
    }

    public void imprimir() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        ArrayList<ProductoReporteAlmacen> detalleReporte = new ArrayList<>();
        for (ProductoLotes p : this.detalle) {
            if (p.getCantidad() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\MovimientoAlmacen.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.entrada.getAlmacen().getEmpresa());

        parameters.put("cedis", this.entrada.getAlmacen().getCedis());
        parameters.put("almacen", this.entrada.getAlmacen().getAlmacen());

        parameters.put("concepto", this.entrada.getTipo().getTipo());
        parameters.put("conceptoTipo", "ENTRADA ALMACEN CONCEPTOS VARIOS");

        parameters.put("capturaFolio", this.entrada.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.entrada.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.entrada.getFecha()));

        parameters.put("idUsuario", this.entrada.getIdUsuario());

        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=EntradaAlmacen_" + this.entrada.getFolio() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cancelar() {
        try {
            this.dao = new DAOMovimientosAlmacen();
            this.dao.cancelarMovimiento(this.entrada.getIdMovtoAlmacen(), true);
            Mensajes.mensajeSucces("La cancelacion de la entrada se realizo con exito !!!");
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void grabar() {
        try {
            if (this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else if (movimientos.Movimientos.sumaPiezasAlmacen(this.detalle) == 0) {
                Mensajes.mensajeAlert("No hay unidades en el movimiento !!!");
            } else {
                TOMovimientoAlmacen toMov = this.convertir(this.entrada);

                this.dao = new DAOMovimientosAlmacen();
                this.dao.grabarDetalle(toMov, true);
                this.entrada.setFolio(toMov.getFolio());
                this.entrada.setFecha(toMov.getFecha());
                this.entrada.setIdUsuario(toMov.getIdUsuario());
                this.entrada.setEstatus(toMov.getEstatus());

                this.obtenDetalle(this.entrada.getIdMovtoAlmacen());
                Mensajes.mensajeSucces("La entrada se grabó correctamente !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void gestionarLotes() {
        boolean okLotes = false;
        if (this.lote.getCantidad() < 0) {
            this.lote.setCantidad(this.lote.getSeparados());
            Mensajes.mensajeAlert("La cantidad no puede ser menor que cero");
        } else {
            try {
                this.dao = new DAOMovimientosAlmacen();
                this.dao.grabarProductoCantidad(this.lote);
                this.producto.setCantidad(this.producto.getCantidad() - this.lote.getSeparados() + this.lote.getCantidad());
                this.lote.setSeparados(this.lote.getCantidad());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", okLotes);
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.lote = this.producto.getLotes().get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
            oldValue = newValue;
        } else {
            newValue = oldValue;
            Mensajes.mensajeAlert("Checar que pasa !!!");
        }
    }

    public void agregarLote() {
        boolean ok = false;
        ArrayList<String> turnos = new ArrayList<>();
        turnos.add("1");
        turnos.add("2");
        turnos.add("3");
        try {
            this.dao = new DAOMovimientosAlmacen();
            if (this.lote.getLote().length() < 5) {
                Mensajes.mensajeAlert("La longitud de un lote no puede ser menor a 5 !!!");
            } else if (turnos.indexOf(this.lote.getLote().substring(4, 5)) == -1) {
                Mensajes.mensajeAlert("Turno incorrecto. Debe ser (1, 2, 3) !!!");
            } else if (!this.dao.validaLote(this.lote.getLote())) {
                Mensajes.mensajeAlert("Lote no valido !!!");
            } else if (this.producto.getLotes().indexOf(this.lote) == -1) {
                TOProductoAlmacen toProd = movimientos.Movimientos.convertir(this.lote);
                this.dao.agregarProducto(toProd);
                this.lote.setFechaCaducidad(toProd.getFechaCaducidad());
                this.producto.getLotes().add(this.lote);
                ok = true;
            } else {
                Mensajes.mensajeAlert("El lote ya se encuetra en el producto !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", ok);
    }

    public void nuevoLote() {
        this.lote = new ProductoAlmacen(this.entrada.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto());
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        ProductoLotes prod = new ProductoLotes(this.mbBuscar.getProducto());
        for (ProductoLotes p : this.detalle) {
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

    public void salir() {
        this.modoEdicion = false;
    }

    private ProductoLotes convertir(TOProductoLotes toProd) throws SQLException {
        ProductoLotes prod = new ProductoLotes(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        return prod;
    }

    private void obtenDetalle(int idMovtoAlmacen) {
        this.detalle = new ArrayList<>();
        try {
            this.dao = new DAOMovimientosAlmacen();
            for (TOProductoLotes to : this.dao.obtenerDetalle(idMovtoAlmacen)) {
                this.detalle.add(this.convertir(to));
            }
            for(ProductoLotes prod: this.detalle) {
                for(ProductoAlmacen l: prod.getLotes()) {
                    prod.setCantidad(prod.getCantidad()+l.getCantidad());
                }
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void obtenerDetalle(SelectEvent event) {
        this.entrada = ((MovimientoAlmacen) event.getObject());
        this.obtenDetalle(this.entrada.getIdMovtoAlmacen());
        this.producto = new ProductoLotes();
        this.modoEdicion = true;
    }

    private MovimientoAlmacen convertir(TOMovimientoAlmacen toMov) throws SQLException {
        MovimientoAlmacen mov = new MovimientoAlmacen(this.tipo, this.mbAlmacenes.getToAlmacen());
        movimientos.Movimientos.convertir(toMov, mov);
        return mov;
    }

    public void pendientes() {
        boolean ok = false;
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un almacen !!!");
        } else {
            this.pendientes = new ArrayList<>();
            try {
                this.dao = new DAOMovimientosAlmacen();
                for (TOMovimientoAlmacen to : this.dao.obtenerMovimientos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.getTipo().getIdTipo(), 0, new Date())) {
                    this.pendientes.add(this.convertir(to));
                }
                ok = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("ok", ok);
    }

    private TOMovimientoAlmacen convertir(MovimientoAlmacen mov) {
        TOMovimientoAlmacen toMov = new TOMovimientoAlmacen();
        movimientos.Movimientos.convertir(mov, toMov);
        return toMov;
    }

    public void capturar() {
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un almacen !!!");
        } else {
            this.entrada = new MovimientoAlmacen(this.tipo, this.mbAlmacenes.getToAlmacen());
            try {
                this.dao = new DAOMovimientosAlmacen();
                this.entrada.setIdMovtoAlmacen(this.dao.agregarMovimiento(this.convertir(this.entrada), false));
                this.detalle = new ArrayList<>();
                this.producto = new ProductoLotes();
                this.modoEdicion = true;
                this.lote = new ProductoAlmacen();
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public String terminar() {
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    private void obtenerTipos() {
        try {
            this.listaMovimientosTipos = new ArrayList<>();
            this.tipo = new MovimientoTipo(0, "Seleccione");
            this.listaMovimientosTipos.add(new SelectItem(this.tipo, this.tipo.toString()));

            this.dao = new DAOMovimientosAlmacen();
            for (MovimientoTipo t : this.dao.obtenerMovimientosTipos(true)) {
                this.listaMovimientosTipos.add(new SelectItem(t, t.toString()));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.mbBuscar.inicializar();
        this.modoEdicion = false;
        this.listaMovimientosTipos = null;
        this.detalle = new ArrayList<>();
    }

    private void inicializa() {
        this.inicializar();
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(27);
        }
        return acciones;
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

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public ArrayList<SelectItem> getListaMovimientosTipos() {
        if (this.listaMovimientosTipos == null) {
            this.obtenerTipos();
        }
        return listaMovimientosTipos;
    }

    public void setListaMovimientosTipos(ArrayList<SelectItem> listaMovimientosTipos) {
        this.listaMovimientosTipos = listaMovimientosTipos;
    }

    public MovimientoTipo getTipo() {
        return tipo;
    }

    public void setTipo(MovimientoTipo tipo) {
        this.tipo = tipo;
    }

    public ArrayList<ProductoLotes> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<ProductoLotes> detalle) {
        this.detalle = detalle;
    }

    public ProductoLotes getProducto() {
        return producto;
    }

    public void setProducto(ProductoLotes producto) {
        this.producto = producto;
    }

    public ProductoAlmacen getLote() {
        return lote;
    }

    public void setLote(ProductoAlmacen lote) {
        this.lote = lote;
    }

    public MovimientoAlmacen getEntrada() {
        return entrada;
    }

    public void setEntrada(MovimientoAlmacen entrada) {
        this.entrada = entrada;
    }

    public ArrayList<MovimientoAlmacen> getPendientes() {
        return pendientes;
    }

    public void setPendientes(ArrayList<MovimientoAlmacen> pendientes) {
        this.pendientes = pendientes;
    }
}
