package recepciones;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
import cedis.MbMiniCedis;
import entradas.dominio.MovimientoRelacionadoProductoReporte;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import movimientos.dominio.MovimientoTipo;
import movimientos.to.TOProductoAlmacen;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import recepciones.dao.DAORecepciones;
import recepciones.dominio.Recepcion;
import recepciones.dominio.RecepcionProducto;
import recepciones.dominio.RecepcionProductoAlmacen;
import recepciones.to.TORecepcion;
import recepciones.to.TORecepcionProducto;
import recepciones.to.TORecepcionProductoAlmacen;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbRecepcion")
@SessionScoped
public class MbRecepcion implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniCedis}")
    private MbMiniCedis mbCedis;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private boolean modoEdicion;

    private TOAlmacenJS almacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private ArrayList<Recepcion> recepciones;
    private Recepcion recepcion;
    private ArrayList<RecepcionProducto> detalle;
    private RecepcionProducto producto;
    private RecepcionProductoAlmacen lote;
    private DAORecepciones dao;

    public MbRecepcion() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbCedis = new MbMiniCedis();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    private MovimientoRelacionadoProductoReporte convertirProductoReporte(RecepcionProducto prod) {
        boolean ya = false;
        MovimientoRelacionadoProductoReporte rep = new MovimientoRelacionadoProductoReporte();
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setCantFacturada(prod.getCantFacturada());
        rep.setUnitario(prod.getUnitario());
        for (RecepcionProductoAlmacen l : prod.getLotes()) {
            if (l.getCantidad() != 0) {
                if (ya) {
                    rep.getLotes().add(l);
                } else {
                    rep.setLote(l.getLote());
                    rep.setLoteCantidad(l.getCantidad());
                    ya = true;
                }
            }
        }
        return rep;
    }

    public void imprimir() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        ArrayList<MovimientoRelacionadoProductoReporte> detalleReporte = new ArrayList<>();
        for (RecepcionProducto p : this.detalle) {
            if (p.getCantFacturada() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\Traspaso.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.recepcion.getAlmacen().getEmpresa());

        parameters.put("cedis", this.recepcion.getAlmacen().getCedis());
        parameters.put("almacen", this.recepcion.getAlmacen().getAlmacen());

        parameters.put("concepto", "RECEPCION DEL ALMACEN :");

        parameters.put("cedisOrigen", this.recepcion.getAlmacenOrigen().getCedis());
        parameters.put("almacenOrigen", this.recepcion.getAlmacenOrigen().getAlmacen());

        parameters.put("capturaFolio", this.recepcion.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.recepcion.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.recepcion.getFecha()));

        parameters.put("idUsuario", this.recepcion.getIdUsuario());

        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=Recepcion_" + this.recepcion.getFolio() + ".pdf");
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
        TORecepcion toMov = this.convertir(this.recepcion);
        try {
            this.dao = new DAORecepciones();
            this.dao.cancelar(toMov);
            this.recepcion.setFecha(toMov.getFecha());
            this.recepcion.setIdUsuario(toMov.getIdUsuario());
            this.recepcion.setEstatus(toMov.getEstatus());
            Mensajes.mensajeSucces("La recepcion se cancelo correctamente !!!");
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public TORecepcion convertir(Recepcion mov) {
        TORecepcion toMov = new TORecepcion();
        toMov.setSolicitudFolio(mov.getSolicitudFolio());
        toMov.setSolicitudFecha(mov.getSolicitudFecha());
        toMov.setTraspasoFolio(mov.getTraspasoFolio());
        toMov.setTraspasoFecha(mov.getTraspasoFecha());
        movimientos.Movimientos.convertir(mov, toMov);
        toMov.setIdReferencia(mov.getAlmacenOrigen().getIdAlmacen());
        toMov.setReferencia(mov.getIdTraspaso());
        return toMov;
    }

    private double sumaPiezas() {
        double suma = 0;
        double sumaLotes;
        for (RecepcionProducto prod : this.detalle) {
            sumaLotes = 0;
            for (RecepcionProductoAlmacen l : prod.getLotes()) {
                sumaLotes += l.getCantidad();
            }
            if (sumaLotes != prod.getCantFacturada()) {
                suma = -1;
                break;
            }
            suma += prod.getCantFacturada();
        }
        return suma;
    }

    public void grabar() {
        double suma;
        if (this.detalle.isEmpty()) {
            Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
        } else if ((suma = sumaPiezas()) == 0) {
            Mensajes.mensajeAlert("No tiene piezas que recepcionar !!!");
        } else if (suma == -1) {
            Mensajes.mensajeAlert("Inconsistencia con lotes !!!");
        } else {
            TORecepcion toMov = this.convertir(this.recepcion);
            try {
                this.dao = new DAORecepciones();
                this.dao.grabar(toMov);
                this.recepcion.setFecha(toMov.getFecha());
                this.recepcion.setIdUsuario(toMov.getIdUsuario());
                this.recepcion.setEstatus(toMov.getEstatus());
                Mensajes.mensajeSucces("La recepcion se grabo correctamente !!!");
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }

    }

    public void gestionar() {
        TOProductoAlmacen toProd = movimientos.Movimientos.convertir(this.lote);
        this.lote.setCantidad(this.lote.getSeparados());
        if (toProd.getCantidad() < 0) {
            Mensajes.mensajeAlert("Cantidad recibida no puede ser menor que cero");
        } else if (toProd.getCantidad() > this.lote.getCantTraspasada()) {
            Mensajes.mensajeAlert("Cantidad recibida mayor que cantidad traspasada");
        } else {
            try {
                this.dao = new DAORecepciones();
                this.dao.actualizarCantidad(this.recepcion.getIdMovto(), toProd, this.lote.getSeparados());
                this.producto.setCantFacturada(this.producto.getCantFacturada() - this.lote.getSeparados() + toProd.getCantidad());
                this.lote.setCantidad(toProd.getCantidad());
                this.lote.setSeparados(toProd.getCantidad());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.lote = this.producto.getLotes().get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
            oldValue = newValue;
        } else {
            newValue = oldValue;
            Mensajes.mensajeAlert("Checar que pasa ( onCellEdit ) !!!");
        }
    }

    public void editar(SelectEvent event) {
        this.producto = (RecepcionProducto) event.getObject();
    }

    public void salir() {
        this.obtenerRecepciones();
        this.modoEdicion = false;
    }
    
    private RecepcionProductoAlmacen convertirProductoAlmacen(TORecepcionProductoAlmacen toProd) {
        RecepcionProductoAlmacen prod = new RecepcionProductoAlmacen();
//        prod.setCantSolicitada(toProd.getCantSolicitada());
        prod.setCantTraspasada(toProd.getCantTraspasada());
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setSeparados(prod.getCantidad());
        return prod;
    }

    private RecepcionProducto convertir(TORecepcionProducto toProd) throws SQLException {
        RecepcionProducto prod = new RecepcionProducto();
        prod.setCantSolicitada(toProd.getCantSolicitada());
        prod.setCantTraspasada(toProd.getCantTraspasada());
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        double sumaLotes = 0;
        for (TORecepcionProductoAlmacen to : this.dao.obtenerDetalleProducto(this.recepcion.getIdMovtoAlmacen(), toProd.getIdProducto())) {
            prod.getLotes().add(this.convertirProductoAlmacen(to));
            sumaLotes += to.getCantidad();
        }
        if (prod.getCantFacturada() != sumaLotes) {
            throw new SQLException("Error de sincronizacion Lotes vs oficina en producto (id=" + toProd.getIdProducto() + ") del movimiento !!!");
        } else {
            prod.setSumaLotes(sumaLotes);
        }
        return prod;
    }

    private void obtenDetalle() {
        try {
            this.dao = new DAORecepciones();
            this.detalle = new ArrayList<>();
            for (TORecepcionProducto p : this.dao.obtenerDetalle(this.recepcion.getIdMovto())) {
                this.detalle.add(this.convertir(p));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void obtenerDetalle(SelectEvent event) {
        this.recepcion = (Recepcion) event.getObject();
        this.obtenDetalle();
        this.producto = new RecepcionProducto();
        this.modoEdicion = true;
    }

    private Recepcion convertir(TORecepcion toMov) {
        Recepcion mov = new Recepcion(new MovimientoTipo(9, "Recepcion"), this.almacen, this.mbAlmacenes.obtenerTOAlmacen(toMov.getIdReferencia()));
        mov.setSolicitudFolio(toMov.getSolicitudFolio());
        mov.setSolicitudFecha(toMov.getSolicitudFecha());
        mov.setTraspasoFolio(toMov.getTraspasoFolio());
        mov.setTraspasoFecha(toMov.getTraspasoFecha());
        movimientos.Movimientos.convertir(toMov, mov);
        mov.setIdTraspaso(toMov.getReferencia());
        return mov;
    }

    public void obtenerRecepciones() {
        this.recepciones = new ArrayList<>();
        try {
            this.dao = new DAORecepciones();
            for (TORecepcion m : this.dao.obtenerRecepciones(this.almacen.getIdAlmacen(), 4, new Date())) {
                this.recepciones.add(this.convertir(m));
            }
            this.recepcion = new Recepcion();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    // ---------------- getters and setters ----------------
    public ArrayList<RecepcionProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<RecepcionProducto> detalle) {
        this.detalle = detalle;
    }

    public RecepcionProducto getProducto() {
        return producto;
    }

    public void setProducto(RecepcionProducto producto) {
        this.producto = producto;
    }

    public Recepcion getRecepcion() {
        return recepcion;
    }

    public void setRecepcion(Recepcion recepcion) {
        this.recepcion = recepcion;
    }

    public ArrayList<Recepcion> getRecepciones() {
        return recepciones;
    }

    public void setRecepciones(ArrayList<Recepcion> recepciones) {
        this.recepciones = recepciones;
    }

    public RecepcionProductoAlmacen getLote() {
        return lote;
    }

    public void setLote(RecepcionProductoAlmacen lote) {
        this.lote = lote;
    }
    // ------------------- generales -----------------------

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    private void inicializaLocales() {
        this.modoEdicion = false;
        this.lote = new RecepcionProductoAlmacen();
        this.recepciones = new ArrayList<>();
    }

    private void inicializa() {
        inicializar();
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.almacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
        this.mbCedis.cargaMiniCedisTodos();
        this.mbBuscar.inicializar();
        inicializaLocales();
    }

    public boolean isModoEdicion() {
        return modoEdicion;

    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public TOAlmacenJS getAlmacen() {
        return almacen;
    }

    public void setAlmacen(TOAlmacenJS almacen) {
        this.almacen = almacen;
    }

    public ArrayList<SelectItem> getListaAlmacenes() {
        return listaAlmacenes;
    }

    public void setListaAlmacenes(ArrayList<SelectItem> listaAlmacenes) {
        this.listaAlmacenes = listaAlmacenes;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(21);
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

    public MbMiniCedis getMbCedis() {
        return mbCedis;
    }

    public void setMbCedis(MbMiniCedis mbCedis) {
        this.mbCedis = mbCedis;
    }
}