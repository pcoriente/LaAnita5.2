package movimientos;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
import cedis.MbMiniCedis;
import entradas.dominio.MovimientoProducto;
import entradas.dominio.MovimientoRelacionadoProductoReporte;
import movimientos.to.TOMovimiento;
import movimientos.to.TOMovimientoProducto;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import movimientos.dao.DAOLotes;
import movimientos.dominio.EntradaProducto;
import movimientos.dominio.Lote;
import movimientos.dominio.Recepcion;
import entradas.to.TOEntradaProducto;
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
    private DAOLotes daoLotes;
    private Lote lote;
    private ArrayList<Recepcion> recepciones;
    private Recepcion recepcion;
    private ArrayList<EntradaProducto> recepcionDetalle;
    private EntradaProducto recepcionProducto;
//    private EntradaProducto resRecepcionProducto;
//    private double sumaLotes;

    public MbRecepcion() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbCedis = new MbMiniCedis();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    private MovimientoRelacionadoProductoReporte convertirProductoReporte(EntradaProducto prod) {
        boolean ya = false;
        MovimientoRelacionadoProductoReporte rep = new MovimientoRelacionadoProductoReporte();
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setCantFacturada(prod.getCantFacturada());
        rep.setUnitario(prod.getUnitario());
        for (Lote l : prod.getLotes()) {
            if (l.getSeparados() != 0) {
                if (ya) {
                    rep.getLotes().add(l);
                } else {
                    rep.setLote(l.getLote());
                    rep.setLoteCantidad(l.getSeparados());
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
        for (EntradaProducto p : this.recepcionDetalle) {
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

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (newValue != null && newValue != oldValue) {
            oldValue = newValue;
            this.lote = this.recepcionProducto.getLotes().get(event.getRowIndex());
        } else {
            newValue = oldValue;
            Mensajes.mensajeAlert("Checar que pasa !!!");
        }
    }

    public void grabar() {
        try {
            if (this.recepcionDetalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else {
                double total = 0;
                for (MovimientoProducto e : this.recepcionDetalle) {
                    total += e.getCantFacturada();
                }
                if (total != 0) {
                    this.dao = new DAOMovimientos();
                    TOMovimiento to=this.convertirTOMovimiento();
                    ArrayList<TOEntradaProducto> detalle=this.convertirDetalle();
                    this.dao.grabarTraspasoRecepcion(to, detalle);
                    this.recepcion.setIdUsuario(to.getIdUsuario());
                    this.recepcion.setEstatus(1);
                    Mensajes.mensajeSucces("La recepcion se grabo correctamente !!!");
                    this.obtenerRecepciones();
                } else {
                    Mensajes.mensajeAlert("No hay unidades en el movimiento !!!");
                }
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public ArrayList<TOEntradaProducto> convertirDetalle() {
        ArrayList<TOEntradaProducto> listaDetalle = new ArrayList<>();
        for (EntradaProducto p : this.recepcionDetalle) {
            listaDetalle.add(this.convertirTOMovimientoDetalle(p));
        }
        return listaDetalle;
    }

    public TOEntradaProducto convertirTOMovimientoDetalle(EntradaProducto d) {
        TOEntradaProducto to = new TOEntradaProducto();
        to.setIdProducto(d.getProducto().getIdProducto());
        to.setCantOrdenada(d.getCantOrdenada());
        to.setCantFacturada(d.getCantFacturada());
        to.setCantRecibida(0);
        to.setCantSinCargo(0);
        to.setCostoOrdenado(0);
        to.setCosto(d.getCosto());
        to.setDesctoConfidencial(0);
        to.setDesctoProducto1(0);
        to.setDesctoProducto2(0);
        to.setUnitario(d.getUnitario());
        to.setIdImpuestoGrupo(0);
        to.setLotes(d.getLotes());
        return to;
    }

    public TOMovimiento convertirTOMovimiento() {
        TOMovimiento to = new TOMovimiento();
        to.setIdMovto(this.recepcion.getIdMovto());
        to.setIdTipo(3);
        to.setIdCedis(this.recepcion.getAlmacen().getIdCedis());
        to.setIdEmpresa(this.recepcion.getAlmacen().getIdEmpresa());
        to.setIdAlmacen(this.recepcion.getAlmacen().getIdAlmacen());
        to.setFolio(this.recepcion.getFolio());
        to.setFecha(this.recepcion.getFecha());
        to.setIdUsuario(this.recepcion.getIdUsuario());
        to.setIdReferencia(this.recepcion.getAlmacenOrigen().getIdAlmacen());
        to.setReferencia(this.recepcion.getReferencia());
        to.setIdMovtoAlmacen(this.recepcion.getIdMovtoAlmacen());
//        to.setFolioAlmacen(this.recepcion.getFolioAlmacen());
        return to;
    }

    public void gestionarLotes() {
        if (this.lote.getSeparados() < 0) {
            Mensajes.mensajeAlert("Cantidad recibida no puede ser menor que cero");
            this.lote.setSeparados(this.lote.getCantidad());
        } else if (this.lote.getSeparados() > this.lote.getSaldo()) {
            Mensajes.mensajeAlert("Cantidad recibida mayor que cantidad enviada");
            this.lote.setSeparados(this.lote.getCantidad());
        } else {
//            this.sumaLotes-=this.resSeparados;
//            this.sumaLotes+=this.lote.getSeparados();
//            this.resSeparados=this.lote.getSeparados();
            this.recepcionProducto.setCantFacturada(this.recepcionProducto.getCantFacturada() - this.lote.getCantidad() + this.lote.getSeparados());
            this.lote.setCantidad(this.lote.getSeparados());
        }
    }

    public boolean comparaLotes(Lote lote) {
        boolean disable = true;
        if (this.lote.getLote().equals(lote.getLote())) {
            disable = false;
        }
        return disable;
    }

    public void respaldaSeparados() {
//        this.resSeparados = this.lote.getSeparados();
    }

//    public void actualizarCantidad() {
//        this.recepcionProducto.setCantFacturada(this.sumaLotes);
//        this.resRecepcionProducto.setCantFacturada(this.sumaLotes);
//    }
    public boolean comparaProductos(MovimientoProducto prod) {
        if (prod.getProducto().getIdProducto() == this.recepcionProducto.getProducto().getIdProducto()) {
            return false;
        } else {
            return true;
        }
    }

    public void editarLotes(SelectEvent event) {
        this.recepcionProducto = (EntradaProducto) event.getObject();
//        this.lote=new Lote();
//        this.sumaLotes=0;
//        for(Lote l:this.recepcionProducto.getLotes()) {
//            this.sumaLotes+=l.getSeparados();
//        }
    }

//    public void respaldaFila() {
//        this.resRecepcionProducto.setCantOrdenada(this.recepcionProducto.getCantOrdenada());
//        this.resRecepcionProducto.setCantFacturada(this.recepcionProducto.getCantFacturada());
//        this.resRecepcionProducto.setCantRecibida(this.recepcionProducto.getCantRecibida());
//        this.resRecepcionProducto.setDesctoConfidencial(this.recepcionProducto.getDesctoConfidencial());
//        this.resRecepcionProducto.setDesctoProducto1(this.recepcionProducto.getDesctoProducto1());
//        this.resRecepcionProducto.setDesctoProducto2(this.recepcionProducto.getDesctoProducto2());
//        this.resRecepcionProducto.setProducto(this.recepcionProducto.getProducto());
//        this.resRecepcionProducto.setImporte(this.recepcionProducto.getImporte());
//        this.resRecepcionProducto.setNeto(this.recepcionProducto.getNeto());
//        this.resRecepcionProducto.setUnitario(this.recepcionProducto.getUnitario());
//        this.resRecepcionProducto.setCosto(this.recepcionProducto.getCosto());
//        this.resRecepcionProducto.setLotes(this.recepcionProducto.getLotes());
//        this.resRecepcionProducto.setImpuestos(this.recepcionProducto.getImpuestos());
//    }
    public void salir() {
//        this.inicializar();
        this.modoEdicion = false;
    }

    public void cargaDetalleRecepcion(SelectEvent event) {
        this.recepcion = (Recepcion) event.getObject();
        try {
            this.dao = new DAOMovimientos();
            this.daoLotes = new DAOLotes();
            this.recepcionDetalle = new ArrayList<>();
            for (TOMovimientoProducto p : this.dao.obtenerDetalle(this.recepcion.getIdMovto())) {
                this.recepcionDetalle.add(this.convertirDetalle(p));
            }
            this.recepcionProducto = new EntradaProducto();
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private EntradaProducto convertirDetalle(TOMovimientoProducto to) throws SQLException {
        EntradaProducto p = new EntradaProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantOrdenada(to.getCantOrdenada());
        p.setCantFacturada(to.getCantFacturada());
        p.setCantRecibida(0);
        p.setCantSinCargo(0);
        p.setCostoOrdenado(0);
        p.setCosto(to.getCosto());
        p.setDesctoProducto1(to.getDesctoProducto1());
        p.setDesctoProducto2(to.getDesctoProducto2());
        p.setDesctoConfidencial(to.getDesctoConfidencial());
        p.setUnitario(to.getUnitario());
        p.setImporte(to.getUnitario() * to.getCantFacturada());
        p.setLotes(this.daoLotes.obtenerLotesMovtoEmpaque(this.recepcion.getIdMovtoAlmacen(), to.getIdProducto()));
        double suma = 0;
        for (Lote l : p.getLotes()) {
            suma += l.getSeparados();
        }
        if (p.getCantFacturada() != suma) {
            throw new SQLException("Error de sincronizacion Lotes en producto: " + p.getProducto().getIdProducto());
        }
        return p;
    }

    public void obtenerRecepciones() {
        this.recepciones = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for (TOMovimiento m : this.dao.obtenerMovimientos(this.toAlmacen.getIdAlmacen(), 9, 0, new Date())) {
                this.recepciones.add(this.convertir(m));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private Recepcion convertir(TOMovimiento to) {
        Recepcion e = new Recepcion();
        e.setIdMovto(to.getIdMovto());
        e.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        e.setAlmacen(this.toAlmacen);
        e.setFolio(to.getFolio());
        e.setFecha(to.getFecha());
        e.setIdUsuario(to.getIdUsuario());
        e.setAlmacenOrigen(this.mbAlmacenes.obtenerTOAlmacen(to.getIdReferencia()));
        e.setReferencia(to.getReferencia());
        e.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
//        e.setFolioAlmacen(to.getFolioAlmacen());
        return e;
    }

    // ---------------- getters and setters ----------------
//    public double getSumaLotes() {
//        return sumaLotes;
//    }
//
//    public void setSumaLotes(double sumaLotes) {
//        this.sumaLotes = sumaLotes;
//    }
    public ArrayList<EntradaProducto> getRecepcionDetalle() {
        return recepcionDetalle;
    }

    public void setRecepcionDetalle(ArrayList<EntradaProducto> recepcionDetalle) {
        this.recepcionDetalle = recepcionDetalle;
    }

    public EntradaProducto getRecepcionProducto() {
        return recepcionProducto;
    }

    public void setRecepcionProducto(EntradaProducto recepcionProducto) {
        this.recepcionProducto = recepcionProducto;
    }

//    public EntradaProducto getResRecepcionProducto() {
//        return resRecepcionProducto;
//    }
//
//    public void setResRecepcionProducto(EntradaProducto resRecepcionProducto) {
//        this.resRecepcionProducto = resRecepcionProducto;
//    }
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

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
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
//        this.resRecepcionProducto=new EntradaProducto();
        this.lote = new Lote();
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
