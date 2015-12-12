package rechazos;

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
import movimientos.to1.Lote1;
import movimientos.dominio.MovimientoTipo;
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
import rechazos.dao.DAORechazos;
import rechazos.dominio.Rechazo;
import rechazos.dominio.RechazoProducto;
import rechazos.dominio.RechazoProductoAlmacen;
import rechazos.to.TORechazo;
import rechazos.to.TORechazoProducto;
import rechazos.to.TORechazoProductoAlmacen;
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
    private Rechazo rechazo;
    private ArrayList<Rechazo> rechazos;
    private ArrayList<RechazoProducto> detalle;
    private RechazoProducto producto;
    private Lote1 lote;
    private Date fechaInicial;
    private DAORechazos dao;

    public MbRechazo() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    private MovimientoRelacionadoProductoReporte convertirProductoReporte(RechazoProducto prod) {
        boolean ya = false;
        MovimientoRelacionadoProductoReporte rep = new MovimientoRelacionadoProductoReporte();
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setCantFacturada(prod.getCantFacturada());
        rep.setUnitario(prod.getUnitario());
        for (TORechazoProductoAlmacen l : prod.getLotes()) {
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
        for (RechazoProducto p : this.detalle) {
            if (p.getCantFacturada() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\Traspaso.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.rechazo.getAlmacen().getEmpresa());

        parameters.put("cedis", this.rechazo.getAlmacen().getCedis());
        parameters.put("almacen", this.rechazo.getAlmacen().getAlmacen());

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
    
//    private RechazoProductoAlmacen convertirProductoAlmacen(TORechazoProductoAlmacen toProd) {
//        RechazoProductoAlmacen prod = new RechazoProductoAlmacen();
//        prod.setCantTraspasada(toProd.getCantTraspasada());
//        prod.setCantRecibida(toProd.getCantRecibida());
//        movimientos.Movimientos.convertir(toProd, prod);
//        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
//        return prod;
//    }

    private RechazoProducto convertir(TORechazoProducto toProd) throws SQLException {
        RechazoProducto prod = new RechazoProducto();
        prod.setCantTraspasada(toProd.getCantTraspasada());
        prod.setCantRecibida(toProd.getCantRecibida());
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        double sumaLotes=0;
        for(TORechazoProductoAlmacen to : this.dao.obtenerDetalleProducto(this.rechazo.getIdMovtoAlmacen(), toProd.getIdProducto())) {
//            prod.getLotes().add(this.convertirProductoAlmacen(to));
            prod.getLotes().add(to);
            sumaLotes+=to.getCantidad();
        }
        if(prod.getCantFacturada()!=sumaLotes) {
            throw new SQLException("Error de sincronizacion Lotes vs oficina en producto (id=" + toProd.getIdProducto() + ") del movimiento !!!");
        } else {
            prod.setSumaLotes(sumaLotes);
        }
        return prod;
    }

    public void obtenerDetalle(SelectEvent event) {
        this.rechazo = (Rechazo) event.getObject();
        try {
            this.dao = new DAORechazos();
            this.detalle = new ArrayList<>();
            for (TORechazoProducto toProd : this.dao.obtenerDetalle(this.rechazo.getIdMovto())) {
                this.detalle.add(this.convertir(toProd));
            }
            this.producto = new RechazoProducto();
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private Rechazo convertir(TORechazo toMov) {
        Rechazo mov = new Rechazo(new MovimientoTipo(54, "Rechazo"), this.almacen, this.mbAlmacenes.obtenerTOAlmacen(toMov.getIdReferencia()));
        mov.setRecepcionFolio(toMov.getRecepcionFolio());
        mov.setRecepcionFecha(toMov.getRecepcionFecha());
        mov.setTraspasoFolio(toMov.getTraspasoFolio());
        mov.setTraspasoFecha(toMov.getTraspasoFecha());
        movimientos.Movimientos.convertir(toMov, mov);
        mov.setIdRecepcion(toMov.getReferencia());
        return mov;
    }

    public void obtenerRechazos() {
        boolean ok = false;
        this.rechazos = new ArrayList<>();
        try {
            this.dao = new DAORechazos();
            for (TORechazo mov : this.dao.obtenerRechazos(this.almacen.getIdAlmacen(), this.fechaInicial)) {
                this.rechazos.add(this.convertir(mov));
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
        this.fechaInicial = new Date();
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

    public Rechazo getRechazo() {
        return rechazo;
    }

    public void setRechazo(Rechazo rechazo) {
        this.rechazo = rechazo;
    }

    public ArrayList<Rechazo> getRechazos() {
        return rechazos;
    }

    public void setRechazos(ArrayList<Rechazo> rechazos) {
        this.rechazos = rechazos;
    }

    public ArrayList<RechazoProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<RechazoProducto> detalle) {
        this.detalle = detalle;
    }

    public RechazoProducto getProducto() {
        return producto;
    }

    public void setProducto(RechazoProducto producto) {
        this.producto = producto;
    }

    public Lote1 getLote() {
        return lote;
    }

    public void setLote(Lote1 lote) {
        this.lote = lote;
    }

    public Date getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
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
