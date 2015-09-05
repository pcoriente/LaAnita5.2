package traspasos;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
import cedis.MbMiniCedis;
import entradas.dominio.MovimientoRelacionadoProductoReporte;
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
import movimientos.dao.DAOMovimientos;
import movimientos.dominio.Lote;
import movimientos.to.TOProductoAlmacen;
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
import traspasos.dominio.Traspaso;
import traspasos.dominio.TraspasoProducto;
import traspasos.to.TOTraspaso;
import traspasos.to.TOTraspasoProducto;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbTraspaso")
@SessionScoped
public class MbTraspaso implements Serializable {

    private boolean modoEdicion;
    private Lote lote;
    private double sumaLotes;
    private Traspaso traspaso;
    private TOAlmacenJS toAlmacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private ArrayList<Traspaso> traspasos;
    private ArrayList<TraspasoProducto> traspasoDetalle;
    private TraspasoProducto traspasoProducto;
    private DAOMovimientos dao;
    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniCedis}")
    private MbMiniCedis mbCedis;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;

    public MbTraspaso() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbCedis = new MbMiniCedis();
        this.mbBuscar = new MbProductosBuscar();

        this.inicializa();
    }
    
    private MovimientoRelacionadoProductoReporte convertirProductoReporte(TraspasoProducto prod) {
        boolean ya = false;
        MovimientoRelacionadoProductoReporte rep = new MovimientoRelacionadoProductoReporte();
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setCantFacturada(prod.getCantFacturada());
        rep.setUnitario(prod.getUnitario());
        for (TOProductoAlmacen l : prod.getLotes()) {
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

        this.cargaDetalle();
        ArrayList<MovimientoRelacionadoProductoReporte> detalleReporte = new ArrayList<>();
        for (TraspasoProducto p : this.traspasoDetalle) {
            if (p.getCantFacturada() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\Traspaso.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.traspaso.getAlmacen().getEmpresa());

        parameters.put("cedis", this.traspaso.getAlmacen().getCedis());
        parameters.put("almacen", this.traspaso.getAlmacen().getAlmacen());

        parameters.put("concepto", "TRASPASO AL ALMACEN :");

        parameters.put("cedisOrigen", this.traspaso.getAlmacenDestino().getCedis());
        parameters.put("almacenOrigen", this.traspaso.getAlmacenDestino().getAlmacen());

        parameters.put("capturaFolio", this.traspaso.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.traspaso.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.traspaso.getFecha()));

        parameters.put("idUsuario", this.traspaso.getIdUsuario());

        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=Traspaso_" + this.traspaso.getFolio() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private TOTraspaso convertir(Traspaso traspaso) {
        TOTraspaso to = new TOTraspaso();
        to.setSolicitudFolio(traspaso.getSolicitudFolio());
        to.setSolicitudFecha(traspaso.getSolicitudFecha());
        to.setSolicitudIdUsuario(traspaso.getSolicitudIdUsuario());
        to.setSolicitudProietario(traspaso.getSolicitudProietario());
        to.setSolicitudEstatus(traspaso.getSolicitudEstatus());
        movimientos.Movimientos.convertir(traspaso, to);
        to.setIdReferencia(traspaso.getAlmacenDestino().getIdAlmacen());
        to.setReferencia(traspaso.getIdSolicitud());
        return to;
    }
    
    public void grabar() {
        try {
            if (this.traspasoDetalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else {
                double total = 0;
                for (TraspasoProducto e : this.traspasoDetalle) {
                    total += e.getCantFacturada();
                }
                if (total != 0) {
                    this.dao = new DAOMovimientos();
                    TOTraspaso to = this.convertir(this.traspaso);
                    this.dao.grabarTraspasoEnvio(this.traspaso.getAlmacen().getIdEmpresa(), to);
                    this.traspaso.setFolio(to.getFolio());
                    this.traspaso.setFecha(to.getFecha());
                    this.traspaso.setIdUsuario(to.getIdUsuario());
                    this.traspaso.setEstatus(to.getEstatus());
                    Mensajes.mensajeSucces("El traspaso se grabo correctamente !!!");
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
    
    public TOTraspasoProducto convertir(TraspasoProducto traspasoProducto) {
        TOTraspasoProducto toTraspasoProducto = new TOTraspasoProducto();
        toTraspasoProducto.setIdSolicitud(traspasoProducto.getIdSolicitud());
        toTraspasoProducto.setCantSolicitada(traspasoProducto.getCantSolicitada());
        movimientos.Movimientos.convertir(traspasoProducto, toTraspasoProducto);
        return toTraspasoProducto;
    }

    public void actualizarProducto() {
        TOTraspaso toMov = this.convertir(this.traspaso);
        TOTraspasoProducto toProd = this.convertir(this.traspasoProducto);
        this.traspasoProducto.setCantFacturada(this.traspasoProducto.getSeparados());
        try {
            this.dao = new DAOMovimientos();
            this.traspasoProducto.setLotes(this.dao.grabarMovimientoDetalle(this.traspaso.getAlmacen().getIdEmpresa(), toMov, toProd, this.traspasoProducto.getCantFacturada(), false));
            this.traspasoProducto.setCantFacturada(toProd.getCantFacturada());
            this.traspasoProducto.setSeparados(this.traspasoProducto.getCantFacturada());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.traspasoProducto = this.traspasoDetalle.get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
        } else {
            this.traspasoProducto.setCantSolicitada((Double) oldValue);
        }
    }

    private TraspasoProducto convertir(TOTraspasoProducto to) throws SQLException {
        TraspasoProducto p = new TraspasoProducto();
        p.setIdSolicitud(to.getIdSolicitud());
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantSolicitada(to.getCantSolicitada());
        movimientos.Movimientos.convertir(to, p);
        p.setLotes(this.dao.obtenerLotes(this.traspaso.getIdMovtoAlmacen(), to.getIdProducto()));
        return p;
    }
    
    private void cargaDetalle() {
        try {
            this.dao = new DAOMovimientos();
            this.traspasoDetalle = new ArrayList<>();
            for (TOTraspasoProducto to : this.dao.obtenerTraspasoDetalle(this.traspaso.getIdSolicitud())) {
                this.traspasoDetalle.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cargarDetalle(SelectEvent event) {
        this.traspaso = (Traspaso) event.getObject();
        this.cargaDetalle();
        this.traspasoProducto = new TraspasoProducto();
        this.modoEdicion = true;
    }
    
    private Traspaso convertir(TOTraspaso to) {
        Traspaso e = new Traspaso();
        e.setIdSolicitud(to.getReferencia());
        e.setSolicitudFolio(to.getSolicitudFolio());
        e.setSolicitudFecha(to.getSolicitudFecha());
        e.setSolicitudIdUsuario(to.getSolicitudIdUsuario());
        e.setSolicitudProietario(to.getSolicitudProietario());
        e.setSolicitudEstatus(to.getSolicitudEstatus());
        e.setAlmacen(this.toAlmacen);
        movimientos.Movimientos.convertir(to, e);
        e.setAlmacenDestino(this.mbAlmacenes.obtenerTOAlmacen(to.getIdReferencia()));
        return e;
    }

    public void obtenerSolicitudes() {
        boolean ok = false;
        this.traspasos = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for (TOTraspaso to : this.dao.obtenerTraspasos(this.toAlmacen.getIdAlmacen(), 1, new Date())) {
                this.traspasos.add(this.convertir(to));
            }
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLista", ok);
    }

    public void salir() {
        this.obtenerSolicitudes();
        this.modoEdicion = false;
    }

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }
    
    private void inicializa() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.toAlmacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
        this.mbCedis.cargaMiniCedisTodos();
        this.mbBuscar.inicializar();
        this.inicializaLocales();
    }

    public void inicializar() {
        this.mbBuscar.inicializar();
        inicializaLocales();
    }

    private void inicializaLocales() {
        this.modoEdicion = false;
        this.lote = new Lote();
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public ArrayList<TraspasoProducto> getTraspasoDetalle() {
        return traspasoDetalle;
    }

    public void setTraspasoDetalle(ArrayList<TraspasoProducto> traspasoDetalle) {
        this.traspasoDetalle = traspasoDetalle;
    }

    public TraspasoProducto getTraspasoProducto() {
        return traspasoProducto;
    }

    public void setTraspasoProducto(TraspasoProducto traspasoProducto) {
        this.traspasoProducto = traspasoProducto;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(18);
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

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public Traspaso getTraspaso() {
        return traspaso;
    }

    public void setTraspaso(Traspaso traspaso) {
        this.traspaso = traspaso;
    }

    public ArrayList<Traspaso> getTraspasos() {
        return traspasos;
    }

    public void setTraspasos(ArrayList<Traspaso> traspasos) {
        this.traspasos = traspasos;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public double getSumaLotes() {
        return sumaLotes;
    }

    public void setSumaLotes(double sumaLotes) {
        this.sumaLotes = sumaLotes;
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
}
