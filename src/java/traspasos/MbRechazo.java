package traspasos;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
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
import movimientos.dao.DAOLotes;
import movimientos.dao.DAOMovimientos;
import movimientos.dominio.EntradaProducto;
import movimientos.to1.Lote1;
import movimientos.dominio.MovimientoRelacionado;
import movimientos.to.TOMovimientoOficina;
import movimientos.to1.TOMovimientoProducto;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbRechazo")
@SessionScoped
public class MbRechazo implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private boolean modoEdicion;
    private TOAlmacenJS almacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private MovimientoRelacionado rechazo;
    private ArrayList<MovimientoRelacionado> rechazos;
    private ArrayList<EntradaProducto> detalle;
    private EntradaProducto producto;
    private Lote1 lote;
    private DAOMovimientos dao;
    private DAOLotes daoLotes;

    public MbRechazo() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
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
        for (Lote1 l : prod.getLotes()) {
            if (l.getSeparados() != 0) {
                if (ya) {
//                    rep.getLotes().add(l);
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
        for (EntradaProducto p : this.detalle) {
            if (p.getCantFacturada() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\Traspaso.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.rechazo.getAlmacenDestino().getEmpresa());

        parameters.put("cedis", this.rechazo.getAlmacenDestino().getCedis());
        parameters.put("almacen", this.rechazo.getAlmacenDestino().getAlmacen());

        parameters.put("concepto", "RECHAZO DEL ALMACEN :");

        parameters.put("cedisOrigen", this.rechazo.getAlmacenOrigen().getCedis());
        parameters.put("almacenOrigen", this.rechazo.getAlmacenOrigen().getAlmacen());

        parameters.put("capturaFolio", this.rechazo.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.rechazo.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.rechazo.getFecha()));

        parameters.put("idUsuario", this.rechazo.getIdUsuario());

        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=Rechazo_" + this.rechazo.getFolio() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void salir() {
        this.obtenerRechazos();
        this.modoEdicion = false;
    }

    public void editarLotes() {
        try {
            this.daoLotes = new DAOLotes();
            this.producto.setLotes(this.daoLotes.obtenerLotes(this.rechazo.getAlmacenDestino().getIdAlmacen(), this.rechazo.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto()));
            this.lote = new Lote1();
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
        p.setCostoOrdenado(to.getCostoOrdenado());
        p.setCantRecibida(to.getCantRecibida());
        p.setCantFacturada(to.getCantFacturada());
        p.setCantSinCargo(to.getCantSinCargo());
        p.setCostoPromedio(to.getCostoPromedio());
        p.setCosto(to.getCosto());
        p.setDesctoProducto1(to.getDesctoProducto1());
        p.setDesctoProducto2(to.getDesctoProducto2());
        p.setDesctoConfidencial(to.getDesctoConfidencial());
        p.setUnitario(to.getUnitario());
        p.setImporte(to.getUnitario() * (to.getCantFacturada() + to.getCantSinCargo()));
        p.setLotes(this.daoLotes.obtenerLotes(this.rechazo.getAlmacenDestino().getIdAlmacen(), this.rechazo.getIdMovtoAlmacen(), to.getIdProducto()));
        int sumaLotes = 0;
        for (Lote1 l : p.getLotes()) {
            sumaLotes += l.getSeparados();
        }
        if (p.getCantFacturada() != sumaLotes) {
            throw new SQLException("(idMovtoAlmacen=" + this.rechazo.getIdMovtoAlmacen() + ") Error de sincronizacion Lotes en producto: " + p.getProducto().getIdProducto());
        }
        return p;
    }

    public void cargaDetalle(SelectEvent event) {
        this.rechazo = (MovimientoRelacionado) event.getObject();
        try {
            this.dao = new DAOMovimientos();
            this.daoLotes = new DAOLotes();
            this.detalle = new ArrayList<>();
            for (TOMovimientoProducto p : this.dao.obtenerDetalle(this.rechazo.getIdMovto())) {
                this.detalle.add(this.convertirDetalle(p));
            }
            this.producto = new EntradaProducto();
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private MovimientoRelacionado convertir(TOMovimientoOficina toMovimiento) {
        MovimientoRelacionado movimiento = new MovimientoRelacionado();
        movimiento.setIdMovto(toMovimiento.getIdMovto());
        movimiento.setIdMovtoAlmacen(toMovimiento.getIdMovtoAlmacen());
        movimiento.setAlmacenDestino(this.almacen);
        movimiento.setFolio(toMovimiento.getFolio());
        movimiento.setFecha(toMovimiento.getFecha());
        movimiento.setIdUsuario(toMovimiento.getIdUsuario());
        movimiento.setAlmacenOrigen(this.mbAlmacenes.obtenerTOAlmacen(toMovimiento.getIdReferencia()));
//        movimiento.setFolioAlmacen(toMovimiento.getFolioAlmacen());
        return movimiento;
    }

    public void obtenerRechazos() {
        boolean ok = false;
        this.rechazos = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for (TOMovimientoOficina m : this.dao.obtenerMovimientos(this.almacen.getIdAlmacen(), 53, 1, new Date())) {
                this.rechazos.add(this.convertir(m));
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

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.inicializa();
        return "index.xhtml";
    }

    public void inicializar() {
        this.inicializa();
    }

    private void inicializaLocales() {
        this.modoEdicion = false;
        this.almacen = new TOAlmacenJS();
    }

    private void inicializa() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.almacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
        this.rechazos = new ArrayList<>();
        this.mbBuscar.inicializar();
        this.inicializaLocales();
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

    public MovimientoRelacionado getRechazo() {
        return rechazo;
    }

    public void setRechazo(MovimientoRelacionado rechazo) {
        this.rechazo = rechazo;
    }

    public ArrayList<MovimientoRelacionado> getRechazos() {
        return rechazos;
    }

    public void setRechazos(ArrayList<MovimientoRelacionado> rechazos) {
        this.rechazos = rechazos;
    }

    public ArrayList<EntradaProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<EntradaProducto> detalle) {
        this.detalle = detalle;
    }

    public EntradaProducto getProducto() {
        return producto;
    }

    public void setProducto(EntradaProducto producto) {
        this.producto = producto;
    }

    public Lote1 getLote() {
        return lote;
    }

    public void setLote(Lote1 lote) {
        this.lote = lote;
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

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }
}
