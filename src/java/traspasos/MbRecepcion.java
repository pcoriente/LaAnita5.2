package traspasos;

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
import traspasos.dominio.Recepcion;
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
import movimientos.dao.DAOMovimientos;
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
import traspasos.dominio.RecepcionProducto;
import traspasos.to.TORecepcion;
import traspasos.to.TORecepcionProducto;
import traspasos.to.TORecepcionProductoAlmacen;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbRecepcion")
@SessionScoped
public class MbRecepcion implements Serializable {

    private boolean modoEdicion;
    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniCedis}")
    private MbMiniCedis mbCedis;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private DAOMovimientos dao;
//    private DAOImpuestosProducto daoImps;
    private TOAlmacenJS toAlmacen;
    private ArrayList<SelectItem> listaAlmacenes;
//    private DAOLotes daoLotes;
    private TORecepcionProductoAlmacen lote;
    private ArrayList<Recepcion> recepciones;
    private Recepcion recepcion;
    private ArrayList<RecepcionProducto> recepcionDetalle;
    private RecepcionProducto recepcionProducto;
//    private EntradaProducto resRecepcionProducto;
//    private double sumaLotes;

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
        for (TORecepcionProductoAlmacen l : prod.getLotes()) {
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
        for (RecepcionProducto p : this.recepcionDetalle) {
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
    
    public TORecepcion convertir(Recepcion mov) {
        TORecepcion toMov = new TORecepcion();
        toMov.setIdSolicitud(mov.getIdSolicitud());
        toMov.setSolicitudFolio(mov.getSolicitudFolio());
        toMov.setSolicitudFecha(mov.getSolicitudFecha());
        toMov.setTraspasoFolio(mov.getTraspasoFolio());
        toMov.setTraspasoFecha(mov.getTraspasoFecha());
        movimientos.Movimientos.convertir(mov, toMov);
        toMov.setIdReferencia(mov.getAlmacenOrigen().getIdAlmacen());
        toMov.setReferencia(mov.getIdTraspaso());
        return toMov;
    }
    
    public void grabar() {
        try {
//            if (this.recepcionDetalle.isEmpty()) {
//                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
//            } else {
//                double total = 0;
//                for (RecepcionProducto e : this.recepcionDetalle) {
//                    total += e.getCantFacturada();
//                }
//                if (total != 0) {
                    TORecepcion to = this.convertir(this.recepcion);
                    
                    this.dao = new DAOMovimientos();
                    this.dao.grabarTraspasoRecepcion(to);
                    this.recepcion.setFecha(to.getFecha());
                    this.recepcion.setIdUsuario(to.getIdUsuario());
                    this.recepcion.setEstatus(to.getEstatus());
                    Mensajes.mensajeSucces("La recepcion se grabo correctamente !!!");
//                    this.obtenerRecepciones();
//                } else {
//                    Mensajes.mensajeAlert("No hay unidades en el movimiento !!!");
//                }
//            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
    public void gestionarLotes() {
        TORecepcionProductoAlmacen toProd = this.convertir(this.lote);
        this.lote.setCantidad(this.lote.getSeparados());
        if (toProd.getCantidad() < 0) {
            Mensajes.mensajeAlert("Cantidad recibida no puede ser menor que cero");
        } else if (toProd.getCantidad() > this.lote.getCantEnviada()) {
            Mensajes.mensajeAlert("Cantidad recibida mayor que cantidad enviada");
        } else {
            try {
                this.dao = new DAOMovimientos();
                this.dao.actualizaEntradaAlmacen(this.recepcion.getIdMovto(), toProd, this.lote.getSeparados());
                this.lote.setCantidad(toProd.getCantidad());
                
                this.recepcionProducto.setCantFacturada(this.recepcionProducto.getCantFacturada() - this.lote.getSeparados() + this.lote.getCantidad());
                this.lote.setSeparados(this.lote.getCantidad());
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
        this.lote = this.recepcionProducto.getLotes().get(event.getRowIndex());
//        this.lote.setCantidad((Double) oldValue);
        if (newValue != null && newValue != oldValue) {
        } else {
            Mensajes.mensajeAlert("Checar que pasa ( onCellEdit ) !!!");
        }
    }

//    public ArrayList<TORecepcionProducto> convertirDetalle() {
//        ArrayList<TORecepcionProducto> listaDetalle = new ArrayList<>();
//        for (RecepcionProducto p : this.recepcionDetalle) {
//            listaDetalle.add(this.convertir(p));
//        }
//        return listaDetalle;
//    }
//
//    public TORecepcionProducto convertir(RecepcionProducto d) {
//        TORecepcionProducto to = new TORecepcionProducto();
//        to.setCantSolicitada(d.getCantSolicitada());
//        to.setCantEnviada(d.getCantEnviada());
//        movimientos.Movimientos.convertir(d, to);
//        return to;
//    }

//    public boolean comparaLotes(Lote lote) {
//        boolean disable = true;
//        if (this.lote.getLote().equals(lote.getLote())) {
//            disable = false;
//        }
//        return disable;
//    }

//    public void respaldaSeparados() {
////        this.resSeparados = this.lote.getSeparados();
//    }

//    public boolean comparaProductos(MovimientoProducto prod) {
//        if (prod.getProducto().getIdProducto() == this.recepcionProducto.getProducto().getIdProducto()) {
//            return false;
//        } else {
//            return true;
//        }
//    }

    public void editarLotes(SelectEvent event) {
        this.recepcionProducto = (RecepcionProducto) event.getObject();
    }

    public void salir() {
        this.obtenerRecepciones();
        this.modoEdicion = false;
    }

    private TORecepcionProductoAlmacen convertir(TOProductoAlmacen to) {
        TORecepcionProductoAlmacen rec = new TORecepcionProductoAlmacen();
        rec.setCantEnviada(to.getCantidad());
        rec.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        rec.setIdProducto(to.getIdProducto());
        rec.setLote(to.getLote());
        rec.setCantidad(to.getCantidad());
        rec.setSeparados(to.getCantidad());
        rec.setFechaCaducidad(to.getFechaCaducidad());
        return rec;
    }

    private RecepcionProducto convertir(TORecepcionProducto toProd) throws SQLException {
        double suma = 0;
        RecepcionProducto prod = new RecepcionProducto();
        prod.setCantSolicitada(toProd.getCantSolicitada());
        prod.setCantEnviada(toProd.getCantEnviada());
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        for (TOProductoAlmacen l : this.dao.obtenerLotes(this.recepcion.getIdMovtoAlmacen(), toProd.getIdProducto())) {
            prod.getLotes().add(this.convertir(l));
            suma += l.getCantidad();
        }
        if (prod.getCantFacturada() != suma) {
            throw new SQLException("Error de sincronizacion Lotes vs oficina en producto (id=" + toProd.getIdProducto() + ") del movimiento !!!");
        }
        return prod;
    }

    private void cargaDetalle() {
        try {
            this.dao = new DAOMovimientos();
//            this.daoLotes = new DAOLotes();
            this.recepcionDetalle = new ArrayList<>();
            for (TORecepcionProducto p : this.dao.obtenerRecepcionDetalle(this.recepcion.getIdMovto())) {
                this.recepcionDetalle.add(this.convertir(p));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cargarDetalle(SelectEvent event) {
        this.recepcion = (Recepcion) event.getObject();
        this.cargaDetalle();
        this.recepcionProducto = new RecepcionProducto();
        this.modoEdicion = true;
    }

    private Recepcion convertir(TORecepcion toMov) {
        Recepcion mov = new Recepcion();
        mov.setIdSolicitud(toMov.getIdSolicitud());
        mov.setSolicitudFolio(toMov.getSolicitudFolio());
        mov.setSolicitudFecha(toMov.getSolicitudFecha());
        mov.setIdTraspaso(toMov.getReferencia());
        mov.setTraspasoFolio(toMov.getTraspasoFolio());
        mov.setTraspasoFecha(toMov.getTraspasoFecha());
        mov.setAlmacen(this.toAlmacen);
        mov.setAlmacenOrigen(this.mbAlmacenes.obtenerTOAlmacen(toMov.getIdReferencia()));
        movimientos.Movimientos.convertir(toMov, mov);
        return mov;
    }

    public void obtenerRecepciones() {
        this.recepciones = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for (TORecepcion m : this.dao.obtenerRecepciones(this.toAlmacen.getIdAlmacen(), 1, new Date())) {
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
    public ArrayList<RecepcionProducto> getRecepcionDetalle() {
        return recepcionDetalle;
    }

    public void setRecepcionDetalle(ArrayList<RecepcionProducto> recepcionDetalle) {
        this.recepcionDetalle = recepcionDetalle;
    }

    public RecepcionProducto getRecepcionProducto() {
        return recepcionProducto;
    }

    public void setRecepcionProducto(RecepcionProducto recepcionProducto) {
        this.recepcionProducto = recepcionProducto;
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

    public TORecepcionProductoAlmacen getLote() {
        return lote;
    }

    public void setLote(TORecepcionProductoAlmacen lote) {
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
        this.lote = new TORecepcionProductoAlmacen();
        this.recepciones = new ArrayList<>();
    }

    private void inicializa() {
        inicializar();
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.toAlmacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
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

    public TOAlmacenJS getToAlmacen() {
        return toAlmacen;
    }

    public void setToAlmacen(TOAlmacenJS toAlmacen) {
        this.toAlmacen = toAlmacen;
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
